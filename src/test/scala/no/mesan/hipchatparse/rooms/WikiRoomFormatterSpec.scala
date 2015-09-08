package no.mesan.hipchatparse.rooms

import akka.event.LoggingAdapter
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class WikiRoomFormatterSpec extends FlatSpec with Matchers {
  import WikiRoomFormatter._

  val mockLog= Mockito.mock(classOf[LoggingAdapter])

  "WikiRoomFormatter" should "quote pipes" in {
      wash("| line | one |", mockLog) shouldBe "\\| line \\| one \\|"
  }

  it should "quote code" in {
    wash("  /code some(a) { ... } ", mockLog) shouldBe "{noformat}some(a) { ... } {noformat}"
  }

  it should "replace user mentions" in {
    wash("Hi, @larsr!", mockLog) shouldBe "Hi, [~larsr]!"
  }
}
