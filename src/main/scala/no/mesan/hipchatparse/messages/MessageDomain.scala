package no.mesan.hipchatparse.messages

import java.time.{LocalDate, LocalDateTime}

import no.mesan.hipchatparse.users.User

/* A message in a room. */
//noinspection ScalaFileName
case class Message(user: User, datestamp: Option[String], text: String) {
  /** Convert to real date object */
  def dateTime: Option[LocalDateTime]= datestamp.map(LocalDateTime.parse)
  /** Format as date only. */
  def dateString: String= dateTime.map(LocalDate.from(_).toString).getOrElse("")
}
