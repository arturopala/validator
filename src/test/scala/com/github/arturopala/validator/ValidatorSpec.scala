/*
 * Copyright 2021 Artur Opala
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

package com.github.arturopala.validator

import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Gen

class ValidatorSpec extends munit.ScalaCheckSuite {

  import Validator._

  case class Foo(bar: String, bazOpt: Option[Int] = None)

  test("Either.left.map") {
    assertEquals(Left(List("a", "b")).left.map(_.reverse), Left(List("b", "a")))
    assertEquals(Right[List[String], Unit](()).left.map(_.reverse), Valid)
  }

  test("check") {
    val c = checkIsTrue[Int](a => a % 2 == 0, "number must be even")
    assertEquals(c(0), Valid)
    assertEquals(c(2), Valid)
    assertEquals(c(4), Valid)
    assertEquals(c(1), Invalid("number must be even"))
    assertEquals(c(3), Invalid("number must be even"))
  }

  test("and combinator") {
    val c1 = checkIsTrue[Int](a => a % 2 == 0, "number must be even")
    val c2 = checkIsTrue[Int](a => a % 3 == 0, "number must be divisible by 3")
    val c = c1 and c2
    assertEquals(c(0), Valid)
    assertEquals(c(1).errorsSummaryOption, Some("number must be even and number must be divisible by 3"))
    assertEquals(c(2).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(c(3).errorsSummaryOption, Some("number must be even"))
    assertEquals(c(4).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(c(5).errorsSummaryOption, Some("number must be even and number must be divisible by 3"))
    assertEquals(c(6), Valid)
    assertEquals(c(7).errorsSummaryOption, Some("number must be even and number must be divisible by 3"))
  }

  test("and combinator with super/sub types") {
    val c1 = checkIsTrue[AnyVal](a => a ne null, "number must be not null")
    val c2 = checkIsTrue[Int](a => a % 3 == 0, "number must be divisible by 3")
    val c: Validate[Int] = c1 and c2
    val d: Validate[Int] = c2 and c1

    assertEquals(c(0), Valid)
    assertEquals(c(1).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(c(2).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(c(3).errorsSummaryOption, None)
    assertEquals(c(4).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(c(5).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(c(6), Valid)
    assertEquals(c(7).errorsSummaryOption, Some("number must be divisible by 3"))

    assertEquals(d(0), Valid)
    assertEquals(d(1).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(d(2).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(d(3).errorsSummaryOption, None)
    assertEquals(d(4).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(d(5).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(d(6), Valid)
    assertEquals(d(7).errorsSummaryOption, Some("number must be divisible by 3"))
  }

  test("or combinator") {
    val c1 = checkIsTrue[Int](a => a % 2 == 0, "number must be even")
    val c2 = checkIsTrue[Int](a => a % 3 == 0, "number must be divisible by 3")
    val c = c1 or c2
    assertEquals(c(0), Valid)
    assertEquals(c(1).errorsSummaryOption, Some("number must be even or number must be divisible by 3"))
    assertEquals(c(2), Valid)
    assertEquals(c(3), Valid)
    assertEquals(c(4), Valid)
    assertEquals(c(5).errorsSummaryOption, Some("number must be even or number must be divisible by 3"))
    assertEquals(c(6), Valid)
    assertEquals(c(7).errorsSummaryOption, Some("number must be even or number must be divisible by 3"))
  }

  test("or combinator with super/sub types") {
    val c1 = checkIsTrue[AnyVal](a => a eq null, "number must be null")
    val c2 = checkIsTrue[Int](a => a % 3 == 0, "number must be divisible by 3")
    val c: Validate[Int] = c1 or c2
    val d: Validate[Int] = c2 or c1

    assertEquals(c(0), Valid)
    assertEquals(c(1).errorsSummaryOption, Some("number must be null or number must be divisible by 3"))
    assertEquals(c(2).errorsSummaryOption, Some("number must be null or number must be divisible by 3"))
    assertEquals(c(3), Valid)
    assertEquals(c(4).errorsSummaryOption, Some("number must be null or number must be divisible by 3"))
    assertEquals(c(5).errorsSummaryOption, Some("number must be null or number must be divisible by 3"))
    assertEquals(c(6), Valid)
    assertEquals(c(7).errorsSummaryOption, Some("number must be null or number must be divisible by 3"))

    assertEquals(d(0), Valid)
    assertEquals(d(1).errorsSummaryOption, Some("number must be divisible by 3 or number must be null"))
    assertEquals(d(2).errorsSummaryOption, Some("number must be divisible by 3 or number must be null"))
    assertEquals(d(3), Valid)
    assertEquals(d(4).errorsSummaryOption, Some("number must be divisible by 3 or number must be null"))
    assertEquals(d(5).errorsSummaryOption, Some("number must be divisible by 3 or number must be null"))
    assertEquals(d(6), Valid)
    assertEquals(d(7).errorsSummaryOption, Some("number must be divisible by 3 or number must be null"))
  }

  test("& combinator") {
    val c1 = checkIsTrue[Int](a => a % 2 == 0, "number must be even")
    val c2 = checkIsTrue[Int](a => a % 3 == 0, "number must be divisible by 3")
    val c = c1 & c2
    assertEquals(c(0), Valid)
    assertEquals(c(1).errorsSummaryOption, Some("number must be even and number must be divisible by 3"))
    assertEquals(c(2).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(c(3).errorsSummaryOption, Some("number must be even"))
    assertEquals(c(4).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(c(5).errorsSummaryOption, Some("number must be even and number must be divisible by 3"))
    assertEquals(c(6), Valid)
    assertEquals(c(7).errorsSummaryOption, Some("number must be even and number must be divisible by 3"))
  }

  test("| combinator") {
    val c1 = checkIsTrue[Int](a => a % 2 == 0, "number must be even")
    val c2 = checkIsTrue[Int](a => a % 3 == 0, "number must be divisible by 3")
    val c = c1 | c2
    assertEquals(c(0), Valid)
    assertEquals(c(1).errorsSummaryOption, Some("number must be even or number must be divisible by 3"))
    assertEquals(c(2), Valid)
    assertEquals(c(3), Valid)
    assertEquals(c(4), Valid)
    assertEquals(c(5).errorsSummaryOption, Some("number must be even or number must be divisible by 3"))
    assertEquals(c(6), Valid)
    assertEquals(c(7).errorsSummaryOption, Some("number must be even or number must be divisible by 3"))
  }

  test("| with & combinator") {
    val c1 = checkIsTrue[Int](a => a % 2 == 0, "number must be even")
    val c2 = checkIsTrue[Int](a => a % 3 == 0, "number must be divisible by 3")
    val c3 = checkIsTrue[Int](a => a < 10, "number must be less than 10")
    val c = (c1 | c2) & c3
    assertEquals(c(0), Valid)
    assertEquals(c(1).errorsSummaryOption, Some("number must be even or number must be divisible by 3"))
    assertEquals(c(2), Valid)
    assertEquals(c(3), Valid)
    assertEquals(c(4), Valid)
    assertEquals(c(5).errorsSummaryOption, Some("number must be even or number must be divisible by 3"))
    assertEquals(c(6), Valid)
    assertEquals(c(7).errorsSummaryOption, Some("number must be even or number must be divisible by 3"))
    assertEquals(c(10).errorsSummaryOption, Some("number must be less than 10"))
    assertEquals(
      c(11).errorsSummaryOption,
      Some("(number must be even or number must be divisible by 3) and number must be less than 10")
    )
    assertEquals(c(12).errorsSummaryOption, Some("number must be less than 10"))
  }

  test("| with | combinator") {
    val c1 = checkIsTrue[Int](a => a % 2 == 0, "number must be even")
    val c2 = checkIsTrue[Int](a => a % 3 == 0, "number must be divisible by 3")
    val c3 = checkIsTrue[Int](a => a > 10, "number must be greater than 10")
    val c = c1 | c2 | c3
    assertEquals(c(0), Valid)
    assertEquals(
      c(1).errorsSummaryOption,
      Some("number must be even or number must be divisible by 3 or number must be greater than 10")
    )
    assertEquals(c(2), Valid)
    assertEquals(c(3), Valid)
    assertEquals(c(4), Valid)
    assertEquals(
      c(5).errorsSummaryOption,
      Some("number must be even or number must be divisible by 3 or number must be greater than 10")
    )
    assertEquals(c(6), Valid)
    assertEquals(
      c(7).errorsSummaryOption,
      Some("number must be even or number must be divisible by 3 or number must be greater than 10")
    )
    assertEquals(c(8), Valid)
    assertEquals(c(9), Valid)
    assertEquals(c(10), Valid)
    assertEquals(c(11), Valid)
    assertEquals(c(12), Valid)
  }

  test("& with | combinator") {
    val c1 = checkIsTrue[Int](a => a % 2 == 0, "number must be even")
    val c2 = checkIsTrue[Int](a => a % 3 == 0, "number must be divisible by 3")
    val c3 = checkIsTrue[Int](a => a > 10, "number must be greater than 10")
    val c = (c1 & c2) | c3
    assertEquals(c(0), Valid)
    assertEquals(
      c(1).errorsSummaryOption,
      Some("(number must be even and number must be divisible by 3) or number must be greater than 10")
    )
    assertEquals(c(2).errorsSummaryOption, Some("number must be divisible by 3 or number must be greater than 10"))
    assertEquals(c(3).errorsSummaryOption, Some("number must be even or number must be greater than 10"))
    assertEquals(c(4).errorsSummaryOption, Some("number must be divisible by 3 or number must be greater than 10"))
    assertEquals(
      c(5).errorsSummaryOption,
      Some("(number must be even and number must be divisible by 3) or number must be greater than 10")
    )
    assertEquals(c(6), Valid)
    assertEquals(
      c(7).errorsSummaryOption,
      Some("(number must be even and number must be divisible by 3) or number must be greater than 10")
    )
    assertEquals(c(11), Valid)
    assertEquals(c(12), Valid)
  }

  test("& with & combinator") {
    val c1 = checkIsTrue[Int](a => a % 2 == 0, "number must be even")
    val c2 = checkIsTrue[Int](a => a % 3 == 0, "number must be divisible by 3")
    val c3 = checkIsTrue[Int](a => a < 10, "number must be lower than 10")
    val c = c1 & c2 & c3
    assertEquals(c(0), Valid)
    assertEquals(
      c(1).errorsSummaryOption,
      Some("number must be even and number must be divisible by 3")
    )
    assertEquals(c(2).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(c(3).errorsSummaryOption, Some("number must be even"))
    assertEquals(c(4).errorsSummaryOption, Some("number must be divisible by 3"))
    assertEquals(
      c(5).errorsSummaryOption,
      Some("number must be even and number must be divisible by 3")
    )
    assertEquals(c(6), Valid)
    assertEquals(
      c(7).errorsSummaryOption,
      Some("number must be even and number must be divisible by 3")
    )
    assertEquals(
      c(10).errorsSummaryOption,
      Some("number must be divisible by 3 and number must be lower than 10")
    )
    assertEquals(
      c(11).errorsSummaryOption,
      Some("number must be even and number must be divisible by 3 and number must be lower than 10")
    )
    assertEquals(
      c(12).errorsSummaryOption,
      Some("number must be lower than 10")
    )
  }

  test("nonEmptyStringValidator") {
    val nonEmptyStringValidator = Validator.checkIsTrue[String](_.nonEmpty, "string must be non-empty")
    assert(nonEmptyStringValidator("").isInvalid)
    assert(nonEmptyStringValidator("a").isValid)
  }

  test("emptyStringValidator") {
    val emptyStringValidator = Validator.checkIsTrue[String](_.isEmpty(), "string must be empty")
    assert(emptyStringValidator("").isValid)
    assert(emptyStringValidator("a").isInvalid)
  }

  property("Validator.all combines provided validators to verify if all checks passes") {
    val nonEmptyStringValidator = Validator.checkIsTrue[String](_.nonEmpty, "string must be non-empty")
    val emptyStringValidator = Validator.checkIsTrue[String](_.isEmpty(), "string must be empty")
    val validate: Validate[String] = nonEmptyStringValidator & emptyStringValidator

    forAll { (string: String) =>
      Prop.all(
        emptyStringValidator("").isValid,
        nonEmptyStringValidator("").isInvalid,
        emptyStringValidator(s"a$string").isInvalid,
        nonEmptyStringValidator(s"a$string").isValid,
        Validator(nonEmptyStringValidator, emptyStringValidator).apply(string).isInvalid,
        Validator.all(nonEmptyStringValidator, emptyStringValidator).apply(string).isInvalid,
        validate(string).isInvalid
      )
    }
  }

  property("Validator.all combines provided checks to verify if all checks passes") {
    val nonEmptyStringValidator = checkIsTrue[String](_.nonEmpty, "string must be non-empty")
    val emptyStringValidator = checkIsTrue[String](_.isEmpty(), "string must be empty")
    val validate: Validate[String] = nonEmptyStringValidator & emptyStringValidator

    forAll { (string: String) =>
      Prop.all(
        emptyStringValidator("").isValid,
        nonEmptyStringValidator("").isInvalid,
        emptyStringValidator(s"a$string").isInvalid,
        nonEmptyStringValidator(s"a$string").isValid,
        Validator[String](nonEmptyStringValidator, emptyStringValidator).apply(string).isInvalid,
        Validator.all[String](nonEmptyStringValidator, emptyStringValidator).apply(string).isInvalid,
        validate(string).isInvalid
      )
    }
  }

  property("Validator.all(with error prefix) combines provided validators to verify if all checks passes") {
    val nonEmptyStringValidator = Validator.checkIsTrue[String](_.nonEmpty, "string must be non-empty")
    val emptyStringValidator = Validator.checkIsTrue[String](_.isEmpty(), "string must be empty")
    val validate: Validate[String] =
      Validator.allWithPrefix("foo: ", nonEmptyStringValidator, emptyStringValidator)

    forAll { (string: String) =>
      Prop.all(
        emptyStringValidator("").isValid,
        ("foo: " @: nonEmptyStringValidator)("").errorsSummaryOption == Some("foo: string must be non-empty"),
        (emptyStringValidator.withErrorPrefix("@ "))(s"a$string").errorsSummaryOption == Some("@ string must be empty"),
        nonEmptyStringValidator(s"a$string").isValid,
        Validator
          .allWithPrefix("foo_", nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorsSummaryOption ==
          Some(if (string.isEmpty) "foo_string must be non-empty" else "foo_string must be empty"),
        Validator
          .allWithPrefix("bar/", nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorsSummaryOption ==
          Some(if (string.isEmpty) "bar/string must be non-empty" else "bar/string must be empty"),
        validate(string).errorsSummaryOption ==
          Some(if (string.isEmpty) "foo: string must be non-empty" else "foo: string must be empty")
      )
    }
  }

  property("Validator.all(with error prefix) combines provided checks to verify if all checks passes") {
    val nonEmptyStringValidator = checkIsTrue[String](_.nonEmpty, "string must be non-empty")
    val emptyStringValidator = checkIsTrue[String](_.isEmpty(), "string must be empty")
    val validate: Validate[String] =
      Validator.allWithPrefix("foo: ", nonEmptyStringValidator, emptyStringValidator)

    forAll { (string: String) =>
      Prop.all(
        emptyStringValidator("").isValid,
        ("foo: " @: nonEmptyStringValidator)("").errorsSummaryOption == Some("foo: string must be non-empty"),
        (emptyStringValidator.withErrorPrefix("@ "))(s"a$string").errorsSummaryOption == Some("@ string must be empty"),
        nonEmptyStringValidator(s"a$string").isValid,
        Validator
          .allWithPrefix[String]("foo_", nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorsSummaryOption ==
          Some(if (string.isEmpty) "foo_string must be non-empty" else "foo_string must be empty"),
        Validator
          .allWithPrefix[String]("bar/", nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorsSummaryOption ==
          Some(if (string.isEmpty) "bar/string must be non-empty" else "bar/string must be empty"),
        validate(string).errorsSummaryOption ==
          Some(if (string.isEmpty) "foo: string must be non-empty" else "foo: string must be empty")
      )
    }
  }

  property("Validator.all(with calculated error prefix) combines provided validators to verify if all checks passes") {
    val nonEmptyStringValidator = Validator.checkIsTrue[String](_.nonEmpty, "string must be non-empty")
    val emptyStringValidator = Validator.checkIsTrue[String](_.isEmpty(), "string must be empty")

    val calculatePrefix: String => String = s => s"${s.take(1)}: "

    forAll { (string: String) =>
      val f = string.take(1)
      Prop.all(
        Validator
          .allWithComputedPrefix(calculatePrefix, nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorsSummaryOption ==
          Some(if (string.isEmpty) s"$f: string must be non-empty" else s"$f: string must be empty"),
        Validator
          .allWithComputedPrefix(calculatePrefix, nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorsSummaryOption ==
          Some(if (string.isEmpty) s"$f: string must be non-empty" else s"$f: string must be empty"),
        Validator
          .allWithComputedPrefix(calculatePrefix, nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorsSummaryOption ==
          Some(if (string.isEmpty) s"$f: string must be non-empty" else s"$f: string must be empty")
      )
    }
  }

  property("Validator.all(with calculated error prefix) combines provided checks to verify if all checks passes") {
    val nonEmptyStringValidator = checkIsTrue[String](_.nonEmpty, "string must be non-empty")
    val emptyStringValidator = checkIsTrue[String](_.isEmpty(), "string must be empty")

    val calculatePrefix: String => String = s => s"${s.take(1)}: "

    forAll { (string: String) =>
      val f = string.take(1)
      Prop.all(
        Validator
          .allWithComputedPrefix[String](calculatePrefix, nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorsSummaryOption ==
          Some(if (string.isEmpty) s"$f: string must be non-empty" else s"$f: string must be empty"),
        Validator
          .allWithComputedPrefix[String](calculatePrefix, nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorsSummaryOption ==
          Some(if (string.isEmpty) s"$f: string must be non-empty" else s"$f: string must be empty"),
        Validator
          .allWithComputedPrefix[String](calculatePrefix, nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorsSummaryOption ==
          Some(if (string.isEmpty) s"$f: string must be non-empty" else s"$f: string must be empty")
      )
    }
  }

  property("Validator.any combines provided validators to verify if any of the checks passes") {
    val hasDigitValidator = Validator.checkIsTrue[String](_.exists(_.isDigit), "some characters must be digits")
    val hasLowerCaseValidator =
      Validator.checkIsTrue[String](_.exists(_.isLower), "some characters must be lower case")
    val validate: Validate[String] = hasDigitValidator | hasLowerCaseValidator

    forAllNoShrink(Gen.alphaChar, Gen.numChar) { (a: Char, d: Char) =>
      (a.isLower) ==>
        Prop.all(
          hasDigitValidator(s"$a/$d").isValid,
          hasLowerCaseValidator(s"$a!$d").isValid,
          hasDigitValidator(s"${a.toUpper}").errorsSummaryOption == Some("some characters must be digits"),
          hasLowerCaseValidator(s"${a.toUpper}").errorsSummaryOption == Some("some characters must be lower case"),
          validate(s"$a-$d").isValid,
          validate(s"$a-$a").isValid,
          validate(s"$d-$d").isValid,
          validate(s"$d-$a").isValid,
          Validator.any(hasDigitValidator, hasLowerCaseValidator).apply(s"$a-$d").isValid,
          Validator.any(hasDigitValidator, hasLowerCaseValidator).apply(s"$a-$a").isValid,
          Validator.any(hasDigitValidator, hasLowerCaseValidator).apply(s"$d-$d").isValid,
          Validator.any(hasDigitValidator, hasLowerCaseValidator).apply(s"$d-$a").isValid,
          validate(s"${a.toUpper}" * d.toInt).errorsSummaryOption == Some(
            "some characters must be digits or some characters must be lower case"
          ),
          Validator
            .any(hasDigitValidator, hasLowerCaseValidator)
            .apply(s"${a.toUpper}" * d.toInt)
            .errorsSummaryOption == Some(
            "some characters must be digits or some characters must be lower case"
          )
        )
    }
  }

  property("Validator.any(with error prefix) combines provided validators to verify if any of the checks passes") {
    val hasDigitValidator = Validator.checkIsTrue[String](_.exists(_.isDigit), "some characters must be digits")
    val hasLowerCaseValidator =
      Validator.checkIsTrue[String](_.exists(_.isLower), "some characters must be lower case")

    forAllNoShrink(Gen.alphaChar, Gen.numChar) { (a: Char, d: Char) =>
      (a.isLower) ==>
        Prop.all(
          Validator.anyWithPrefix("foo_", hasDigitValidator, hasLowerCaseValidator).apply(s"$a-$d").isValid,
          Validator.anyWithPrefix("foo_", hasDigitValidator, hasLowerCaseValidator).apply(s"$a-$a").isValid,
          Validator.anyWithPrefix("foo_", hasDigitValidator, hasLowerCaseValidator).apply(s"$d-$d").isValid,
          Validator.anyWithPrefix("foo_", hasDigitValidator, hasLowerCaseValidator).apply(s"$d-$a").isValid,
          Validator
            .anyWithPrefix("foo_", hasDigitValidator, hasLowerCaseValidator)
            .apply(s"${a.toUpper}" * d.toInt)
            .errorsSummaryOption == Some(
            "foo_some characters must be digits or foo_some characters must be lower case"
          )
        )
    }
  }

  property(
    "Validator.any(with calculated error prefix) combines provided validators to verify if any of the checks passes"
  ) {
    val hasDigitValidator = Validator.checkIsTrue[String](_.exists(_.isDigit), "some characters must be digits")
    val hasLowerCaseValidator =
      Validator.checkIsTrue[String](_.exists(_.isLower), "some characters must be lower case")

    val calculatePrefix: String => String = s => s"${s.take(1)}_"

    forAllNoShrink(Gen.alphaChar, Gen.numChar) { (a: Char, d: Char) =>
      (a.isLower) ==>
        Prop.all(
          Validator
            .anyWithComputedPrefix(calculatePrefix, hasDigitValidator, hasLowerCaseValidator)
            .apply(s"${a.toUpper}" * d.toInt)
            .errorsSummaryOption == Some(
            s"${a.toUpper}_some characters must be digits or ${a.toUpper}_some characters must be lower case"
          )
        )
    }
  }

  test("Validator.conditionally runs the test and follows with either first or second check") {
    val validateOnlyDigits = Validator.checkIsTrue[String](_.forall(_.isDigit), "all characters must be digits")
    val validateNonEmpty = Validator.checkIsTrue[String](_.nonEmpty, "must be non empty string")
    def validateLength(length: Int) =
      Validator.checkIsTrue[String](_.length() == length, s"must have $length characters")
    val validateAllUpperCase = Validator.checkIsTrue[String](_.forall(_.isUpper), "all characters must be upper case")
    val validate: Validate[String] =
      Validator.conditionally[String](
        _.headOption.contains('0'),
        validateLength(3) & validateOnlyDigits,
        validateNonEmpty & validateAllUpperCase
      )

    assert(validate("A").isValid)
    assert(validate("AZ").isValid)
    assert(validate("ABC").isValid)
    assert(validate("000").isValid)
    assert(validate("012").isValid)
    assert(validate("").errorsSummaryOption == Some("must be non empty string"))
    assert(validate("Az").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("az").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("a").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("0").errorsSummaryOption == Some("must have 3 characters"))
    assert(validate("00").errorsSummaryOption == Some("must have 3 characters"))
    assert(validate("123").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("0000").errorsSummaryOption == Some("must have 3 characters"))
  }

  test("Validator.whenTrue runs the test and if true then follows with the next check") {
    val validateOnlyDigits = Validator.checkIsTrue[String](_.forall(_.isDigit), "all characters must be digits")
    def validateLength(length: Int) =
      Validator.checkIsTrue[String](_.length() == length, s"must have $length characters")
    val validate: Validate[String] =
      Validator.whenTrue[String](_.headOption.contains('0'), validateLength(3) & validateOnlyDigits)

    assert(validate("000").isValid)
    assert(validate("012").isValid)
    assert(validate("A").isValid)
    assert(validate("AZ").isValid)
    assert(validate("ABC").isValid)
    assert(validate("").isValid)
    assert(validate("Az").isValid)
    assert(validate("az").isValid)
    assert(validate("a").isValid)
    assert(validate("123").isValid)
    assert(validate("0").errorsSummaryOption == Some("must have 3 characters"))
    assert(validate("00").errorsSummaryOption == Some("must have 3 characters"))
    assert(validate("0000").errorsSummaryOption == Some("must have 3 characters"))
  }

  test("Validator.whenFalse runs the test and if false then tries the next check") {
    val validateNonEmpty = Validator.checkIsTrue[String](_.nonEmpty, "must be non empty string")
    val validateAllUpperCase = Validator.checkIsTrue[String](_.forall(_.isUpper), "all characters must be upper case")
    val validate: Validate[String] =
      Validator.whenFalse[String](_.headOption.contains('0'), validateNonEmpty & validateAllUpperCase)

    assert(validate("A").isValid)
    assert(validate("AZ").isValid)
    assert(validate("ABC").isValid)
    assert(validate("0").isValid)
    assert(validate("00").isValid)
    assert(validate("000").isValid)
    assert(validate("0000").isValid)
    assert(validate("0abc").isValid)
    assert(validate("012").isValid)
    assert(validate("0123").isValid)
    assert(validate("").errorsSummaryOption == Some("must be non empty string"))
    assert(validate("Az").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("az").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("a").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("1").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("12").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("123").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("1ABC").errorsSummaryOption == Some("all characters must be upper case"))
  }

  test("Validator.when runs the guard check and follows with either first or second check") {
    val validateStartsWithZero =
      Validator.checkIsTrue[String](_.headOption.contains('0'), "first character must be a Zero")
    val validateOnlyDigits = Validator.checkIsTrue[String](_.forall(_.isDigit), "all characters must be digits")
    val validateNonEmpty = Validator.checkIsTrue[String](_.nonEmpty, "must be non empty string")
    def validateLength(length: Int) =
      Validator.checkIsTrue[String](_.length() == length, s"must have $length characters")
    val validateAllUpperCase = Validator.checkIsTrue[String](_.forall(_.isUpper), "all characters must be upper case")
    val validate: Validate[String] =
      Validator.when(
        validateStartsWithZero,
        validateLength(3) & validateOnlyDigits,
        validateNonEmpty & validateAllUpperCase
      )

    assert(validate("A").isValid)
    assert(validate("AZ").isValid)
    assert(validate("ABC").isValid)
    assert(validate("000").isValid)
    assert(validate("012").isValid)
    assert(validate("").errorsSummaryOption == Some("must be non empty string"))
    assert(validate("Az").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("az").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("a").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("0").errorsSummaryOption == Some("must have 3 characters"))
    assert(validate("00").errorsSummaryOption == Some("must have 3 characters"))
    assert(validate("123").errorsSummaryOption == Some("all characters must be upper case"))
    assert(validate("0000").errorsSummaryOption == Some("must have 3 characters"))
  }

  test("Validator.whenValid runs the guard check and if valid then follows with the next check") {
    val validateStartsWithZero =
      Validator.checkIsTrue[String](_.headOption.contains('0'), "first character must be a Zero")
    val validateOnlyDigits = Validator.checkIsTrue[String](_.forall(_.isDigit), "all characters must be digits")
    def validateLength(length: Int) =
      Validator.checkIsTrue[String](_.length() == length, s"must have $length characters")
    val validate1: Validate[String] =
      Validator.whenValid(validateStartsWithZero, validateLength(3) & validateOnlyDigits)
    val validate2: Validate[String] =
      validateStartsWithZero.andWhenValid(validateLength(3) & validateOnlyDigits)
    val validate3: Validate[String] =
      validateStartsWithZero ? (validateLength(3) & validateOnlyDigits)

    def runtWith(validate: Validate[String]) = {
      assert(validate("000").isValid)
      assert(validate("012").isValid)
      assert(validate("A").errorsSummaryOption == Some("first character must be a Zero"))
      assert(validate("AZ").errorsSummaryOption == Some("first character must be a Zero"))
      assert(validate("ABC").errorsSummaryOption == Some("first character must be a Zero"))
      assert(validate("").errorsSummaryOption == Some("first character must be a Zero"))
      assert(validate("Az").errorsSummaryOption == Some("first character must be a Zero"))
      assert(validate("az").errorsSummaryOption == Some("first character must be a Zero"))
      assert(validate("a").errorsSummaryOption == Some("first character must be a Zero"))
      assert(validate("123").errorsSummaryOption == Some("first character must be a Zero"))
      assert(validate("0").errorsSummaryOption == Some("must have 3 characters"))
      assert(validate("00").errorsSummaryOption == Some("must have 3 characters"))
      assert(validate("0000").errorsSummaryOption == Some("must have 3 characters"))
    }

    runtWith(validate1)
    runtWith(validate2)
    runtWith(validate3)
  }

  test("Validator.whenInvalid runs the guard check and if invalid then tries the next check") {
    val validateStartsWithZero =
      Validator.checkIsTrue[String](_.headOption.contains('0'), "first character must be a Zero")
    val validateNonEmpty = Validator.checkIsTrue[String](_.nonEmpty, "must be non empty string")
    val validateAllUpperCase = Validator.checkIsTrue[String](_.forall(_.isUpper), "all characters must be upper case")
    val validate1: Validate[String] =
      Validator.whenInvalid(validateStartsWithZero, validateNonEmpty & validateAllUpperCase)
    val validate2: Validate[String] =
      validateStartsWithZero.andwhenInvalid(validateNonEmpty & validateAllUpperCase)
    val validate3: Validate[String] =
      validateStartsWithZero ?! (validateNonEmpty & validateAllUpperCase)

    def runtWith(validate: Validate[String]) = {
      assert(validate("A").isValid)
      assert(validate("AZ").isValid)
      assert(validate("ABC").isValid)
      assert(validate("0").isValid)
      assert(validate("00").isValid)
      assert(validate("000").isValid)
      assert(validate("0000").isValid)
      assert(validate("012").isValid)
      assert(validate("0123").isValid)
      assert(validate("").errorsSummaryOption == Some("must be non empty string"))
      assert(validate("Az").errorsSummaryOption == Some("all characters must be upper case"))
      assert(validate("az").errorsSummaryOption == Some("all characters must be upper case"))
      assert(validate("a").errorsSummaryOption == Some("all characters must be upper case"))
      assert(validate("1").errorsSummaryOption == Some("all characters must be upper case"))
      assert(validate("12").errorsSummaryOption == Some("all characters must be upper case"))
      assert(validate("123").errorsSummaryOption == Some("all characters must be upper case"))
    }

    runtWith(validate1)
    runtWith(validate2)
    runtWith(validate3)
  }

  property("Validator.product combines provided validators to verify tuples of values") {
    val hasDigitValidator = Validator.checkIsTrue[Char](_.isDigit, "character must be a digit")
    val hasLowerCaseValidator =
      Validator.checkIsTrue[Char](_.isLower, "character must be lower case")
    val validate: Validate[(Char, Char)] = hasDigitValidator * hasLowerCaseValidator

    forAllNoShrink(Gen.alphaChar, Gen.numChar) { (a: Char, d: Char) =>
      (a.isLower && !a.isDigit) ==>
        Prop.all(
          hasDigitValidator(d).isValid,
          hasLowerCaseValidator(a).isValid,
          hasDigitValidator(a).errorsSummaryOption == Some("character must be a digit"),
          hasLowerCaseValidator(a.toUpper).errorsSummaryOption == Some("character must be lower case"),
          validate.apply((d, a)).isValid,
          validate.apply((a, d)).errorsSummaryOption == Some(
            "character must be a digit and character must be lower case"
          )
        )
    }
  }

  property("Validator.checkIsTrue returns Valid only if condition fulfilled") {
    val validate =
      Validator.checkIsTrue[Foo]((foo: Foo) => foo.bar.startsWith("a"), "foo.bar must start with a")

    Prop.all(
      forAll { (string: String) =>
        validate(Foo(s"a$string")).isValid
        validate(Foo(s"b$string")).isInvalid
        validate(Foo(s"b$string")).errorsSummaryOption == Some("foo.bar must start with a")
      }
    )
  }

  property("Validator.checkIsFalse returns Valid only if condition not met") {
    val validate =
      Validator.checkIsFalse[Foo]((foo: Foo) => foo.bar.startsWith("a"), "foo.bar must not start with a")

    Prop.all(
      forAll { (string: String) =>
        validate(Foo(s"b$string")).isValid
        validate(Foo(s"a$string")).isInvalid
        validate(Foo(s"a$string")).errorsSummaryOption == Some("foo.bar must not start with a")
      }
    )
  }

  property("Validator.checkEquals returns Valid only if values are the same") {
    val validate =
      Validator.checkEquals[Foo, Int](_.bar.toInt, _.bazOpt.getOrElse(0), "foo.bar must be the same as foo.baz")

    forAll { (int: Int) =>
      validate(Foo(int.toString(), Some(int))).isValid
      validate(Foo(int.toString(), Some(int - 1))).isInvalid
      validate(Foo(int.toString(), Some(int + 1))).isInvalid
    }
  }

  property("Validator.checkNotEquals returns Valid only if values are not the same") {
    val validate =
      Validator.checkNotEquals[Foo, Int](_.bar.toInt, _.bazOpt.getOrElse(0), "foo.bar must be not the same as foo.baz")

    forAll { (int: Int) =>
      validate(Foo(int.toString(), Some(int))).isInvalid
      validate(Foo(int.toString(), Some(int - 1))).isValid
      validate(Foo(int.toString(), Some(int + 1))).isValid
    }
  }

  property("Validator.checkIsDefined returns Valid only if condition returns Some") {
    val validate: Validate[Option[Int]] =
      Validator.checkIsDefined[Option[Int]](identity, "option must be defined")

    Prop.all(
      forAll { (int: Int) =>
        validate(Some(int)).isValid
      },
      validate(None).errorsSummaryOption == Some("option must be defined")
    )
  }

  property("Validator.checkIsEmpty returns Valid only if condition returns None") {
    val validate: Validate[Option[Int]] =
      Validator.checkIsEmpty[Option[Int]](identity, "option must be defined")

    Prop.all(
      forAll { (int: Int) =>
        validate(Some(int)).isInvalid
      },
      validate(None).isValid
    )
  }

  property("Validator.checkFromEither returns Valid only if condition returns Right") {
    val validate: Validate[Int] =
      Validator.checkFromEither[Int]((i: Int) => if (i > 0) Right(i) else Left("must be positive"))

    forAll { (int: Int) =>
      if (int > 0)
        validate(int).isValid
      else
        validate(int).errorsSummaryOption == Some("must be positive")
    }
  }

  property("Validator.checkFromEither(with error prefix) returns Valid only if condition returns Right") {
    val validate: Validate[Int] =
      Validator
        .checkFromEither[Int]((i: Int) => if (i > 0) Right(i) else Left("must be positive"))
        .withErrorPrefix("integer ")

    forAll { (int: Int) =>
      if (int > 0)
        validate(int).isValid
      else
        validate(int).errorsSummaryOption == Some("integer must be positive")
    }
  }

  property("Validator.checkWith returns Valid only if extracted property passes check") {
    val nonEmptyStringValidator: Validate[String] =
      Validator.checkIsTrue[String](_.nonEmpty, "string must be non-empty")
    val validate: Validate[Foo] =
      Validator.checkWith((foo: Foo) => foo.bar, nonEmptyStringValidator)

    forAll { (string: String) =>
      if (string.nonEmpty)
        validate(Foo(string)).isValid
      else
        validate(Foo(string)).errorsSummaryOption == Some("string must be non-empty")
    }
  }

  property("Validator.checkWithImplicitly returns Valid only if extracted property passes check") {
    implicit val nonEmptyStringValidator: Validate[String] =
      Validator.checkIsTrue[String](_.nonEmpty, "string must be non-empty")
    val validate: Validate[Foo] =
      Validator.checkWithImplicitly((foo: Foo) => foo.bar)

    forAll { (string: String) =>
      if (string.nonEmpty)
        validate(Foo(string)).isValid
      else
        validate(Foo(string)).errorsSummaryOption == Some("string must be non-empty")
    }
  }

  property("Validator.checkUsing(with error prefix) returns Valid only if nested validator returns Valid") {
    val nonEmptyStringValidator = Validator.checkIsTrue[String](_.nonEmpty, "string must be non-empty")
    val validate: Validate[Foo] =
      Validator
        .checkWith[Foo, String]((foo: Foo) => foo.bar, nonEmptyStringValidator)
        .withErrorPrefix("Foo.bar ")

    forAll { (string: String) =>
      if (string.nonEmpty)
        validate(Foo(string)).isValid
      else
        validate(Foo(string)).errorsSummaryOption == Some("Foo.bar string must be non-empty")
    }
  }

  property("Validator.checkIfSome returns Valid only if nested validator returns Valid") {
    val positiveIntegerValidator = Validator.checkIsTrue[Int](_ > 0, "must be positive integer")
    val validate: Validate[Foo] =
      Validator.checkIfSome[Foo, Int]((foo: Foo) => foo.bazOpt, positiveIntegerValidator)

    forAll { (int: Int) =>
      Prop.all(
        validate(Foo("", None)).isValid,
        if (int > 0)
          validate(Foo("", Some(int))).isValid
        else
          validate(Foo("", Some(int))).errorsSummaryOption == Some("must be positive integer")
      )
    }
  }

  property("Validator.checkIfSome(with invalid if None) returns Valid only if nested validator returns Valid") {
    val positiveIntegerValidator = Validator.checkIsTrue[Int](_ > 0, "must be positive integer")
    val validate: Validate[Foo] =
      Validator.checkIfSome[Foo, Int]((foo: Foo) => foo.bazOpt, positiveIntegerValidator, isValidIfNone = false)

    forAll { (int: Int) =>
      Prop.all(
        validate(Foo("", None)).isInvalid,
        if (int > 0)
          validate(Foo("", Some(int))).isValid
        else
          validate(Foo("", Some(int))).errorsSummaryOption == Some("must be positive integer")
      )
    }
  }

  property("Validator.checkIfSome(with error prefix) returns Valid only if nested validator returns Valid") {
    val positiveIntegerValidator = Validator.checkIsTrue[Int](_ > 0, "must be positive integer")
    val validate: Validate[Foo] =
      Validator
        .checkIfSome[Foo, Int]((foo: Foo) => foo.bazOpt, positiveIntegerValidator, isValidIfNone = true)
        .withErrorPrefix("Foo.bazOpt ")

    forAll { (int: Int) =>
      Prop.all(
        validate(Foo("", None)).isValid,
        if (int > 0)
          validate(Foo("", Some(int))).isValid
        else
          validate(Foo("", Some(int))).errorsSummaryOption == Some("Foo.bazOpt must be positive integer")
      )
    }
  }

  property(
    "Validator.checkIfSome(with error prefix and invalid if none) returns Valid only if nested validator returns Valid"
  ) {
    val positiveIntegerValidator = Validator.checkIsTrue[Int](_ > 0, "must be positive integer")
    val validate: Validate[Foo] =
      Validator
        .checkIfSome[Foo, Int]((foo: Foo) => foo.bazOpt, positiveIntegerValidator, isValidIfNone = false)
        .withErrorPrefix("Foo.bazOpt ")

    forAll { (int: Int) =>
      Prop.all(
        validate(Foo("", None)).isInvalid,
        if (int > 0)
          validate(Foo("", Some(int))).isValid
        else
          validate(Foo("", Some(int))).errorsSummaryOption == Some("Foo.bazOpt must be positive integer")
      )
    }
  }

  property(
    "Validator.checkEach returns Valid only if all elements of the sequence passes check"
  ) {
    case class Ints(seq: Seq[Int])
    val negativeIntegerValidator = Validator.checkIsTrue[Int](_ < 0, "must be negative integer")
    val validate: Validate[Ints] =
      Validator.checkEach[Ints, Int]((i: Ints) => i.seq, negativeIntegerValidator)

    Prop.all(
      validate(Ints(Seq.empty)).isValid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, -1))) { (ints: Seq[Int]) =>
        validate(Ints(ints)).isValid
      },
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(0, Integer.MAX_VALUE))) { (ints: Seq[Int]) =>
        validate(Ints(ints)).errorsSummaryOption == Some("must be negative integer")
      }
    )
  }

  property(
    "Validator.checkEach(with error prefix fx) returns Valid only if all elements of the sequence passes check"
  ) {
    case class Ints(seq: Seq[Int])
    val negativeIntegerValidator = Validator.checkIsTrue[Int](_ < 0, "must be negative integer")
    val validate: Validate[Ints] =
      Validator
        .checkEachWithErrorPrefix[Ints, Int]((i: Ints) => i.seq, negativeIntegerValidator, (i: Int) => s"is[$i] ")

    Prop.all(
      validate(Ints(Seq.empty)).isValid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, -1))) { (ints: Seq[Int]) =>
        validate(Ints(ints)).isValid
      },
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(0, Integer.MAX_VALUE))) { (ints: Seq[Int]) =>
        val result = validate(Ints(ints))
        Prop.all(result.isInvalid, result.errorsCount == ints.size)
      }
    )
  }

  property(
    "Validator.checkEach(with error prefix fx) returns Valid only if all elements of the sequence passes check"
  ) {
    case class Ints(seq: Seq[Int])
    val negativeIntegerValidator = Validator.checkIsTrue[Int](_ < 0, "must be negative integer")
    val validate: Validate[Ints] =
      Validator.checkEachWithErrorPrefix[Ints, Int](
        (i: Ints) => i.seq,
        negativeIntegerValidator,
        (_: Int) => s"each element of 'is' "
      )

    Prop.all(
      validate(Ints(Seq.empty)).isValid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, -1))) { (ints: Seq[Int]) =>
        validate(Ints(ints)).isValid
      },
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(0, Integer.MAX_VALUE))) { (ints: Seq[Int]) =>
        val result = validate(Ints(ints))
        Prop.all(
          result.isInvalid,
          result.errorsCount == 1,
          result.errorsSummaryOption == Some("each element of 'is' must be negative integer")
        )
      }
    )
  }

  property(
    "Validator.checkEachIfNonEmpty returns Valid only if all elements of the sequence passes check"
  ) {
    case class Ints(seq: Seq[Int])
    val negativeIntegerValidator = Validator.checkIsTrue[Int](_ < 0, "must be negative integer")
    val validate: Validate[Ints] =
      Validator.checkEachIfNonEmpty[Ints, Int]((i: Ints) => i.seq, negativeIntegerValidator)

    Prop.all(
      validate(Ints(Seq.empty)).isInvalid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, -1))) { (ints: Seq[Int]) =>
        validate(Ints(ints)).isValid
      },
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(0, Integer.MAX_VALUE))) { (ints: Seq[Int]) =>
        validate(Ints(ints)).errorsSummaryOption == Some("must be negative integer")
      }
    )
  }

  property(
    "Validator.checkEachIfNonEmpty(with error prefix fx) returns Valid only if all elements of the sequence passes check"
  ) {
    case class Ints(seq: Seq[Int])
    val negativeIntegerValidator = Validator.checkIsTrue[Int](_ < 0, "must be negative integer")
    val validate: Validate[Ints] =
      Validator
        .checkEachIfNonEmptyWithErrorPrefix[Ints, Int](
          (i: Ints) => i.seq,
          negativeIntegerValidator,
          (i: Int) => s"is[$i] "
        )

    Prop.all(
      validate(Ints(Seq.empty)).isInvalid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, -1))) { (ints: Seq[Int]) =>
        validate(Ints(ints)).isValid
      },
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(0, Integer.MAX_VALUE))) { (ints: Seq[Int]) =>
        val result = validate(Ints(ints))
        Prop.all(result.isInvalid, result.errorsCount == ints.size)
      }
    )
  }

  property(
    "Validator.checkEachIfNonEmpty(with error prefix fx) returns Valid only if all elements of the sequence passes check"
  ) {
    case class Ints(seq: Seq[Int])
    val negativeIntegerValidator = Validator.checkIsTrue[Int](_ < 0, "must be negative integer")
    val validate: Validate[Ints] =
      Validator.checkEachIfNonEmptyWithErrorPrefix[Ints, Int](
        (i: Ints) => i.seq,
        negativeIntegerValidator,
        (_: Int) => s"each element of 'is' "
      )

    Prop.all(
      validate(Ints(Seq.empty)).isInvalid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, -1))) { (ints: Seq[Int]) =>
        validate(Ints(ints)).isValid
      },
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(0, Integer.MAX_VALUE))) { (ints: Seq[Int]) =>
        val result = validate(Ints(ints))
        Prop.all(
          result.isInvalid,
          result.errorsCount == 1,
          result.errorsSummaryOption == Some("each element of 'is' must be negative integer")
        )
      }
    )
  }

  property(
    "Validator.checkEachIfSome returns Valid only if None or all elements of the sequence passes check"
  ) {
    case class Ints(seqOpt: Option[Seq[Int]])
    val positiveIntegerValidator = Validator.checkIsTrue[Int](_ > 0, "must be positive integer")
    val validate: Validate[Ints] =
      Validator.checkEachIfSome[Ints, Int]((i: Ints) => i.seqOpt, positiveIntegerValidator)

    Prop.all(
      validate(Ints(None)).isValid,
      validate(Ints(Some(Seq.empty))).isValid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, 0))) { (ints: Seq[Int]) =>
        validate(Ints(Some(ints))).errorsSummaryOption == Some("must be positive integer")
      },
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(1, Integer.MAX_VALUE))) { (ints: Seq[Int]) =>
        validate(Ints(Some(ints))).isValid
      }
    )
  }

  property(
    "Validator.checkEachIfSome returns Valid only if sequence is defined and all elements of the sequence passes check"
  ) {
    case class Ints(seqOpt: Option[Seq[Int]])
    val positiveIntegerValidator = Validator.checkIsTrue[Int](_ > 0, "must be positive integer")
    val validate: Validate[Ints] =
      Validator.checkEachIfSome[Ints, Int]((i: Ints) => i.seqOpt, positiveIntegerValidator, isValidIfNone = false)

    Prop.all(
      validate(Ints(None)).isInvalid,
      validate(Ints(Some(Seq.empty))).isValid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, 0))) { (ints: Seq[Int]) =>
        validate(Ints(Some(ints))).errorsSummaryOption == Some("must be positive integer")
      },
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(1, Integer.MAX_VALUE))) { (ints: Seq[Int]) =>
        validate(Ints(Some(ints))).isValid
      }
    )
  }

  property(
    "Validator.checkEachIfSome(with error prefix) returns Valid only if sequence is None or all elements of the sequence passes check"
  ) {
    case class Ints(seqOpt: Option[Seq[Int]])
    val positiveIntegerValidator = Validator.checkIsTrue[Int](_ > 0, "must be positive integer")
    val validate: Validate[Ints] =
      Validator.checkEachIfSomeWithErrorPrefix[Ints, Int](
        (i: Ints) => i.seqOpt,
        positiveIntegerValidator,
        (i: Int) => s"intsOpt[$i] ",
        isValidIfNone = true
      )

    Prop.all(
      validate(Ints(None)).isValid,
      validate(Ints(Some(Seq.empty))).isValid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, 0))) { (ints: Seq[Int]) =>
        validate(Ints(Some(ints))).errorsCount == ints.size
      },
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(1, Integer.MAX_VALUE))) { (ints: Seq[Int]) =>
        validate(Ints(Some(ints))).isValid
      }
    )
  }

  property(
    "Validator.checkEachIfSome(with error prefix) returns Valid only if sequence is defined and all elements of the sequence passes check"
  ) {
    case class Ints(seqOpt: Option[Seq[Int]])
    val positiveIntegerValidator = Validator.checkIsTrue[Int](_ > 0, "must be positive integer")
    val validate: Validate[Ints] =
      Validator.checkEachIfSomeWithErrorPrefix[Ints, Int](
        (i: Ints) => i.seqOpt,
        positiveIntegerValidator,
        (i: Int) => s"intsOpt[$i] ",
        isValidIfNone = false
      )

    Prop.all(
      validate(Ints(None)).isInvalid,
      validate(Ints(Some(Seq.empty))).isValid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, 0))) { (ints: Seq[Int]) =>
        validate(Ints(Some(ints))).errorsCount == ints.size
      },
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(1, Integer.MAX_VALUE))) { (ints: Seq[Int]) =>
        validate(Ints(Some(ints))).isValid
      }
    )
  }

  property(
    "Validator.checkIfAllDefined returns Valid only if all of the provided functions returns Some"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator.checkIfAllDefined[Bar](Seq(_.a, _.b, _.c, _.d), "a, b, c, d")

    Prop.all(
      validate(Bar(None, None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(None, Some(0), None, None)).isInvalid,
      validate(Bar(None, None, Some(false), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(-1), Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(Some(""), Some(4), Some(true), Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(Some(""), Some(4), Some(true), None)).isInvalid,
      validate(Bar(Some(""), Some(4), None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(4), Some(true), None)).isInvalid
    )
  }

  property(
    "Validator.checkIfAllEmpty returns Valid only if all of the provided functions returns None"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator.checkIfAllEmpty[Bar](Seq(_.a, _.b, _.c, _.d), "a, b, c, d")

    Prop.all(
      validate(Bar(None, None, None, None)).isValid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(None, Some(0), None, None)).isInvalid,
      validate(Bar(None, None, Some(false), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(-1), Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(Some(""), Some(4), Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(Some(""), Some(4), Some(true), None)).isInvalid,
      validate(Bar(Some(""), Some(4), None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(4), Some(true), None)).isInvalid
    )
  }

  property(
    "Validator.checkIfAllOrNoneDefined returns Valid only if all of the provided functions returns None"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator.checkIfAllOrNoneDefined[Bar](Seq(_.a, _.b, _.c, _.d), "a, b, c, d")

    Prop.all(
      validate(Bar(None, None, None, None)).isValid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(None, Some(0), None, None)).isInvalid,
      validate(Bar(None, None, Some(false), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(-1), Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(Some(""), Some(4), Some(true), Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(Some(""), Some(4), Some(true), None)).isInvalid,
      validate(Bar(Some(""), Some(4), None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(4), Some(true), None)).isInvalid
    )
  }

  property(
    "Validator.checkIfAtLeastOneIsDefined returns Valid only if at least one of the provided functions returns Some"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator.checkIfAtLeastOneIsDefined[Bar](Seq(_.a, _.b, _.c, _.d), "a, b, c, d")

    Prop.all(
      validate(Bar(None, None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isValid,
      validate(Bar(None, Some(0), None, None)).isValid,
      validate(Bar(None, None, Some(false), None)).isValid,
      validate(Bar(None, None, None, Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(None, Some(-1), Some(true), Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(Some(""), Some(4), Some(true), Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(Some(""), Some(4), Some(true), None)).isValid,
      validate(Bar(Some(""), Some(4), None, None)).isValid,
      validate(Bar(Some(""), None, None, None)).isValid,
      validate(Bar(Some(""), None, None, Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(None, Some(4), Some(true), None)).isValid
    )
  }

  property(
    "Validator.checkIfAtMostOneIsDefined returns Valid only if at most one of the provided functions returns Some"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator.checkIfAtMostOneIsDefined[Bar](Seq(_.a, _.b, _.c, _.d), "a, b, c, d")

    Prop.all(
      validate(Bar(None, None, None, None)).isValid,
      validate(Bar(Some(""), None, None, None)).isValid,
      validate(Bar(None, Some(0), None, None)).isValid,
      validate(Bar(None, None, Some(false), None)).isValid,
      validate(Bar(None, None, None, Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(-1), Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(Some(""), Some(4), Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(Some(""), Some(4), Some(true), None)).isInvalid,
      validate(Bar(Some(""), Some(4), None, None)).isInvalid,
      validate(Bar(Some(""), None, None, Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(4), Some(true), None)).isInvalid
    )
  }

  property(
    "Validator.checkIfOnlyOneIsDefined returns Valid if only one of the provided functions returns Some"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator.checkIfOnlyOneIsDefined[Bar](Seq(_.a, _.b, _.c, _.d), "a, b, c, d")

    Prop.all(
      validate(Bar(None, None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isValid,
      validate(Bar(None, Some(0), None, None)).isValid,
      validate(Bar(None, None, Some(false), None)).isValid,
      validate(Bar(None, None, None, Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(-1), Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(Some(""), Some(4), Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(Some(""), Some(4), Some(true), None)).isInvalid,
      validate(Bar(Some(""), Some(4), None, None)).isInvalid,
      validate(Bar(Some(""), None, None, Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(4), Some(true), None)).isInvalid
    )
  }

  property(
    "Validator.checkIfOnlyOneSetIsDefined returns Valid if only one of the provided set of functions have all results defined"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator.checkIfOnlyOneSetIsDefined[Bar](Seq(Set(_.a, _.b, _.c, _.d)), "a and b and c and d")

    Prop.all(
      validate(Bar(None, None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(None, Some(0), None, None)).isInvalid,
      validate(Bar(None, None, Some(false), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(-1), Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(Some(""), Some(4), Some(true), Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(Some(""), Some(4), Some(true), None)).isInvalid,
      validate(Bar(Some(""), Some(4), None, None)).isInvalid,
      validate(Bar(Some(""), None, None, Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(4), Some(true), None)).isInvalid
    )
  }

  property(
    "Validator.checkIfOnlyOneSetIsDefined returns Valid if only one of the provided set of functions have all results defined"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator.checkIfOnlyOneSetIsDefined[Bar](
      Seq(
        Set(_.c, _.d),
        Set(_.a, _.b)
      ),
      "(a and b) or (c and d)"
    )

    Prop.all(
      validate(Bar(None, None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(None, Some(0), None, None)).isInvalid,
      validate(Bar(None, None, Some(false), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(None, Some(-1), Some(true), Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(Some(""), Some(4), Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(Some(""), Some(4), Some(true), None)).isValid,
      validate(Bar(Some(""), Some(4), None, None)).isValid,
      validate(Bar(Some(""), None, None, Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(4), Some(true), None)).isInvalid
    )
  }

  property(
    "Validator.checkIfAllTrue returns Valid only if all of the provided functions return true"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator
      .checkIfAllTrue[Bar](
        Seq(_.a.isDefined, _.b.exists(_ > 0), _.c.contains(false), _.d.exists(_.size > 1)),
        "a must be defined or b must be gt zero or c must contain false or d must be a sequence of at least two elements"
      )

    Prop.all(
      validate(Bar(None, None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(None, Some(1), None, None)).isInvalid,
      validate(Bar(None, None, Some(false), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1, 2)))).isInvalid,
      validate(Bar(None, Some(0), None, None)).isInvalid,
      validate(Bar(None, None, Some(true), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1)))).isInvalid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(0), Some(false), Some(Seq(1)))).isInvalid,
      validate(Bar(None, Some(0), Some(true), Some(Seq(1)))).isInvalid,
      validate(Bar(Some(""), Some(-1), Some(true), Some(Seq.empty))).isInvalid,
      validate(Bar(Some(""), Some(0), Some(true), None)).isInvalid,
      validate(Bar(Some(""), Some(0), None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, Some(Seq(1)))).isInvalid,
      validate(Bar(None, Some(1), Some(true), None)).isInvalid,
      validate(Bar(None, Some(0), Some(true), None)).isInvalid,
      validate(Bar(None, Some(0), Some(false), None)).isInvalid,
      validate(Bar(Some(""), Some(1), Some(false), Some(Seq(1, 2)))).isValid
    )
  }

  property(
    "Validator.checkIfAllFalse returns Valid only if all of the provided functions return true"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator
      .checkIfAllFalse[Bar](
        Seq(_.a.isDefined, _.b.exists(_ > 0), _.c.contains(false), _.d.exists(_.size > 1)),
        "a must be defined or b must be gt zero or c must contain false or d must be a sequence of at least two elements"
      )

    Prop.all(
      validate(Bar(None, None, None, None)).isValid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(None, Some(1), None, None)).isInvalid,
      validate(Bar(None, None, Some(false), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1, 2)))).isInvalid,
      validate(Bar(None, Some(0), None, None)).isValid,
      validate(Bar(None, None, Some(true), None)).isValid,
      validate(Bar(None, None, None, Some(Seq(1)))).isValid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(0), Some(false), Some(Seq(1)))).isInvalid,
      validate(Bar(None, Some(0), Some(true), Some(Seq(1)))).isValid,
      validate(Bar(Some(""), Some(-1), Some(true), Some(Seq.empty))).isInvalid,
      validate(Bar(Some(""), Some(0), Some(true), None)).isInvalid,
      validate(Bar(Some(""), Some(0), None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, Some(Seq(1)))).isInvalid,
      validate(Bar(None, Some(1), Some(true), None)).isInvalid,
      validate(Bar(None, Some(0), Some(true), None)).isValid,
      validate(Bar(None, Some(0), Some(false), None)).isInvalid,
      validate(Bar(Some(""), Some(1), Some(false), Some(Seq(1, 2)))).isInvalid
    )
  }

  property(
    "Validator.checkIfAtLeastOneIsTrue returns Valid if only if at least one of the provided functions return true"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator
      .checkIfAtLeastOneIsTrue[Bar](
        Seq(_.a.isDefined, _.b.exists(_ > 0), _.c.contains(false), _.d.exists(_.size > 1)),
        "a must be defined or b must be gt zero or c must contain false or d must be a sequence of at least two elements"
      )

    Prop.all(
      validate(Bar(None, None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isValid,
      validate(Bar(None, Some(1), None, None)).isValid,
      validate(Bar(None, None, Some(false), None)).isValid,
      validate(Bar(None, None, None, Some(Seq(1, 2)))).isValid,
      validate(Bar(None, Some(0), None, None)).isInvalid,
      validate(Bar(None, None, Some(true), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1)))).isInvalid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(None, Some(0), Some(false), Some(Seq(1)))).isValid,
      validate(Bar(None, Some(0), Some(true), Some(Seq(1)))).isInvalid,
      validate(Bar(Some(""), Some(-1), Some(true), Some(Seq.empty))).isValid,
      validate(Bar(Some(""), Some(0), Some(true), None)).isValid,
      validate(Bar(Some(""), Some(0), None, None)).isValid,
      validate(Bar(Some(""), None, None, None)).isValid,
      validate(Bar(Some(""), None, None, Some(Seq(1)))).isValid,
      validate(Bar(None, Some(1), Some(true), None)).isValid,
      validate(Bar(None, Some(0), Some(true), None)).isInvalid,
      validate(Bar(None, Some(0), Some(false), None)).isValid
    )
  }

  property(
    "Validator.checkIfAtMostOneIsTrue returns Valid only if at most one of the provided functions return true"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator
      .checkIfAtMostOneIsTrue[Bar](
        Seq(_.a.isDefined, _.b.exists(_ > 0), _.c.contains(false), _.d.exists(_.size > 1)),
        "a must be defined or b must be gt zero or c must contain false or d must be a sequence of at least two elements"
      )

    Prop.all(
      validate(Bar(None, None, None, None)).isValid,
      validate(Bar(Some(""), None, None, None)).isValid,
      validate(Bar(None, Some(1), None, None)).isValid,
      validate(Bar(None, None, Some(false), None)).isValid,
      validate(Bar(None, None, None, Some(Seq(1, 2)))).isValid,
      validate(Bar(None, Some(0), None, None)).isValid,
      validate(Bar(None, None, Some(true), None)).isValid,
      validate(Bar(None, None, None, Some(Seq(1)))).isValid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(None, Some(0), Some(false), Some(Seq(1)))).isValid,
      validate(Bar(None, Some(0), Some(true), Some(Seq(1)))).isValid,
      validate(Bar(Some(""), Some(-1), Some(true), Some(Seq.empty))).isValid,
      validate(Bar(Some(""), Some(0), Some(true), None)).isValid,
      validate(Bar(Some(""), Some(0), None, None)).isValid,
      validate(Bar(Some(""), None, None, None)).isValid,
      validate(Bar(Some(""), None, None, Some(Seq(1)))).isValid,
      validate(Bar(None, Some(1), Some(true), None)).isValid,
      validate(Bar(None, Some(0), Some(true), None)).isValid,
      validate(Bar(None, Some(1), Some(false), None)).isInvalid,
      validate(Bar(Some(""), Some(1), Some(true), None)).isInvalid,
      validate(Bar(Some(""), Some(0), Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(Some(""), Some(1), Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(Some(""), Some(1), Some(false), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(0), Some(false), None)).isValid
    )
  }

  property(
    "Validator.checkIfOnlyOneIsTrue returns Valid if only one of the provided functions return true"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator
      .checkIfOnlyOneIsTrue[Bar](
        Seq(_.a.isDefined, _.b.exists(_ > 0), _.c.contains(false), _.d.exists(_.size > 1)),
        "a must be defined or b must be gt zero or c must contain false or d must be a sequence of at least two elements"
      )

    Prop.all(
      validate(Bar(None, None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isValid,
      validate(Bar(None, Some(1), None, None)).isValid,
      validate(Bar(None, None, Some(false), None)).isValid,
      validate(Bar(None, None, None, Some(Seq(1, 2)))).isValid,
      validate(Bar(None, Some(0), None, None)).isInvalid,
      validate(Bar(None, None, Some(true), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1)))).isInvalid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isValid,
      validate(Bar(None, Some(0), Some(false), Some(Seq(1)))).isValid,
      validate(Bar(None, Some(0), Some(true), Some(Seq(1)))).isInvalid,
      validate(Bar(Some(""), Some(-1), Some(true), Some(Seq.empty))).isValid,
      validate(Bar(Some(""), Some(0), Some(true), None)).isValid,
      validate(Bar(Some(""), Some(0), None, None)).isValid,
      validate(Bar(Some(""), None, None, None)).isValid,
      validate(Bar(Some(""), None, None, Some(Seq(1)))).isValid,
      validate(Bar(None, Some(1), Some(true), None)).isValid,
      validate(Bar(None, Some(0), Some(true), None)).isInvalid,
      validate(Bar(None, Some(0), Some(false), None)).isValid
    )
  }

  property(
    "Validator.checkIfOnlyOneSetIsTrue returns Valid if only one of the provided set of functions is all true"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator
      .checkIfOnlyOneSetIsTrue[Bar](
        Seq(Set(_.a.isDefined, _.b.exists(_ > 0), _.c.contains(false), _.d.exists(_.size > 1))),
        "a must be defined or b must be gt zero or c must contain false or d must be a sequence of at least two elements"
      )

    Prop.all(
      validate(Bar(None, None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(None, Some(1), None, None)).isInvalid,
      validate(Bar(None, None, Some(false), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1, 2)))).isInvalid,
      validate(Bar(None, Some(0), None, None)).isInvalid,
      validate(Bar(None, None, Some(true), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1)))).isInvalid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(0), Some(false), Some(Seq(1)))).isInvalid,
      validate(Bar(None, Some(0), Some(true), Some(Seq(1)))).isInvalid,
      validate(Bar(Some(""), Some(-1), Some(true), Some(Seq.empty))).isInvalid,
      validate(Bar(Some(""), Some(0), Some(true), None)).isInvalid,
      validate(Bar(Some(""), Some(0), None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, Some(Seq(1)))).isInvalid,
      validate(Bar(None, Some(1), Some(true), None)).isInvalid,
      validate(Bar(None, Some(0), Some(true), None)).isInvalid,
      validate(Bar(None, Some(0), Some(false), None)).isInvalid,
      validate(Bar(Some(""), Some(1), Some(false), Some(Seq(1, 2)))).isValid
    )
  }

  property(
    "Validator.checkIfOnlyOneSetIsTrue returns Valid if only one of the provided set of functions is all true"
  ) {
    case class Bar(a: Option[String], b: Option[Int], c: Option[Boolean], d: Option[Seq[Int]])
    val validate = Validator
      .checkIfOnlyOneSetIsTrue[Bar](
        Seq(
          Set(_.a.isDefined, _.b.exists(_ > 0)),
          Set(_.c.contains(false), _.d.exists(_.size > 1))
        ),
        "a must be defined or b must be gt zero or c must contain false or d must be a sequence of at least two elements"
      )

    Prop.all(
      validate(Bar(None, None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(None, Some(1), None, None)).isInvalid,
      validate(Bar(None, None, Some(false), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1, 2)))).isInvalid,
      validate(Bar(None, Some(0), None, None)).isInvalid,
      validate(Bar(None, None, Some(true), None)).isInvalid,
      validate(Bar(None, None, None, Some(Seq(1)))).isInvalid,
      validate(Bar(None, None, Some(true), Some(Seq(1, 2, 3)))).isInvalid,
      validate(Bar(None, Some(0), Some(false), Some(Seq(1)))).isInvalid,
      validate(Bar(None, Some(0), Some(true), Some(Seq(1)))).isInvalid,
      validate(Bar(Some(""), Some(-1), Some(true), Some(Seq.empty))).isInvalid,
      validate(Bar(Some(""), Some(0), Some(true), None)).isInvalid,
      validate(Bar(Some(""), Some(0), None, None)).isInvalid,
      validate(Bar(Some(""), None, None, None)).isInvalid,
      validate(Bar(Some(""), None, None, Some(Seq(1)))).isInvalid,
      validate(Bar(None, Some(1), Some(true), None)).isInvalid,
      validate(Bar(None, Some(0), Some(true), None)).isInvalid,
      validate(Bar(None, Some(0), Some(false), None)).isInvalid,
      validate(Bar(Some(""), Some(1), Some(false), Some(Seq(1, 2)))).isInvalid,
      validate(Bar(Some(""), Some(0), Some(false), Some(Seq(1, 2)))).isValid,
      validate(Bar(Some(""), Some(1), Some(true), Some(Seq(1, 2)))).isValid
    )

  }

  test("example") {

    case class Address(street: String, town: String, postcode: String, country: String)
    case class PhoneNumber(prefix: String, number: String, description: String)
    case class Contact(name: String, address: Address, phoneNumbers: Seq[PhoneNumber])

    object Country {
      val codes = Set("en", "de", "fr")
      val telephonePrefixes = Set("+44", "+41", "+42")
    }

    val postcodeCheck = checkIsTrue[String](_.matches("""\d{5}"""), "address.postcode.invalid")
    val countryCheck = checkIsTrue[String](_.isOneOf(Country.codes), "address.country.invalid")
    val phoneNumberPrefixCheck =
      checkIsTrue[String](_.isOneOf(Country.telephonePrefixes), "address.phone.prefix.invalid")
    val phoneNumberValueCheck = checkIsTrue[String](_.matches("""\d{7}"""), "address.phone.prefix.invalid")

    val addressCheck = all[Address](
      checkIsTrue(_.street.nonEmpty, "address.street.empty"),
      checkIsTrue(_.town.nonEmpty, "address.town.empty"),
      checkWith(_.postcode, postcodeCheck),
      checkWith(_.country, countryCheck)
    )

    val phoneNumberCheck = all[PhoneNumber](
      checkWith(_.prefix, phoneNumberPrefixCheck),
      checkWith(_.number, phoneNumberValueCheck)
    )

    val contactCheck = all[Contact](
      checkIsTrue(_.name.nonEmpty, "contact.name.empty"),
      checkWith(_.address, addressCheck),
      checkEach(_.phoneNumbers, phoneNumberCheck)
    )

    val c1 = Contact(
      name = "Foo Bar",
      address = Address(street = "Sesame Street 1", town = "Cookieburgh", country = "en", postcode = "00001"),
      phoneNumbers = Seq(PhoneNumber("+44", "1234567", "ceo"), PhoneNumber("+41", "7654321", "sales"))
    )

    assert(contactCheck(c1).isValid, "expected checks to pass")

    val c2 = Contact(
      name = "",
      address = Address(street = "", town = "", country = "ca", postcode = "foobar"),
      phoneNumbers = Seq(PhoneNumber("+1", "11111111111", "ceo"), PhoneNumber("+01", "00000000", "sales"))
    )

    assert(contactCheck(c2).isInvalid, "expected checks to fail")
  }

}

object ValidatorSpec {}
