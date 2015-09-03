package no.mesan.hipchatparse.users

import akka.actor._
import akka.event.LoggingReceive
import no.mesan.hipchatparse.messages.{Message, MessageFilter}
import no.mesan.hipchatparse.{TaskDone, ActorNames}
import no.mesan.hipchatparse.HipChatParseMain.CheckIfDone
import MessageFilter.FilterRoom
import no.mesan.hipchatparse.rooms.Room
import no.mesan.hipchatparse.users.UserDb.{GetUsers, FoundUser, UserNotFound}
import no.mesan.hipchatparse.utils.NameHelper

/** Adds room information to messages. */
class UserFilter(master: ActorRef, userDb: ActorRef, formatter: ActorRef) extends Actor
  with ActorLogging with NameHelper {
  import UserFilter.FilterRoomUser

  var currentRoom: Room= _
  var missingIds: Set[String]= _
  var userMap = Map.empty[String, Option[User]]

  override def receive: Receive = LoggingReceive {
    case FilterRoomUser(room) =>
      currentRoom= room
      missingIds= room.conversation.map(_.user.ID).toSet
      userDb ! GetUsers(missingIds.toList)
      context become waiting
  }

  val doneWorking: Receive = LoggingReceive {
    case CheckIfDone => Unit
    case msg => log.warning(s"Too late, how come? $msg")
  }

  val waiting: Receive = LoggingReceive {
    case UserNotFound(id) =>
      missingIds = missingIds - id
      self ! CheckIfDone

    case FoundUser(id, user) =>
      missingIds = missingIds - id
      userMap = userMap + (id -> Some(user))
      self ! CheckIfDone

    case CheckIfDone =>
      if (missingIds.isEmpty) {
        val newList = for (msg <- currentRoom.conversation) yield
          if (userMap contains msg.user.ID) Message(userMap(msg.user.ID).get, msg.datestamp, msg.text)
          else msg
        val shortName= makeActorSuffix(currentRoom.name)
        val next = context.actorOf(MessageFilter.props(master, formatter), s"${ActorNames.messageFilter}-$shortName")
        next ! FilterRoom(currentRoom withConversation newList)
        master ! TaskDone(s"user filter for ${currentRoom.name}")
        context become doneWorking
      }
  }
}

object UserFilter {
  /** Filter room contents. */
  case class FilterRoomUser(room: Room)

  /** "Constructor" */
  def props(master: ActorRef, userDb: ActorRef, formatter: ActorRef) =
    Props(new UserFilter(master, userDb, formatter))
}