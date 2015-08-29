package no.mesan.hipchatparse.rooms

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import no.mesan.hipchatparse.system.{ActorNames, ConfigValue, TaskDone}

/** Writes result. */
class RoomWriter(master: ActorRef) extends Actor with ActorLogging {
  import no.mesan.hipchatparse.rooms.RoomWriter.{WriteRoom, WroteRoom}

  var baseDir= "."

  override def receive: Receive = LoggingReceive {
    case ConfigValue(varName, varValue) if varName==ActorNames.resultDir =>
      baseDir= varValue.asInstanceOf[String]

    case WriteRoom(room, formatted) =>
      val fileName= s"$baseDir/${room.name}.room"
      log.debug(s"writing ${room.name} to $fileName")
      Files.write(Paths.get(fileName), formatted.getBytes(StandardCharsets.UTF_8))
      master ! TaskDone(s"wrote room ${room.name}")
      master ! WroteRoom(room.name, room.conversation.size)
  }
}

object RoomWriter {
  /** Write room data. */
  case class WriteRoom(room: Room, formatted: String)
  /** Report a room being finished. */
  case class WroteRoom(roomName: String, numberOfLines: Int)

  /** "Constructor" */
  def props(master: ActorRef) = Props(new RoomWriter(master))
}
