package no.mesan.hipchatparse

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import no.mesan.hipchatparse.rooms.RoomDirReader.{LastRoom, FoundRoom, BuildRooms}
import no.mesan.hipchatparse.rooms.RoomWriter.WroteRoom
import no.mesan.hipchatparse.rooms._
import no.mesan.hipchatparse.system._
import no.mesan.hipchatparse.users.UserReader.BuildUserDB
import no.mesan.hipchatparse.users.{UserDb, UserReader}

/** Main actor -- starts and ends the show. */
class HipChatParseMain extends Actor with ActorLogging {
  import no.mesan.hipchatparse.HipChatParseMain.{Start, CheckIfDone}

  private val userDb= context.actorOf(UserDb.props(self), ActorNames.userDb)
  private val userReader= context.actorOf(UserReader.props(self, userDb), ActorNames.userReader)
  private val writer= context.actorOf(RoomWriter.props(self), ActorNames.roomWriter)
  private val formatter= context.actorOf(WikiRoomFormatter.props(self, writer), ActorNames.roomFormatter)
  private val roomParser= context.actorOf(RoomParser.props(self, userDb, formatter), ActorNames.roomParser)
  private val roomFileReader= context.actorOf(RoomFileReader.props(self, roomParser), ActorNames.roomFileReader)
  private val roomDirReader= context.actorOf(RoomDirReader.props(self, roomFileReader), ActorNames.roomDirReader)
  // instantiated per room userFilter
  // instantiated per room messageFilter

  private var roomList = Set.empty[String]
  private var canExit= false // to avoid stopping before we have started (not likely)

  override def receive: Receive = LoggingReceive{

    case Start(exportDir, resultDir) =>
      writer ! ConfigValue(ActorNames.resultDir, resultDir)
      userReader ! BuildUserDB(exportDir + "/" + HipChatConfig.userFile)
      roomDirReader ! BuildRooms(exportDir + "/" + HipChatConfig.roomDir)

    case FoundRoom(name) =>
      roomList = roomList + name

    case WroteRoom(name, count) =>
      roomList = roomList - name
      println(s"$name :: $count")
      self ! CheckIfDone

    case LastRoom =>
      canExit= true
      self ! CheckIfDone

    case CheckIfDone =>
      if (roomList.isEmpty) {
        if (canExit) context.system.shutdown()
        else log.debug("roomList is empty, but cannot exit")
      }
      else log.debug(s"waiting for $roomList...")

    case Breakdown(err) =>
      log.error("FATAL: " + err)
      context.system.shutdown()

    case TaskDone(task) =>
      log.debug(s"Trace: $task -- done")
  }
}

object HipChatParseMain {
  /** Get running. */
  case class Start(baseDir: String, resultDir: String)

  /** Main object -- check if last entry processed. */
  case object CheckIfDone

  /** "Constructor" */
  def props() = Props[HipChatParseMain]
}