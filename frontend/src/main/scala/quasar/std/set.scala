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

trait SetLib {
  val Take: BinaryFunc = BinaryFunc(
    Sifting,
    "Takes the first N elements from a set")

  val Sample: BinaryFunc = BinaryFunc(
    Sifting,
    "Randomly selects N elements from a set")

  val Drop: BinaryFunc = BinaryFunc(
    Sifting,
    "Drops the first N elements from a set")

  val Range = BinaryFunc(
    Mapping,
    "Creates an array of values in the range from `a` to `b`, inclusive.")

  val Filter = BinaryFunc(
    Sifting,
    "Filters a set to include only elements where a projection is true")

  val GroupBy = BinaryFunc(
    Transformation,
    "Groups a projection of a set by another projection")

  val Union = BinaryFunc(
    Transformation,
    "Creates a new set with all the elements of each input set, keeping duplicates.")

  val Intersect = BinaryFunc(
    Transformation,
    "Creates a new set with only the elements that exist in both input sets, keeping duplicates.")

  val Except = BinaryFunc(
    Transformation,
    "Removes the elements of the second set from the first set.")

  val In = BinaryFunc(
    Sifting,
    "Determines whether a value is in a given set.")

  val Within = BinaryFunc(
    Mapping,
    "Determines whether a value is in a given array.")

  val Constantly = BinaryFunc(
    Transformation,
    "Always return the same value")
}

object SetLib extends SetLib
