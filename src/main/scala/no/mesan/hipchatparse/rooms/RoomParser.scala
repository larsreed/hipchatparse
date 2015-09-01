package no.mesan.hipchatparse.rooms

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import no.mesan.hipchatparse.{TaskDone, ActorNames, HipChatConfig}
import no.mesan.hipchatparse.rooms.RoomWriter.RoomDone
import HipChatConfig._
import no.mesan.hipchatparse.users.UserFilter.FilterRoomUser
import no.mesan.hipchatparse.users.{User, UserFilter}
import no.mesan.hipchatparse.utils.NameHelper
import play.api.libs.json.{JsValue, Json}

import scala.util.{Failure, Success, Try}

/** Interprets room file contents. */
class RoomParser(master: ActorRef, userDb: ActorRef, formatter: ActorRef) extends Actor
  with ActorLogging with NameHelper {
  import no.mesan.hipchatparse.rooms.RoomParser.MakeRoom

  override def receive: Receive = LoggingReceive {
    case MakeRoom(name, list) =>
      val shortName= makeActorSuffix(name)
      val filter= context.actorOf(UserFilter.props(master, userDb, formatter), s"${ActorNames.userFilter}-$shortName")
      var res= List.empty[Message]
      for (text <- list) {
        val parsed= RoomParser.jsonParse(text)
        if (parsed.isFailure) {
          val err= s"cannot parse $name -- " // $text
          parsed.failed.map(log.error(_, err))
        }
        else res = res ++ parsed.get
      }
      if (res.isEmpty) {
        log.warning(s"$name has no content")
        master ! RoomDone(name, 0)
      }
      else {
        filter ! FilterRoomUser(Room(name, res))
      }
      master ! TaskDone(s"parsing room $name")
  }
}

object RoomParser  {
  /** Parse room data. */
  case class MakeRoom(roomName: String, conversation: List[String])

  /** "Constructor" */
  def props(master: ActorRef, userDb: ActorRef, formatter: ActorRef) =
    Props(new RoomParser(master, userDb, formatter))

  private def unString(s: String)= s.substring(1).replaceFirst(".$", "")


  /** Make a list of messages from the JSON contents. */
  def jsonParse(contents: String): Try[List[Message]] = {
    try {
      val json: JsValue = Json.parse(contents)
      val dates= (json \\ dateRoomKey ).map(v=> unString(v.toString().substring(0, 21)))
      val from= json \\ fromRoomKey
      val authorNames= from.map(v => unString((v \ nameRoomKey).get.toString()))
      val authorIds= from.map(v => (v \ userIdRoomKey).get.toString())
      val messages= (json \\ messageRoomKey).map(v=> unString(v.toString()))
      Success(List(dates, authorIds, authorNames, messages).transpose.map {
        v => //noinspection ZeroIndexToHead
          Message (User(ID=v(1), fullName=v(2)), Some(v(0)), v(3))
      })
    }
    catch {
      case e:Throwable =>
        Failure(e)
    }
  }
}