package no.mesan.hipchatparse.rooms

import akka.actor.{Props, Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import no.mesan.hipchatparse.{TaskDone, Config}
import no.mesan.hipchatparse.rooms.RoomWriter.WriteRoom
import Config._

/** Formats room content for output. Could be replaced for different outputs. */
class WikiRoomFormatter(master: ActorRef, writer: ActorRef) extends Actor with ActorLogging {
  import no.mesan.hipchatparse.rooms.WikiRoomFormatter.FormatRoom

  override def receive: Receive = LoggingReceive {
    case FormatRoom(room) =>
      val tab1 = for (msg <- room.conversation) yield {
        val usr= msg.user
        val sig= if (usr.mention.isDefined) s"[~${usr.mention.get}]" else usr.fullName
        val dt= msg.dateString
        val txt= WikiRoomFormatter.wash(msg.text)
        s"| $dt | $sig | $txt |"
      }
      val tab2= tab1.grouped(500).toList
      val tab3= tab2.map(lst=> lst.mkString("\n") + "\n")
      val res= "h2. " + room.fullName + LF + LF + tab3.mkString(LF) + LF
      writer ! WriteRoom(room, res)
      master ! TaskDone(s"formatted room ${room.fullName}")
  }
}

object WikiRoomFormatter {

  /** Cleanup contents. */
  def wash(text: String): String = text.
      replaceAll("[@](\\w+)\\b", "[~$1]"). // Create room refs from mentions
      replaceAll("[|]", "\\\\|").
      replaceAll("(?ms)^\\s*/code\\s*(.*)", "{noformat}$1{noformat}").
      replaceAll("""\\"""", """"""").
      replaceAll("\\\\[\n\r]+", " \\\\\\\\\n")

  /** Format room data. */
  case class FormatRoom(room: Room)

  /** "Constructor" */
  def props(master: ActorRef, writer: ActorRef) = Props(new WikiRoomFormatter(master, writer))
}