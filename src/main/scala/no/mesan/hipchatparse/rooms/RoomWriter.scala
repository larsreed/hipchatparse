package no.mesan.hipchatparse.rooms

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import no.mesan.hipchatparse._

/** Writes result. */
class RoomWriter(master: ActorRef) extends Actor with ActorLogging {
  import no.mesan.hipchatparse.rooms.RoomWriter.WriteRoom

  var baseDir= "."

  override def receive: Receive = LoggingReceive {
    case ConfigValue(varName, varValue) if varName==ActorNames.resultDir =>
      baseDir= varValue.asInstanceOf[String]

    case WriteRoom(room, formatted) =>
      val fileName= s"$baseDir/${room.name}.room"
      log.debug(s"writing ${room.name} to $fileName")
      writeToFile(formatted, fileName)
      master ! TaskDone(s"wrote room ${room.name}")
      master ! RoomDone(room.name, room.conversation.size)
  }

  private def writeToFile(formatted: String, fileName: String) {
    try {
      Files.write(Paths.get(fileName), formatted.getBytes(Config.resultCharSet))
    }
    catch {
      case e: IOException => log.error(e, s"Cannot write to $fileName")
    }
  }
}

object RoomWriter {
  /** Write room data. */
  case class WriteRoom(room: Room, formatted: String)

  /** "Constructor" */
  def props(master: ActorRef) = Props(new RoomWriter(master))
}
