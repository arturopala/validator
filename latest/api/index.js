Index.PACKAGES = {"com" : [], "com.github" : [], "com.github.arturopala" : [], "com.github.arturopala.validator" : [{"name" : "com.github.arturopala.validator.Validator", "shortDescription" : "Simpler validator abstraction using Cats Validated https:\/\/typelevel.org\/cats\/datatypes\/validated.html.", "object" : "com\/github\/arturopala\/validator\/Validator$.html", "members_object" : [{"label" : "validateCollectionNonEmpty", "tail" : "(errorMessage: String): (Iterable[_]) => Result", "member" : "com.github.arturopala.validator.Validator.validateCollectionNonEmpty", "link" : "com\/github\/arturopala\/validator\/Validator$.html#validateCollectionNonEmpty(errorMessage:String):Iterable[_]=>com.github.arturopala.validator.Validator.Result", "kind" : "def"}, {"label" : "validateStringNonEmpty", "tail" : "(errorMessage: String): Validate[String]", "member" : "com.github.arturopala.validator.Validator.validateStringNonEmpty", "link" : "com\/github\/arturopala\/validator\/Validator$.html#validateStringNonEmpty(errorMessage:String):com.github.arturopala.validator.Validator.Validate[String]", "kind" : "def"}, {"label" : "Or", "tail" : "", "member" : "com.github.arturopala.validator.Validator.Or", "link" : "com\/github\/arturopala\/validator\/Validator$.html#OrextendsValidator.ErrorwithProductwithSerializable", "kind" : "final case class"}, {"label" : "And", "tail" : "", "member" : "com.github.arturopala.validator.Validator.And", "link" : "com\/github\/arturopala\/validator\/Validator$.html#AndextendsValidator.ErrorwithProductwithSerializable", "kind" : "final case class"}, {"label" : "Single", "tail" : "", "member" : "com.github.arturopala.validator.Validator.Single", "link" : "com\/github\/arturopala\/validator\/Validator$.html#SingleextendsValidator.ErrorwithProductwithSerializable", "kind" : "final case class"}, {"label" : "ValidationResultOps", "tail" : "", "member" : "com.github.arturopala.validator.Validator.ValidationResultOps", "link" : "com\/github\/arturopala\/validator\/Validator$.html#ValidationResultOpsextendsAnyRef", "kind" : "implicit final class"}, {"label" : "ValidateOps", "tail" : "", "member" : "com.github.arturopala.validator.Validator.ValidateOps", "link" : "com\/github\/arturopala\/validator\/Validator$.html#ValidateOps[T]extendsAnyRef", "kind" : "implicit final class"}, {"label" : "BooleanOps", "tail" : "", "member" : "com.github.arturopala.validator.Validator.BooleanOps", "link" : "com\/github\/arturopala\/validator\/Validator$.html#BooleanOpsextendsAnyVal", "kind" : "implicit final class"}, {"label" : "OptionalBigDecimalMatchers", "tail" : "", "member" : "com.github.arturopala.validator.Validator.OptionalBigDecimalMatchers", "link" : "com\/github\/arturopala\/validator\/Validator$.html#OptionalBigDecimalMatchersextendsAnyVal", "kind" : "implicit final class"}, {"label" : "BigDecimalMatchers", "tail" : "", "member" : "com.github.arturopala.validator.Validator.BigDecimalMatchers", "link" : "com\/github\/arturopala\/validator\/Validator$.html#BigDecimalMatchersextendsAnyVal", "kind" : "implicit final class"}, {"label" : "OptionalIntMatchers", "tail" : "", "member" : "com.github.arturopala.validator.Validator.OptionalIntMatchers", "link" : "com\/github\/arturopala\/validator\/Validator$.html#OptionalIntMatchersextendsAnyVal", "kind" : "implicit final class"}, {"label" : "IntMatchers", "tail" : "", "member" : "com.github.arturopala.validator.Validator.IntMatchers", "link" : "com\/github\/arturopala\/validator\/Validator$.html#IntMatchersextendsAnyVal", "kind" : "implicit final class"}, {"label" : "OptionalStringMatchers", "tail" : "", "member" : "com.github.arturopala.validator.Validator.OptionalStringMatchers", "link" : "com\/github\/arturopala\/validator\/Validator$.html#OptionalStringMatchersextendsAnyVal", "kind" : "implicit final class"}, {"label" : "StringMatchers", "tail" : "", "member" : "com.github.arturopala.validator.Validator.StringMatchers", "link" : "com\/github\/arturopala\/validator\/Validator$.html#StringMatchersextendsAnyVal", "kind" : "implicit final class"}, {"label" : "checkIfOnlyOneSetIsTrue", "tail" : "(tests: Seq[Set[(T) => Boolean]], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfOnlyOneSetIsTrue", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfOnlyOneSetIsTrue[T](tests:Seq[Set[T=>Boolean]],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfOnlyOneSetIsDefined", "tail" : "(extractors: Seq[Set[(T) => Option[Any]]], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfOnlyOneSetIsDefined", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfOnlyOneSetIsDefined[T](extractors:Seq[Set[T=>Option[Any]]],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfOnlyOneIsTrue", "tail" : "(tests: Seq[(T) => Boolean], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfOnlyOneIsTrue", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfOnlyOneIsTrue[T](tests:Seq[T=>Boolean],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfOnlyOneIsDefined", "tail" : "(extractors: Seq[(T) => Option[Any]], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfOnlyOneIsDefined", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfOnlyOneIsDefined[T](extractors:Seq[T=>Option[Any]],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfAtMostOneIsTrue", "tail" : "(tests: Seq[(T) => Boolean], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfAtMostOneIsTrue", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfAtMostOneIsTrue[T](tests:Seq[T=>Boolean],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfAtMostOneIsDefined", "tail" : "(extractors: Seq[(T) => Option[Any]], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfAtMostOneIsDefined", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfAtMostOneIsDefined[T](extractors:Seq[T=>Option[Any]],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfAtLeastOneIsTrue", "tail" : "(tests: Seq[(T) => Boolean], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfAtLeastOneIsTrue", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfAtLeastOneIsTrue[T](tests:Seq[T=>Boolean],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfAtLeastOneIsDefined", "tail" : "(extractors: Seq[(T) => Option[Any]], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfAtLeastOneIsDefined", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfAtLeastOneIsDefined[T](extractors:Seq[T=>Option[Any]],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfAllFalse", "tail" : "(tests: Seq[(T) => Boolean], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfAllFalse", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfAllFalse[T](tests:Seq[T=>Boolean],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfAllTrue", "tail" : "(tests: Seq[(T) => Boolean], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfAllTrue", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfAllTrue[T](tests:Seq[T=>Boolean],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfAllOrNoneDefined", "tail" : "(extractors: Seq[(T) => Option[Any]], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfAllOrNoneDefined", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfAllOrNoneDefined[T](extractors:Seq[T=>Option[Any]],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfAllEmpty", "tail" : "(extractors: Seq[(T) => Option[Any]], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfAllEmpty", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfAllEmpty[T](extractors:Seq[T=>Option[Any]],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfAllDefined", "tail" : "(extractors: Seq[(T) => Option[Any]], expectations: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfAllDefined", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfAllDefined[T](extractors:Seq[T=>Option[Any]],expectations:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkEachIfSomeWithErrorPrefix", "tail" : "(extract: (T) => Option[Seq[E]], constraint: Validate[E], errorPrefix: (Int) => String, isValidIfNone: Boolean): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkEachIfSomeWithErrorPrefix", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkEachIfSomeWithErrorPrefix[T,E](extract:T=>Option[Seq[E]],constraint:com.github.arturopala.validator.Validator.Validate[E],errorPrefix:Int=>String,isValidIfNone:Boolean):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkEachIfSome", "tail" : "(extract: (T) => Option[Seq[E]], constraint: Validate[E], isValidIfNone: Boolean): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkEachIfSome", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkEachIfSome[T,E](extract:T=>Option[Seq[E]],constraint:com.github.arturopala.validator.Validator.Validate[E],isValidIfNone:Boolean):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkEachIfNonEmptyWithErrorPrefix", "tail" : "(elements: (T) => Seq[E], constraint: Validate[E], errorPrefix: (Int) => String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkEachIfNonEmptyWithErrorPrefix", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkEachIfNonEmptyWithErrorPrefix[T,E](elements:T=>Seq[E],constraint:com.github.arturopala.validator.Validator.Validate[E],errorPrefix:Int=>String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkEachIfNonEmpty", "tail" : "(elements: (T) => Seq[E], constraint: Validate[E]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkEachIfNonEmpty", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkEachIfNonEmpty[T,E](elements:T=>Seq[E],constraint:com.github.arturopala.validator.Validator.Validate[E]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkEachWithErrorPrefix", "tail" : "(elements: (T) => Seq[E], constraint: Validate[E], errorPrefix: (Int) => String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkEachWithErrorPrefix", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkEachWithErrorPrefix[T,E](elements:T=>Seq[E],constraint:com.github.arturopala.validator.Validator.Validate[E],errorPrefix:Int=>String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkEach", "tail" : "(elements: (T) => Seq[E], constraint: Validate[E]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkEach", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkEach[T,E](elements:T=>Seq[E],constraint:com.github.arturopala.validator.Validator.Validate[E]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkEither", "tail" : "(element: (T) => Either[L, R], constraintLeft: Validate[L], constraintRight: Validate[R]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkEither", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkEither[T,L,R](element:T=>Either[L,R],constraintLeft:com.github.arturopala.validator.Validator.Validate[L],constraintRight:com.github.arturopala.validator.Validator.Validate[R]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIfSome", "tail" : "(element: (T) => Option[E], constraint: Validate[E], isValidIfNone: Boolean): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIfSome", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIfSome[T,E](element:T=>Option[E],constraint:com.github.arturopala.validator.Validator.Validate[E],isValidIfNone:Boolean):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIsEmpty", "tail" : "(test: (T) => Option[Any], error: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIsEmpty", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIsEmpty[T](test:T=>Option[Any],error:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIsDefined", "tail" : "(test: (T) => Option[Any], error: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIsDefined", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIsDefined[T](test:T=>Option[Any],error:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkFromEither", "tail" : "(test: (T) => Either[String, Any]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkFromEither", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkFromEither[T](test:T=>Either[String,Any]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkNotEquals", "tail" : "(value1: (T) => A, value2: (T) => A, error: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkNotEquals", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkNotEquals[T,A](value1:T=>A,value2:T=>A,error:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkEquals", "tail" : "(value1: (T) => A, value2: (T) => A, error: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkEquals", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkEquals[T,A](value1:T=>A,value2:T=>A,error:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkWith", "tail" : "(element: (T) => E, constraint: Validate[E]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkWith", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkWith[T,E](element:T=>E,constraint:com.github.arturopala.validator.Validator.Validate[E]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkProp", "tail" : "(element: (T) => E, constraint: Validate[E]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkProp", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkProp[T,E](element:T=>E,constraint:com.github.arturopala.validator.Validator.Validate[E]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkWithImplicitly", "tail" : "(element: (T) => E)(constraint: Validate[E]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkWithImplicitly", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkWithImplicitly[T,E](element:T=>E)(implicitconstraint:com.github.arturopala.validator.Validator.Validate[E]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIsFalse", "tail" : "(test: (T) => Boolean, error: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIsFalse", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIsFalse[T](test:T=>Boolean,error:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "checkIsTrue", "tail" : "(test: (T) => Boolean, error: String): Validate[T]", "member" : "com.github.arturopala.validator.Validator.checkIsTrue", "link" : "com\/github\/arturopala\/validator\/Validator$.html#checkIsTrue[T](test:T=>Boolean,error:String):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "product", "tail" : "(constraintA: Validate[A], constraintB: Validate[B], constraintC: Validate[C], constraintD: Validate[D]): Validate[(A, B, C, D)]", "member" : "com.github.arturopala.validator.Validator.product", "link" : "com\/github\/arturopala\/validator\/Validator$.html#product[A,B,C,D](constraintA:com.github.arturopala.validator.Validator.Validate[A],constraintB:com.github.arturopala.validator.Validator.Validate[B],constraintC:com.github.arturopala.validator.Validator.Validate[C],constraintD:com.github.arturopala.validator.Validator.Validate[D]):com.github.arturopala.validator.Validator.Validate[(A,B,C,D)]", "kind" : "def"}, {"label" : "product", "tail" : "(constraintA: Validate[A], constraintB: Validate[B], constraintC: Validate[C]): Validate[(A, B, C)]", "member" : "com.github.arturopala.validator.Validator.product", "link" : "com\/github\/arturopala\/validator\/Validator$.html#product[A,B,C](constraintA:com.github.arturopala.validator.Validator.Validate[A],constraintB:com.github.arturopala.validator.Validator.Validate[B],constraintC:com.github.arturopala.validator.Validator.Validate[C]):com.github.arturopala.validator.Validator.Validate[(A,B,C)]", "kind" : "def"}, {"label" : "product", "tail" : "(constraintA: Validate[A], constraintB: Validate[B]): Validate[(A, B)]", "member" : "com.github.arturopala.validator.Validator.product", "link" : "com\/github\/arturopala\/validator\/Validator$.html#product[A,B](constraintA:com.github.arturopala.validator.Validator.Validate[A],constraintB:com.github.arturopala.validator.Validator.Validate[B]):com.github.arturopala.validator.Validator.Validate[(A,B)]", "kind" : "def"}, {"label" : "whenInvalid", "tail" : "(guardConstraint: Validate[T], constraintWhenInvalid: Validate[T]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.whenInvalid", "link" : "com\/github\/arturopala\/validator\/Validator$.html#whenInvalid[T](guardConstraint:com.github.arturopala.validator.Validator.Validate[T],constraintWhenInvalid:com.github.arturopala.validator.Validator.Validate[T]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "whenValid", "tail" : "(guardConstraint: Validate[T], constraintWhenValid: Validate[T]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.whenValid", "link" : "com\/github\/arturopala\/validator\/Validator$.html#whenValid[T](guardConstraint:com.github.arturopala.validator.Validator.Validate[T],constraintWhenValid:com.github.arturopala.validator.Validator.Validate[T]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "when", "tail" : "(guardConstraint: Validate[T], constraintWhenValid: Validate[T], constraintWhenInvalid: Validate[T]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.when", "link" : "com\/github\/arturopala\/validator\/Validator$.html#when[T](guardConstraint:com.github.arturopala.validator.Validator.Validate[T],constraintWhenValid:com.github.arturopala.validator.Validator.Validate[T],constraintWhenInvalid:com.github.arturopala.validator.Validator.Validate[T]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "whenFalse", "tail" : "(test: (T) => Boolean, constraintWhenFalse: Validate[T]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.whenFalse", "link" : "com\/github\/arturopala\/validator\/Validator$.html#whenFalse[T](test:T=>Boolean,constraintWhenFalse:com.github.arturopala.validator.Validator.Validate[T]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "whenTrue", "tail" : "(test: (T) => Boolean, constraintWhenTrue: Validate[T]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.whenTrue", "link" : "com\/github\/arturopala\/validator\/Validator$.html#whenTrue[T](test:T=>Boolean,constraintWhenTrue:com.github.arturopala.validator.Validator.Validate[T]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "conditionally", "tail" : "(test: (T) => Boolean, constraintWhenTrue: Validate[T], constraintWhenFalse: Validate[T]): Validate[T]", "member" : "com.github.arturopala.validator.Validator.conditionally", "link" : "com\/github\/arturopala\/validator\/Validator$.html#conditionally[T](test:T=>Boolean,constraintWhenTrue:com.github.arturopala.validator.Validator.Validate[T],constraintWhenFalse:com.github.arturopala.validator.Validator.Validate[T]):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "anyWithComputedPrefix", "tail" : "(errorPrefix: (T) => String, constraints: Validate[T]*): Validate[T]", "member" : "com.github.arturopala.validator.Validator.anyWithComputedPrefix", "link" : "com\/github\/arturopala\/validator\/Validator$.html#anyWithComputedPrefix[T](errorPrefix:T=>String,constraints:com.github.arturopala.validator.Validator.Validate[T]*):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "anyWithPrefix", "tail" : "(errorPrefix: String, constraints: Validate[T]*): Validate[T]", "member" : "com.github.arturopala.validator.Validator.anyWithPrefix", "link" : "com\/github\/arturopala\/validator\/Validator$.html#anyWithPrefix[T](errorPrefix:String,constraints:com.github.arturopala.validator.Validator.Validate[T]*):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "any", "tail" : "(constraints: Validate[T]*): Validate[T]", "member" : "com.github.arturopala.validator.Validator.any", "link" : "com\/github\/arturopala\/validator\/Validator$.html#any[T](constraints:com.github.arturopala.validator.Validator.Validate[T]*):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "allWithComputedPrefix", "tail" : "(errorPrefix: (T) => String, constraints: Validate[T]*): Validate[T]", "member" : "com.github.arturopala.validator.Validator.allWithComputedPrefix", "link" : "com\/github\/arturopala\/validator\/Validator$.html#allWithComputedPrefix[T](errorPrefix:T=>String,constraints:com.github.arturopala.validator.Validator.Validate[T]*):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "allWithPrefix", "tail" : "(errorPrefix: String, constraints: Validate[T]*): Validate[T]", "member" : "com.github.arturopala.validator.Validator.allWithPrefix", "link" : "com\/github\/arturopala\/validator\/Validator$.html#allWithPrefix[T](errorPrefix:String,constraints:com.github.arturopala.validator.Validator.Validate[T]*):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "allWithShortCircuit", "tail" : "(constraints: Validate[T]*): Validate[T]", "member" : "com.github.arturopala.validator.Validator.allWithShortCircuit", "link" : "com\/github\/arturopala\/validator\/Validator$.html#allWithShortCircuit[T](constraints:com.github.arturopala.validator.Validator.Validate[T]*):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "all", "tail" : "(constraints: Validate[T]*): Validate[T]", "member" : "com.github.arturopala.validator.Validator.all", "link" : "com\/github\/arturopala\/validator\/Validator$.html#all[T](constraints:com.github.arturopala.validator.Validator.Validate[T]*):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "never", "tail" : "(): Validate[T]", "member" : "com.github.arturopala.validator.Validator.never", "link" : "com\/github\/arturopala\/validator\/Validator$.html#never[T]:com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "always", "tail" : "(): Validate[T]", "member" : "com.github.arturopala.validator.Validator.always", "link" : "com\/github\/arturopala\/validator\/Validator$.html#always[T]:com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "validate", "tail" : "(constraints: Validate[T]*): Validate[T]", "member" : "com.github.arturopala.validator.Validator.validate", "link" : "com\/github\/arturopala\/validator\/Validator$.html#validate[T](constraints:com.github.arturopala.validator.Validator.Validate[T]*):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "apply", "tail" : "(constraints: Validate[T]*): Validate[T]", "member" : "com.github.arturopala.validator.Validator.apply", "link" : "com\/github\/arturopala\/validator\/Validator$.html#apply[T](constraints:com.github.arturopala.validator.Validator.Validate[T]*):com.github.arturopala.validator.Validator.Validate[T]", "kind" : "def"}, {"label" : "Valid", "tail" : ": Right[Nothing, Unit]", "member" : "com.github.arturopala.validator.Validator.Valid", "link" : "com\/github\/arturopala\/validator\/Validator$.html#Valid:scala.util.Right[Nothing,Unit]", "kind" : "val"}, {"label" : "Invalid", "tail" : "", "member" : "com.github.arturopala.validator.Validator.Invalid", "link" : "com\/github\/arturopala\/validator\/Validator$.html#Invalid", "kind" : "object"}, {"label" : "Validate", "tail" : "", "member" : "com.github.arturopala.validator.Validator.Validate", "link" : "com\/github\/arturopala\/validator\/Validator$.html#Validate[-T]=T=>com.github.arturopala.validator.Validator.Result", "kind" : "type"}, {"label" : "Result", "tail" : "", "member" : "com.github.arturopala.validator.Validator.Result", "link" : "com\/github\/arturopala\/validator\/Validator$.html#Result=Either[com.github.arturopala.validator.Validator.Error,Unit]", "kind" : "type"}, {"label" : "Error", "tail" : "", "member" : "com.github.arturopala.validator.Validator.Error", "link" : "com\/github\/arturopala\/validator\/Validator$.html#Error", "kind" : "object"}, {"label" : "Error", "tail" : "", "member" : "com.github.arturopala.validator.Validator.Error", "link" : "com\/github\/arturopala\/validator\/Validator$.html#ErrorextendsAnyRef", "kind" : "sealed trait"}, {"label" : "synchronized", "tail" : "(arg0: => T0): T0", "member" : "scala.AnyRef.synchronized", "link" : "com\/github\/arturopala\/validator\/Validator$.html#synchronized[T0](x$1:=>T0):T0", "kind" : "final def"}, {"label" : "##", "tail" : "(): Int", "member" : "scala.AnyRef.##", "link" : "com\/github\/arturopala\/validator\/Validator$.html###:Int", "kind" : "final def"}, {"label" : "!=", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.!=", "link" : "com\/github\/arturopala\/validator\/Validator$.html#!=(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "==", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.==", "link" : "com\/github\/arturopala\/validator\/Validator$.html#==(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "ne", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.ne", "link" : "com\/github\/arturopala\/validator\/Validator$.html#ne(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "eq", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.eq", "link" : "com\/github\/arturopala\/validator\/Validator$.html#eq(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "finalize", "tail" : "(): Unit", "member" : "scala.AnyRef.finalize", "link" : "com\/github\/arturopala\/validator\/Validator$.html#finalize():Unit", "kind" : "def"}, {"label" : "wait", "tail" : "(): Unit", "member" : "scala.AnyRef.wait", "link" : "com\/github\/arturopala\/validator\/Validator$.html#wait():Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long, arg1: Int): Unit", "member" : "scala.AnyRef.wait", "link" : "com\/github\/arturopala\/validator\/Validator$.html#wait(x$1:Long,x$2:Int):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long): Unit", "member" : "scala.AnyRef.wait", "link" : "com\/github\/arturopala\/validator\/Validator$.html#wait(x$1:Long):Unit", "kind" : "final def"}, {"label" : "notifyAll", "tail" : "(): Unit", "member" : "scala.AnyRef.notifyAll", "link" : "com\/github\/arturopala\/validator\/Validator$.html#notifyAll():Unit", "kind" : "final def"}, {"label" : "notify", "tail" : "(): Unit", "member" : "scala.AnyRef.notify", "link" : "com\/github\/arturopala\/validator\/Validator$.html#notify():Unit", "kind" : "final def"}, {"label" : "toString", "tail" : "(): String", "member" : "scala.AnyRef.toString", "link" : "com\/github\/arturopala\/validator\/Validator$.html#toString():String", "kind" : "def"}, {"label" : "clone", "tail" : "(): AnyRef", "member" : "scala.AnyRef.clone", "link" : "com\/github\/arturopala\/validator\/Validator$.html#clone():Object", "kind" : "def"}, {"label" : "equals", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.equals", "link" : "com\/github\/arturopala\/validator\/Validator$.html#equals(x$1:Object):Boolean", "kind" : "def"}, {"label" : "hashCode", "tail" : "(): Int", "member" : "scala.AnyRef.hashCode", "link" : "com\/github\/arturopala\/validator\/Validator$.html#hashCode():Int", "kind" : "def"}, {"label" : "getClass", "tail" : "(): Class[_ <: AnyRef]", "member" : "scala.AnyRef.getClass", "link" : "com\/github\/arturopala\/validator\/Validator$.html#getClass():Class[_]", "kind" : "final def"}, {"label" : "asInstanceOf", "tail" : "(): T0", "member" : "scala.Any.asInstanceOf", "link" : "com\/github\/arturopala\/validator\/Validator$.html#asInstanceOf[T0]:T0", "kind" : "final def"}, {"label" : "isInstanceOf", "tail" : "(): Boolean", "member" : "scala.Any.isInstanceOf", "link" : "com\/github\/arturopala\/validator\/Validator$.html#isInstanceOf[T0]:Boolean", "kind" : "final def"}], "kind" : "object"}]};