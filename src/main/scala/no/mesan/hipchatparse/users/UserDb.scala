package no.mesan.hipchatparse.users

import akka.actor._
import akka.event.LoggingReceive
import no.mesan.hipchatparse.{TaskDone, HipChatConfig}
import no.mesan.hipchatparse.users.UserParser.LastUser

/** Master user mapping DB. */
class UserDb(master: ActorRef) extends Actor with Stash with ActorLogging {
  import UserDb.{AddUser, GetUsers, FoundUser, UserNotFound}

  private var userMap= Map.empty[String, User]

  override def receive: Receive = LoggingReceive {
    case AddUser(user) =>
      userMap += (user.ID -> user)

    case LastUser =>
      master ! TaskDone("building userDB")
      unstashAll()
      context become active

    case GetUsers =>
      stash()
  }

  val active: Actor.Receive = LoggingReceive {
    case GetUsers(ids) =>
      for (id <- ids) {
        val res = userMap.get(id)
        sender ! res.map(FoundUser(id, _))
                    .getOrElse(UserNotFound(id))
        if (res.isEmpty && !(id==HipChatConfig.apiUser)) log.debug(s"ID $id unknown")
      }
  }
}

object UserDb {
  /** Add a new room to the DB. */
  case class AddUser(user: User)

  /** Search for room. */
  case class GetUsers(ids: List[String])

  /** Successful search. */
  case class FoundUser(id: String, user: User)
  /** Failed search. */
  case class UserNotFound(id: String)

  /** "Constructor" */
  def props(master: ActorRef) = Props(new UserDb(master))
}