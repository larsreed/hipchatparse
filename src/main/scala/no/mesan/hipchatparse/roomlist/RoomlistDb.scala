package no.mesan.hipchatparse.roomlist

import akka.actor._
import akka.event.LoggingReceive
import no.mesan.hipchatparse.{LastRoom, TaskDone}
import no.mesan.hipchatparse.utils.NameHelper

/** Master room mapping DB. */
class RoomlistDb(master: ActorRef) extends Actor with Stash with ActorLogging with NameHelper {
  import RoomlistDb.{AddRoom, FoundRoom, GetRoom, RoomNotFound}

  private var roomMap= Map.empty[String, RoomDef]

  override def receive: Receive = LoggingReceive {
    case AddRoom(room) =>
      roomMap += (room.id -> room)

    case LastRoom =>
      master ! TaskDone("building roomDB")
      unstashAll()
      context become active

    case GetRoom(_) =>
      stash()
  }

  val active: Receive = LoggingReceive {
    case GetRoom(id) =>
      val res = roomMap.get(dirMap(id))
      sender ! res.map(FoundRoom(id, _))
                  .getOrElse(RoomNotFound(id))
      if (res.isEmpty) log.warning(s"ID $id unknown")
  }
}

object RoomlistDb {
  /** Add a new room to the DB. */
  case class AddRoom(room: RoomDef)

  /** Search for room. */
  case class GetRoom(name: String)

  /** Successful search. */
  case class FoundRoom(id: String, room: RoomDef)
  /** Failed search. */
  case class RoomNotFound(id: String)

  /** "Constructor" */
  def props(master: ActorRef) = Props(new RoomlistDb(master))
}