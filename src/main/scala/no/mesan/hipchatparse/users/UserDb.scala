package no.mesan.hipchatparse.users

import akka.actor._
import akka.event.LoggingReceive
import no.mesan.hipchatparse.{TaskDone, HipChatConfig}
import no.mesan.hipchatparse.users.UserParser.LastUser

/** Master room mapping DB. */
class UserDb(master: ActorRef) extends Actor with Stash with ActorLogging {
  import no.mesan.hipchatparse.users.UserDb.{AddUser, GetUsers, FoundUser, UserNotFound}

  private var userMap= Map.empty[String, User]

  override def receive: Receive = LoggingReceive {
    case AddUser(user) =>
      userMap += (user.ID -> user)
    case LastUser =>
      master ! TaskDone("building userDB")
      unstashAll()
      context.become(active)
    case GetUsers(_) =>
      stash()
  }

  val active: Actor.Receive = LoggingReceive {
    case GetUsers(ids) =>
      for (id <- ids) {
        val res = userMap.get(id)
        val msg = if (res.isDefined) FoundUser(id, res.get)
                  else UserNotFound(id)
        sender ! msg
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