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

package quasar.api.destination

import slamdata.Predef.{Stream => _, _}

import quasar.api.push.RenderConfig
import quasar.api.resource.ResourcePath
import quasar.api.table.TableColumns

import fs2.Stream

sealed trait ResultSink[F[_]] extends Product with Serializable

object ResultSink {

  final case class CreateSink[F[_]](
      format: RenderConfig.Csv,
      ingest: (ResourcePath, TableColumns, Stream[F, Byte]) => Stream[F, Unit])
      extends ResultSink[F]

  final case class ReplaceSink[F[_]](
      format: RenderConfig.Csv,
      ingest: (ResourcePath, TableColumns, Array[Byte], Stream[F, Byte]) => Stream[F, Unit])
      extends ResultSink[F]

  final case class DeleteSink[F[_]](
      format: RenderConfig.Csv,
      ingest: (ResourcePath, TableColumns, Stream[F, Array[Byte]]) => Stream[F, Unit])
      extends ResultSink[F]
}
