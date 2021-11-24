[![Build and test](https://github.com/arturopala/validator/actions/workflows/build.yml/badge.svg)](https://github.com/arturopala/validator/actions/workflows/build.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/validator_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/validator_2.13)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.7.0.svg)](https://www.scala-js.org)

Validator
===

This is a micro-library for Scala

    "com.github.arturopala" %% "validator" % "0.2.0"

Cross-compiles to Scala versions `2.13.6`, `2.12.15`, `3.1.0`, 
and ScalaJS version `1.7.0`, and ScalaNative version `0.4.0`.

[Latest API Scaladoc](https://arturopala.github.io/validator/latest/api/com/github/arturopala/validator/index.html)

Motivation
---
Writing validation rules for the complex data structure is a must for developers. There are multiple ways available in Scala to do validation, still one of the best is [Cats Validated](https://typelevel.org/cats/datatypes/validated.html). This library provides a thin wrapper around original `Validated` with a simpler API and opinionated type parameters. 

Here the validator is represented by the function type alias:

    type Validate[T] = T => Validated[List[String], Unit]

The rest of the API is focused on creating and combining instances of `Validate[T]`.

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
// res0: Boolean = true
validateIsEven(1).isInvalid
// res1: Boolean = true

validateIsNonEmpty("").isInvalid
// res2: Boolean = true
validateIsNonEmpty("abc").isValid
// res3: Boolean = true

validateStringLengthPair(("abc",3)).isValid
// res4: Boolean = true
validateStringLengthPair(("ab",1)).isInvalid
// res5: Boolean = true
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
// res6: Boolean = true
evenAndPositive(1).isInvalid
// res7: Boolean = true
evenAndPositive(-1).isInvalid
// res8: Boolean = true
evenAndPositive(-2).isInvalid
// res9: Boolean = true

evenOrPositive(2).isValid
// res10: Boolean = true
evenOrPositive(1).isValid
// res11: Boolean = true
evenOrPositive(-1).isInvalid
// res12: Boolean = true
evenOrPositive(-2).isValid
// res13: Boolean = true

evenPositivePair((2,1)).isValid
// res14: Boolean = true
evenPositivePair((1,2)).isInvalid
// res15: Boolean = true
```

Validate objects using `checkProperty`, `checkIfSome`, `checkEach`, `checkEachIfSome`, etc.:
```scala
case class Foo(a: String, b: Option[Int], c: Boolean, d: Seq[String], e: Bar)
case class Bar(f: BigDecimal, h: Option[Seq[Int]])

val validateBar: Validate[Bar] = all[Bar]("[Bar]", // <-- errors prefix
    check(_.f.inRange(0,100),".f must be in range 0..100 inclusive"),
    checkEachIfSome(_.h, validateIsEvenAndPositive, i => s".h[$i] ", isValidIfNone = false)
)

val prefix: AnyRef => String = o => s"[${o.getClass.getSimpleName}]"

val validateFoo: Validate[Foo] = all[Foo](prefix, // <-- errors prefix function
    checkProperty(_.a, validateIsNonEmpty),
    check(_.a.matches("[A-Z]\\d{3,5}"),".a must follow pattern [A-Z]\\d{3,5}"),
    checkIfSome(_.b, evenOrPositive, ".b", isValidIfNone = true),
    conditionally[Foo](_.c)(
        checkEach(_.d, validateIsNonEmpty & check(_.lengthMax(64),"64 characters maximum"), 
        i => s".d[$i] "),
        checkProperty(_.e, validateBar, ".e")
    )
)
```
```scala
validateFoo(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8)))))
// res16: cats.data.Validated[List[String], Unit] = Valid(a = ())
validateFoo(Foo("X67",Some(-1),true,Seq("abc",""),Bar(500,Some(Seq(7)))))
// res17: cats.data.Validated[List[String], Unit] = Invalid(
//   e = List(
//     "[Foo].a must follow pattern [A-Z]\\d{3,5}",
//     "[Foo].bmust be even integer",
//     "[Foo].bmust be positive integer",
//     "[Foo].d[1] must be non-empty string"
//   )
// )
validateFoo(Foo("X678",Some(2),false,Seq("abc"),Bar(99,None)))
// res18: cats.data.Validated[List[String], Unit] = Invalid(
//   e = List("[Foo].e[Bar]Expected Some sequence but got None")
// )
validateFoo(Foo("X",Some(3),false,Seq("abc",""),Bar(-1,Some(Seq(7,8,9)))))
// res19: cats.data.Validated[List[String], Unit] = Invalid(
//   e = List(
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
// res20: Option[String] = Some(
//   value = "must be even integer,must be positive integer"
// )
("prefix: " @: evenOrPositive).apply(-1).errorString
// res21: Option[String] = Some(
//   value = "prefix: must be even integer,prefix: must be positive integer"
// )
evenOrPositive.withPrefix("foo_").apply(-1).errorString
// res22: Option[String] = Some(
//   value = "foo_must be even integer,foo_must be positive integer"
// )
evenOrPositive.withPrefix(i => s"($i) ").apply(-1).errorString
// res23: Option[String] = Some(
//   value = "(-1) must be even integer,(-1) must be positive integer"
// )
```

Debug validator:
```scala
// debug input and output
validateFoo.debug.apply(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8)))))
// Foo(X678,Some(2),true,List(abc),Bar(500,Some(List(8)))) => Valid(Valid)
// res24: cats.data.Validated[List[String], Unit] = Valid(a = ())
// debug only output
validateFoo.apply(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8))))).debug
// Valid(Valid)
// res25: cats.data.Validated[List[String], Unit] = Valid(a = ())
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