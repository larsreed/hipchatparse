package no.mesan.hipchatparse.roomlist

import akka.actor._
import akka.event.LoggingReceive
import no.mesan.hipchatparse.roomlist.RoomlistDb.{FoundRoom, GetRoom, RoomNotFound}
import no.mesan.hipchatparse.rooms.Room
import no.mesan.hipchatparse.users.UserFilter
import no.mesan.hipchatparse.users.UserFilter.FilterRoomUser
import no.mesan.hipchatparse.utils.NameHelper
import no.mesan.hipchatparse.{TaskDone, ActorNames, Config, RoomDone}

/** Filter private rooms, set correct names. */
class RoomFilter(master: ActorRef, userDb:ActorRef, roomDb: ActorRef, formatter: ActorRef) extends Actor
  with Stash with ActorLogging with NameHelper {
  import RoomFilter.FilterRoom

  var currentRoom: Room= _

  override def receive: Receive = LoggingReceive {
    case FilterRoom(room) =>
      currentRoom= room
      roomDb ! GetRoom(room.name)
      context become waiting
  }

  val waiting: Receive = LoggingReceive {
    case FilterRoom(room) =>
      stash()

    case RoomNotFound(id) =>
      log.error(s"No definition for room $id")
      master ! RoomDone(id, 0)
      unstashAll()
      context become receive

    case FoundRoom(id, roomDef) =>
      if ( roomDef.isPublic || !Config.excludePrivate ) {
        val shortName= makeActorSuffix(id)
        val next = context.actorOf(UserFilter.props(master, userDb, formatter), s"${ActorNames.messageFilter}-$shortName")
        next ! FilterRoomUser(currentRoom.withFullName(roomDef.name))
        master ! TaskDone(s"filtering room $id (included)")
      }
      else {
        log.debug(s"Skipping private room $id")
        master ! RoomDone(id, 0)
        master ! TaskDone(s"filtering room $id (excluded)")
      }
      unstashAll()
      context become receive
  }
}

object RoomFilter {
  /** Filter room contents. */
  case class FilterRoom(room: Room)

  /** "Constructor" */
  def props(master: ActorRef, userDb: ActorRef, roomDb: ActorRef, formatter: ActorRef) =
    Props(new RoomFilter(master, userDb, roomDb, formatter))
}