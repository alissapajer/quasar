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

import slamdata.Predef.{Array, Boolean, Option, SuppressWarnings}
import quasar.api.QueryEvaluator
import quasar.api.datasource.DatasourceType
import quasar.api.resource._

import cats.data.NonEmptyList
import monocle.{Lens, PLens}
import scodec.Codec

/** @tparam F effects
  * @tparam G multiple results
  * @tparam Q query
  */
trait Datasource[F[_], G[_], Q, P <: ResourcePathType] {

  /** The type used to represent the incremental load offset. */
  type Offset

  /** The type of this datasource. */
  def kind: DatasourceType

  def offsetCodec: Codec[Offset]

  /** The data loaders supported by this datasource. */
  def loaders: NonEmptyList[Loader[F, Offset, Q]]

  /** Returns whether or not the specified path refers to a resource in the
    * specified datasource.
    */
  def pathIsResource(path: ResourcePath): F[Boolean]

  /** Returns the name and type of the `ResourcePath`s implied by concatenating
    * each name to `prefixPath` or `None` if `prefixPath` does not exist.
    */
  def prefixedChildPaths(prefixPath: ResourcePath)
      : F[Option[G[(ResourceName, P)]]]
}

object Datasource {
  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def widenPathType[F[_], G[_], Q, PI <: ResourcePathType, PO >: PI <: ResourcePathType](
      ds: Datasource[F, G, Q, PI]): Datasource[F, G, Q, PO] =
    ds.asInstanceOf[Datasource[F, G, Q, PO]]
}
