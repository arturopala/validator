[![Build and test](https://github.com/arturopala/validator/actions/workflows/build.yml/badge.svg)](https://github.com/arturopala/validator/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.arturopala/validator_2.13.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.arturopala%22%20AND%20a:%22validator_2.13%22)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.7.0.svg)](https://www.scala-js.org)
![Code size](https://img.shields.io/github/languages/code-size/arturopala/validator)
![GitHub](https://img.shields.io/github/license/arturopala/validator)

Validator
===

This is a micro-library for Scala

    "com.github.arturopala" %% "validator" % "0.19.0"

Cross-compiles to Scala versions `2.13.10`, `2.12.17`, `3.2.1`, 
and ScalaJS version `1.12.0`, and ScalaNative version `0.4.9`.

[Latest API Scaladoc](https://arturopala.github.io/validator/latest/api/com/github/arturopala/validator/index.html)

Motivation
---
Writing validation rules for the complex data structure is a must for developers. There are multiple ways available in Scala to do the validation but still one of the simplest ways to represent and manipulate validation results is to use the built-in `Either`. 

This library provides a thin wrapper around `Either` with a simpler API and opinionated type parameters. 

Here the validator is represented by the function type alias:

    sealed trait Error
    type Result = Either[Error, Unit]

    type Validate[-T] = T => Result

The rest of the API is focused on creating and composing instances of `Validate[T]`.

API
---
[See latest API - Scaladoc](https://arturopala.github.io/validator/latest/api/com/github/arturopala/validator/Validator$.html)

Simple example
---

```scala
import com.github.arturopala.validator.Validator._

    case class Address(street: String, town: String, postcode: String, country: String)
    case class PhoneNumber(prefix: String, number: String, description: String)
    case class Contact(name: String, address: Address, phoneNumbers: Seq[PhoneNumber])

    object Country {
      val codes = Set("en", "de", "fr")
      val telephonePrefixes = Set("+44", "+41", "+42")
    }

    val postcodeCheck = checkIsTrue[String](_.matches("""\d{5}"""), "address.postcode.invalid")
// postcodeCheck: Validate[String] = com.github.arturopala.validator.Validator$$$Lambda$12601/1219290978@6628ed17
    val countryCheck = checkIsTrue[String](_.isOneOf(Country.codes), "address.country.invalid")
// countryCheck: Validate[String] = com.github.arturopala.validator.Validator$$$Lambda$12601/1219290978@6003951a
    val phoneNumberPrefixCheck =
      checkIsTrue[String](_.isOneOf(Country.telephonePrefixes), "address.phone.prefix.invalid")
// phoneNumberPrefixCheck: Validate[String] = com.github.arturopala.validator.Validator$$$Lambda$12601/1219290978@78c78e8e
    val phoneNumberValueCheck = checkIsTrue[String](_.matches("""\d{7}"""), "address.phone.prefix.invalid")
// phoneNumberValueCheck: Validate[String] = com.github.arturopala.validator.Validator$$$Lambda$12601/1219290978@59dc367f

    val addressCheck = all[Address](
      checkIsTrue(_.street.nonEmpty, "address.street.empty"),
      checkIsTrue(_.town.nonEmpty, "address.town.empty"),
      checkWith(_.postcode, postcodeCheck),
      checkWith(_.country, countryCheck)
    )
// addressCheck: Address => Either[Error, Unit] = com.github.arturopala.validator.Validator$$$Lambda$12603/79102320@3275137f

    val phoneNumberCheck = all[PhoneNumber](
      checkWith(_.prefix, phoneNumberPrefixCheck),
      checkWith(_.number, phoneNumberValueCheck)
    )
// phoneNumberCheck: PhoneNumber => Either[Error, Unit] = com.github.arturopala.validator.Validator$$$Lambda$12603/79102320@4ea2fb6e

    val contactCheck = all[Contact](
      checkIsTrue(_.name.nonEmpty, "contact.name.empty"),
      checkWith(_.address, addressCheck),
      checkEach(_.phoneNumbers, phoneNumberCheck)
    )
// contactCheck: Contact => Either[Error, Unit] = com.github.arturopala.validator.Validator$$$Lambda$12603/79102320@a55bfca

    val c1 = Contact(
      name = "Foo Bar",
      address = Address(street = "Sesame Street 1", town = "Cookieburgh", country = "en", postcode = "00001"),
      phoneNumbers = Seq(PhoneNumber("+44", "1234567", "ceo"), PhoneNumber("+41", "7654321", "sales"))
    )
// c1: Contact = Contact(
//   name = "Foo Bar",
//   address = Address(
//     street = "Sesame Street 1",
//     town = "Cookieburgh",
//     postcode = "00001",
//     country = "en"
//   ),
//   phoneNumbers = List(
//     PhoneNumber(prefix = "+44", number = "1234567", description = "ceo"),
//     PhoneNumber(prefix = "+41", number = "7654321", description = "sales")
//   )
// )

    contactCheck(c1).isValid
// res0: Boolean = true

    val c2 = Contact(
      name = "",
      address = Address(street = "", town = "", country = "ca", postcode = "foobar"),
      phoneNumbers = Seq(PhoneNumber("+1", "11111111111", "ceo"), PhoneNumber("+01", "00000000", "sales"))
    )
// c2: Contact = Contact(
//   name = "",
//   address = Address(street = "", town = "", postcode = "foobar", country = "ca"),
//   phoneNumbers = List(
//     PhoneNumber(prefix = "+1", number = "11111111111", description = "ceo"),
//     PhoneNumber(prefix = "+01", number = "00000000", description = "sales")
//   )
// )

    contactCheck(c2).errorsOption
// res1: Option[Seq[String]] = Some(
//   value = List(
//     "contact.name.empty",
//     "address.street.empty",
//     "address.town.empty",
//     "address.postcode.invalid",
//     "address.country.invalid",
//     "address.phone.prefix.invalid"
//   )
// )
```

All batteries included
---

```scala
import com.github.arturopala.validator.Validator._

case class E(a: Int, b: String, c: Option[Int], d: Seq[Int], e: Either[String,E], f: Option[Seq[Int]], g: Boolean, h: Option[String])

val divisibleByThree = checkIsTrue[Int](_ % 3 == 0, "must be divisible by three")
// divisibleByThree: Validate[Int] = com.github.arturopala.validator.Validator$$$Lambda$12601/1219290978@7984e963

val validateE: Validate[E] = any[E](
    checkEquals(_.a.toString, _.b, "a must be same as b"),
    checkNotEquals(_.a.toString, _.b, "a must be different to b"),
    checkFromEither(_.e),
    checkIsDefined(_.c, "c must be defined"),
    checkIsEmpty(_.c, "c must be not defined"),
    checkWith(_.a, divisibleByThree),
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
// validateE: Validate[E] = com.github.arturopala.validator.Validator$$$Lambda$12631/335032202@38c06c1f
```

Usage
---

Create a simple validator:
```scala
import com.github.arturopala.validator.Validator._

val validateIsEven: Validate[Int] = 
    checkIsTrue[Int](_ % 2 == 0, "must be even integer")

val validateIsNonEmpty: Validate[String] = 
    checkIsTrue[String](_.nonEmpty, "must be non-empty string") 

val validateStringLengthPair: Validate[(String,Int)] = 
    checkIsTrue[(String,Int)]({case (s,l) => s.length() == l}, "string must be of expected length")
```

and run it with the tested value:
```scala
validateIsEven(2).isValid
// res2: Boolean = true
validateIsEven(1).isInvalid
// res3: Boolean = true

validateIsNonEmpty("").isInvalid
// res4: Boolean = true
validateIsNonEmpty("abc").isValid
// res5: Boolean = true

validateStringLengthPair(("abc",3)).isValid
// res6: Boolean = true
validateStringLengthPair(("ab",1)).isInvalid
// res7: Boolean = true
```

Validators can be combined using different strategies:
```scala
val validateIsPositive: Validate[Int] = 
    checkIsTrue[Int](_ > 0, "must be positive integer")
 
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
// res8: Boolean = true
evenAndPositive(1).isInvalid
// res9: Boolean = true
evenAndPositive(-1).isInvalid
// res10: Boolean = true
evenAndPositive(-2).isInvalid
// res11: Boolean = true

evenOrPositive(2).isValid
// res12: Boolean = true
evenOrPositive(1).isValid
// res13: Boolean = true
evenOrPositive(-1).isInvalid
// res14: Boolean = true
evenOrPositive(-2).isValid
// res15: Boolean = true

evenPositivePair((2,1)).isValid
// res16: Boolean = true
evenPositivePair((1,2)).isInvalid
// res17: Boolean = true
```

Validate objects using `checkWith`, `checkIfSome`, `checkEach`, `checkEachIfSome`, etc.:
```scala
case class Foo(a: String, b: Option[Int], c: Boolean, d: Seq[String], e: Bar)
case class Bar(f: BigDecimal, h: Option[Seq[Int]])

val validateBar: Validate[Bar] = all[Bar](
    checkIsTrue(_.f.inRange(0,100),".f must be in range 0..100 inclusive"),
    checkEachIfSomeWithErrorPrefix(_.h, validateIsEvenAndPositive, i => s".h[$i] ", isValidIfNone = false)
).withErrorPrefix("[Bar]")

val prefix: AnyRef => String = o => s"[${o.getClass.getSimpleName}]"

val validateFoo: Validate[Foo] = all[Foo](
    checkWith(_.a, validateIsNonEmpty),
    checkIsTrue(_.a.matches("[A-Z]\\d{3,5}"),".a must follow pattern [A-Z]\\d{3,5}"),
    checkIfSome[Foo,Int](_.b, evenOrPositive, isValidIfNone = true).withErrorPrefix(".b"),
    conditionally(
        _.c,
        checkEachWithErrorPrefix(_.d, validateIsNonEmpty & checkIsTrue(_.lengthMax(64),"64 characters maximum"), 
        i => s".d[$i] "),
        checkWith[Foo,Bar](_.e, validateBar).withErrorPrefix(".e")
    )
).withErrorPrefixComputed(prefix)
```
```scala
validateFoo(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8)))))
// res18: Result = Right(value = ())
validateFoo(Foo("X67",Some(-1),true,Seq("abc",""),Bar(500,Some(Seq(7)))))
// res19: Result = Left(
//   value = And(
//     errors = List(
//       Single(message = "[Foo].a must follow pattern [A-Z]\\d{3,5}"),
//       Or(
//         errors = List(
//           Single(message = "[Foo].bmust be even integer"),
//           Single(message = "[Foo].bmust be positive integer")
//         )
//       ),
//       Single(message = "[Foo].d[1] must be non-empty string")
//     )
//   )
// )
validateFoo(Foo("X678",Some(2),false,Seq("abc"),Bar(99,None)))
// res20: Result = Left(
//   value = Single(message = "[Foo].e[Bar]Expected Some sequence but got None")
// )
validateFoo(Foo("X",Some(3),false,Seq("abc",""),Bar(-1,Some(Seq(7,8,9)))))
// res21: Result = Left(
//   value = And(
//     errors = List(
//       Single(message = "[Foo].a must follow pattern [A-Z]\\d{3,5}"),
//       Single(message = "[Foo].e[Bar].f must be in range 0..100 inclusive"),
//       Single(message = "[Foo].e[Bar].h[0] must be even integer"),
//       Single(message = "[Foo].e[Bar].h[2] must be even integer")
//     )
//   )
// )
```

Tag validator with prefix:
```scala
evenOrPositive.apply(-1).errorsSummaryOption
// res22: Option[String] = Some(
//   value = "must be even integer or must be positive integer"
// )
("prefix: " @: evenOrPositive).apply(-1).errorsSummaryOption
// res23: Option[String] = Some(
//   value = "prefix: must be even integer or prefix: must be positive integer"
// )
evenOrPositive.withErrorPrefix("foo_").apply(-1).errorsSummaryOption
// res24: Option[String] = Some(
//   value = "foo_must be even integer or foo_must be positive integer"
// )
evenOrPositive.withErrorPrefixComputed(i => s"($i) ").apply(-1).errorsSummaryOption
// res25: Option[String] = Some(
//   value = "(-1) must be even integer or (-1) must be positive integer"
// )
```

Debug validator:
```scala
// debug input and output
validateFoo.debug.apply(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8)))))
// Foo(X678,Some(2),true,List(abc),Bar(500,Some(List(8)))) => Valid
// res26: Result = Right(value = ())
// debug only output
validateFoo.apply(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8))))).debug
// Valid
// res27: Result = Right(value = ())
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
