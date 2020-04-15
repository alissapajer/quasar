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

package quasar.connector

import slamdata.Predef.{Eq => _, _}

import quasar.contrib.std.errorImpossible

import java.util.Arrays

import cats._
import cats.implicits._

sealed trait IdBatch[I] extends Product with Serializable {
  val size: Int
}

object IdBatch {
  final case class Strings(values: Array[String], size: Int) extends IdBatch[String]
  final case class Longs(values: Array[Long], size: Int) extends IdBatch[Long]
  final case class Doubles(values: Array[Double], size: Int) extends IdBatch[Double]
  final case class BigDecimals(values: Array[BigDecimal], size: Int) extends IdBatch[BigDecimal]

  implicit def idBatchEq[I]: Eq[IdBatch[I]] =
    new Eq[IdBatch[I]] {
      def eqv(x: IdBatch[I], y: IdBatch[I]) =
        (x, y) match {
          case (Strings(xs, sizex), Strings(ys, sizey)) =>
            sizex === sizey &&
              Arrays.equals(xs.asInstanceOf[Array[AnyRef]], ys.asInstanceOf[Array[AnyRef]])

          case (Longs(xs, sizex), Longs(ys, sizey)) =>
            sizex === sizey &&
              Arrays.equals(xs, ys)

          case (Doubles(xs, sizex), Doubles(ys, sizey)) =>
            sizex === sizey &&
              Arrays.equals(xs, ys)

          case (BigDecimals(xs, sizex), BigDecimals(ys, sizey)) =>
            sizex === sizey &&
              Arrays.equals(xs.asInstanceOf[Array[AnyRef]], ys.asInstanceOf[Array[AnyRef]])

          case _ => errorImpossible
        }
    }

  implicit def idBatchShow[I]: Show[IdBatch[I]] =
    Show show {
      case Strings(values, size) => s"Strings(${values.toList.map(_.show)}, $size)"
      case Longs(values, size) => s"Longs(${values.toList.map(_.show)}, $size)"
      case Doubles(values, size) => s"Doubles(${values.toList.map(_.show)}, $size)"
      case BigDecimals(values, size) => s"BigDecimals(${values.toList.map(_.show)}, $size)"
    }
}