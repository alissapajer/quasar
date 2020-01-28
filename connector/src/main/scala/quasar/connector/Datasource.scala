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
import quasar.api.datasource.DatasourceType
import quasar.api.resource._

import cats.data.NonEmptyList

/** @tparam F effects
  * @tparam G multiple results
  * @tparam Q query
  */
trait Datasource[F[_], G[_], Q, +R1, +R2[_], P <: ResourcePathType] {

  type Offset

  /** The type of this datasource. */
  def kind: DatasourceType

  /** The data loaders supported by this datasource. */
  def loaders: NonEmptyList[Loader[F, Q, Offset, R1, R2[Offset]]]

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
  def widenPathType[F[_], G[_], Q, PI <: ResourcePathType, PO >: PI <: ResourcePathType, R1, R2[_]](
      ds: Datasource[F, G, Q, R1, R2, PI]): Datasource[F, G, Q, R1, R2, PO] =
    ds.asInstanceOf[Datasource[F, G, Q, R1, R2, PO]]

  def pevaluator[F[_], G[_], Q1, R11, R21[_], Q2, R21, R22[_], P <: ResourcePathType]
      : PLens[Datasource[F, G, Q1, R11, R12, P], Datasource[F, G, Q2, R21, R22, P], QueryEvaluator[F, Q1, R1], QueryEvaluator[F, Q2, R2]] =
    PLens((ds: Datasource[F, G, Q1, R11, R12, P]) => ds: QueryEvaluator[F, Q1, R1]) { qe: QueryEvaluator[F, Q2, R2] => ds =>
      new Datasource[F, G, Q2, R2, P] {
        val kind = ds.kind
        def evaluate(q: Q2) = qe.evaluate(q)
        def pathIsResource(p: ResourcePath) = ds.pathIsResource(p)
        def prefixedChildPaths(pfx: ResourcePath) = ds.prefixedChildPaths(pfx)
      }
    }
}
