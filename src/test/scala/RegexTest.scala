import org.scalacheck.Prop.forAll
import org.scalacheck.Gen
import org.scalatest.propspec.AnyPropSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class RegexSpec extends AnyPropSpec with ScalaCheckPropertyChecks with Matchers {
  // Generate simple regex patterns
  val simpleRegexGen = Gen.oneOf(
    "(a|b)+",
    "a*b+",
    "(ab)*",
    "a|b",
    "..."
  )

  property("a+ should match one or more a's") {
    forAll(Gen.nonEmptyListOf(Gen.const('a'))) { chars =>
      val str = chars.mkString
      Regex.fullMatch(str, "a+").getOrElse(false) shouldBe true
    }
  }

  property("invalid regex patterns should return Left") {
    forAll(Gen.oneOf("(", ")", "*+", "||")) { invalidPattern =>
      Regex.fullMatch("test", invalidPattern).isLeft shouldBe true
    }
  }

  property("empty string should not match non-empty pattern") {
    Regex.fullMatch("", "a+").getOrElse(false) shouldBe false
  }

  property("dot operator should match any character") {
    forAll(Gen.alphaChar) { char =>
      Regex.fullMatch(char.toString, ".").getOrElse(false) shouldBe true
    }
  }

  property("concatenation should match sequences") {
    Regex.fullMatch("abc", "a.b.c").getOrElse(false) shouldBe true
  }
}