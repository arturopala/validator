[![Build and test](https://github.com/arturopala/validator/actions/workflows/build.yml/badge.svg)](https://github.com/arturopala/validator/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.arturopala/validator_2.13.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.arturopala%22%20AND%20a:%22validator_2.13%22)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.7.0.svg)](https://www.scala-js.org)
![Code size](https://img.shields.io/github/languages/code-size/arturopala/validator)
![GitHub](https://img.shields.io/github/license/arturopala/validator)

Validator
===

This is a micro-library for Scala

    "com.github.arturopala" %% "validator" % "@VERSION@"

Cross-compiles to Scala versions @SUPPORTED_SCALA_VERSIONS@, 
and ScalaJS version `@SCALA_JS_VERSION@`, and ScalaNative version `@SCALA_NATIVE_VERSION@`.

[Latest API Scaladoc](https://arturopala.github.io/validator/latest/api/com/github/arturopala/validator/index.html)

Motivation
---
Writing validation rules for the complex data structure is an everyday business developer's life. There are multiple ways available in Scala to do the validation but still one of the simplest ways to represent and manipulate validation results is to use the built-in `Either`. 

This library provides a thin wrapper around `Either` with a simpler API and opinionated type parameters. 

Here the validator is represented by the function type alias:

    sealed trait Error
    type Result = Either[Error, Unit]

    type Validate[-T] = T => Result

The rest of the API is focused on creating and composing instances of `Validate[T]`.

Scaladoc
---
<https://arturopala.github.io/validator/latest/api/com/github/arturopala/validator/Validator$.html>

Try in Scastie!
---
<https://scastie.scala-lang.org/arturopala/EsOKlzujSy6cGWrh8qZb9g/31>

Simple example
---

```scala mdoc
import com.github.arturopala.validator.Validator._

case class Address(
    street: String,
    town: String,
    postcode: String,
    country: String
)
case class PhoneNumber(
    prefix: String, 
    number: String, 
    description: String
)
case class Contact(
    name: String,
    address: Either[String, Address],
    phoneNumbers: Seq[PhoneNumber] = Seq.empty,
    email: Option[String] = None
)

object Country {
  val codes = Set("en", "de", "fr")
  val telephonePrefixes = Set("+44", "+41", "+42")
}

val validatePostcode =
  checkIsTrue[String](_.matches("""\d{5}"""), "address.postcode.invalid")

val validateCountry =
  checkIsTrue[String](_.isOneOf(Country.codes), "address.country.invalid")

val validatePhoneNumberPrefix =
  checkIsTrue[String](
    _.isOneOf(Country.telephonePrefixes),
    "address.phone.prefix.invalid"
  )

val validatePhoneNumberValue =
  checkIsTrue[String](_.matches("""\d{7}"""), "address.phone.prefix.invalid")

val validatePhoneNumber =
  all[PhoneNumber](
    checkProp(_.prefix, validatePhoneNumberPrefix),
    checkProp(_.number, validatePhoneNumberValue)
  )

val validateEmail = 
  all[String](
    checkIsTrue[String](_.contains("@"), "address.email.invalid")
  )

val validateAddress =
  all[Address](
    checkIsTrue(_.street.nonEmpty, "address.street.empty"),
    checkIsTrue(_.town.nonEmpty, "address.town.empty"),
    checkProp(_.postcode, validatePostcode),
    checkProp(_.country, validateCountry)
  )

val validateContact =
  all[Contact](
    checkIsTrue(_.name.nonEmpty, "contact.name.empty"),
    checkEither(_.address, validateStringNonEmpty("address.manual.empty") , validateAddress),
    any(
        checkIfSome(_.email, validateEmail, isValidIfNone = false),
        all(
            checkProp(_.phoneNumbers, validateCollectionNonEmpty("address.phoneNumbers.empty")),
            checkEach(_.phoneNumbers, validatePhoneNumber)
        )
    )
  )

// TEST

val c1 = Contact(
  name = "Foo Bar",
  address = Right(Address(
    street = "Sesame Street 1",
    town = "Cookieburgh",
    country = "en",
    postcode = "00001"
  )),
  phoneNumbers = Seq(
    PhoneNumber("+44", "1234567", "ceo"),
    PhoneNumber("+41", "7654321", "sales")
  )
)

validateContact(c1).isValid
validateContact(c1).errorsOption

val c2 = Contact(
  name = "",
  address =
    Right(Address(street = "", town = "", country = "ca", postcode = "foobar")),
  phoneNumbers = Seq(
    PhoneNumber("+1", "11111111111", "ceo"),
    PhoneNumber("+01", "00000000", "sales")
  )
)

validateContact(c2).isValid
validateContact(c2).errorsOption

val c3 = Contact(
  name = "Alice",
  address =
    Left("1 Home Av. Daisytown CA"),
  email = Some("alice@home")
)

validateContact(c3).isValid
validateContact(c3).errorsOption

val c4 = Contact(
  name = "",
  address =
    Left(""),
  email = Some("alice.home")
)

validateContact(c4).isValid
validateContact(c4).errorsOption

```

All batteries included
---

```scala mdoc
import com.github.arturopala.validator.Validator._

case class E(a: Int, b: String, c: Option[Int], d: Seq[Int], e: Either[String,E], f: Option[Seq[Int]], g: Boolean, h: Option[String])

val divisibleByThree = checkIsTrue[Int](_ % 3 == 0, "must be divisible by three")

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
```

Usage
---

Create a simple validator:
```scala mdoc:silent
import com.github.arturopala.validator.Validator._

val validateIsEven: Validate[Int] = 
    checkIsTrue[Int](_ % 2 == 0, "must be even integer")

val validateIsNonEmpty: Validate[String] = 
    checkIsTrue[String](_.nonEmpty, "must be non-empty string") 

val validateStringLengthPair: Validate[(String,Int)] = 
    checkIsTrue[(String,Int)]({case (s,l) => s.length() == l}, "string must be of expected length")
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

Validate objects using `checkWith`, `checkIfSome`, `checkEach`, `checkEachIfSome`, etc.:
```scala mdoc:silent
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
        checkEachWithErrorPrefix(_.d, validateIsNonEmpty & checkIsTrue[String](_.lengthMax(64),"64 characters maximum"), 
        i => s".d[$i] "),
        checkWith[Foo,Bar](_.e, validateBar).withErrorPrefix(".e")
    )
).withErrorPrefixComputed(prefix)
```
```scala mdoc 
validateFoo(Foo("X678",Some(2),true,Seq("abc"),Bar(500,Some(Seq(8)))))
validateFoo(Foo("X67",Some(-1),true,Seq("abc",""),Bar(500,Some(Seq(7)))))
validateFoo(Foo("X678",Some(2),false,Seq("abc"),Bar(99,None)))
validateFoo(Foo("X",Some(3),false,Seq("abc",""),Bar(-1,Some(Seq(7,8,9)))))
```

Tag validator with prefix:
```scala mdoc
evenOrPositive.apply(-1).errorsSummaryOption
("prefix: " @: evenOrPositive).apply(-1).errorsSummaryOption
evenOrPositive.withErrorPrefix("foo_").apply(-1).errorsSummaryOption
evenOrPositive.withErrorPrefixComputed(i => s"($i) ").apply(-1).errorsSummaryOption
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
