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

/** Simpler validator abstraction using Cats Validated https://typelevel.org/cats/datatypes/validated.html. */
object Validator {

  sealed trait Error {

    /** Returns first error message. */
    def headMessage: String

    /** Returns all errors' messages in a depth-first flattened sequence. */
    def messages: Seq[String]

    /** Combines all errors' messages into a single sentence. */
    def summary: String

    /** Maps error messages with the provided function. */
    def map(f: String => String): Error

    /** Combines this error with another one using logical conjunction. */
    def and(other: Error): Error

    /** Combines this error with another one using logical disjunction. */
    def or(other: Error): Error
  }
  object Error {

    /** Wraps a message as a single error. */
    def apply(message: String): Error = Single(message)
  }

  /** Validation result type. */
  type Result = Either[Error, Unit]

  /** The validator function type. */
  type Validate[-T] = T => Result

  /** Invalid result helpers. */
  object Invalid {

    /** Creates representation of a failed validation result with single error message. */
    def apply(errorMessage: String): Result =
      Left(Single(errorMessage))

    /** Creates representation of a failed validation result with multiple error messages. */
    def apply(firstErrorMessage: String, nextErrorMessages: String*): Result =
      Left(And(Single(firstErrorMessage) +: nextErrorMessages.map(Single.apply)))
  }

  /** Successsful validation result alias. */
  val Valid = Right(())

  /** Runs all provided checks. */
  def apply[T](constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      constraints
        .foldLeft[Result](Valid)((v, fx) => v.and(fx(entity)))

  /** Runs all provided checks. Provided as a named alias to the apply method. */
  def validate[T](constraints: Validate[T]*): Validate[T] =
    apply(constraints: _*)

  /** Validator that always succeeds. */
  def always[T]: Validate[T] = (_: T) => Valid

  /** Validator that always fails. */
  def never[T]: Validate[T] = (_: T) => Left(Error("this validation never succeeds"))

  /** Conjuction. Succeeds only if all constraints are valid. */
  def all[T](constraints: Validate[T]*): Validate[T] =
    apply(constraints: _*)

  /** Conjuction. Succeeds only if all constraints are valid, otherwise prepend errorPrefix. */
  def allWithPrefix[T](errorPrefix: String, constraints: Validate[T]*): Validate[T] =
    (entity: T) => all(constraints: _*)(entity).left.map(_.map(e => s"$errorPrefix$e"))

  /** Conjuction. Succeeds only if all constraints are valid, otherwise prepend calculated errorPrefix. */
  def allWithComputedPrefix[T](errorPrefix: T => String, constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      all(constraints: _*)(entity).left.map { r =>
        val prefix = errorPrefix(entity)
        r.map(e => s"$prefix$e")
      }

  /** Disjunction. Succeeds if any of the constraints is valid. */
  def any[T](constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      if (constraints.isEmpty) Valid
      else {
        val results = constraints.map(_.apply(entity))
        if (results.exists(_.isRight)) Valid
        else results.reduce((a, b) => a or b)
      }

  /** Disjunction. Succeeds if any of the constraints is valid, otherwise prepend errorPrefix. */
  def anyWithPrefix[T](errorPrefix: String, constraints: Validate[T]*): Validate[T] =
    (entity: T) => any(constraints: _*)(entity).left.map(_.map(e => s"$errorPrefix$e"))

  /** Disjunction. Succeeds if any of the constraints is valid, otherwise prepend errorPrefix. */
  def anyWithComputedPrefix[T](errorPrefix: T => String, constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      any(constraints: _*)(entity).left.map { r =>
        val prefix = errorPrefix(entity)
        r.map(e => s"$prefix$e")
      }

  /** Depending on the test result follow continue with either first or second constraint. */
  def conditionally[T](
    test: T => Boolean,
    constraintWhenTrue: Validate[T],
    constraintWhenFalse: Validate[T]
  ): Validate[T] =
    (entity: T) =>
      if (test(entity)) constraintWhenTrue(entity)
      else constraintWhenFalse(entity)

  /** If the test is true then check the next constraint, otherwise valid. */
  def whenTrue[T](test: T => Boolean, constraintWhenTrue: Validate[T]): Validate[T] =
    (entity: T) =>
      if (test(entity)) constraintWhenTrue(entity)
      else Valid

  /** If the test is false then try the next constraint, otherwise valid. */
  def whenFalse[T](test: T => Boolean, constraintWhenFalse: Validate[T]): Validate[T] =
    (entity: T) =>
      if (test(entity)) Valid
      else constraintWhenFalse(entity)

  /** Depending on the guard constraint result continue with either first or second constraint. */
  def when[T](
    guardConstraint: Validate[T],
    constraintWhenValid: Validate[T],
    constraintWhenInvalid: Validate[T]
  ): Validate[T] =
    (entity: T) =>
      guardConstraint(entity) match {
        case Right(_) => constraintWhenValid(entity)
        case Left(_)  => constraintWhenInvalid(entity)
      }

  /** If the guard constraint is valid then check next constraint. */
  def whenValid[T](guardConstraint: Validate[T], constraintWhenValid: Validate[T]): Validate[T] =
    (entity: T) =>
      guardConstraint(entity) match {
        case Right(_) => constraintWhenValid(entity)
        case invalid  => invalid
      }

  /** If the guard constraint is invalid then try next constraint. */
  def whenInvalid[T](guardConstraint: Validate[T], constraintWhenInvalid: Validate[T]): Validate[T] =
    (entity: T) =>
      guardConstraint(entity) match {
        case Left(_) => constraintWhenInvalid(entity)
        case valid   => valid
      }

  /** Combine two constraints to make a constraint on a pair. */
  def product[A, B](constraintA: Validate[A], constraintB: Validate[B]): Validate[(A, B)] =
    (entity: (A, B)) => constraintA(entity._1).and(constraintB(entity._2))

  /** Combine three constraints to make a constraint on a triplet. */
  def product[A, B, C](
    constraintA: Validate[A],
    constraintB: Validate[B],
    constraintC: Validate[C]
  ): Validate[(A, B, C)] =
    (entity: (A, B, C)) => constraintA(entity._1).and(constraintB(entity._2)).and(constraintC(entity._3))

  /** Combine four constraints to make a constraint on a tuple. */
  def product[A, B, C, D](
    constraintA: Validate[A],
    constraintB: Validate[B],
    constraintC: Validate[C],
    constraintD: Validate[D]
  ): Validate[(A, B, C, D)] =
    (entity: (A, B, C, D)) =>
      constraintA(entity._1)
        .and(constraintB(entity._2))
        .and(constraintC(entity._3))
        .and(constraintD(entity._4))

  /** Validate if the test passes, otherwise fail with error. */
  def checkIsTrue[T](test: T => Boolean, error: String): Validate[T] =
    a => Either.cond(test(a), (), Error(error))

  /** Validate if the test fails, otherwise fail with error. */
  def checkIsFalse[T](test: T => Boolean, error: String): Validate[T] =
    a => Either.cond(!test(a), (), Error(error))

  /** Validate using the provided implicit constraint applied to the extracted property. */
  def checkWithImplicitly[T, E](element: T => E)(implicit constraint: Validate[E]): Validate[T] =
    (entity: T) => constraint(element(entity))

  /** Validate with the provided constraint applied to the extracted property. */
  def checkWith[T, E](element: T => E, constraint: Validate[E]): Validate[T] =
    (entity: T) => constraint(element(entity))

  /** Validate if two properties return the same value. */
  def checkEquals[T, A](value1: T => A, value2: T => A, error: String): Validate[T] =
    checkIsTrue((entity: T) => value1(entity) == value2(entity), error)

  /** Validate if two properties return different value. */
  def checkNotEquals[T, A](value1: T => A, value2: T => A, error: String): Validate[T] =
    checkIsTrue((entity: T) => value1(entity) != value2(entity), error)

  /** Validate if the test returns Right, otherwise fail with Left error. */
  def checkFromEither[T](test: T => Either[String, Any]): Validate[T] =
    (entity: T) => test(entity).map(_ => ()).left.map(Error.apply(_))

  /** Validate if the test returns Some, otherwise fail with error. */
  def checkIsDefined[T](test: T => Option[Any], error: String): Validate[T] =
    (entity: T) => test(entity).map(_ => ()).toRight(Error(error))

  /** Validate if the test returns None, otherwise fail with error. */
  def checkIsEmpty[T](test: T => Option[Any], error: String): Validate[T] =
    checkIsTrue((entity: T) => test(entity).isEmpty, error)

  /** Apply constraint to the extracted property if defined, otherwise follow isValidIfNone flag. */
  def checkIfSome[T, E](
    element: T => Option[E],
    constraint: Validate[E],
    isValidIfNone: Boolean = true
  ): Validate[T] =
    (entity: T) =>
      element(entity)
        .map(constraint)
        .getOrElse(
          if (isValidIfNone) Valid else Left(Error("Expected Some value but got None"))
        )

  /** Apply constraint to each element of the extracted sequence. */
  def checkEach[T, E](elements: T => Seq[E], constraint: Validate[E]): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.map(constraint)
          .reduce(_.and(_))
      else Valid
    }

  /** Apply constraint to each element of the extracted sequence.
    *  If invalid then compute and add prefix to the errors.
    */
  def checkEachWithErrorPrefix[T, E](
    elements: T => Seq[E],
    constraint: Validate[E],
    errorPrefix: Int => String
  ): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.zipWithIndex
          .map { case (v, i) => constraint(v).left.map(_.map(e => s"${errorPrefix(i)}$e")) }
          .reduce(_.and(_))
      else Valid
    }

  /** Apply constraint to each element of the extracted sequence if non empty. */
  def checkEachIfNonEmpty[T, E](elements: T => Seq[E], constraint: Validate[E]): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.map(constraint)
          .reduce(_.and(_))
      else Left(Error("Sequence must not be empty"))
    }

  /** Apply constraint to each element of the extracted sequence if non empty.
    *  If invalid then compute and add prefix to the errors.
    */
  def checkEachIfNonEmptyWithErrorPrefix[T, E](
    elements: T => Seq[E],
    constraint: Validate[E],
    errorPrefix: Int => String
  ): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.zipWithIndex
          .map { case (v, i) => constraint(v).left.map(_.map(e => s"${errorPrefix(i)}$e")) }
          .reduce(_.and(_))
      else Left(Error("Sequence must not be empty"))
    }

  /** Apply constraint to each element of the extracted sequence if defined. */
  def checkEachIfSome[T, E](
    extract: T => Option[Seq[E]],
    constraint: Validate[E],
    isValidIfNone: Boolean = true
  ): Validate[T] =
    (entity: T) =>
      extract(entity)
        .map(checkEach(identity, constraint))
        .getOrElse(
          if (isValidIfNone) Valid else Left(Error("Expected Some sequence but got None"))
        )

  /** Apply constraint to each element of the extracted sequence if non empty.
    *  If invalid then compute and add prefix to the errors.
    */
  def checkEachIfSomeWithErrorPrefix[T, E](
    extract: T => Option[Seq[E]],
    constraint: Validate[E],
    errorPrefix: Int => String,
    isValidIfNone: Boolean
  ): Validate[T] =
    (entity: T) =>
      extract(entity)
        .map(checkEachWithErrorPrefix(identity, constraint, errorPrefix))
        .getOrElse(
          if (isValidIfNone) Valid else Left(Error("Expected Some sequence but got None"))
        )

  /** Check if all extracted optional properties are defined. */
  def checkIfAllDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.forall(f => f(entity).isDefined)) Valid
      else Left(Error(s"All of $expectations must be defined"))

  /** Check if all extracted optional properties are empty. */
  def checkIfAllEmpty[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.forall(f => f(entity).isEmpty)) Valid
      else Left(Error(s"All of $expectations must be empty"))

  /** Check if the extracted optional properties are either all defined or all empty. */
  def checkIfAllOrNoneDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) => {
      val checks = extractors.map(f => f(entity))
      if (checks.forall(_.isDefined) || checks.forall(_.isEmpty)) Valid
      else Left(Error(s"The $expectations must be either all defined or all empty"))
    }

  /** Check if all tests passes */
  def checkIfAllTrue[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.forall(f => f(entity))) Valid
      else Left(Error(s"All of $expectations must be true"))

  /** Check if all tests fails */
  def checkIfAllFalse[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.forall(f => !f(entity))) Valid
      else Left(Error(s"All of $expectations must be false"))

  /** Check if at least one extracted property is defined. */
  def checkIfAtLeastOneIsDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.exists(f => f(entity).isDefined)) Valid
      else Left(Error(s"One of $expectations must be defined"))

  /** Check if at least one test passes. */
  def checkIfAtLeastOneIsTrue[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.exists(f => f(entity))) Valid
      else Left(Error(s"One of $expectations must be true"))

  /** Check if at most one extracted property is defined. */
  def checkIfAtMostOneIsDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.count(f => f(entity).isDefined) <= 1) Valid
      else Left(Error(s"At most one of $expectations can be defined"))

  /** Check if at most one test passes. */
  def checkIfAtMostOneIsTrue[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.count(f => f(entity)) <= 1) Valid
      else Left(Error(s"At most one of $expectations can be true"))

  /** Check if one and only one extracted property is defined. */
  def checkIfOnlyOneIsDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.count(f => f(entity).isDefined) == 1) Valid
      else Left(Error(s"Only one of $expectations can be defined"))

  /** Check if one and only one test passes. */
  def checkIfOnlyOneIsTrue[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.count(f => f(entity)) == 1) Valid
      else Left(Error(s"Only one of $expectations can be true"))

  /** Check if one and only one set of properties is fully defined. */
  def checkIfOnlyOneSetIsDefined[T](
    extractors: Seq[Set[T => Option[Any]]],
    expectations: String
  ): Validate[T] =
    (entity: T) => {
      val definedSetCount =
        extractors.map(_.map(f => f(entity).isDefined).reduce(_ && _)).count(_ == true)
      if (definedSetCount == 0)
        Left(
          Error(
            s"One of the alternative sets $expectations must be defined"
          )
        )
      else if (definedSetCount > 1)
        Left(
          Error(s"Only one of the alternative sets $expectations can be defined")
        )
      else Valid
    }

  /** Check if one and only one set of tests passes. */
  def checkIfOnlyOneSetIsTrue[T](
    tests: Seq[Set[T => Boolean]],
    expectations: String
  ): Validate[T] =
    (entity: T) => {
      val definedSetCount =
        tests.map(_.map(f => f(entity)).reduce(_ && _)).count(_ == true)
      if (definedSetCount == 0)
        Left(
          Error(
            s"One of the alternative sets $expectations must be all true"
          )
        )
      else if (definedSetCount > 1)
        Left(
          Error(s"Only one of the alternative sets $expectations can all be true")
        )
      else Valid
    }

  final implicit class StringMatchers(val value: String) extends AnyVal {
    def lengthMinMaxInclusive(min: Int, max: Int): Boolean =
      value != null && value.length >= min && value.length <= max
    def lengthMin(min: Int): Boolean = value != null && value.length >= min
    def lengthMax(max: Int): Boolean = value != null && value.length <= max
    def isRight(test: String => Either[String, _]): Boolean = test(value).isRight
    def isLeft(test: String => Either[String, _]): Boolean = test(value).isLeft
    def isTrue(test: String => Boolean): Boolean = test(value)
    def isFalse(test: String => Boolean): Boolean = !test(value)
    def isOneOf(seq: Seq[String]): Boolean = seq.contains(value)
    def isOneOf(set: Set[String]): Boolean = set.contains(value)
  }

  final implicit class OptionalStringMatchers(val value: Option[String]) extends AnyVal {
    def lengthMinMaxInclusive(min: Int, max: Int): Boolean =
      value.forall(v => v != null && v.length >= min && v.length <= max)
    def lengthMin(min: Int): Boolean =
      value.forall(v => v != null && v.length >= min)
    def lengthMax(max: Int): Boolean =
      value.forall(v => v != null && v.length <= max)
    def isRight(test: String => Either[String, _]): Boolean = value.forall(_.isRight(test))
    def isLeft(test: String => Either[String, _]): Boolean = value.forall(_.isLeft(test))
    def isTrue(test: String => Boolean): Boolean = value.forall(_.isTrue(test))
    def isFalse(test: String => Boolean): Boolean = value.forall(_.isFalse(test))
    def matches(regex: String): Boolean = value.forall(_.matches(regex))
    def isOneOf(seq: Seq[String]): Boolean = value.forall(seq.contains)
    def isOneOf(set: Set[String]): Boolean = value.forall(set.apply)
  }

  final implicit class IntMatchers(val value: Int) extends AnyVal {
    def inRange(min: Int, max: Int, multipleOf: Option[Int] = None): Boolean =
      value <= max && value >= min && multipleOf.forall(a => (value % a).abs < 0.0001)
    def lteq(max: Int, multipleOf: Option[Int] = None): Boolean =
      value <= max && multipleOf.forall(a => (value % a).abs < 0.0001)
    def gteq(min: Int, multipleOf: Option[Int] = None): Boolean =
      value >= min && multipleOf.forall(a => (value % a).abs < 0.0001)
  }

  final implicit class OptionalIntMatchers(val value: Option[Int]) extends AnyVal {
    def inRange(min: Int, max: Int, multipleOf: Option[Int] = None): Boolean =
      value.forall(v => v <= max && v >= min && multipleOf.forall(a => (v % a).abs < 0.0001))
    def lteq(max: Int, multipleOf: Option[Int] = None): Boolean =
      value.forall(v => v <= max && multipleOf.forall(a => (v % a).abs < 0.0001))
    def gteq(min: Int, multipleOf: Option[Int] = None): Boolean =
      value.forall(v => v >= min && multipleOf.forall(a => (v % a).abs < 0.0001))
  }

  final implicit class BigDecimalMatchers(val value: BigDecimal) extends AnyVal {
    def inRange(min: BigDecimal, max: BigDecimal, multipleOf: Option[BigDecimal] = None): Boolean =
      value != null && value <= max && value >= min && multipleOf.forall(a => (value % a).abs < 0.0001)
    def lteq(max: BigDecimal, multipleOf: Option[BigDecimal] = None): Boolean =
      value != null && value <= max && multipleOf.forall(a => (value % a).abs < 0.0001)
    def gteq(min: BigDecimal, multipleOf: Option[BigDecimal] = None): Boolean =
      value != null && value >= min && multipleOf.forall(a => (value % a).abs < 0.0001)
  }

  final implicit class OptionalBigDecimalMatchers(val value: Option[BigDecimal]) extends AnyVal {
    def inRange(min: BigDecimal, max: BigDecimal, multipleOf: Option[BigDecimal] = None): Boolean =
      value.forall(v => v != null && v <= max && v >= min && multipleOf.forall(a => (v % a).abs < 0.0001))
    def lteq(max: BigDecimal, multipleOf: Option[BigDecimal] = None): Boolean =
      value.forall(v => v != null && v <= max && multipleOf.forall(a => (v % a).abs < 0.0001))
    def gteq(min: BigDecimal, multipleOf: Option[BigDecimal] = None): Boolean =
      value.forall(v => v != null && v >= min && multipleOf.forall(a => (v % a).abs < 0.0001))
  }

  final implicit class BooleanOps(val value: Boolean) extends AnyVal {
    def map[T](f: Unit => T): Option[T] = if (value) Some(f(())) else None
    def orElse(b: => Boolean): Boolean = value || b
    def asOption: Option[Unit] = if (value) Some(()) else None
    def isDefined: Boolean = value
  }

  final implicit class ValidateOps[T](val thisValidate: T => Result) {

    /** Conjuction. Compose this check with another check and expect them both to pass. */
    def and(otherValidate: Validate[T]): Validate[T] =
      (entity: T) =>
        thisValidate(entity)
          .fold(
            error1 =>
              otherValidate(entity) match {
                case Left(error2) => Left(error1 and error2)
                case Right(_)     => Left(error1)
              },
            _ => otherValidate(entity)
          )

    /** Disjunction. Compose this check with another check and expect at least one of them to pass. */
    def or(otherValidate: Validate[T]): Validate[T] =
      (entity: T) =>
        thisValidate(entity).left
          .flatMap(error1 =>
            otherValidate(entity).left
              .map(error2 => error1 or error2)
          )

    /** Conjuction. Compose this check with another check and expect them both to pass. */
    def &(otherValidate: Validate[T]): Validate[T] =
      thisValidate.and(otherValidate)

    /** Disjunction. Compose this check with another check and expect at least one of them to pass. */
    def |(otherValidate: Validate[T]): Validate[T] =
      thisValidate.or(otherValidate)

    /** Product. Compose this check with another check to construct tuple of checks. */
    def *[U](otherValidate: Validate[U]): Validate[(T, U)] =
      Validator.product(thisValidate, otherValidate)

    /** Chain two constraints so that if first is true than the second is evaluated. */
    def ?(otherValidate: Validate[T]): Validate[T] =
      Validator.whenValid[T](thisValidate, otherValidate)

    /** Chain two constraints so that if first is false than the second is evaluated. */
    def ?!(otherValidate: Validate[T]): Validate[T] =
      Validator.whenInvalid[T](thisValidate, otherValidate)

    /** Chain two constraints so that if first is true than the second is evaluated. */
    def andWhenValid(otherValidate: Validate[T]): Validate[T] =
      Validator.whenValid[T](thisValidate, otherValidate)

    /** Chain two constraints so that if first is false than the second is evaluated. */
    def andwhenInvalid(otherValidate: Validate[T]): Validate[T] =
      Validator.whenInvalid[T](thisValidate, otherValidate)

    /** Adds prefix to the error messages. */
    def @:(errorPrefix: String): Validate[T] = withErrorPrefix(errorPrefix)

    /** Adds prefix to the error messages. */
    def @@(errorPrefix: String): Validate[T] = withErrorPrefix(errorPrefix)

    /** Adds prefix to the error messages. */
    def withErrorPrefix(errorPrefix: String): Validate[T] =
      (entity: T) => thisValidate(entity).left.map(_.map(e => s"$errorPrefix$e"))

    /** Adds computed prefix to the error messages. */
    def withErrorPrefixComputed(errorPrefix: T => String): Validate[T] =
      (entity: T) =>
        thisValidate(entity).left.map { r =>
          val prefix = errorPrefix(entity)
          r.map(e => s"$prefix$e")
        }

    /** Prints entity and the result for debug purposes. */
    def debug: Validate[T] =
      (entity: T) => {
        print(entity)
        print(" => ")
        thisValidate(entity).debug
      }

    /** Prints entity using supplied function and the result for debug purposes. */
    def debugWith(show: T => String): Validate[T] =
      (entity: T) => {
        print(show(entity))
        print(" => ")
        thisValidate(entity).debug
      }
  }

  final implicit class ValidationResultOps(val result: Result) {

    /** Conjuction of errors. */
    def and(otherResult: Result): Result = result match {
      case Left(error1) =>
        otherResult match {
          case Left(error2) => Left(error1 and error2)
          case Right(())    => Left(error1)
        }
      case Right(()) =>
        otherResult match {
          case Left(error2) => Left(error2)
          case Right(())    => Right(())
        }
    }

    /** Disjunction of errors. */
    def or(otherResult: Result): Result = result match {
      case Left(error1) =>
        otherResult match {
          case Left(error2) => Left(error1 or error2)
          case Right(())    => Right(())
        }
      case Right(()) => Right(())
    }

    def isValid: Boolean = result.isRight
    def isInvalid: Boolean = result.isLeft

    def errorsCount: Int = errorsOption.map(_.length).getOrElse(0)

    /** If result is invalid then returns some sequence of all error messages. */
    def errorsOption: Option[Seq[String]] = result match {
      case Left(error) => Some(error.messages)
      case Right(_)    => None
    }

    /** If result is invalid then returns some errors summary message. */
    def errorsSummaryOption: Option[String] = result match {
      case Left(error) => Some(error.summary)
      case Right(_)    => None
    }

    /** If result is invalid then returns first error message. */
    def headErrorOption: Option[String] = result match {
      case Left(error) => Some(error.headMessage)
      case Right(_)    => None
    }

    /** Prints result for debugging purposes. */
    def debug: Result = {
      println(result.fold(e => s"Invalid(${e.summary})", _ => "Valid"))
      result
    }
  }

  /** Single error representation. */
  final case class Single private[Validator] (message: String) extends Error {
    override def headMessage: String = message
    override def messages: Seq[String] = Seq(message)
    override def summary: String = message
    override def map(f: String => String): Error = Single(f(message))
    override def and(other: Error): Error = other match {
      case Single(otherMessage) if message == otherMessage => this
      case And(otherErrors)                                => And((this +: otherErrors).distinct)
      case _                                               => And(Seq(this, other))
    }
    override def or(other: Error): Error = other match {
      case Single(otherMessage) if message == otherMessage => this
      case Or(otherErrors)                                 => Or((this +: otherErrors).distinct)
      case _                                               => Or(Seq(this, other))
    }
  }

  /** Sequence of errors resulting from the logical conjunction of constraints. */
  final case class And private[Validator] (errors: Seq[Error]) extends Error {
    override def headMessage: String = errors.head.headMessage
    override def messages: Seq[String] = errors.flatMap(_.messages)

    override def summary: String = errors
      .map {
        case Single(m) => m
        case e: And    => e.summary
        case e: Or     => s"(${e.summary})"
      }
      .mkString(" and ")

    override def map(f: String => String): Error = And(errors.map(_.map(f)).distinct)

    override def and(other: Error): Error = other match {
      case And(otherErrors) if otherErrors.sameElements(errors) => this
      case And(otherErrors)                                     => And((errors ++ otherErrors).distinct)
      case Single(_)                                            => And((errors :+ other).distinct)
      case _                                                    => And(Seq(this, other))
    }

    override def or(other: Error): Error = other match {
      case Or(otherErrors) => Or((this +: otherErrors).distinct)
      case _               => Or(Seq(this, other))
    }
  }

  /** Sequence of errors resulting from the logical disjunction of constraints. */
  final case class Or private[Validator] (errors: Seq[Error]) extends Error {
    override def headMessage: String = errors.head.headMessage
    override def messages: Seq[String] = errors.flatMap(_.messages)

    override def summary: String = errors
      .map {
        case Single(m) => m
        case e: And    => s"(${e.summary})"
        case e: Or     => e.summary
      }
      .mkString(" or ")

    override def map(f: String => String): Error = Or(errors.map(_.map(f)).distinct)

    override def and(other: Error): Error = other match {
      case And(otherErrors) => And((this +: otherErrors).distinct)
      case _                => And(Seq(this, other))
    }

    override def or(other: Error): Error = other match {
      case Or(otherErrors) if otherErrors.sameElements(errors) => this
      case Or(otherErrors)                                     => Or((errors ++ otherErrors).distinct)
      case Single(_)                                           => Or((errors :+ other).distinct)
      case _                                                   => Or(Seq(this, other))
    }
  }

}
