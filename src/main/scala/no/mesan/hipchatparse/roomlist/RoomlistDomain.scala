package no.mesan.hipchatparse.roomlist

import no.mesan.hipchatparse.rooms.Room

object NoRoom extends Room("", None, List.empty)

case class RoomDef(id: String, name: String, privateRoom: String) {
  def isPublic: Boolean = privateRoom=="false"
}