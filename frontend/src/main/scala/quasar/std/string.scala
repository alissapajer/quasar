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

import slamdata.Predef._
import quasar._

trait StringLib {

  val Concat = BinaryFunc(
    Mapping,
    "Concatenates two (or more) string values")

  val Like = TernaryFunc(
    Mapping,
    "Determines if a string value matches a pattern.")

  val Search = TernaryFunc(
    Mapping,
    "Determines if a string value matches a regular expression. If the third argument is true, then it is a case-insensitive match.")

  val Length = UnaryFunc(
    Mapping,
    "Counts the number of characters in a string.")

  val Lower = UnaryFunc(
    Mapping,
    "Converts the string to lower case.")

  val Upper = UnaryFunc(
    Mapping,
    "Converts the string to upper case.")

  val Substring: TernaryFunc = TernaryFunc(
    Mapping,
    "Extracts a portion of the string")

  val Split = BinaryFunc(
    Mapping,
    "Splits a string into an array of substrings based on a delimiter.")

  val Boolean = UnaryFunc(
    Mapping,
    "Converts the strings “true” and “false” into boolean values. This is a partial function – arguments that don’t satisfy the constraint have undefined results.")

  val Integer = UnaryFunc(
    Mapping,
    "Converts strings containing integers into integer values. This is a partial function – arguments that don’t satisfy the constraint have undefined results.")

  val Decimal = UnaryFunc(
    Mapping,
    "Converts strings containing decimals into decimal values. This is a partial function – arguments that don’t satisfy the constraint have undefined results.")

  val Number = UnaryFunc(
    Mapping,
    "Converts strings containing numbers into number values. This is a partial function – arguments that don’t satisfy the constraint have undefined results.")

  val Null = UnaryFunc(
    Mapping,
    "Converts strings containing “null” into the null value. This is a partial function – arguments that don’t satisfy the constraint have undefined results.")

  val ToString: UnaryFunc = UnaryFunc(
    Mapping,
    "Converts any primitive type to a string.")
}

object StringLib extends StringLib {

  /** Substring which always gives a result, no matter what offsets are provided.
    * Reverse-engineered from MongoDb's \$substr op, for lack of a better idea
    * of how this should work. Note: if `start` < 0, the result is `""`.
    * If `length` < 0, then result includes the rest of the string. Otherwise
    * the behavior is as you might expect.
    */
  def safeSubstring(str: String, start: Int, length: Int): String =
    if (start < 0 || start > str.length) ""
    else if (length < 0) str.substring(start, str.length)
    else str.substring(start, (start + length) min str.length)
}
