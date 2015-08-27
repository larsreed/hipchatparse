package no.mesan.hipchatparse.system

/** Misc. constants. */
object Config {
  /** Line feed. */
  val LF= "\n"
}

/** Names of actors and config variables. */
object ActorNames {
  /** Name of actor holding user mappings. */
  val userDb = "UserDB"
  /** Name of main actor. */
  val master= "Master"
  /** Name of User dir reader. */
  val userReader = "UserReader"
  /** Name of room dir reader. */
  val roomDirReader = "RoomDirReader"
  /** Name of room file reader. */
  val roomFileReader = "RoomFileReader"
  /** Name of room file reader. */
  val roomParser = "RoomParser"
  /** Name of user converter. */
  val userConverter = "UserConverter"
  /** Name of user mapping filter. */
  val userFilter = "UserFilter"
  /** Name of message exclusion. */
  val messageFilter = "UserFilter"
  /** Name of room formatter. */
  val roomFormatter = "RoomFormatter"
  /** Name of room writer. */
  val roomWriter = "RoomWriter"

  /** Result dir variable. */
  val resultDir = "resultDir"
}

/** HipChat export layout.
 */
object HipChatConfig {
  /** Name of user directory in export file. */
  val userDir = "users"
  /** Name of user file in export file. */
  val userFile = userDir + "/" + "list.json"

  /** ID of api user. */
  val apiUser= """"api""""

  /** Name of room directory in export file. */
  val roomDir = "rooms"

  /** Key in user file. */
  val userIdUserKey = "user_id"
  /** Key in user file. */
  val mentionUserKey = "mention_name"
  /** Key in user file. */
  val nameUserKey = "name"

  /** Key in room file. */
  val dateRoomKey = "date"
  /** Key in room file. */
  val fromRoomKey = "from"
  /** Key in room file. */
  val nameRoomKey = "name"
  /** Key in room file. */
  val userIdRoomKey = "user_id"
  /** Key in room file. */
  val messageRoomKey = "message"
}
