package no.mesan.hipchatparse.rooms

import java.util.regex.Pattern

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.{LoggingAdapter, LoggingReceive}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

import no.mesan.hipchatparse.Config._
import no.mesan.hipchatparse.rooms.RoomWriter.WriteRoom
import no.mesan.hipchatparse.utils.Tools
import no.mesan.hipchatparse.{FormatRoom, TaskDone}

import scala.util.{Failure, Success}

/** Formats room content for output. Could be replaced for different outputs. */
class WikiRoomFormatter(master: ActorRef, writer: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = LoggingReceive {
    case FormatRoom(room) =>
      val tab1 = for (msg <- room.conversation) yield {
        val usr= msg.user
        val sig= if (usr.mention.isDefined) s"[~${usr.mention.get}]" else usr.fullName
        val dt= msg.dateString
        val txt= WikiRoomFormatter.wash(msg.text, log)
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
  def wash(text: String, log: LoggingAdapter): String = {
    val washed= text
      .replaceAll("""\\"""", """"""") // Replace extraneous quoting
      .replaceAll("(?ms)^\\s*/code\\s+(\\S.*)", noformat + "$1" + noformat) // quote code
      .replaceAll("\\\\(\r?)[\n]", " \\\\\\\\\n") // double quote line feeds within strings
    val splat= washed.split(Pattern.quote(noformat))
    val res= (for (i <- 0 to splat.size-1) yield {
      if (isOdd(i)) splat(i).replaceAll(" *\\\\\\\\[\n\r]+", "\n") // Remove line quotes in code
      else linkify(splat(i)
        .replaceAll("[|{}]", "\\\\$0")             // quote braces and bars
        .replaceAll("[@](\\w+)\\b", "[~$1]"), log) // Create user refs from mentions
    }).mkString(noformat)
    if (washed.endsWith(noformat)) res + noformat else res
  }

  private def linkify(s: String, log: LoggingAdapter): String = {
    Tools.urlParseMaybe(s, 1 second) match {
      case Failure(err) =>
        log.debug(s"""linkify failed: $err on ${s.replaceAll("[\n\r+]", "")}""")
        s
      case Success(list) =>
        val mapped= list.map { case (isLink, text)  =>
          if (isLink) s"[$text]"
          else text }
        mapped.mkString("")
    }
  }

  /** "Constructor" */
  def props(master: ActorRef, writer: ActorRef) = Props(new WikiRoomFormatter(master, writer))
}