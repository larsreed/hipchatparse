package no.mesan.hipchatparse.messages

import java.time.{LocalDate, LocalDateTime}

import no.mesan.hipchatparse.users.User

//noinspection ScalaFileName
/* A message in a room. */
case class Message(user: User, datestamp: Option[String], text: String, hideDate: Boolean = false) {
  /** Convert to real date object */
  def dateTime: Option[LocalDateTime]= datestamp.map(LocalDateTime.parse)
  /** Format as date only. */
  def dateString: String=
    if (hideDate) "" else dateTime.map(LocalDate.from(_).toString).getOrElse("")
  /** Format as time only. */
  def timeString: String= dateTime.map(dt=> dt.toLocalTime.toString).getOrElse("")
}
