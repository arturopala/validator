![Build](https://github.com/arturopala/validator/workflows/Build/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/validator_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.arturopala/validator_2.13)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.5.0.svg)](https://www.scala-js.org)

Validator
===

This is a micro-library for Scala

    "com.github.arturopala" %% "validator" % "@VERSION@"

Cross-compiles to Scala versions @SUPPORTED_SCALA_VERSIONS@, 
and ScalaJS version `@SCALA_JS_VERSION@`, and ScalaNative version `@SCALA_NATIVE_VERSION@`.

Motivation
---

Usage
---

```scala mdoc
import com.github.arturopala.validator.Validator._

val validateIsEven: Validate[Int] = 
    check[Int](_ % 2 == 0, "must be even integer")

validateIsEven(2).isValid
validateIsEven(1).isInvalid
validateIsEven(1).errors

val validateIsPositive: Validate[Int] = 
    check[Int](_ > 0, "must be positive integer")
  
validateIsPositive(1).isValid  
validateIsPositive(-1).isInvalid    
validateIsPositive(-1).errors  
validateIsPositive(0).isInvalid 

val validateIsEvenAndPositive: Validate[Int] = 
    all(validateIsEven, validateIsPositive)

validateIsEvenAndPositive(2).isValid  
validateIsEvenAndPositive(1).isInvalid  
validateIsEvenAndPositive(1).errors  
validateIsEvenAndPositive(-1).isInvalid    
validateIsEvenAndPositive(-1).errors  
validateIsEvenAndPositive(0).isInvalid 

val validateIsEvenOrPositive: Validate[Int] = 
    any(validateIsEven, validateIsPositive)

validateIsEvenOrPositive(2).isValid   
validateIsEvenOrPositive(1).isValid   
validateIsEvenOrPositive(-1).isInvalid    
validateIsEvenOrPositive(-1).errors  
validateIsEvenOrPositive(0).isValid 
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
