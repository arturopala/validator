[![Build and test](https://github.com/arturopala/validator/actions/workflows/build.yml/badge.svg)](https://github.com/arturopala/validator/actions/workflows/build.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/validator_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/validator_2.13)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.7.0.svg)](https://www.scala-js.org)

Validator
===

This is a micro-library for Scala

    "com.github.arturopala" %% "validator" % "0.1.0"

Cross-compiles to Scala versions `2.13.6`, `2.12.15`, `3.1.0`, 
and ScalaJS version `1.7.0`, and ScalaNative version `0.4.0`.

Motivation
---

Usage
---

```scala
import com.github.arturopala.validator.Validator._

val validateIsEven: Validate[Int] = 
    check[Int](_ % 2 == 0, "must be even integer")
// validateIsEven: Int => cats.data.Validated[List[String], Unit] = com.github.arturopala.validator.Validator$$$Lambda$12271/776076516@18279340

validateIsEven(2).isValid
// res0: Boolean = true
validateIsEven(1).isInvalid
// res1: Boolean = true
validateIsEven(1).errors
// res2: Option[Seq[String]] = Some(value = List("must be even integer"))

val validateIsPositive: Validate[Int] = 
    check[Int](_ > 0, "must be positive integer")
// validateIsPositive: Int => cats.data.Validated[List[String], Unit] = com.github.arturopala.validator.Validator$$$Lambda$12271/776076516@2d49eada
  
validateIsPositive(1).isValid  
// res3: Boolean = true  
validateIsPositive(-1).isInvalid    
// res4: Boolean = true    
validateIsPositive(-1).errors  
// res5: Option[Seq[String]] = Some(value = List("must be positive integer"))  
validateIsPositive(0).isInvalid 
// res6: Boolean = true 

val validateIsEvenAndPositive: Validate[Int] = 
    all(validateIsEven, validateIsPositive)
// validateIsEvenAndPositive: Int => cats.data.Validated[List[String], Unit] = com.github.arturopala.validator.Validator$$$Lambda$12274/575287246@67e582ef

validateIsEvenAndPositive(2).isValid  
// res7: Boolean = true  
validateIsEvenAndPositive(1).isInvalid  
// res8: Boolean = true  
validateIsEvenAndPositive(1).errors  
// res9: Option[Seq[String]] = Some(value = List("must be even integer"))  
validateIsEvenAndPositive(-1).isInvalid    
// res10: Boolean = true    
validateIsEvenAndPositive(-1).errors  
// res11: Option[Seq[String]] = Some(
//   value = List("must be even integer", "must be positive integer")
// )  
validateIsEvenAndPositive(0).isInvalid 
// res12: Boolean = true 

val validateIsEvenOrPositive: Validate[Int] = 
    any(validateIsEven, validateIsPositive)
// validateIsEvenOrPositive: Int => cats.data.Validated[List[String], Unit] = com.github.arturopala.validator.Validator$$$Lambda$12279/1216505141@548db928

validateIsEvenOrPositive(2).isValid   
// res13: Boolean = true   
validateIsEvenOrPositive(1).isValid   
// res14: Boolean = true   
validateIsEvenOrPositive(-1).isInvalid    
// res15: Boolean = true    
validateIsEvenOrPositive(-1).errors  
// res16: Option[Seq[String]] = Some(
//   value = List("must be even integer", "must be positive integer")
// )  
validateIsEvenOrPositive(0).isValid 
// res17: Boolean = true
```

Development
---

Compile

    sbt compile

Compile for all Scala versions

    sbt +compile

Test

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

Github Actions
===

For a setup follow <https://github.com/olafurpg/sbt-ci-release/blob/main/readme.md>.

 - **build.yaml**: runs on every push or pull request, except for README.md
 - **release.yaml**: manual release of a new version
 - **publish.yaml**: builds and publishes artefacts in Sonatype repository
 - **site.yaml**: manual update of README and push of API docs to Github Pages

 Required secrets
 ---

- PAT (personal access token for new version release)
- PGP_PASSPHRASE
- PGP_SECRET
- SONATYPE_PASSWORD
- SONATYPE_USERNAME
