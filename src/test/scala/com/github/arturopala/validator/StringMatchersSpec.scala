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

class StringMatchersSpec extends munit.ScalaCheckSuite {

  import Validator._

  test("StringMatchers.lengthMinMaxInclusive") {
    assert("a".lengthMinMaxInclusive(0, 1) == true)
    assert("a".lengthMinMaxInclusive(1, 2) == true)
    assert("a".lengthMinMaxInclusive(2, 3) == false)
    assert("a".lengthMinMaxInclusive(0, 3) == true)
    assert("abc".lengthMinMaxInclusive(0, 1) == false)
    assert("abc".lengthMinMaxInclusive(1, 2) == false)
    assert("abc".lengthMinMaxInclusive(2, 3) == true)
    assert("abc".lengthMinMaxInclusive(0, 3) == true)
  }

  test("StringMatchers.lengthMin") {
    assert("a".lengthMin(1) == true)
    assert("a".lengthMin(2) == false)
    assert("aa".lengthMin(2) == true)
    assert("aa".lengthMin(3) == false)
    assert("aaa".lengthMin(3) == true)
  }

  test("StringMatchers.lengthMax") {
    assert("a".lengthMax(1) == true)
    assert("a".lengthMax(2) == true)
    assert("aa".lengthMax(2) == true)
    assert("aa".lengthMax(3) == true)
    assert("aaa".lengthMax(3) == true)
    assert("aaa".lengthMax(2) == false)
    assert("aa".lengthMax(1) == false)
    assert("a".lengthMax(0) == false)
  }

  test("StringMatchers.isTrue") {
    def onlyDigits(s: String): Boolean = s.forall(_.isDigit)
    assert("a".isTrue(onlyDigits) == false)
    assert("a1a".isTrue(onlyDigits) == false)
    assert("1a2".isTrue(onlyDigits) == false)
    assert("a12".isTrue(onlyDigits) == false)
    assert("1".isTrue(onlyDigits) == true)
    assert("21".isTrue(onlyDigits) == true)
    assert("321".isTrue(onlyDigits) == true)
  }

  test("StringMatchers.isFalse") {
    def onlyDigits(s: String): Boolean = s.forall(_.isDigit)
    assert("a".isFalse(onlyDigits) == true)
    assert("a1a".isFalse(onlyDigits) == true)
    assert("1a2".isFalse(onlyDigits) == true)
    assert("a12".isFalse(onlyDigits) == true)
    assert("1".isFalse(onlyDigits) == false)
    assert("21".isFalse(onlyDigits) == false)
    assert("321".isFalse(onlyDigits) == false)
  }

}
