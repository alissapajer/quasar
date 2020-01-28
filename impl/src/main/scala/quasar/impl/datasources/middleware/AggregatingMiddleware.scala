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

package quasar.impl
package datasources.middleware

import quasar.api.resource.{ResourcePath, ResourcePathType}
import quasar.connector.{Datasource, MonadResourceErr}
import quasar.impl.datasource.{AggregateResult, AggregatingDatasource, MonadCreateErr}
import quasar.impl.datasources.ManagedDatasource
import quasar.qscript.{InterpretedRead, QScriptEducated}

import scala.util.{Either, Left}

import cats.Monad
import cats.effect.Sync
import cats.syntax.functor._
import fs2.Stream
import shims.{functorToCats, functorToScalaz}

object AggregatingMiddleware {
  def apply[T[_[_]], F[_]: MonadResourceErr: MonadCreateErr: Sync, I, R1, R2[_]](
      datasourceId: I,
      mds: ManagedDatasource[T, F, Stream[F, ?], R1, R2, ResourcePathType.Physical])
      : F[ManagedDatasource[T, F, Stream[F, ?], Either[R1, AggregateResult[F, R1]], Either[R2, AggregateResult[F, R2]], ResourcePathType]] =
    Monad[F].pure(mds) map {
      case ManagedDatasource.ManagedLightweight(lw) =>
        val ds: Datasource[F, Stream[F, ?], InterpretedRead[ResourcePath], R1, R2, ResourcePathType.Physical] = lw
        ManagedDatasource.lightweight[T](
          AggregatingDatasource(ds, InterpretedRead.path))

      // TODO: union all in QScript?
      case ManagedDatasource.ManagedHeavyweight(hw) =>
        type Q = T[QScriptEducated[T, ?]]
        val ds: Datasource[F, Stream[F, ?], Q, Either[R1, AggregateResult[F, R1]], Either[R2, AggregateResult[F, R2]], ResourcePathType.Physical] =
          Datasource.pevaluator[F, Stream[F, ?], Q, R, Q, Either[R1, AggregateResult[F, R1]], Either[R2, AggregateResult[F, R2]], ResourcePathType.Physical]
          .modify(_.map(Left(_)))(hw)
        ManagedDatasource.heavyweight(Datasource.widenPathType(ds))
    }
}
