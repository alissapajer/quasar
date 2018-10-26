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

import quasar.{Mapping, UnaryFunc, BinaryFunc, TernaryFunc}

trait RelationsLib {
  val Eq = BinaryFunc(
    Mapping,
    "Determines if two values are equal")

  val Neq = BinaryFunc(
    Mapping,
    "Determines if two values are not equal")

  val Lt = BinaryFunc(
    Mapping,
    "Determines if one value is less than another value of the same type")

  val Lte = BinaryFunc(
    Mapping,
    "Determines if one value is less than or equal to another value of the same type")

  val Gt = BinaryFunc(
    Mapping,
    "Determines if one value is greater than another value of the same type")

  val Gte = BinaryFunc(
    Mapping,
    "Determines if one value is greater than or equal to another value of the same type")

  val Between = TernaryFunc(
    Mapping,
    "Determines if a value is between two other values of the same type, inclusive")

  val IfUndefined = BinaryFunc(
    Mapping,
    "This is the only way to recognize an undefined value. If the first argument is undefined, return the second argument, otherwise, return the first.")

  val And = BinaryFunc(
    Mapping,
    "Performs a logical AND of two boolean values")

  val Or = BinaryFunc(
    Mapping,
    "Performs a logical OR of two boolean values")

  val Not = UnaryFunc(
    Mapping,
    "Performs a logical negation of a boolean value")

  val Cond = TernaryFunc(
    Mapping,
    "Chooses between one of two cases based on the value of a boolean expression")
}

object RelationsLib extends RelationsLib
