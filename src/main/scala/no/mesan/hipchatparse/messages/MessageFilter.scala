package no.mesan.hipchatparse.messages

import akka.actor._
import akka.event.LoggingReceive
import no.mesan.hipchatparse.TaskDone
import no.mesan.hipchatparse.messages.MessageFilter.FilterRoom
import no.mesan.hipchatparse.rooms.Room
import no.mesan.hipchatparse.rooms.WikiRoomFormatter.FormatRoom
import no.mesan.hipchatparse.users.NoUser

/** Discards unwanted messages. */
class MessageFilter(master: ActorRef, formatter: ActorRef) extends Actor with ActorLogging {

  private val ignoredUsers=List("JIRA", "GitHub")

  override def receive: Receive = LoggingReceive {
    case FilterRoom(room) =>
      val newList = MessageFilter.filterDuplicates(room.conversation
          .filter(msg => MessageFilter.okText(msg.text))
          .filter(msg => !ignoredUsers.contains(msg.user.fullName))
          .map{msg => Message(msg.user, msg.datestamp, MessageFilter.wash(msg.text))})
      formatter ! FormatRoom(room withConversation newList)
      master ! TaskDone(s"message filter for ${room.name}")
      self ! PoisonPill // One for each instance, time to die
  }
}

object MessageFilter {
  /** Filter contents. */
  case class FilterRoom(room: Room)

  /** "Constructor" */
  def props(master: ActorRef, formatter: ActorRef) = Props(new MessageFilter(master, formatter))

  private val ignoredText= List(
    ".*Welcome to Hipchat. You can @-mention me by typing @HipChat.*",
    ".*You can ask me about:.*HipChat.*",
    "^\\s*$",
    "^\\s*@HipChat\\s*$"
  )

  /** Filter unwanted texts. */
  def okText(text: String): Boolean = ignoredText.forall(pattern => !text.matches(pattern))

  /** Convert text in messages. */
  def wash(s: String): String = s.
    replaceAll("""\\n""", "\n"). // Handle inline line feeds
    replaceAll("(\\\n)+", "\n")

  /** Blank repeated values for date and room. */
  def filterDuplicates(messages: List[Message]): List[Message] = {
    var lastUser = ""
    var lastDate= ""
    for (msg <- messages) yield {
      val res1=
        if (msg.user.ID == lastUser) Message(NoUser, msg.datestamp, msg.text)
        else msg
      val res2=
        if (res1.dateString == lastDate) Message (res1.user, None, res1.text)
        else res1
      lastUser= msg.user.ID
      lastDate= msg.dateString
      res2
    }
  }
}
