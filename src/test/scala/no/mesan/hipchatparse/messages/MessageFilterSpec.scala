package no.mesan.hipchatparse.messages

import no.mesan.hipchatparse.messages.MessageFilter._
import no.mesan.hipchatparse.rooms.Room
import no.mesan.hipchatparse.users.User
import org.junit.runner.RunWith
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class MessageFilterSpec extends FlatSpec with Matchers {
  import MessageFilter._

  "messageFilter" should "exclude welcome message" in {
      okText("Hi @larsr! Welcome to Hipchat. You can @-mention me by typing @HipChat and I'll tell you what HipChat can do!") shouldBe false
  }

  it should "exclude standard help texts" in {
    okText("You can ask me about:<ul><li><i>video</i> - Call your teammates with HipChat Video") shouldBe false
  }

  it should "accept other texts" in {
    okText("Hi @larsr! I'll tell you what HipChat can do!") shouldBe true
  }

  it should "except empty strings" in {
    okText("\t \t") shouldBe false
  }

  it should "and cries for help" in {
    okText(" @HipChat") shouldBe false
  }

  it should "null repeated username" in {
    val testRoom= Room("Test", None, List(
    Message(User("ID1", Some("x"), "Name"), Some("2015-08-19T06:32:52"), "Text"),
    Message(User("ID1", Some("x"), "Name"), Some("2015-08-20T06:32:52"), "More text"),
    Message(User("ID2", Some("y"), "Name"), Some("2015-08-21T06:32:52"), "Another text")))
    val result= filterDuplicates(testRoom.conversation)
    result should have length 3
    result.map(_.user.ID) should equal (List("ID1", "", "ID2"))
  }

  it should "null repeated date" in {
    val testRoom= Room("Test", None, List(
    Message(User("ID1", Some("x"), "Name"), Some("2015-08-20T06:32:52"), "Text"),
    Message(User("ID2", Some("x"), "Name"), Some("2015-08-21T06:32:52"), "More text"),
    Message(User("ID3", Some("y"), "Name"), Some("2015-08-21T06:32:55"), "Another text")))

    val result= filterDuplicates(testRoom.conversation)
    result should have length 3
    result.map(_.dateString) should equal (List("2015-08-20", "2015-08-21", ""))
  }
}
