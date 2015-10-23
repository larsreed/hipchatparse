package no.mesan.hipchatparse

import java.nio.charset.StandardCharsets

/** Misc. constants. */
object Config {
  /** Line feed. */
  val LF= "\n"

  /** Exclude private rooms. */
  val excludePrivate= true

  /** Output charset */
  val resultCharSet = StandardCharsets.UTF_8
}

/** Names of actors and config variables. */
object ActorNames {

  /** Name of actor holding room mappings. */
  val roomDb = "RoomDB"
  /** Name of actor parsing room list. */
  val roomListParser = "RoomListParser"
  /** Name of actor parsing room list. */
  val roomFilter = "RoomFilter"
  /** Name of actor holding user mappings. */
  val userDb = "UserDB"
  /** Name of main actor. */
  val master= "Master"
  /** Name of User dir reader. */
  val userReader = "UserParser"
  /** Name of room dir reader. */
  val roomDirReader = "RoomDirReader"
  /** Name of room file reader. */
  val roomFileReader = "RoomFileReader"
  /** Name of room file reader. */
  val roomParser = "RoomParser"
  /** Name of room converter. */
  val userConverter = "UserConverter"
  /** Name of room mapping filter. */
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

/** HipChat export layout. */
object HipChatConfig {
  /** Name of room directory in export file. */
  val userDir = "users"
  /** Name of room file in export file. */
  val userFile = userDir + "/" + "list.json"

  /** ID of api room. */
  val apiUser= "api"

  /** Name of room directory in export file. */
  val roomDir = "rooms"
  /** Name of room file in export file. */
  val roomFile = roomDir + "/" + "list.json"

  /** Key in room file. */
  val userIdUserKey = "user_id"
  /** Key in room file. */
  val mentionUserKey = "mention_name"
  /** Key in room file. */
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

  /** Key in room list file. */
  val roomIdListKey = "room_id"
  /** Key in room list file. */
  val roomNameListKey = "name"
  /** Key in room list file. */
  val privateListKey = "is_private"
}
