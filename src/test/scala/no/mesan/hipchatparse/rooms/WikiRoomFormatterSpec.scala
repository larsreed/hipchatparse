package no.mesan.hipchatparse.rooms

import org.junit.runner.RunWith
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class WikiRoomFormatterSpec extends FlatSpec with Matchers {
  import WikiRoomFormatter._

  "WikiRoomFormatter" should "quote pipes" in {
      wash("| line | one |") shouldBe "\\| line \\| one \\|"
  }

  it should "quote code" in {
    wash("  /code some(a) { ... } ") shouldBe "{noformat}some(a) { ... } {noformat}"
  }

  it should "replace user mentions" in {
    wash("Hi, @larsr!") shouldBe "Hi, [~larsr]!"
  }
}
