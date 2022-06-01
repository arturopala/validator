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

  property("Validator.all combines provided validators to verify if all checks passes") {
    val nonEmptyStringValidator = Validator.check[String](_.nonEmpty, "string must be non-empty")
    val emptyStringValidator = Validator.check[String](_.isEmpty(), "string must be empty")
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
    val nonEmptyStringValidator = Check[String](_.nonEmpty, "string must be non-empty")
    val emptyStringValidator = Check[String](_.isEmpty(), "string must be empty")
    val validate: Check[String] = nonEmptyStringValidator && emptyStringValidator

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
    val nonEmptyStringValidator = Validator.check[String](_.nonEmpty, "string must be non-empty")
    val emptyStringValidator = Validator.check[String](_.isEmpty(), "string must be empty")
    val validate: Validate[String] =
      Validator.allWithPrefix("foo: ", nonEmptyStringValidator, emptyStringValidator)

    forAll { (string: String) =>
      Prop.all(
        emptyStringValidator("").isValid,
        ("foo: " @: nonEmptyStringValidator)("").errorString == Some("foo: string must be non-empty"),
        (emptyStringValidator.withErrorPrefix("@ "))(s"a$string").errorString == Some("@ string must be empty"),
        nonEmptyStringValidator(s"a$string").isValid,
        Validator.allWithPrefix("foo_", nonEmptyStringValidator, emptyStringValidator).apply(string).errorString ==
          Some(if (string.isEmpty) "foo_string must be non-empty" else "foo_string must be empty"),
        Validator.allWithPrefix("bar/", nonEmptyStringValidator, emptyStringValidator).apply(string).errorString ==
          Some(if (string.isEmpty) "bar/string must be non-empty" else "bar/string must be empty"),
        validate(string).errorString ==
          Some(if (string.isEmpty) "foo: string must be non-empty" else "foo: string must be empty")
      )
    }
  }

  property("Validator.all(with error prefix) combines provided checks to verify if all checks passes") {
    val nonEmptyStringValidator = Check[String](_.nonEmpty, "string must be non-empty")
    val emptyStringValidator = Check[String](_.isEmpty(), "string must be empty")
    val validate: Validate[String] =
      Validator.allWithPrefix("foo: ", nonEmptyStringValidator, emptyStringValidator)

    forAll { (string: String) =>
      Prop.all(
        emptyStringValidator("").isValid,
        ("foo: " @: nonEmptyStringValidator)("").errorString == Some("foo: string must be non-empty"),
        (emptyStringValidator.withErrorPrefix("@ "))(s"a$string").errorString == Some("@ string must be empty"),
        nonEmptyStringValidator(s"a$string").isValid,
        Validator
          .allWithPrefix[String]("foo_", nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorString ==
          Some(if (string.isEmpty) "foo_string must be non-empty" else "foo_string must be empty"),
        Validator
          .allWithPrefix[String]("bar/", nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorString ==
          Some(if (string.isEmpty) "bar/string must be non-empty" else "bar/string must be empty"),
        validate(string).errorString ==
          Some(if (string.isEmpty) "foo: string must be non-empty" else "foo: string must be empty")
      )
    }
  }

  property("Validator.all(with calculated error prefix) combines provided validators to verify if all checks passes") {
    val nonEmptyStringValidator = Validator.check[String](_.nonEmpty, "string must be non-empty")
    val emptyStringValidator = Validator.check[String](_.isEmpty(), "string must be empty")

    val calculatePrefix: String => String = s => s"${s.take(1)}: "

    forAll { (string: String) =>
      val f = string.take(1)
      Prop.all(
        Validator
          .allWithComputedPrefix(calculatePrefix, nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorString ==
          Some(if (string.isEmpty) s"$f: string must be non-empty" else s"$f: string must be empty"),
        Validator
          .allWithComputedPrefix(calculatePrefix, nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorString ==
          Some(if (string.isEmpty) s"$f: string must be non-empty" else s"$f: string must be empty"),
        Validator
          .allWithComputedPrefix(calculatePrefix, nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorString ==
          Some(if (string.isEmpty) s"$f: string must be non-empty" else s"$f: string must be empty")
      )
    }
  }

  property("Validator.all(with calculated error prefix) combines provided checks to verify if all checks passes") {
    val nonEmptyStringValidator = Check[String](_.nonEmpty, "string must be non-empty")
    val emptyStringValidator = Check[String](_.isEmpty(), "string must be empty")

    val calculatePrefix: String => String = s => s"${s.take(1)}: "

    forAll { (string: String) =>
      val f = string.take(1)
      Prop.all(
        Validator
          .allWithComputedPrefix[String](calculatePrefix, nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorString ==
          Some(if (string.isEmpty) s"$f: string must be non-empty" else s"$f: string must be empty"),
        Validator
          .allWithComputedPrefix[String](calculatePrefix, nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorString ==
          Some(if (string.isEmpty) s"$f: string must be non-empty" else s"$f: string must be empty"),
        Validator
          .allWithComputedPrefix[String](calculatePrefix, nonEmptyStringValidator, emptyStringValidator)
          .apply(string)
          .errorString ==
          Some(if (string.isEmpty) s"$f: string must be non-empty" else s"$f: string must be empty")
      )
    }
  }

  property("Validator.any combines provided validators to verify if any of the checks passes") {
    val hasDigitValidator = Validator.check[String](_.exists(_.isDigit), "some characters must be digits")
    val hasLowerCaseValidator =
      Validator.check[String](_.exists(_.isLower), "some characters must be lower case")
    val validate: Validate[String] = hasDigitValidator | hasLowerCaseValidator

    forAllNoShrink(Gen.alphaChar, Gen.numChar) { (a: Char, d: Char) =>
      (a.isLower) ==>
        Prop.all(
          hasDigitValidator(s"$a/$d").isValid,
          hasLowerCaseValidator(s"$a!$d").isValid,
          hasDigitValidator(s"${a.toUpper}").errorString == Some("some characters must be digits"),
          hasLowerCaseValidator(s"${a.toUpper}").errorString == Some("some characters must be lower case"),
          validate(s"$a-$d").isValid,
          validate(s"$a-$a").isValid,
          validate(s"$d-$d").isValid,
          validate(s"$d-$a").isValid,
          Validator.any(hasDigitValidator, hasLowerCaseValidator).apply(s"$a-$d").isValid,
          Validator.any(hasDigitValidator, hasLowerCaseValidator).apply(s"$a-$a").isValid,
          Validator.any(hasDigitValidator, hasLowerCaseValidator).apply(s"$d-$d").isValid,
          Validator.any(hasDigitValidator, hasLowerCaseValidator).apply(s"$d-$a").isValid,
          validate(s"${a.toUpper}" * d.toInt).errorString(", ") == Some(
            "some characters must be digits, some characters must be lower case"
          ),
          Validator
            .any(hasDigitValidator, hasLowerCaseValidator)
            .apply(s"${a.toUpper}" * d.toInt)
            .errorString(", ") == Some(
            "some characters must be digits, some characters must be lower case"
          )
        )
    }
  }

  property("Validator.any(with error prefix) combines provided validators to verify if any of the checks passes") {
    val hasDigitValidator = Validator.check[String](_.exists(_.isDigit), "some characters must be digits")
    val hasLowerCaseValidator =
      Validator.check[String](_.exists(_.isLower), "some characters must be lower case")

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
            .errorString(", ") == Some(
            "foo_some characters must be digits, foo_some characters must be lower case"
          )
        )
    }
  }

  property(
    "Validator.any(with calculated error prefix) combines provided validators to verify if any of the checks passes"
  ) {
    val hasDigitValidator = Validator.check[String](_.exists(_.isDigit), "some characters must be digits")
    val hasLowerCaseValidator =
      Validator.check[String](_.exists(_.isLower), "some characters must be lower case")

    val calculatePrefix: String => String = s => s"${s.take(1)}_"

    forAllNoShrink(Gen.alphaChar, Gen.numChar) { (a: Char, d: Char) =>
      (a.isLower) ==>
        Prop.all(
          Validator
            .anyWithComputedPrefix(calculatePrefix, hasDigitValidator, hasLowerCaseValidator)
            .apply(s"${a.toUpper}" * d.toInt)
            .errorString(", ") == Some(
            s"${a.toUpper}_some characters must be digits, ${a.toUpper}_some characters must be lower case"
          )
        )
    }
  }

  test("Validator.conditionally runs the test and follows with either first or second check") {
    val validateOnlyDigits = Validator.check[String](_.forall(_.isDigit), "all characters must be digits")
    val validateNonEmpty = Validator.check[String](_.nonEmpty, "must be non empty string")
    def validateLength(length: Int) = Validator.check[String](_.length() == length, s"must have $length characters")
    val validateAllUpperCase = Validator.check[String](_.forall(_.isUpper), "all characters must be upper case")
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
    assert(validate("").errorString == Some("must be non empty string"))
    assert(validate("Az").errorString == Some("all characters must be upper case"))
    assert(validate("az").errorString == Some("all characters must be upper case"))
    assert(validate("a").errorString == Some("all characters must be upper case"))
    assert(validate("0").errorString == Some("must have 3 characters"))
    assert(validate("00").errorString == Some("must have 3 characters"))
    assert(validate("123").errorString == Some("all characters must be upper case"))
    assert(validate("0000").errorString == Some("must have 3 characters"))
  }

  test("Validator.whenTrue runs the test and if true then follows with the next check") {
    val validateOnlyDigits = Validator.check[String](_.forall(_.isDigit), "all characters must be digits")
    def validateLength(length: Int) = Validator.check[String](_.length() == length, s"must have $length characters")
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
    assert(validate("0").errorString == Some("must have 3 characters"))
    assert(validate("00").errorString == Some("must have 3 characters"))
    assert(validate("0000").errorString == Some("must have 3 characters"))
  }

  test("Validator.whenFalse runs the test and if false then tries the next check") {
    val validateNonEmpty = Validator.check[String](_.nonEmpty, "must be non empty string")
    val validateAllUpperCase = Validator.check[String](_.forall(_.isUpper), "all characters must be upper case")
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
    assert(validate("").errorString == Some("must be non empty string"))
    assert(validate("Az").errorString == Some("all characters must be upper case"))
    assert(validate("az").errorString == Some("all characters must be upper case"))
    assert(validate("a").errorString == Some("all characters must be upper case"))
    assert(validate("1").errorString == Some("all characters must be upper case"))
    assert(validate("12").errorString == Some("all characters must be upper case"))
    assert(validate("123").errorString == Some("all characters must be upper case"))
    assert(validate("1ABC").errorString == Some("all characters must be upper case"))
  }

  test("Validator.when runs the guard check and follows with either first or second check") {
    val validateStartsWithZero =
      Validator.check[String](_.headOption.contains('0'), "first character must be a Zero")
    val validateOnlyDigits = Validator.check[String](_.forall(_.isDigit), "all characters must be digits")
    val validateNonEmpty = Validator.check[String](_.nonEmpty, "must be non empty string")
    def validateLength(length: Int) = Validator.check[String](_.length() == length, s"must have $length characters")
    val validateAllUpperCase = Validator.check[String](_.forall(_.isUpper), "all characters must be upper case")
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
    assert(validate("").errorString == Some("must be non empty string"))
    assert(validate("Az").errorString == Some("all characters must be upper case"))
    assert(validate("az").errorString == Some("all characters must be upper case"))
    assert(validate("a").errorString == Some("all characters must be upper case"))
    assert(validate("0").errorString == Some("must have 3 characters"))
    assert(validate("00").errorString == Some("must have 3 characters"))
    assert(validate("123").errorString == Some("all characters must be upper case"))
    assert(validate("0000").errorString == Some("must have 3 characters"))
  }

  test("Validator.whenValid runs the guard check and if valid then follows with the next check") {
    val validateStartsWithZero =
      Validator.check[String](_.headOption.contains('0'), "first character must be a Zero")
    val validateOnlyDigits = Validator.check[String](_.forall(_.isDigit), "all characters must be digits")
    def validateLength(length: Int) = Validator.check[String](_.length() == length, s"must have $length characters")
    val validate1: Validate[String] =
      Validator.whenValid(validateStartsWithZero, validateLength(3) & validateOnlyDigits).debug
    val validate2: Validate[String] =
      validateStartsWithZero.andWhenValid(validateLength(3) & validateOnlyDigits)
    val validate3: Validate[String] =
      validateStartsWithZero ? (validateLength(3) & validateOnlyDigits)

    def runtWith(validate: Validate[String]) = {
      assert(validate("000").isValid)
      assert(validate("012").isValid)
      assert(validate("A").errorString == Some("first character must be a Zero"))
      assert(validate("AZ").errorString == Some("first character must be a Zero"))
      assert(validate("ABC").errorString == Some("first character must be a Zero"))
      assert(validate("").errorString == Some("first character must be a Zero"))
      assert(validate("Az").errorString == Some("first character must be a Zero"))
      assert(validate("az").errorString == Some("first character must be a Zero"))
      assert(validate("a").errorString == Some("first character must be a Zero"))
      assert(validate("123").errorString == Some("first character must be a Zero"))
      assert(validate("0").errorString == Some("must have 3 characters"))
      assert(validate("00").errorString == Some("must have 3 characters"))
      assert(validate("0000").errorString == Some("must have 3 characters"))
    }

    runtWith(validate1)
    runtWith(validate2)
    runtWith(validate3)
  }

  test("Validator.whenInvalid runs the guard check and if invalid then tries the next check") {
    val validateStartsWithZero =
      Validator.check[String](_.headOption.contains('0'), "first character must be a Zero")
    val validateNonEmpty = Validator.check[String](_.nonEmpty, "must be non empty string")
    val validateAllUpperCase = Validator.check[String](_.forall(_.isUpper), "all characters must be upper case")
    val validate1: Validate[String] =
      Validator.whenInvalid(validateStartsWithZero, validateNonEmpty & validateAllUpperCase)
    val validate2: Validate[String] =
      validateStartsWithZero.andWhenInvalid(validateNonEmpty & validateAllUpperCase)
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
      assert(validate("").errorString == Some("must be non empty string"))
      assert(validate("Az").errorString == Some("all characters must be upper case"))
      assert(validate("az").errorString == Some("all characters must be upper case"))
      assert(validate("a").errorString == Some("all characters must be upper case"))
      assert(validate("1").errorString == Some("all characters must be upper case"))
      assert(validate("12").errorString == Some("all characters must be upper case"))
      assert(validate("123").errorString == Some("all characters must be upper case"))
    }

    runtWith(validate1)
    runtWith(validate2)
    runtWith(validate3)
  }

  property("Validator.product combines provided validators to verify tuples of values") {
    val hasDigitValidator = Validator.check[Char](_.isDigit, "character must be a digit")
    val hasLowerCaseValidator =
      Validator.check[Char](_.isLower, "character must be lower case")
    val validate: Validate[(Char, Char)] = hasDigitValidator * hasLowerCaseValidator

    forAllNoShrink(Gen.alphaChar, Gen.numChar) { (a: Char, d: Char) =>
      (a.isLower && !a.isDigit) ==>
        Prop.all(
          hasDigitValidator(d).isValid,
          hasLowerCaseValidator(a).isValid,
          hasDigitValidator(a).errorString == Some("character must be a digit"),
          hasLowerCaseValidator(a.toUpper).errorString == Some("character must be lower case"),
          validate.apply((d, a)).isValid,
          validate.apply((a, d)).errorString == Some("character must be a digit,character must be lower case")
        )
    }
  }

  property("Validator.check returns Valid only if condition fulfilled") {
    val validate =
      Validator.check[Foo]((foo: Foo) => foo.bar.startsWith("a"), "foo.bar must start with A")

    Prop.all(
      forAll { (string: String) =>
        validate(Foo(s"a$string")).isValid
        validate(Foo(s"a$string")).errorString.isEmpty
      },
      forAll { (string: String, char: Char) =>
        (char != 'a') ==>
          (validate(Foo(s"$char$string")).errorString == Some("foo.bar must start with A"))
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
      validate(None).errorString == Some("option must be defined")
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
        validate(int).errorString == Some("must be positive")
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
        validate(int).errorString == Some("integer must be positive")
    }
  }

  property("Validator.checkProperty returns Valid only if extracted property passes check") {
    val nonEmptyStringValidator = Validator.check[String](_.nonEmpty, "string must be non-empty")
    val validate: Validate[Foo] =
      Validator.checkProperty[Foo, String]((foo: Foo) => foo.bar, nonEmptyStringValidator)

    forAll { (string: String) =>
      if (string.nonEmpty)
        validate(Foo(string)).isValid
      else
        validate(Foo(string)).errorString == Some("string must be non-empty")
    }
  }

  property("Validator.checkProperty(with error prefix) returns Valid only if nested validator returns Valid") {
    val nonEmptyStringValidator = Validator.check[String](_.nonEmpty, "string must be non-empty")
    val validate: Validate[Foo] =
      Validator
        .checkProperty[Foo, String]((foo: Foo) => foo.bar, nonEmptyStringValidator)
        .withErrorPrefix("Foo.bar ")

    forAll { (string: String) =>
      if (string.nonEmpty)
        validate(Foo(string)).isValid
      else
        validate(Foo(string)).errorString == Some("Foo.bar string must be non-empty")
    }
  }

  property("Validator.checkIfSome returns Valid only if nested validator returns Valid") {
    val positiveIntegerValidator = Validator.check[Int](_ > 0, "must be positive integer")
    val validate: Validate[Foo] =
      Validator.checkIfSome[Foo, Int]((foo: Foo) => foo.bazOpt, positiveIntegerValidator)

    forAll { (int: Int) =>
      Prop.all(
        validate(Foo("", None)).isValid,
        if (int > 0)
          validate(Foo("", Some(int))).isValid
        else
          validate(Foo("", Some(int))).errorString == Some("must be positive integer")
      )
    }
  }

  property("Validator.checkIfSome(with invalid if None) returns Valid only if nested validator returns Valid") {
    val positiveIntegerValidator = Validator.check[Int](_ > 0, "must be positive integer")
    val validate: Validate[Foo] =
      Validator.checkIfSome[Foo, Int]((foo: Foo) => foo.bazOpt, positiveIntegerValidator, isValidIfNone = false)

    forAll { (int: Int) =>
      Prop.all(
        validate(Foo("", None)).isInvalid,
        if (int > 0)
          validate(Foo("", Some(int))).isValid
        else
          validate(Foo("", Some(int))).errorString == Some("must be positive integer")
      )
    }
  }

  property("Validator.checkIfSome(with error prefix) returns Valid only if nested validator returns Valid") {
    val positiveIntegerValidator = Validator.check[Int](_ > 0, "must be positive integer")
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
          validate(Foo("", Some(int))).errorString == Some("Foo.bazOpt must be positive integer")
      )
    }
  }

  property(
    "Validator.checkIfSome(with error prefix and invalid if none) returns Valid only if nested validator returns Valid"
  ) {
    val positiveIntegerValidator = Validator.check[Int](_ > 0, "must be positive integer")
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
          validate(Foo("", Some(int))).errorString == Some("Foo.bazOpt must be positive integer")
      )
    }
  }

  property(
    "Validator.checkEach returns Valid only if all elements of the sequence passes check"
  ) {
    case class Ints(seq: Seq[Int])
    val negativeIntegerValidator = Validator.check[Int](_ < 0, "must be negative integer")
    val validate: Validate[Ints] =
      Validator.checkEach[Ints, Int]((i: Ints) => i.seq, negativeIntegerValidator)

    Prop.all(
      validate(Ints(Seq.empty)).isValid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, -1))) { (ints: Seq[Int]) =>
        validate(Ints(ints)).isValid
      },
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(0, Integer.MAX_VALUE))) { (ints: Seq[Int]) =>
        validate(Ints(ints)).errorString == Some("must be negative integer")
      }
    )
  }

  property(
    "Validator.checkEach(with error prefix fx) returns Valid only if all elements of the sequence passes check"
  ) {
    case class Ints(seq: Seq[Int])
    val negativeIntegerValidator = Validator.check[Int](_ < 0, "must be negative integer")
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
    val negativeIntegerValidator = Validator.check[Int](_ < 0, "must be negative integer")
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
          result.errorString == Some("each element of 'is' must be negative integer")
        )
      }
    )
  }

  property(
    "Validator.checkEachIfNonEmpty returns Valid only if all elements of the sequence passes check"
  ) {
    case class Ints(seq: Seq[Int])
    val negativeIntegerValidator = Validator.check[Int](_ < 0, "must be negative integer")
    val validate: Validate[Ints] =
      Validator.checkEachIfNonEmpty[Ints, Int]((i: Ints) => i.seq, negativeIntegerValidator)

    Prop.all(
      validate(Ints(Seq.empty)).isInvalid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, -1))) { (ints: Seq[Int]) =>
        validate(Ints(ints)).isValid
      },
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(0, Integer.MAX_VALUE))) { (ints: Seq[Int]) =>
        validate(Ints(ints)).errorString == Some("must be negative integer")
      }
    )
  }

  property(
    "Validator.checkEachIfNonEmpty(with error prefix fx) returns Valid only if all elements of the sequence passes check"
  ) {
    case class Ints(seq: Seq[Int])
    val negativeIntegerValidator = Validator.check[Int](_ < 0, "must be negative integer")
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
    val negativeIntegerValidator = Validator.check[Int](_ < 0, "must be negative integer")
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
          result.errorString == Some("each element of 'is' must be negative integer")
        )
      }
    )
  }

  property(
    "Validator.checkEachIfSome returns Valid only if None or all elements of the sequence passes check"
  ) {
    case class Ints(seqOpt: Option[Seq[Int]])
    val positiveIntegerValidator = Validator.check[Int](_ > 0, "must be positive integer")
    val validate: Validate[Ints] =
      Validator.checkEachIfSome[Ints, Int]((i: Ints) => i.seqOpt, positiveIntegerValidator)

    Prop.all(
      validate(Ints(None)).isValid,
      validate(Ints(Some(Seq.empty))).isValid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, 0))) { (ints: Seq[Int]) =>
        validate(Ints(Some(ints))).errorString == Some("must be positive integer")
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
    val positiveIntegerValidator = Validator.check[Int](_ > 0, "must be positive integer")
    val validate: Validate[Ints] =
      Validator.checkEachIfSome[Ints, Int]((i: Ints) => i.seqOpt, positiveIntegerValidator, isValidIfNone = false)

    Prop.all(
      validate(Ints(None)).isInvalid,
      validate(Ints(Some(Seq.empty))).isValid,
      forAll(Gen.nonEmptyContainerOf[Seq, Int](Gen.chooseNum(Integer.MIN_VALUE, 0))) { (ints: Seq[Int]) =>
        validate(Ints(Some(ints))).errorString == Some("must be positive integer")
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
    val positiveIntegerValidator = Validator.check[Int](_ > 0, "must be positive integer")
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
    val positiveIntegerValidator = Validator.check[Int](_ > 0, "must be positive integer")
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
      .debug

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

}
