/*
 * Copyright 2020 Precog Data
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

package quasar.api.scheduler

import slamdata.Predef._

import cats.Show
import cats.implicits._

sealed trait SchedulerError[+I, +C] extends Product with Serializable

object SchedulerError {
  sealed trait CreateError[+C] extends SchedulerError[Nothing, C]

  final case class SchedulerUnsupported(kind: SchedulerType, supported: Set[SchedulerType])
      extends CreateError[Nothing]
  final case class SchedulerNameExists(name: String)
      extends CreateError[Nothing]

  sealed trait InitializationError[+C] extends CreateError[C]
  final case class MalformedConfiguration[C](kind: SchedulerType, config: C, reason: String)
      extends InitializationError[C]
  final case class InvalidConfiguration[C](kind: SchedulerType, config: C, problems: Set[String])
      extends InitializationError[C]
  final case class ConnectionFailed[C](kind: SchedulerType, config: C, cause: Exception)
      extends InitializationError[C]
  final case class AccessDenied[C](kind: SchedulerType, config: C, reason: String)
      extends InitializationError[C]


  final case class SchedulerNotFound[I](index: I)
      extends SchedulerError[I, Nothing]

  object CreateError {
    implicit def show[C: Show]: Show[CreateError[C]] = Show.show {
      case SchedulerUnsupported(kind, set) => s"SchedulerUnsupported(${kind.show}, ${set.show})"
      case SchedulerNameExists(name) => s"SchedulerNameExists(${name.show})"
      case e: InitializationError[C] => e.show
    }
  }

  object InitializationError {
    implicit def show[C: Show]: Show[InitializationError[C]] = Show.show {
      case MalformedConfiguration(kind, config, reason) =>
        s"MalformedConfiguration(${kind.show}, ${config.show}, ${reason.show})"
      case InvalidConfiguration(kind, config, problems) =>
        s"InvalidConfiguration(${kind.show}, ${config.show}, ${problems.show})"
      case ConnectionFailed(kind, config, cause) =>
        s"ConnectionFailed(${kind.show}, ${config.show}, ${cause.getMessage().show})"
      case AccessDenied(kind, config, reason) =>
        s"AccessDenied(${kind.show}, ${config.show}, ${reason.show})"
    }
  }

  implicit def show[I: Show, C: Show]: Show[SchedulerError[I, C]] = Show.show {
    case e: CreateError[C] => e.show
    case SchedulerNotFound(index) => s"SchedulerNotFound(${index.show})"
  }
}
