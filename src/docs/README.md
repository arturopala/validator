[![Build and test](https://github.com/arturopala/validator/actions/workflows/build.yml/badge.svg)](https://github.com/arturopala/validator/actions/workflows/build.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/validator_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/validator_2.13)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.7.0.svg)](https://www.scala-js.org)

Validator
===

This is a micro-library for Scala

    "com.github.arturopala" %% "validator" % "@VERSION@"

Cross-compiles to Scala versions @SUPPORTED_SCALA_VERSIONS@, 
and ScalaJS version `@SCALA_JS_VERSION@`, and ScalaNative version `@SCALA_NATIVE_VERSION@`.

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
```scala mdoc:silent
import com.github.arturopala.validator.Validator._

val validateIsEven: Validate[Int] = 
    check[Int](_ % 2 == 0, "must be even integer")

val validateIsNonEmpty: Validate[String] = 
    check[String](_.nonEmpty, "must be non-empty string") 

val validateStringLengthPair: Validate[(String,Int)] = 
    check[(String,Int)]({case (s,l) => s.length() == l}, "string must be of expected length")
```

and run it with the tested value:
```scala mdoc
validateIsEven(2).isValid
validateIsEven(1).isInvalid

validateIsNonEmpty("").isInvalid
validateIsNonEmpty("abc").isValid

validateStringLengthPair(("abc",3)).isValid
validateStringLengthPair(("ab",1)).isInvalid
```

Validators can be combined using different strategies:
```scala mdoc:silent
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
```scala mdoc
evenAndPositive(2).isValid
evenAndPositive(1).isInvalid
evenAndPositive(-1).isInvalid
evenAndPositive(-2).isInvalid

evenOrPositive(2).isValid
evenOrPositive(1).isValid
evenOrPositive(-1).isInvalid
evenOrPositive(-2).isValid

evenPositivePair((2,1)).isValid
evenPositivePair((1,2)).isInvalid
```

Validate objects using `checkProperty`, `checkIfSome`, `checkEach`, `checkEachIfSome`, etc.:
```scala mdoc:silent
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
```scala mdoc 
validateFoo(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8)))))
validateFoo(Foo("X67",Some(-1),true,Seq("abc",""),Bar(500,Some(Seq(7)))))
validateFoo(Foo("X678",Some(2),false,Seq("abc"),Bar(99,None)))
validateFoo(Foo("X",Some(3),false,Seq("abc",""),Bar(-1,Some(Seq(7,8,9)))))
```

Tag validator with prefix:
```scala mdoc
evenOrPositive.apply(-1).errorString
("prefix: " @: evenOrPositive).apply(-1).errorString
evenOrPositive.withPrefix("foo_").apply(-1).errorString
evenOrPositive.withPrefix(i => s"($i) ").apply(-1).errorString
```

Debug validator:
```scala mdoc
// debug input and output
validateFoo.debug.apply(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8)))))
// debug only output
validateFoo.apply(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8))))).debug
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
