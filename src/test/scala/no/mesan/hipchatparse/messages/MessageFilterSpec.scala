package no.mesan.hipchatparse.messages

import no.mesan.hipchatparse.messages.MessageFilter._
import no.mesan.hipchatparse.rooms.{Message, Room}
import no.mesan.hipchatparse.users.User
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class MessageFilterSpec extends FlatSpec {

  "messageFilter" should "exclude welcome message" in {
      assert(!MessageFilter.okText("Hi @larsr! Welcome to Hipchat. You can @-mention me by typing @HipChat and I'll tell you what HipChat can do!"))
  }

  it should "exclude standard help texts" in {
    assert(!MessageFilter.okText("You can ask me about:<ul><li><i>video</i> - Call your teammates with HipChat Video"))
  }

  it should "accept other texts" in {
    assert(MessageFilter.okText("Hi @larsr! I'll tell you what HipChat can do!"))
  }

  it should "except empty strings" in {
    assert(!MessageFilter.okText("\t \t"))
  }

  it should "and cries for help" in {
    assert(!MessageFilter.okText(" @HipChat"))
  }

  it should "null repeated username" in {
    val testRoom= Room("Test", List(
    Message(User("ID1", Some("x"), "Name"), Some("2015-08-19T06:32:52"), "Text"),
    Message(User("ID1", Some("x"), "Name"), Some("2015-08-20T06:32:52"), "More text"),
    Message(User("ID2", Some("y"), "Name"), Some("2015-08-21T06:32:52"), "Another text")))
    val res= filterDuplicates(testRoom.conversation)
    assert(res.size===3)
    //noinspection ZeroIndexToHead
    assert(res(0).user.ID==="ID1")
    assert(res(1).user.ID==="")
    assert(res(2).user.ID==="ID2")
  }

  it should "null repeated date" in {
    val testRoom= Room("Test", List(
    Message(User("ID1", Some("x"), "Name"), Some("2015-08-20T06:32:52"), "Text"),
    Message(User("ID2", Some("x"), "Name"), Some("2015-08-21T06:32:52"), "More text"),
    Message(User("ID3", Some("y"), "Name"), Some("2015-08-21T06:32:55"), "Another text")))
    val res= filterDuplicates(testRoom.conversation)
    assert(res.size===3)
    //noinspection ZeroIndexToHead
    assert(res(0).dateString==="2015-08-20")
    assert(res(1).dateString==="2015-08-21")
    assert(res(2).dateString==="")
  }

}
