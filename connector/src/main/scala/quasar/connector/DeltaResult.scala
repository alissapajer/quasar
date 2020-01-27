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

import fs2.Stream
import qdata.QDataDecode

sealed trait DeltaResult[F[_], O] extends Product with Serializable

object DeltaResult {

  final case class Parsed[F[_], O, A](
      decode: QDataDecode[A],
      changes: Stream[F, Change[F, O, A]])
      extends DeltaResult[F, O]
}
