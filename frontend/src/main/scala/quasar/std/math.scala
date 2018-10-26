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

import quasar.{UnaryFunc, BinaryFunc, Mapping}

trait MathLib {

  /** Adds two numeric or temporal values, promoting to decimal when appropriate
    * if either operand is decimal.
    */
  val Add = BinaryFunc(
    Mapping,
    "Adds two numeric or temporal values")

  /**
   * Multiplies two numeric or temporal values, promoting to decimal when appropriate
   * if either operand is decimal.
   */
  val Multiply = BinaryFunc(
    Mapping,
    "Multiplies two numeric values or one interval and one numeric value")

  val Power = BinaryFunc(
    Mapping,
    "Raises the first argument to the power of the second")

  /** Subtracts one numeric or temporal value from another,
    * promoting to decimal when appropriate if either operand is decimal.
    */
  val Subtract = BinaryFunc(
    Mapping,
    "Subtracts two numeric or temporal values")

  /**
   * Divides one numeric value by another, promoting to decimal if either operand is decimal.
   */
  val Divide = BinaryFunc(
    Mapping,
    "Divides one numeric value by another (non-zero) numeric value")

  /**
   * Aka "unary minus".
   */
  val Negate = UnaryFunc(
    Mapping,
    "Reverses the sign of a numeric or interval value")

  val Abs = UnaryFunc(
    Mapping,
    "Returns the absolute value of a numeric or interval value")

    val Ceil = UnaryFunc(
      Mapping,
      "Returns the nearest integer greater than or equal to a numeric value")

    val Floor = UnaryFunc(
      Mapping,
      "Returns the nearest integer less than or equal to a numeric value")

    val Trunc = UnaryFunc(
      Mapping,
      "Truncates a numeric value towards zero")

    val Round = UnaryFunc(
      Mapping,
      "Rounds a numeric value to the closest integer, utilizing a half-even strategy")

    val FloorScale = BinaryFunc(
      Mapping,
      "Returns the nearest number less-than or equal-to a given number, with the specified number of decimal digits")

    val CeilScale = BinaryFunc(
      Mapping,
      "Returns the nearest number greater-than or equal-to a given number, with the specified number of decimal digits")

    val RoundScale = BinaryFunc(
      Mapping,
      "Returns the nearest number to a given number with the specified number of decimal digits")

  // TODO: Come back to this, Modulo docs need to stop including Interval.
  // Note: there are 2 interpretations of `%` which return different values for negative numbers.
  // Depending on the interpretation `-5.5 % 1` can either be `-0.5` or `0.5`.
  // Generally, the first interpretation seems to be referred to as "remainder" and the 2nd as "modulo".
  // Java/scala and PostgreSQL all use the remainder interpretation, so we use it here too.
  // However, since PostgreSQL uses the function name `mod` as an alias for `%` while using the term
  // remainder in its description we keep the term `Modulo` around.
  val Modulo = BinaryFunc(
    Mapping,
    "Finds the remainder of one number divided by another")
}

object MathLib extends MathLib
