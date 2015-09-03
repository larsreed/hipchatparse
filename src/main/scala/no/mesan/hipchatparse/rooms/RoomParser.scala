package no.mesan.hipchatparse.rooms

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import no.mesan.hipchatparse.HipChatConfig._
import no.mesan.hipchatparse.messages.Message
import no.mesan.hipchatparse.roomlist.RoomFilter.FilterRoom
import no.mesan.hipchatparse.users.User
import no.mesan.hipchatparse.utils.NameHelper
import no.mesan.hipchatparse.{RoomDone, TaskDone}
import play.api.libs.json.{JsValue, Json}

import scala.util.{Failure, Success, Try}

/** Interprets room file contents. */
class RoomParser(master: ActorRef, filter: ActorRef) extends Actor
  with ActorLogging with NameHelper {
  import no.mesan.hipchatparse.rooms.RoomParser.MakeRoom

  override def receive: Receive = LoggingReceive {
    case MakeRoom(name, list) =>
      val parsed= list.map(s=> RoomParser.jsonParse(s))
      parsed.filter(_.isFailure).foreach(err=> err.failed.map(log.error(_, s"cannot parse $name")))
      val res= parsed
        .filter(_.isSuccess)
        .foldLeft(List.empty[Message])((lst, msg)=> lst ++ msg.get)
      if (res.isEmpty) {
        log.warning(s"$name has no content")
        master ! RoomDone(name, 0)
      }
      else {
        filter ! FilterRoom(Room(name, None, res))
      }
      master ! TaskDone(s"parsing room $name")
  }
}

object RoomParser extends NameHelper {
  /** Parse room data. */
  case class MakeRoom(roomName: String, conversation: List[String])

  /** "Constructor" */
  def props(master: ActorRef, filter: ActorRef) = Props(new RoomParser(master, filter))


  /** Make a list of messages from the JSON contents. */ //noinspection ZeroIndexToHead
  def jsonParse(contents: String): Try[List[Message]] = {
    try {
      val json: JsValue = Json.parse(contents)
      val dates= (json \\ dateRoomKey ).map(v=> washJson(v.toString().substring(0, 21)))
      val from= json \\ fromRoomKey
      val authorNames= from.map(v => washJson((v \ nameRoomKey).get.toString()))
      val authorIds= from.map(v => (v \ userIdRoomKey).get.toString())
      val messages= (json \\ messageRoomKey).map(v=> washJson(v.toString()))
      Success(List(dates, authorIds, authorNames, messages).transpose.map {
        v => Message(User(ID=v(1), fullName=v(2)), Some(v(0)), v(3))
      })
    }
    catch {
      case e:Throwable => Failure(e)
    }
  }
}