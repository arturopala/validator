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

import cats.Semigroup
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.Eq

/** Simpler validator abstraction using Cats Validated https://typelevel.org/cats/datatypes/validated.html. */
object Validator {

  import Implicits._

  /** The validator function. */
  final type Validate[T] = T => Validated[List[String], Unit]

  /** Combine provided validator functions. */
  def apply[T](constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      constraints
        .foldLeft[Validated[List[String], Unit]](Valid(()))((v, fx) => v.combine(fx(entity)))

  def always[T]: Validate[T] = (_: T) => Valid(())

  def never[T]: Validate[T] = (_: T) => Invalid(Nil)

  /** Succeed only if all constraints are valid. */
  def all[T](constraints: Validate[T]*): Validate[T] = apply(constraints: _*)

  /** Succeed only if all constraints are valid, otherwise prepend errorPrefix. */
  def allWithPrefix[T](errorPrefix: String, constraints: Validate[T]*): Validate[T] =
    (entity: T) => all(constraints: _*)(entity).leftMap(_.map(e => s"$errorPrefix$e"))

  /** Succeed only if all constraints are valid, otherwise prepend calculated errorPrefix. */
  def allWithComputedPrefix[T](errorPrefix: T => String, constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      all(constraints: _*)(entity).leftMap { r =>
        val prefix = errorPrefix(entity)
        r.map(e => s"$prefix$e")
      }

  /** Succeed if any of the constraints is valid. */
  def any[T](constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      if (constraints.isEmpty) Valid(())
      else {
        val results = constraints.map(_.apply(entity))
        if (results.exists(_.isValid)) Valid(())
        else results.reduce((a, b) => a.combine(b))
      }

  /** Succeed if any of the constraints is valid, otherwise prepend errorPrefix. */
  def anyWithPrefix[T](errorPrefix: String, constraints: Validate[T]*): Validate[T] =
    (entity: T) => any(constraints: _*)(entity).leftMap(_.map(e => s"$errorPrefix$e"))

  /** Succeed if any of the constraints is valid, otherwise prepend errorPrefix. */
  def anyWithComputedPrefix[T](errorPrefix: T => String, constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      any(constraints: _*)(entity).leftMap { r =>
        val prefix = errorPrefix(entity)
        r.map(e => s"$prefix$e")
      }

  /** Depending on the test result follow continue with either first or second constraint. */
  def conditionally[T](
    test: T => Boolean
  )(constraintWhenTrue: Validate[T], constraintWhenFalse: Validate[T]): Validate[T] =
    (entity: T) =>
      if (test(entity)) constraintWhenTrue(entity)
      else constraintWhenFalse(entity)

  /** If the test is true then check the next constraint, otherwise valid. */
  def whenTrue[T](test: T => Boolean)(constraintWhenTrue: Validate[T]): Validate[T] =
    (entity: T) =>
      if (test(entity)) constraintWhenTrue(entity)
      else Valid(())

  /** If the test is false then try the next constraint, otherwise valid. */
  def whenFalse[T](test: T => Boolean)(constraintWhenFalse: Validate[T]): Validate[T] =
    (entity: T) =>
      if (test(entity)) Valid(())
      else constraintWhenFalse(entity)

  /** Depending on the guard constraint result continue with either first or second constraint. */
  def when[T](
    guardConstraint: Validate[T]
  )(constraintWhenValid: Validate[T], constraintWhenInvalid: Validate[T]): Validate[T] =
    (entity: T) =>
      guardConstraint(entity) match {
        case Valid(_)   => constraintWhenValid(entity)
        case Invalid(_) => constraintWhenInvalid(entity)
      }

  /** If the guard constraint is valid then check next constraint. */
  def whenValid[T](guardConstraint: Validate[T])(constraintWhenValid: Validate[T]): Validate[T] =
    (entity: T) =>
      guardConstraint(entity) match {
        case Valid(_) => constraintWhenValid(entity)
        case invalid  => invalid
      }

  /** If the guard constraint is invalid then try next constraint. */
  def whenInvalid[T](guardConstraint: Validate[T])(constraintWhenInvalid: Validate[T]): Validate[T] =
    (entity: T) =>
      guardConstraint(entity) match {
        case Invalid(_) => constraintWhenInvalid(entity)
        case valid      => valid
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

  private type SimpleValidator[T] = T => Validated[String, Unit]

  def validate[T](constraints: SimpleValidator[T]*): Validate[T] =
    (entity: T) =>
      constraints
        .foldLeft[Validated[List[String], Unit]](Valid(()))((v, fx) => v.combine(fx(entity).leftMap(_ :: Nil)))

  /** Validate if the test passes, otherwise fail with error. */
  def check[T](test: T => Boolean, error: String): Validate[T] =
    (entity: T) => Validated.cond(test(entity), (), error :: Nil)

  /** Validate if two properties return the same value. */
  def checkEquals[T, A: Eq](value1: T => A, value2: T => A, error: String): Validate[T] =
    (entity: T) => Validated.cond(implicitly[Eq[A]].eqv(value1(entity), value2(entity)), (), error :: Nil)

  /** Validate if the test returns Right, otherwise fail with Left error. */
  def checkFromEither[T](test: T => Either[String, Any]): Validate[T] =
    (entity: T) => Validated.fromEither(test(entity).map(_ => ()).left.map(_ :: Nil))

  /** Validate if the test returns Right, otherwise fail with Left error prefixed. */
  def checkFromEither[T](test: T => Either[String, Any], errorPrefix: String): Validate[T] =
    (entity: T) => Validated.fromEither(test(entity).map(_ => ()).left.map(e => s"$errorPrefix$e" :: Nil))

  /** Validate if the test returns Some, otherwise fail with error. */
  def checkIsDefined[T](test: T => Option[Any], error: String): Validate[T] =
    (entity: T) => Validated.fromOption(test(entity).map(_ => ()), error :: Nil)

  /** Apply constraint to the extracted property. */
  def checkProperty[T, E](element: T => E, constraint: Validate[E]): Validate[T] =
    (entity: T) => constraint(element(entity))

  /** Apply constraint to the extracted property and if invalid then add prefix to the errors. */
  def checkProperty[T, E](element: T => E, constraint: Validate[E], errorPrefix: String): Validate[T] =
    (entity: T) => constraint(element(entity)).leftMap(_.map(e => s"$errorPrefix$e"))

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
          if (isValidIfNone) Valid(()) else Invalid(List("Expected Some value but got None"))
        )

  /** Apply constraint to the extracted property if defined, otherwise follow isValidIfNone flag.
    * If invalid then add prefix to the errors.
    */
  def checkIfSome[T, E](
    element: T => Option[E],
    validator: Validate[E],
    errorPrefix: String,
    isValidIfNone: Boolean
  ): Validate[T] =
    (entity: T) =>
      element(entity)
        .map(v =>
          validator
            .apply(v)
            .leftMap(_.map(e => s"$errorPrefix$e"))
        )
        .getOrElse(
          if (isValidIfNone) Valid(()) else Invalid(List(s"$errorPrefix expected Some value but got None"))
        )

  /** Apply constraint to each element of the extracted sequence. */
  def checkEach[T, E](elements: T => Seq[E], constraint: Validate[E]): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.map(constraint)
          .reduce(_.combine(_).leftMap(_.distinct))
      else Valid(())
    }

  /** Apply constraint to each element of the extracted sequence.
    *  If invalid then compute and add prefix to the errors.
    */
  def checkEach[T, E](elements: T => Seq[E], constraint: Validate[E], errorPrefix: Int => String): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.zipWithIndex
          .map { case (v, i) => constraint(v).leftMap(_.map(e => s"${errorPrefix(i)}$e")) }
          .reduce(_.combine(_).leftMap(_.distinct))
      else Valid(())
    }

  /** Apply constraint to each element of the extracted sequence if non empty. */
  def checkEachIfNonEmpty[T, E](elements: T => Seq[E], constraint: Validate[E]): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.map(constraint)
          .reduce(_.combine(_))
      else Invalid(List("Sequence must not be empty"))
    }

  /** Apply constraint to each element of the extracted sequence if non empty.
    *  If invalid then compute and add prefix to the errors.
    */
  def checkEachIfNonEmpty[T, E](
    elements: T => Seq[E],
    constraint: Validate[E],
    errorPrefix: Int => String
  ): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.zipWithIndex
          .map { case (v, i) => constraint(v).leftMap(_.map(e => s"${errorPrefix(i)}$e")) }
          .reduce(_.combine(_).leftMap(_.distinct))
      else Invalid(List("Sequence must not be empty"))
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
          if (isValidIfNone) Valid(()) else Invalid(List("Expected Some sequence but got None"))
        )

  /** Apply constraint to each element of the extracted sequence if non empty.
    *  If invalid then compute and add prefix to the errors.
    */
  def checkEachIfSome[T, E](
    extract: T => Option[Seq[E]],
    constraint: Validate[E],
    errorPrefix: Int => String,
    isValidIfNone: Boolean
  ): Validate[T] =
    (entity: T) =>
      extract(entity)
        .map(checkEach(identity, constraint, errorPrefix))
        .getOrElse(
          if (isValidIfNone) Valid(()) else Invalid(List("Expected Some sequence but got None"))
        )

  /** Check if all extracted properties are defined. */
  def checkIfAllDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.forall(f => f(entity).isDefined)) Valid(())
      else Invalid(List(s"All of $expectations must be defined"))

  /** Check if all tests passes */
  def checkIfAllTrue[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.forall(f => f(entity))) Valid(())
      else Invalid(List(s"All of $expectations must be true"))

  /** Check if at least one extracted property is defined. */
  def checkIfAtLeastOneIsDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.exists(f => f(entity).isDefined)) Valid(())
      else Invalid(List(s"One of $expectations must be defined"))

  /** Check if at least one test passes. */
  def checkIfAtLeastOneIsTrue[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.exists(f => f(entity))) Valid(())
      else Invalid(List(s"One of $expectations must be true"))

  /** Check if at most one extracted property is defined. */
  def checkIfAtMostOneIsDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.count(f => f(entity).isDefined) <= 1) Valid(())
      else Invalid(List(s"At most one of $expectations can be defined"))

  /** Check if at most one test passes. */
  def checkIfAtMostOneIsTrue[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.count(f => f(entity)) <= 1) Valid(())
      else Invalid(List(s"At most one of $expectations can be true"))

  /** Check if one and only one extracted property is defined. */
  def checkIfOnlyOneIsDefined[T](
    extractors: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (extractors.count(f => f(entity).isDefined) == 1) Valid(())
      else Invalid(List(s"Only one of $expectations can be defined"))

  /** Check if one and only one test passes. */
  def checkIfOnlyOneIsTrue[T](
    tests: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (tests.count(f => f(entity)) == 1) Valid(())
      else Invalid(List(s"Only one of $expectations can be true"))

  /** Check if one and only one set of properties is fully defined. */
  def checkIfOnlyOneSetIsDefined[T](
    extractors: Seq[Set[T => Option[Any]]],
    expectations: String
  ): Validate[T] =
    (entity: T) => {
      val definedSetCount =
        extractors.map(_.map(f => f(entity).isDefined).reduce(_ && _)).count(_ == true)
      if (definedSetCount == 0)
        Invalid(
          List(
            s"One of the alternative sets $expectations must be defined"
          )
        )
      else if (definedSetCount > 1)
        Invalid(
          List(s"Only one of the alternative sets $expectations can be defined")
        )
      else Valid(())
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
        Invalid(
          List(
            s"One of the alternative sets $expectations must be all true"
          )
        )
      else if (definedSetCount > 1)
        Invalid(
          List(s"Only one of the alternative sets $expectations can all be true")
        )
      else Valid(())
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

  final implicit class ValidateOps[T](val thisValidate: T => Validated[List[String], Unit]) {
    def &(otherValidate: Validate[T]): Validate[T] = Validator.all(thisValidate, otherValidate)
    def |(otherValidate: Validate[T]): Validate[T] = Validator.any(thisValidate, otherValidate)
    def *[U](otherValidate: Validate[U]): Validate[(T, U)] = Validator.product(thisValidate, otherValidate)

    def ?(otherValidate: Validate[T]): Validate[T] = Validator.whenValid[T](thisValidate)(otherValidate)
    def ?!(otherValidate: Validate[T]): Validate[T] = Validator.whenInvalid[T](thisValidate)(otherValidate)

    def andWhenValid(otherValidate: Validate[T]): Validate[T] = Validator.whenValid[T](thisValidate)(otherValidate)
    def andWhenInvalid(otherValidate: Validate[T]): Validate[T] = Validator.whenInvalid[T](thisValidate)(otherValidate)

    def @:(errorPrefix: String): Validate[T] = withPrefix(errorPrefix)
    def withPrefix(errorPrefix: String): Validate[T] =
      (entity: T) => thisValidate(entity).leftMap(_.map(e => s"$errorPrefix$e"))
    def withComputedPrefix(errorPrefix: T => String): Validate[T] =
      (entity: T) =>
        thisValidate(entity).leftMap { r =>
          val prefix = errorPrefix(entity)
          r.map(e => s"$prefix$e")
        }

    def debug: Validate[T] =
      (entity: T) => {
        print(entity)
        print(" => ")
        thisValidate(entity).debug
      }
  }

  final implicit class ValidatedOps(val validated: Validated[List[String], Unit]) {
    def errors: Option[Seq[String]] = validated match {
      case Invalid(errors) => Some(errors.distinct)
      case Valid(_)        => None
    }

    def errorsCount: Int = errors.map(_.length).getOrElse(0)

    def errorString: Option[String] = errors.map(_.mkString(","))
    def errorString(sep: String): Option[String] = errors.map(_.mkString(sep))
    def errorString(start: String, sep: String, end: String): Option[String] = errors.map(_.mkString(start, sep, end))

    def debug: Validated[List[String], Unit] = {
      println(validated.bimap(e => s"Invalid(${e.mkString(", ")})", _ => "Valid"))
      validated
    }
  }

  object Implicits {
    implicit val listSemigroup: Semigroup[List[String]] = Semigroup.instance(_ ++ _)
    implicit val unitSemigroup: Semigroup[Unit] = Semigroup.instance((_, _) => ())
    implicit val stringSemigroup: Semigroup[String] = Semigroup.instance(_ + _)
  }
}
