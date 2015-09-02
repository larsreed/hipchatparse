package no.mesan.hipchatparse.roomlist

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import no.mesan.hipchatparse.HipChatConfig._
import no.mesan.hipchatparse.roomlist.RoomlistDb.AddRoom
import no.mesan.hipchatparse.utils.{NameHelper, FileIO}
import no.mesan.hipchatparse.{Breakdown, TaskDone}
import play.api.libs.json.{JsValue, Json}

import scala.util.{Failure, Success, Try}

/** Reads the room file. */
class RoomlistParser(master: ActorRef, roomDb: ActorRef) extends Actor with ActorLogging {
  import RoomlistParser.{BuildRoomDb, LastRoom}

  override def receive: Receive = LoggingReceive {
    case BuildRoomDb(fileName) =>
      val contents= FileIO.fromFile(fileName)
      if (contents.isEmpty) {
        sender ! Breakdown(s"cannot open room directory $fileName")
      }
      else {
        val rooms= RoomlistParser.jsonParse(contents.get)
        if (rooms.isFailure) sender ! Breakdown(s"cannot parse room file $fileName, error: ${rooms.failed}")
        else for (room <- rooms.get ) roomDb ! AddRoom(room)
      }
      roomDb ! LastRoom // Signal end of list
      master ! TaskDone("Read room JSON file")
  }
}

object RoomlistParser extends NameHelper {
  /** Start reading room file. */
  case class BuildRoomDb(fileName: String)
  /** Last room read from file. */
  case object LastRoom

  /** "Constructor" */
  def props(master: ActorRef, roomDb: ActorRef) = Props(new RoomlistParser(master, roomDb))

  /** Make a list of users from the JSON contents. */
  //noinspection ZeroIndexToHead
  def jsonParse(contents: String): Try[List[RoomDef]] = {
    try {
      val json: JsValue = Json.parse(contents)
      val names= (json \\ roomNameListKey ).map(v => washJson(v.toString()))
      val ids= names.map(n=> dirMap(n))
      val privates= (json \\ privateListKey).map(v=> v.toString())
      Success(List(ids, names, privates).transpose.map(v=> RoomDef(v(0), v(1), v(2))))
    }
    catch {
      case e:Throwable =>
        Failure(e)
    }
  }
}