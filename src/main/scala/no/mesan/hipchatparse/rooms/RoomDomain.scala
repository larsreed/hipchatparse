package no.mesan.hipchatparse.rooms

import no.mesan.hipchatparse.messages.Message

/** A room. */ //noinspection ScalaFileName
case class Room(name: String, fullName: Option[String], conversation: List[Message]) {
  def withFullName(name: String) = this.copy(fullName=Some(name))
  def withConversation(talk: List[Message]) = this.copy(conversation=talk)
}
