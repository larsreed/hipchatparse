package no.mesan.hipchatparse.rooms

import akka.actor.{Props, Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import no.mesan.hipchatparse.rooms.RoomWriter.WriteRoom
import no.mesan.hipchatparse.system.Config._
import no.mesan.hipchatparse.system.TaskDone

/** Formats room content for output. Could be replaced for different outputs. */
class WikiRoomFormatter(master: ActorRef, writer: ActorRef) extends Actor with ActorLogging {
  import no.mesan.hipchatparse.rooms.WikiRoomFormatter.FormatRoom

  override def receive: Receive = LoggingReceive {
    case FormatRoom(room) =>
      val table = for (msg <- room.conversation) yield {
        val usr= msg.user
        val sig= if (usr.mention.isDefined) s"[~${usr.mention.get}]" else usr.fullName
        val dt= msg.dateString
        val txt= msg.text
        s"| $dt | $sig | $txt |"
      }
      val res= "h2. " + room.name + LF + LF + table.mkString(LF) + LF
      writer ! WriteRoom(room, res)
      master ! TaskDone(s"formatted room ${room.name}")
  }
}

object WikiRoomFormatter {
  /** Format room data. */
  case class FormatRoom(room: Room)

  /** "Constructor" */
  def props(master: ActorRef, writer: ActorRef) = Props(new WikiRoomFormatter(master, writer))
}