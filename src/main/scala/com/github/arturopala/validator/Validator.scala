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

object Validator {

  import Implicits._

  final type Validate[T] = T => Validated[List[String], Unit]

  def apply[T](constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      constraints
        .foldLeft[Validated[List[String], Unit]](Valid(()))((v, fx) => v.combine(fx(entity)))

  def always[T]: Validate[T] = (_: T) => Valid(())

  def never[T]: Validate[T] = (_: T) => Invalid(Nil)

  def all[T](constraints: Validate[T]*): Validate[T] = apply(constraints: _*)

  def any[T](constraints: Validate[T]*): Validate[T] =
    (entity: T) =>
      if (constraints.isEmpty) Valid(())
      else {
        val results = constraints.map(_.apply(entity))
        if (results.exists(_.isValid)) Valid(())
        else results.reduce((a, b) => a.combine(b))
      }

  def product[A, B](constraintA: Validate[A], constraintB: Validate[B]): Validate[(A, B)] =
    (entity: (A, B)) => constraintA(entity._1).combine(constraintB(entity._2))

  def product[A, B, C](
    constraintA: Validate[A],
    constraintB: Validate[B],
    constraintC: Validate[C]
  ): Validate[(A, B, C)] =
    (entity: (A, B, C)) => constraintA(entity._1).combine(constraintB(entity._2)).combine(constraintC(entity._3))

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

  def check[T](test: T => Boolean, error: String): Validate[T] =
    (entity: T) => Validated.cond(test(entity), (), error :: Nil)

  def checkFromEither[T](test: T => Either[String, Any]): Validate[T] =
    (entity: T) => Validated.fromEither(test(entity).map(_ => ()).left.map(_ :: Nil))

  def checkFromEither[T](test: T => Either[String, Any], errorPrefix: String): Validate[T] =
    (entity: T) => Validated.fromEither(test(entity).map(_ => ()).left.map(e => s"$errorPrefix$e" :: Nil))

  def checkFromOption[T](test: T => Option[Any], error: String): Validate[T] =
    (entity: T) => Validated.fromOption(test(entity).map(_ => ()), error :: Nil)

  def checkProperty[T, E](element: T => E, validator: Validate[E]): Validate[T] =
    (entity: T) => validator(element(entity))

  def checkProperty[T, E](element: T => E, validator: Validate[E], errorPrefix: String): Validate[T] =
    (entity: T) => validator(element(entity)).leftMap(_.map(e => s"$errorPrefix$e"))

  def checkIfSome[T, E](
    element: T => Option[E],
    validator: Validate[E],
    isValidIfNone: Boolean = true
  ): Validate[T] =
    (entity: T) =>
      element(entity)
        .map(validator)
        .getOrElse(
          if (isValidIfNone) Valid(()) else Invalid(List("Expected Some value but got None"))
        )

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

  def checkEach[T, E](elements: T => Seq[E], validator: Validate[E]): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.map(validator)
          .reduce(_.combine(_).leftMap(_.distinct))
      else Valid(())
    }

  def checkEach[T, E](elements: T => Seq[E], validator: Validate[E], errorPrefix: Int => String): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.zipWithIndex
          .map { case (v, i) => validator(v).leftMap(_.map(e => s"${errorPrefix(i)}$e")) }
          .reduce(_.combine(_).leftMap(_.distinct))
      else Valid(())
    }

  def checkEachIfNonEmpty[T, E](elements: T => Seq[E], validator: Validate[E]): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.map(validator)
          .reduce(_.combine(_))
      else Invalid(List("Sequence must not be empty"))
    }

  def checkEachIfNonEmpty[T, E](
    elements: T => Seq[E],
    validator: Validate[E],
    errorPrefix: Int => String
  ): Validate[T] =
    (entity: T) => {
      val es = elements(entity)
      if (es.nonEmpty)
        es.zipWithIndex
          .map { case (v, i) => validator(v).leftMap(_.map(e => s"${errorPrefix(i)}$e")) }
          .reduce(_.combine(_).leftMap(_.distinct))
      else Invalid(List("Sequence must not be empty"))
    }

  def checkEachIfSome[T, E](
    extract: T => Option[Seq[E]],
    validator: Validate[E],
    isValidIfNone: Boolean = true
  ): Validate[T] =
    (entity: T) =>
      extract(entity)
        .map(checkEach(identity, validator))
        .getOrElse(
          if (isValidIfNone) Valid(()) else Invalid(List("Expected Some sequence but got None"))
        )

  def checkEachIfSome[T, E](
    extract: T => Option[Seq[E]],
    validator: Validate[E],
    errorPrefix: Int => String,
    isValidIfNone: Boolean
  ): Validate[T] =
    (entity: T) =>
      extract(entity)
        .map(checkEach(identity, validator, errorPrefix))
        .getOrElse(
          if (isValidIfNone) Valid(()) else Invalid(List("Expected Some sequence but got None"))
        )

  def checkIfAllDefined[T](
    alternatives: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (alternatives.forall(f => f(entity).isDefined)) Valid(())
      else Invalid(List(s"All of $expectations must be defined"))

  def checkIfAllTrue[T](
    alternatives: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (alternatives.forall(f => f(entity))) Valid(())
      else Invalid(List(s"All of $expectations must be true"))

  def checkIfAtLeastOneIsDefined[T](
    alternatives: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (alternatives.exists(f => f(entity).isDefined)) Valid(())
      else Invalid(List(s"One of $expectations must be defined"))

  def checkIfAtLeastOneIsTrue[T](
    alternatives: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (alternatives.exists(f => f(entity))) Valid(())
      else Invalid(List(s"One of $expectations must be true"))

  def checkIfAtMostOneIsDefined[T](
    alternatives: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (alternatives.count(f => f(entity).isDefined) <= 1) Valid(())
      else Invalid(List(s"At most one of $expectations can be defined"))

  def checkIfAtMostOneIsTrue[T](
    alternatives: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (alternatives.count(f => f(entity)) <= 1) Valid(())
      else Invalid(List(s"At most one of $expectations can be true"))

  def checkIfOnlyOneIsDefined[T](
    alternatives: Seq[T => Option[Any]],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (alternatives.count(f => f(entity).isDefined) == 1) Valid(())
      else Invalid(List(s"Only one of $expectations can be defined"))

  def checkIfOnlyOneIsTrue[T](
    alternatives: Seq[T => Boolean],
    expectations: String
  ): Validate[T] =
    (entity: T) =>
      if (alternatives.count(f => f(entity)) == 1) Valid(())
      else Invalid(List(s"Only one of $expectations can be true"))

  def checkIfOnlyOneSetIsDefined[T](
    alternatives: Seq[Set[T => Option[Any]]],
    expectations: String
  ): Validate[T] =
    (entity: T) => {
      val definedSetCount =
        alternatives.map(_.map(f => f(entity).isDefined).reduce(_ && _)).count(_ == true)
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

  def checkIfOnlyOneSetIsTrue[T](
    alternatives: Seq[Set[T => Boolean]],
    expectations: String
  ): Validate[T] =
    (entity: T) => {
      val definedSetCount =
        alternatives.map(_.map(f => f(entity)).reduce(_ && _)).count(_ == true)
      if (definedSetCount == 0)
        Invalid(
          List(
            s"One of the alternative sets $expectations must be all true"
          )
        )
      else if (definedSetCount > 1)
        Invalid(
          List(s"Only one of the alternative sets $expectations can be all true")
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
    def &&(otherValidate: Validate[T]): Validate[T] = Validator.all(thisValidate, otherValidate)
    def ||(otherValidate: Validate[T]): Validate[T] = Validator.any(thisValidate, otherValidate)
    def **[U](otherValidate: Validate[U]): Validate[(T, U)] = Validator.product(thisValidate, otherValidate)
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
      println(errorString(", "))
      validated
    }
  }

  object Implicits {
    implicit val listSemigroup: Semigroup[List[String]] = Semigroup.instance(_ ++ _)
    implicit val unitSemigroup: Semigroup[Unit] = Semigroup.instance((_, _) => ())
    implicit val stringSemigroup: Semigroup[String] = Semigroup.instance(_ + _)
  }
}
