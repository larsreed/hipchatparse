package no.mesan.hipchatparse.users

import akka.actor._
import akka.event.LoggingReceive
import no.mesan.hipchatparse.messages.MessageFilter
import no.mesan.hipchatparse.{TaskDone, ActorNames}
import no.mesan.hipchatparse.HipChatParseMain.CheckIfDone
import MessageFilter.FilterRoomContents
import no.mesan.hipchatparse.rooms.{Room, Message}
import no.mesan.hipchatparse.users.UserDb.{GetUsers, FoundUser, UserNotFound}
import no.mesan.hipchatparse.utils.NameHelper

/** Adds user information to messages. */
class UserFilter(master: ActorRef, userDb: ActorRef, formatter: ActorRef) extends Actor
  with ActorLogging with NameHelper {
  import no.mesan.hipchatparse.users.UserFilter.FilterRoomUser

  var roomName: String= _
  var msgList: List[Message]= _
  var missingIds: Set[String]= _
  var userMap = Map.empty[String, Option[User]]

  override def receive: Receive = LoggingReceive {
    case FilterRoomUser(room) =>
      roomName= room.name
      msgList= room.conversation
      missingIds= msgList.map(_.user.ID).toSet
      userDb ! GetUsers(missingIds.toList)
      context.become(waiting)
  }

  val doneWorking: Receive = LoggingReceive {
    case CheckIfDone => true
    case msg => log.warning(s"Too late, how come? $msg")
  }

  val waiting: Receive = LoggingReceive {
    case UserNotFound(id) =>
      missingIds = missingIds - id
      self ! CheckIfDone

    case FoundUser(id, user) =>
      missingIds = missingIds - id
      self ! CheckIfDone
      userMap = userMap + (id -> Some(user))

    case CheckIfDone =>
      if (missingIds.isEmpty) {
        val newList = for (msg <- msgList) yield
          if (userMap contains msg.user.ID) Message(userMap(msg.user.ID).get, msg.datestamp, msg.text)
          else msg
        val shortName= makeActorSuffix(roomName)
        val next = context.actorOf(MessageFilter.props(master, formatter), s"${ActorNames.messageFilter}-$shortName")
        next ! FilterRoomContents(Room(roomName, newList))
        master ! TaskDone(s"user filter for $roomName")
        context.become(doneWorking)
      }
  }
}

object UserFilter {
  /** Filter user contents. */
  case class FilterRoomUser(room: Room)

  /** "Constructor" */
  def props(master: ActorRef, userDb: ActorRef, formatter: ActorRef) =
    Props(new UserFilter(master, userDb, formatter))
}