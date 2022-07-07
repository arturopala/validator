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

  /** The validator function. */
  final type Validate[-T] = T => Either[List[String], Unit]

  /** Combine provided validator functions. */
  def apply[T](constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      constraints
        .foldLeft[Either[List[String], Unit]](Right(()))((v, fx) => v.combine(fx(entity)))

  def always[T]: Validate[T] = (_: T) => Right(())

  def never[T]: Validate[T] = (_: T) => Left(Nil)

  /** Succeed only if all constraints are valid. */
  def all[T](constraints: Validate[T]*): Validate[T] = apply(constraints: _*)

  /** Succeed only if all constraints are valid, otherwise prepend errorPrefix. */
  def allWithPrefix[T](errorPrefix: String, constraints: Validate[T]*): Validate[T] =
    (entity: T) => all(constraints: _*)(entity).left.map(_.map(e => s"$errorPrefix$e"))

  /** Succeed only if all constraints are valid, otherwise prepend calculated errorPrefix. */
  def allWithComputedPrefix[T](errorPrefix: T => String, constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      all(constraints: _*)(entity).left.map { r =>
        val prefix = errorPrefix(entity)
        r.map(e => s"$prefix$e")
      }

  /** Succeed if any of the constraints is valid. */
  def any[T](constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      if (constraints.isEmpty) Right(())
      else {
        val results = constraints.map(_.apply(entity))
        if (results.exists(_.isRight)) Right(())
        else results.reduce((a, b) => a.combine(b))
      }

  /** Succeed if any of the constraints is valid, otherwise prepend errorPrefix. */
  def anyWithPrefix[T](errorPrefix: String, constraints: Validate[T]*): Validate[T] =
    (entity: T) => any(constraints: _*)(entity).left.map(_.map(e => s"$errorPrefix$e"))

  /** Succeed if any of the constraints is valid, otherwise prepend errorPrefix. */
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
      else Right(())

  /** If the test is false then try the next constraint, otherwise valid. */
  def whenFalse[T](test: T => Boolean, constraintWhenFalse: Validate[T]): Validate[T] =
    (entity: T) =>
      if (test(entity)) Right(())
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
    (entity: (A, B)) => constraintA(entity._1).combine(constraintB(entity._2))

  /** Combine three constraints to make a constraint on a tuple. */
  def product[A, B, C](
    constraintA: Validate[A],
    constraintB: Validate[B],
    constraintC: Validate[C]
  ): Validate[(A, B, C)] =
    (entity: (A, B, C)) => constraintA(entity._1).combine(constraintB(entity._2)).combine(constraintC(entity._3))

  /** Combine four constraints to make a constraint on a tuple. */
  def product[A, B, C, D](
    constraintA: Validate[A],
    constraintB: Validate[B],
    constraintC: Validate[C],
    constraintD: Validate[D]
  ): Validate[(A, B, C, D)] =
    (entity: (A, B, C, D)) =>
      constraintA(entity._1)
        .combine(constraintB(entity._2))
        .combine(constraintC(entity._3))
        .combine(constraintD(entity._4))

  def validate[T](constraints: Check[T]*): Validate[T] =
    (entity: T) =>
      constraints
        .foldLeft[Either[List[String], Unit]](Right(()))((v, fx) => v.combine(fx(entity)))

  /** Validate if the test passes, otherwise fail with error. */
  def check[T](test: T => Boolean, error: String): Check[T] =
    Check(test, error)

  /** Validate if two properties return the same value. */
  def checkEquals[T, A](value1: T => A, value2: T => A, error: String): Check[T] =
    Check((entity: T) => value1(entity) == value2(entity), error)

  /** Validate if two properties return different value. */
  def checkNotEquals[T, A](value1: T => A, value2: T => A, error: String): Check[T] =
    Check((entity: T) => value1(entity) != value2(entity), error)

  /** Validate if the test returns Right, otherwise fail with Left error. */
  def checkFromEither[T](test: T => Either[String, Any]): Check[T] =
    Check.fromEither(test)

  /** Validate if the test returns Some, otherwise fail with error. */
  def checkIsDefined[T](test: T => Option[Any], error: String): Check[T] =
    Check.fromOption(test, error)

  /** Validate if the test returns None, otherwise fail with error. */
  def checkIsEmpty[T](test: T => Option[Any], error: String): Check[T] =
    Check((entity: T) => test(entity).isEmpty, error)

  /** Apply constraint to the extracted property. */
  def checkProperty[T, E](element: T => E, constraint: Validate[E]): Validate[T] =
    (entity: T) => constraint(element(entity))

  /** Apply check to the extracted nested attribute. */
  def checkAttribute[T, E](attribute: T => E, check: Check[E]): Check[T] =
    Check(attribute, check)

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
          if (isValidIfNone) Right(()) else Left(List("Expected Some value but got None"))
        )

  /** Apply constraint to each element of the extracted sequence. */
  def checkEach[T, E](elements: T => Seq[E], constraint: Validate[E]): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.map(constraint)
          .reduce(_.combine(_).left.map(_.distinct))
      else Right(())
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
          .reduce(_.combine(_).left.map(_.distinct))
      else Right(())
    }

  /** Apply constraint to each element of the extracted sequence if non empty. */
  def checkEachIfNonEmpty[T, E](elements: T => Seq[E], constraint: Validate[E]): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.map(constraint)
          .reduce(_.combine(_))
      else Left(List("Sequence must not be empty"))
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
          .reduce(_.combine(_).left.map(_.distinct))
      else Left(List("Sequence must not be empty"))
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
          if (isValidIfNone) Right(()) else Left(List("Expected Some sequence but got None"))
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
          if (isValidIfNone) Right(()) else Left(List("Expected Some sequence but got None"))
        )

  /** Check if all extracted optional properties are defined. */
  def checkIfAllDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.forall(f => f(entity).isDefined)) Right(())
      else Left(List(s"All of $expectations must be defined"))

  /** Check if all extracted optional properties are empty. */
  def checkIfAllEmpty[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.forall(f => f(entity).isEmpty)) Right(())
      else Left(List(s"All of $expectations must be empty"))

  /** Check if the extracted optional properties are either all defined or all empty. */
  def checkIfAllOrNoneDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) => {
      val checks = extractors.map(f => f(entity))
      if (checks.forall(_.isDefined) || checks.forall(_.isEmpty)) Right(())
      else Left(List(s"The $expectations must be either all defined or all empty"))
    }

  /** Check if all tests passes */
  def checkIfAllTrue[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.forall(f => f(entity))) Right(())
      else Left(List(s"All of $expectations must be true"))

  /** Check if all tests fails */
  def checkIfAllFalse[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.forall(f => !f(entity))) Right(())
      else Left(List(s"All of $expectations must be false"))

  /** Check if at least one extracted property is defined. */
  def checkIfAtLeastOneIsDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.exists(f => f(entity).isDefined)) Right(())
      else Left(List(s"One of $expectations must be defined"))

  /** Check if at least one test passes. */
  def checkIfAtLeastOneIsTrue[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.exists(f => f(entity))) Right(())
      else Left(List(s"One of $expectations must be true"))

  /** Check if at most one extracted property is defined. */
  def checkIfAtMostOneIsDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.count(f => f(entity).isDefined) <= 1) Right(())
      else Left(List(s"At most one of $expectations can be defined"))

  /** Check if at most one test passes. */
  def checkIfAtMostOneIsTrue[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.count(f => f(entity)) <= 1) Right(())
      else Left(List(s"At most one of $expectations can be true"))

  /** Check if one and only one extracted property is defined. */
  def checkIfOnlyOneIsDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.count(f => f(entity).isDefined) == 1) Right(())
      else Left(List(s"Only one of $expectations can be defined"))

  /** Check if one and only one test passes. */
  def checkIfOnlyOneIsTrue[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.count(f => f(entity)) == 1) Right(())
      else Left(List(s"Only one of $expectations can be true"))

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
          List(
            s"One of the alternative sets $expectations must be defined"
          )
        )
      else if (definedSetCount > 1)
        Left(
          List(s"Only one of the alternative sets $expectations can be defined")
        )
      else Right(())
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
          List(
            s"One of the alternative sets $expectations must be all true"
          )
        )
      else if (definedSetCount > 1)
        Left(
          List(s"Only one of the alternative sets $expectations can all be true")
        )
      else Right(())
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

  final implicit class ValidateOps[T](val thisValidate: T => Either[List[String], Unit]) {
    def asCheck: Check[T] = Check.fromValidate(thisValidate)

    def &(otherValidate: Validate[T]): Validate[T] = Validator.all(thisValidate, otherValidate)
    def |(otherValidate: Validate[T]): Validate[T] = Validator.any(thisValidate, otherValidate)
    def *[U](otherValidate: Validate[U]): Validate[(T, U)] = Validator.product(thisValidate, otherValidate)

    def ?(otherValidate: Validate[T]): Validate[T] = Validator.whenValid[T](thisValidate, otherValidate)
    def ?!(otherValidate: Validate[T]): Validate[T] = Validator.whenInvalid[T](thisValidate, otherValidate)

    def andWhenValid(otherValidate: Validate[T]): Validate[T] = Validator.whenValid[T](thisValidate, otherValidate)
    def andwhenInvalid(otherValidate: Validate[T]): Validate[T] = Validator.whenInvalid[T](thisValidate, otherValidate)

    def @:(errorPrefix: String): Validate[T] = withErrorPrefix(errorPrefix)
    def @@(errorPrefix: String): Validate[T] = withErrorPrefix(errorPrefix)

    def withErrorPrefix(errorPrefix: String): Validate[T] =
      (entity: T) => thisValidate(entity).left.map(_.map(e => s"$errorPrefix$e"))

    def withErrorPrefixComputed(errorPrefix: T => String): Validate[T] =
      (entity: T) =>
        thisValidate(entity).left.map { r =>
          val prefix = errorPrefix(entity)
          r.map(e => s"$prefix$e")
        }

    def debug: Validate[T] =
      (entity: T) => {
        print(entity)
        print(" => ")
        thisValidate(entity).debug
      }

    def debugWith(show: T => String): Validate[T] =
      (entity: T) => {
        print(show(entity))
        print(" => ")
        thisValidate(entity).debug
      }
  }

  final implicit class ValidationResultOps(val validated: Either[List[String], Unit]) {
    final def combine(otherValidated: Either[List[String], Unit]): Either[List[String], Unit] =
      validated match {
        case Right(_) => otherValidated
        case Left(errors) =>
          otherValidated match {
            case Right(_)          => Left(errors)
            case Left(otherErrors) => Left(errors ::: otherErrors)
          }
      }

    final def isValid: Boolean = validated.isRight
    final def isInvalid: Boolean = validated.isLeft

    final def errors: Option[Seq[String]] = validated match {
      case Left(errors) => Some(errors.distinct)
      case Right(_)     => None
    }

    final def errorsCount: Int = errors.map(_.length).getOrElse(0)

    final def errorString: Option[String] = errors.map(_.mkString(","))
    final def errorString(sep: String): Option[String] = errors.map(_.mkString(sep))
    final def errorString(start: String, sep: String, end: String): Option[String] =
      errors.map(_.mkString(start, sep, end))

    final def debug: Either[List[String], Unit] = {
      println(validated.fold(e => s"Left(${e.mkString(", ")})", _ => "Valid"))
      validated
    }
  }

  trait Check[-A] extends Validate[A] {
    def check(a: A): Either[String, Unit]

    def apply(a: A): Either[List[String], Unit] =
      check(a) match {
        case Right(_)    => Right(())
        case Left(value) => Left(List(value))
      }
  }

  object Check {
    def apply[A](condition: A => Boolean, errorMessage: String): Check[A] =
      new Check[A] {
        def check(a: A): Either[String, Unit] =
          Either.cond(condition(a), (), errorMessage)
      }

    def apply[A, B](extractProperty: A => B, propertyCheck: Check[B]): Check[A] =
      new Check[A] {
        def check(a: A): Either[String, Unit] =
          propertyCheck.check(extractProperty(a))
      }

    def fromValidate[A](constraint: Validate[A]): Check[A] =
      new Check[A] {
        def check(a: A): Either[String, Unit] =
          constraint(a).left.map(_.mkString(", "))

        override def apply(a: A): Either[List[String], Unit] =
          constraint(a)
      }

    def fromEither[A](test: A => Either[String, Any]): Check[A] =
      new Check[A] {
        def check(a: A): Either[String, Unit] =
          test(a).map(_ => ())
      }

    def fromOption[A](test: A => Option[Any], errorMessage: String): Check[A] =
      new Check[A] {
        def check(a: A): Either[String, Unit] =
          test(a).map(_ => ()).toRight(errorMessage)
      }

    implicit class CheckOps[A](val thisCheck: Check[A]) {

      final def &&(otherCheck: Check[A]): Check[A] =
        thisCheck.and(otherCheck)

      /** concatenation */
      final def and(otherCheck: Check[A]): Check[A] =
        new Check[A] {
          def check(a: A): Either[String, Unit] =
            thisCheck
              .check(a)
              .fold(
                e1 =>
                  otherCheck
                    .check(a)
                    .fold(e2 => Left(e1 + " and " + e2), _ => Left(e1)),
                _ =>
                  otherCheck
                    .check(a)
                    .fold(e2 => Left(e2), _ => Right(()))
              )
        }

      /** alternative */
      final def ||(otherCheck: Check[A]): Check[A] =
        thisCheck.or(otherCheck)

      final def or(otherCheck: Check[A]): Check[A] =
        new Check[A] {
          def check(a: A): Either[String, Unit] =
            thisCheck
              .check(a)
              .fold(
                e1 =>
                  otherCheck
                    .check(a)
                    .fold(e2 => Left(e1 + " or " + e2), _ => Right(())),
                _ => Right(())
              )
        }

      /** run next check only if first is true */
      final def whenTrueThen(otherCheck: Check[A]): Check[A] =
        new Check[A] {
          def check(a: A): Either[String, Unit] =
            thisCheck
              .check(a)
              .fold(
                _ => Right(()),
                _ => otherCheck.check(a)
              )
        }

      /** run next check only if first is false */
      final def whenFalseThen(otherCheck: Check[A]): Check[A] =
        new Check[A] {
          def check(a: A): Either[String, Unit] =
            thisCheck
              .check(a)
              .fold(
                _ => otherCheck.check(a),
                _ => Right(())
              )
        }

      // run next check depending on the first one result
      final def thenEither(checkIfTrue: Check[A], checkIfFalse: Check[A]): Check[A] =
        new Check[A] {
          def check(a: A): Either[String, Unit] =
            thisCheck
              .check(a)
              .fold(
                _ => checkIfFalse.check(a),
                _ => checkIfTrue.check(a)
              )
        }
    }
  }
}
