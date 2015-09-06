package no.mesan.hipchatparse

import no.mesan.hipchatparse.rooms.Room

/** Cannot continue :( */
case class Breakdown(message: String)

/** Status update. */
case class TaskDone(name: String)

/** Configure a value */
case class ConfigValue[A](name: String, value: A)

/** Report a room being finished. */
case class RoomDone(roomName: String, numberOfLines: Int)

/** Format room data. */
case class FormatRoom(room: Room)

/** Last room read. */
case object LastRoom
