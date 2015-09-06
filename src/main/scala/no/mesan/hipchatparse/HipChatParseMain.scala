package no.mesan.hipchatparse

import no.mesan.hipchatparse.roomlist.RoomlistParser.BuildRoomDb
import no.mesan.hipchatparse.roomlist.{RoomFilter, RoomlistParser, RoomlistDb}

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.{ReceiveTimeout, Actor, ActorLogging, Props}
import akka.event.LoggingReceive

import no.mesan.hipchatparse.rooms.RoomDirReader.{RoomDiscovered, BuildRooms}
import no.mesan.hipchatparse.rooms._
import no.mesan.hipchatparse.users.UserParser.BuildUserDB
import no.mesan.hipchatparse.users.{UserParser, UserDb}

/** Main actor -- starts and ends the show. */
class HipChatParseMain extends Actor with ActorLogging {
  import HipChatParseMain.{Start, Stop, CheckIfDone}

  private val userDb= context.actorOf(UserDb.props(self), ActorNames.userDb)
  private val roomDb= context.actorOf(RoomlistDb.props(self), ActorNames.roomDb)
  private val userReader= context.actorOf(UserParser.props(self, userDb), ActorNames.userReader)
  private val writer= context.actorOf(RoomWriter.props(self), ActorNames.roomWriter)
  private val formatter= context.actorOf(WikiRoomFormatter.props(self, writer), ActorNames.roomFormatter)
  private val roomListParser= context.actorOf(RoomlistParser.props(self, roomDb), ActorNames.roomListParser)
  private val roomFilter= context.actorOf(RoomFilter.props(self, userDb, roomDb, formatter), ActorNames.roomFilter)
  private val roomParser= context.actorOf(RoomParser.props(self, roomFilter), ActorNames.roomParser)
  private val roomFileReader= context.actorOf(RoomFileReader.props(self, roomParser), ActorNames.roomFileReader)
  private val roomDirReader= context.actorOf(RoomDirReader.props(self, roomFileReader), ActorNames.roomDirReader)
  // instantiated per room userFilter
  // instantiated per room messageFilter

  private var roomList = Set.empty[String]
  private var canExit = false // to avoid stopping before we have started (not likely)
  private var messagesWritten = 0

  override def receive: Receive = LoggingReceive{

    case Start(exportDir, resultDir) =>
      log.debug("Starting")
      context.setReceiveTimeout(5 seconds) // Break out if hanging
      writer ! ConfigValue(ActorNames.resultDir, resultDir)

      // Build DBs
      userReader ! BuildUserDB(exportDir + "/" + HipChatConfig.userFile)
      roomListParser ! BuildRoomDb(exportDir + "/" + HipChatConfig.roomFile)
      // The main pipe starts here
      roomDirReader ! BuildRooms(exportDir + "/" + HipChatConfig.roomDir)

    case Stop =>
      context.system.shutdown()

    case RoomDiscovered(name) =>
      roomList = roomList + name

    case RoomDone(name, count) =>
      roomList = roomList - name
      messagesWritten  += count
      println(s"$name :: $count")
      self ! CheckIfDone

    case LastRoom =>
      canExit= true
      self ! CheckIfDone

    case CheckIfDone =>
      val res=
        if (roomList.nonEmpty) s"waiting for $roomList..."
        else if (canExit) {
            self ! Stop
            s"Exiting, $messagesWritten messages written"
        }
        else "roomList is empty, but cannot exit"
      log.debug(res)

    case Breakdown(err) =>
      log.error("FATAL: " + err)
      self ! Stop

    case TaskDone(task) =>
      log.debug(s"Trace: $task -- done")

    case ReceiveTimeout =>
      log.error(s"No action for too long -- terminated while waiting for $roomList")
      self ! Stop
  }
}

object HipChatParseMain {
  /** Get running. */
  case class Start(baseDir: String, resultDir: String)

  /** Done! */
  case object Stop

  /** Check if last entry processed. */
  case object CheckIfDone

  /** "Constructor" */
  def props() = Props[HipChatParseMain]
}