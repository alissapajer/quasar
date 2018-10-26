/*
 * Copyright 2014–2018 SlamData Inc.
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

package quasar.std

import quasar._

trait AggLib {

  val Count = UnaryFunc(
    Reduction,
    "Counts the values in a set")

  val Sum = UnaryFunc(
    Reduction,
    "Sums the values in a set")

  val Min = UnaryFunc(
    Reduction,
    "Finds the minimum in a set of values")

  val Max = UnaryFunc(
    Reduction,
    "Finds the maximum in a set of values")

  val First = UnaryFunc(
    Reduction,
    "Finds the first value in a set.")

  val Last = UnaryFunc(
    Reduction,
    "Finds the last value in a set.")

  val Avg = UnaryFunc(
    Reduction,
    "Finds the average in a set of numeric values")

  val Arbitrary = UnaryFunc(
    Reduction,
    "Returns an arbitrary value from a set")
}

object AggLib extends AggLib
