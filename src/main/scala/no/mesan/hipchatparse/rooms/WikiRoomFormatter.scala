package no.mesan.hipchatparse.rooms

import java.util.regex.Pattern

import akka.actor.{Props, Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import no.mesan.hipchatparse.{TaskDone, Config}
import no.mesan.hipchatparse.rooms.RoomWriter.WriteRoom
import Config._

/** Formats room content for output. Could be replaced for different outputs. */
class WikiRoomFormatter(master: ActorRef, writer: ActorRef) extends Actor with ActorLogging {
  import WikiRoomFormatter.FormatRoom

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
      val res= "h2. " + room.fullName.get + LF + LF + tab3.mkString(LF) + LF
      writer ! WriteRoom(room, res)
      master ! TaskDone(s"formatted room ${room.fullName.get}")
  }
}

object WikiRoomFormatter {
  private def isOdd(i: Int): Boolean = (i%2)==1
  private val noformat= "{noformat}"

  /** Cleanup contents. */
  def wash(text: String): String = {
    val washed= text
      .replaceAll("""\\"""", """"""") // Replace extraneous quoting
      .replaceAll("(?ms)^\\s*/code\\s+(\\S.*)", noformat + "$1" + noformat) // quote code
      .replaceAll("\\\\(\r?)[\n]", " \\\\\\\\\n") // double quote line feeds within strings
    val splat= washed.split(Pattern.quote(noformat))
    val res= (for (i <- 0 to splat.size-1) yield {
      if (isOdd(i)) splat(i).replaceAll(" *\\\\\\\\[\n\r]+", "\n") // Remove line quotes in code
      else splat(i)
        .replaceAll("[|{}]", "\\\\$0")       // quote braces and bars
        .replaceAll("[@](\\w+)\\b", "[~$1]") // Create user refs from mentions
    }).mkString(noformat)
    if (washed.endsWith(noformat)) res + noformat else res
  }

  /** Format room data. */
  case class FormatRoom(room: Room)

  /** "Constructor" */
  def props(master: ActorRef, writer: ActorRef) = Props(new WikiRoomFormatter(master, writer))
}