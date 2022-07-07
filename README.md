[![Build and test](https://github.com/arturopala/validator/actions/workflows/build.yml/badge.svg)](https://github.com/arturopala/validator/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.arturopala/validator_2.13.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.arturopala%22%20AND%20a:%22validator_2.13%22)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.7.0.svg)](https://www.scala-js.org)
![Code size](https://img.shields.io/github/languages/code-size/arturopala/validator)
![GitHub](https://img.shields.io/github/license/arturopala/validator)

Validator
===

This is a micro-library for Scala

    "com.github.arturopala" %% "validator" % "0.11.0"

Cross-compiles to Scala versions `2.13.6`, `2.12.15`, `3.1.1`, 
and ScalaJS version `1.10.1`, and ScalaNative version `0.4.5`.

[Latest API Scaladoc](https://arturopala.github.io/validator/latest/api/com/github/arturopala/validator/index.html)

Motivation
---
Writing validation rules for the complex data structure is a must for developers. There are multiple ways available in Scala to do the validation but still one of the simplest ways to represent and manipulate validation results is to use the built-in `Either`. 

This library provides a thin wrapper around `Either` with a simpler API and opinionated type parameters. 

Here the validator is represented by the function type alias:

    type Validate[-T] = T => Either[List[String], Unit]

and a simple check wrapper:

    trait Check[-T] extends Validate[T] {
        def check(t: T): Either[String,Unit]
    }

The rest of the API is focused on creating and combining instances of `Validate[T]` and `Check[T]`.

Check
---

Check is a simple variant of validate, combining test function with error message:

```scala
import com.github.arturopala.validator.Validator._

val c1 = check[Int](a => a % 2 == 0, "number must be even")
// c1: Check[Int] = <function1>
val c2 = check[Int](a => a % 3 == 0, "number must be divisible by 3")
// c2: Check[Int] = <function1>
val c3 = c1 and c2
// c3: Check[Int] = <function1>
val c4 = c1 or c2
// c4: Check[Int] = <function1>

c1.check(2)
// res0: Either[String, Unit] = Right(value = ())
c1.check(3)
// res1: Either[String, Unit] = Left(value = "number must be even")
c2.check(2)
// res2: Either[String, Unit] = Left(value = "number must be divisible by 3")
c2.check(3)
// res3: Either[String, Unit] = Right(value = ())
c3.check(5)
// res4: Either[String, Unit] = Left(
//   value = "number must be even and number must be divisible by 3"
// )
c3.check(6)
// res5: Either[String, Unit] = Right(value = ())
c4.check(5)
// res6: Either[String, Unit] = Left(
//   value = "number must be even or number must be divisible by 3"
// )
c4.check(6)
// res7: Either[String, Unit] = Right(value = ())
c4.check(7)
// res8: Either[String, Unit] = Left(
//   value = "number must be even or number must be divisible by 3"
// )
```


All batteries included
---

```scala
import com.github.arturopala.validator.Validator._

case class E(a: Int, b: String, c: Option[Int], d: Seq[Int], e: Either[String,E], f: Option[Seq[Int]], g: Boolean, h: Option[String])

val divisibleByThree = check[Int](_ % 3 == 0, "must be divisible by three")
// divisibleByThree: Check[Int] = <function1>

val validateE: Validate[E] = any[E](
    checkEquals(_.a.toString, _.b, "a must be same as b"),
    checkNotEquals(_.a.toString, _.b, "a must be different to b"),
    checkFromEither(_.e),
    checkIsDefined(_.c, "c must be defined"),
    checkIsEmpty(_.c, "c must be not defined"),
    checkProperty(_.a, divisibleByThree),
    checkIfSome(_.c, divisibleByThree, isValidIfNone = true),
    all(
        checkIfSome(_.c, divisibleByThree, isValidIfNone = false),
        checkEach(_.d, divisibleByThree),
        checkEachIfNonEmpty(_.d, divisibleByThree),
        checkEachIfSome(_.f, divisibleByThree),
    ),
    checkIfAllDefined(Seq(_.c, _.f),"c and f must be all defined"),
    checkIfAllEmpty(Seq(_.c, _.f),"c and f must be all empty"),
    checkIfAllOrNoneDefined(Seq(_.c, _.f),"c and f must be either all defined or all empty"),
    checkIfAtLeastOneIsDefined(Seq(_.c,_.f),"c or f or both must be defined"),
    checkIfAtMostOneIsDefined(Seq(_.c,_.f),"none or c or f must be defined"),
    checkIfOnlyOneIsDefined(Seq(_.c,_.f),"c or f must be defined"),
    checkIfOnlyOneSetIsDefined[E](Seq(Set(_.c,_.f), Set(_.c,_.h)),"only (c and f) or (c and h) must be defined"),
    checkIfAllTrue(Seq(_.a.inRange(0,10), _.g),"a must be 0..10 if g is true"),
    checkIfAllFalse(Seq(_.a.inRange(0,10), _.g),"a must not be 0..10 if g is false"),
    checkIfAtLeastOneIsTrue(Seq(_.a.inRange(0,10), _.g),"a must not be 0..10 or g or both must be true"),
    checkIfAtMostOneIsTrue(Seq(_.a.inRange(0,10), _.g),"none or a must not be 0..10 or g must be true"),
    checkIfOnlyOneIsTrue(Seq(_.a.inRange(0,10), _.g),"a must not be 0..10 or g must be true"),
    checkIfOnlyOneSetIsTrue[E](Seq(Set(_.a.inRange(0,10), _.g), Set(_.g,_.h.isDefined)),"only (g and a must not be 0..10) or (g and h.isDefined) must be true"),
)
// validateE: Validate[E] = com.github.arturopala.validator.Validator$$$Lambda$12535/937314512@3e0267d1
```

Usage
---

Create a simple validator:
```scala
import com.github.arturopala.validator.Validator._

val validateIsEven: Validate[Int] = 
    check[Int](_ % 2 == 0, "must be even integer")

val validateIsNonEmpty: Validate[String] = 
    check[String](_.nonEmpty, "must be non-empty string") 

val validateStringLengthPair: Validate[(String,Int)] = 
    check[(String,Int)]({case (s,l) => s.length() == l}, "string must be of expected length")
```

and run it with the tested value:
```scala
validateIsEven(2).isValid
// res9: Boolean = true
validateIsEven(1).isInvalid
// res10: Boolean = true

validateIsNonEmpty("").isInvalid
// res11: Boolean = true
validateIsNonEmpty("abc").isValid
// res12: Boolean = true

validateStringLengthPair(("abc",3)).isValid
// res13: Boolean = true
validateStringLengthPair(("ab",1)).isInvalid
// res14: Boolean = true
```

Validators can be combined using different strategies:
```scala
val validateIsPositive: Validate[Int] = 
    check[Int](_ > 0, "must be positive integer")
 
// combine using ANY to validate whether all checks pass
val validateIsEvenAndPositive: Validate[Int] = 
    all(
        validateIsEven, 
        validateIsPositive
    )

// or use an infix operator &
val evenAndPositive = validateIsEven & validateIsPositive  

// combine using ANY to validate whether any check passes
val validateIsEvenOrPositive: Validate[Int] = 
    any(
        validateIsEven, 
        validateIsPositive
    )

// or use an infix operator |
val evenOrPositive = validateIsEven | validateIsPositive 

// combine using PRODUCT to validate a pair
val validateIsEvenPositivePair: Validate[(Int,Int)] = 
    product(
        validateIsEven, 
        validateIsPositive
    )

// or use an infix operator *
val evenPositivePair = validateIsEven * validateIsPositive 

```
```scala
evenAndPositive(2).isValid
// res15: Boolean = true
evenAndPositive(1).isInvalid
// res16: Boolean = true
evenAndPositive(-1).isInvalid
// res17: Boolean = true
evenAndPositive(-2).isInvalid
// res18: Boolean = true

evenOrPositive(2).isValid
// res19: Boolean = true
evenOrPositive(1).isValid
// res20: Boolean = true
evenOrPositive(-1).isInvalid
// res21: Boolean = true
evenOrPositive(-2).isValid
// res22: Boolean = true

evenPositivePair((2,1)).isValid
// res23: Boolean = true
evenPositivePair((1,2)).isInvalid
// res24: Boolean = true
```

Validate objects using `checkProperty`, `checkIfSome`, `checkEach`, `checkEachIfSome`, etc.:
```scala
case class Foo(a: String, b: Option[Int], c: Boolean, d: Seq[String], e: Bar)
case class Bar(f: BigDecimal, h: Option[Seq[Int]])

val validateBar: Validate[Bar] = all[Bar](
    check(_.f.inRange(0,100),".f must be in range 0..100 inclusive"),
    checkEachIfSomeWithErrorPrefix(_.h, validateIsEvenAndPositive, i => s".h[$i] ", isValidIfNone = false)
).withErrorPrefix("[Bar]")

val prefix: AnyRef => String = o => s"[${o.getClass.getSimpleName}]"

val validateFoo: Validate[Foo] = all[Foo](
    checkProperty(_.a, validateIsNonEmpty),
    check(_.a.matches("[A-Z]\\d{3,5}"),".a must follow pattern [A-Z]\\d{3,5}"),
    checkIfSome[Foo,Int](_.b, evenOrPositive, isValidIfNone = true).withErrorPrefix(".b"),
    conditionally(
        _.c,
        checkEachWithErrorPrefix(_.d, validateIsNonEmpty & check(_.lengthMax(64),"64 characters maximum"), 
        i => s".d[$i] "),
        checkProperty[Foo,Bar](_.e, validateBar).withErrorPrefix(".e")
    )
).withErrorPrefixComputed(prefix)
```
```scala
validateFoo(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8)))))
// res25: Either[List[String], Unit] = Right(value = ())
validateFoo(Foo("X67",Some(-1),true,Seq("abc",""),Bar(500,Some(Seq(7)))))
// res26: Either[List[String], Unit] = Left(
//   value = List(
//     "[Foo].a must follow pattern [A-Z]\\d{3,5}",
//     "[Foo].bmust be even integer",
//     "[Foo].bmust be positive integer",
//     "[Foo].d[1] must be non-empty string"
//   )
// )
validateFoo(Foo("X678",Some(2),false,Seq("abc"),Bar(99,None)))
// res27: Either[List[String], Unit] = Left(
//   value = List("[Foo].e[Bar]Expected Some sequence but got None")
// )
validateFoo(Foo("X",Some(3),false,Seq("abc",""),Bar(-1,Some(Seq(7,8,9)))))
// res28: Either[List[String], Unit] = Left(
//   value = List(
//     "[Foo].a must follow pattern [A-Z]\\d{3,5}",
//     "[Foo].e[Bar].f must be in range 0..100 inclusive",
//     "[Foo].e[Bar].h[0] must be even integer",
//     "[Foo].e[Bar].h[2] must be even integer"
//   )
// )
```

Tag validator with prefix:
```scala
evenOrPositive.apply(-1).errorString
// res29: Option[String] = Some(
//   value = "must be even integer,must be positive integer"
// )
("prefix: " @: evenOrPositive).apply(-1).errorString
// res30: Option[String] = Some(
//   value = "prefix: must be even integer,prefix: must be positive integer"
// )
evenOrPositive.withErrorPrefix("foo_").apply(-1).errorString
// res31: Option[String] = Some(
//   value = "foo_must be even integer,foo_must be positive integer"
// )
evenOrPositive.withErrorPrefixComputed(i => s"($i) ").apply(-1).errorString
// res32: Option[String] = Some(
//   value = "(-1) must be even integer,(-1) must be positive integer"
// )
```

Debug validator:
```scala
// debug input and output
validateFoo.debug.apply(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8)))))
// Foo(X678,Some(2),true,List(abc),Bar(500,Some(List(8)))) => Valid
// res33: Either[List[String], Unit] = Right(value = ())
// debug only output
validateFoo.apply(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8))))).debug
// Valid
// res34: Either[List[String], Unit] = Right(value = ())
```

Development
---

Compile

    sbt compile

Compile for all Scala versions

    sbt +compile

Test

    sbt +test

    sbt rootJVM/test
    sbt rootJS/test
    sbt rootNative/test

Test with all Scala versions

    sbt +test
    sbt +rootJVM/test


Generate README and docs

    sbt docs/mdoc

Apply scalafixes

    sbt rootJMV/scalafixAll    
