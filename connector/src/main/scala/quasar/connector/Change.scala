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

package quasar.connector

import slamdata.Predef._

import quasar.ScalarStages

import fs2.Stream

sealed trait Change[F[_], +O, +A] extends Product with Serializable {

  /** The offset representing the end of the event payload. */
  def endOffset: O
}

object Change {

  final case class Create[F[_], O, A](
      stages: ScalarStages,
      data: Stream[F, A],
      endOffset: O)
      extends Change[F, O, A]

  final case class Replace[F[_], O, A](
      stages: ScalarStages,
      correlationId: Array[Byte],
      endOffset: O,
      data: Stream[F, A])
      extends Change[F, O, A]

  final case class Delete[F[_], O](
      correlationIds: Stream[F, Array[Byte]],
      endOffset: O)
      extends Change[F, O, Nothing]
}
