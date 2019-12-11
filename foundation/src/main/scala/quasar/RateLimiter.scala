/*
 * Copyright 2014–2019 SlamData Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package quasar

import slamdata.Predef._

import quasar.contrib.cats.hash.toHashing
import quasar.contrib.cats.eqv.toEquiv

import java.util.concurrent.TimeUnit

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._
import scala.reflect.{ClassTag, classTag}

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import cats.kernel.Hash
import cats.implicits._

import fs2.{Pipe, Stream}
import fs2.concurrent.Queue

import skolems._

final class RateLimiter[F[_]: Concurrent: Timer] private (
    caution: Double,
    queue: Queue[F, Message]) {

  private val configs: TrieMap[Exists[Key], Config] =
    new TrieMap[Exists[Key], Config](
      toHashing[Exists[Key]],
      toEquiv[Exists[Key]])

  private val states: TrieMap[Exists[Key], Ref[F, State]] =
    new TrieMap[Exists[Key], Ref[F, State]](
      toHashing[Exists[Key]],
      toEquiv[Exists[Key]])

  val subscribe: Stream[F, Message] = queue.dequeue

  val update: Pipe[F, Message, Unit] = _ evalMap {
    case Reset(key) =>
      for {
        ref <- Sync[F].delay(states.get(key))
        now <- nowF
        _ <- ref match {
          case Some(r) =>
            r.tryUpdate(_ => State(0, now, now))
          case None =>
            Ref.of[F, State](State(0, now, now)).map(r =>
              states.update(key, r))
        }
      } yield ()

    case PlusOne(key) =>
      for {
        ref <- Sync[F].delay(states.get(key))
        now <- nowF
        _ <- ref match {
          case Some(r) =>
            r.modify(s => (s.copy(count = s.count + 1), ()))
          case None =>
            Ref.of[F, State](State(1, now, now)).map(r =>
              states.update(key, r))
        }
      } yield ()

    case Wait(key, length) =>
      for {
        ref <- Sync[F].delay(states.get(key))
        now <- nowF
        _ <- ref match {
          case Some(r) =>
            r.modify(s => (s.copy(waitUntil = length + now), ()))
          case None =>
            Ref.of[F, State](State(0, now, length + now)).map(r =>
              states.update(key, r))
        }
      } yield ()

    case Configure(key, config) =>
      Sync[F].delay(configs.putIfAbsent(key, config)) >> ().pure[F]
  }

  def apply[A: Hash: ClassTag](key: A, max: Int, window: FiniteDuration)
      : F[F[Unit]] =
    for {
      hashkey <- Key(key, Hash[A], classTag[A]).pure[F]

      config <- Sync[F] delay {
        val c = Config(max, window)
        configs.putIfAbsent(hashkey, c).getOrElse(c)
      }

      now <- nowF
      maybeR <- Ref.of[F, State](State(0, now, now))
      stateRef <- Sync[F] delay {
        states.putIfAbsent(hashkey, maybeR).getOrElse(maybeR)
      }
    } yield limit(hashkey, config, stateRef)

  private def limit(key: Exists[Key], config: Config, stateRef: Ref[F, State])
      : F[Unit] = {

    import config._

    val resetStateF : F[Boolean] =
      nowF.flatMap(now => stateRef.tryUpdate(_ => State(0, now, now)))

    for {
      now <- nowF
      state <- stateRef.get
      _ <-
        if (state.waitUntil > now) {
          Timer[F].sleep(state.waitUntil - now) >>
            resetStateF >>
            limit(key, config, stateRef)
        } else {
          ().pure[F]
        }
      back <-
        if (state.start + window < now) {
          queue.enqueue1(Reset(key)) >>
            resetStateF >>
            limit(key, config, stateRef)
        } else {
          stateRef.modify(s => (s.copy(count = s.count + 1), s.count)) flatMap { count =>
            if (count >= max * caution) {
              Timer[F].sleep((state.start + window) - now) >>
                queue.enqueue1(Reset(key)) >>
                resetStateF >>
                limit(key, config, stateRef)
            } else {
              queue.enqueue1(PlusOne(key))
            }
          }
        }
    } yield back
  }

  private val nowF: F[FiniteDuration] =
    Timer[F].clock.realTime(TimeUnit.MILLISECONDS).map(_.millis)

  private case class State(
      count: Int,
      start: FiniteDuration,
      waitUntil: FiniteDuration)
}

object RateLimiter {
  def apply[F[_]: Concurrent: Timer](caution: Double): F[RateLimiter[F]] =
    Queue.unbounded[F, Message].map(queue =>
      new RateLimiter[F](caution, queue))
}

case class Config(max: Int, window: FiniteDuration)

sealed trait Message
final case class PlusOne(key: Exists[Key]) extends Message
final case class Reset(key: Exists[Key]) extends Message
final case class Wait(key: Exists[Key], length: FiniteDuration) extends Message
final case class Configure(key: Exists[Key], config: Config) extends Message

final case class Key[A](value: A, hash: Hash[A], tag: ClassTag[A])

object Key {
  implicit def hash: Hash[Exists[Key]] =
    new Hash[Exists[Key]] {

      def hash(k: Exists[Key]) =
        k().hash.hash(k().value)

      def eqv(left: Exists[Key], right: Exists[Key]) = {
        (left().tag == right().tag) &&
          left().hash.eqv(
            left().value,
            right().value.asInstanceOf[left.A])
      }
    }
}
