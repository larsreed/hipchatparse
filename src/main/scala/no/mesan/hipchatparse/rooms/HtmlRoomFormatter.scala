package no.mesan.hipchatparse.rooms

import java.util.regex.Pattern

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.{LoggingAdapter, LoggingReceive}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

import no.mesan.hipchatparse.rooms.RoomWriter.WriteRoom
import no.mesan.hipchatparse.utils.{Tools, NameHelper}
import no.mesan.hipchatparse.{FormatRoom, LastRoom}

import scala.concurrent.{Await, Future}
import scala.util.{Success, Failure}
import scala.xml.Utility

/** Create a HTML page for all rooms. */
class HtmRoomFormatter(master: ActorRef, writer: ActorRef) extends Actor with ActorLogging with NameHelper {

  // A virtual room to the formatter
  private val virtualRoom = Room("all_rooms.html", None, List.empty)

  private var outputHead= List(
    """<!DOCTYPE html>
      |<html>""".stripMargin,
    """<head>
      |    <meta charset="UTF-8">
      |    <title>HipChat &ndash; All rooms</title>
      |    <style>
      |      table, th, td { border: 1px solid black; }
      |      table { border-collapse: collapse; }
      |      th { text-align: left; }
      |      th { background-color: #CCCCCC; }
      |      pre { background-color: #EEEEEE; }
      |      td { vertical-align: top; }
      |    </style>
      |</head>""".stripMargin,
    "<body>",
    """<h1>All rooms</h1>
      |
      |<h2>TOC</h2>
      |<ul>""".stripMargin).reverse // reversed again later
  private var outputBody= List.empty[String]
  private val outputEnd = List(
    """</body>
      |</html>""".stripMargin)

  private def roomTitle(room: Room) = s"<h2 id='${name2id(room.name)}'>${room.roomName}</h2>"

  override def receive: Receive = LoggingReceive {
    case FormatRoom(room) =>
      outputHead ::= s"<li><a href='#${name2id(room.name)}'>${room.roomName}</a></li>"
      outputBody ::= ""
      outputBody ::= roomTitle(room)
      outputBody ::=
        """<table class="roomTable">
          |  <thead>
          |    <tr>
          |      <th class="dateHead">Date</th>
          |      <th class="nameHead">User</th>
          |      <th class="textHead">Text</th>
          |    </tr>
          |  </thead>""".stripMargin
      outputBody ::= "  <tbody>"
      for (msg <- room.conversation)
        outputBody ::=
          s"""    <tr>
             |      <td class="dateCell"><nobr>${msg.dateString}</nobr></td>
             |      <td class="nameCell"><nobr>${msg.user.fullName}</nobr></td>
             |      <td class="textCell">${HtmlRoomFormatter.wash(msg.text, log)}</td>
             |    </tr>""".stripMargin
      outputBody ::=
        """  </tbody>
          |</table>""".stripMargin

    case LastRoom =>
      outputHead ::= "</ul>"
      val msg= (outputHead.reverse ++ outputBody.reverse ++ outputEnd).mkString("\n")
      writer ! WriteRoom(virtualRoom, msg)
  }
}

object HtmlRoomFormatter {

  /** "Constructor" */
  def props(master: ActorRef, writer: ActorRef) = Props(new HtmRoomFormatter(master, writer))

  private def isOdd(i: Int): Boolean = (i%2)==1
  private val noformat= "{noformat}"

  /** Cleanup contents. */
  def wash(text: String, log: LoggingAdapter): String = {
    val washed= text
      .replaceAll("""\\"""", """"""") // Replace extraneous quoting
      .replaceAll("(?ms)^\\s*/code\\s+(\\S.*)", noformat + "$1" + noformat) // quote code
      .replaceAll("\\\\(\r?)[\n]", " \n") // remove escaped line feeds
    val splat= washed.split(Pattern.quote(noformat))
    val coded= for (i <- 0 to splat.size - 1) yield
      if (isOdd(i)) "<pre>" + Utility.escape(splat(i)) + "</pre>"
      else Utility.escape(splat(i))
    linkify(coded.mkString(""), log)
  }

  private def linkify(s: String, log: LoggingAdapter): String = {
    Tools.urlParseMaybe(s, 2 seconds) match {
      case Failure(err) =>
        log.debug(s"""linkify failed: $err on ${s.replaceAll("[\n\r+]", "")}""")
        s
      case Success(list) =>
        val mapped= list.map { case (isLink, text)  =>
          if (isLink) s"<a href='$text'>$text</a>"
          else text }
        mapped.mkString("")
    }
  }
}