package no.mesan.hipchatparse.rooms

import java.time.{LocalDate, LocalDateTime}

import no.mesan.hipchatparse.users.User

/** A room. */
case class Room(name: String, conversation: List[Message])

/* A message in a room. */
case class Message(user: User, datestamp: Option[String], text: String) {
  /** Convert to real date object */
  def dateTime: Option[LocalDateTime]= datestamp.map(LocalDateTime.parse)
  /** Format as date only. */
  def dateString: String= dateTime.map(LocalDate.from(_).toString).getOrElse("")
}
