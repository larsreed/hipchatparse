package no.mesan.hipchatparse

/** Cannot continue :( */
case class Breakdown(message: String)

/** Status update. */
case class TaskDone(name: String)

/** Configure a value */
case class ConfigValue[A](name: String, value: A)

/** Report a room being finished. */
case class RoomDone(roomName: String, numberOfLines: Int)
