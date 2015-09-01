package no.mesan.hipchatparse.users

import akka.actor.{Props, ActorRef, Actor, ActorLogging}
import akka.event.LoggingReceive
import no.mesan.hipchatparse.utils.FileIO
import no.mesan.hipchatparse.system.HipChatConfig._
import no.mesan.hipchatparse.system.{Breakdown, TaskDone}
import no.mesan.hipchatparse.users.UserDb.AddUser
import no.mesan.hipchatparse.users.UserParser.LastUser
import play.api.libs.json.{JsValue, Json}

import scala.util.{Failure, Success, Try}

/** Reads the user file. */
class UserParser(master: ActorRef, userDb: ActorRef) extends Actor with ActorLogging {
  import no.mesan.hipchatparse.users.UserParser.BuildUserDB

  override def receive: Receive = LoggingReceive {
    case BuildUserDB(fileName) =>
      val contents= FileIO.fromFile(fileName)
      if (contents.isEmpty) {
        sender ! Breakdown(s"cannot open user directory $fileName")
      }
      else {
        val users= UserParser.jsonParse(contents.get)
        if (users.isFailure) sender ! Breakdown(s"cannot parse user file $fileName, error: ${users.failed}")
        else for (user <- users.get ) userDb ! AddUser(user)
      }
      userDb ! LastUser // Signal end of list
      master ! TaskDone("Read user JSON file")
  }
}

object UserParser  {
  /** Start reading user file. */
  case class BuildUserDB(fileName: String)
  /** Last user read from file. */
  case object LastUser

  /** "Constructor" */
  def props(master: ActorRef, userDb: ActorRef) = Props(new UserParser(master, userDb))

  private def unString(s: String)= s.substring(1).replaceFirst(".$", "" )

  /** Make a list of users from the JSON contents. */
  def jsonParse(contents: String): Try[List[User]] = {
    try {
      val json: JsValue = Json.parse(contents)
      val ids= (json \\ userIdUserKey ).map(v=> v.toString())
      val mentions= (json \\ mentionUserKey ).map(v => unString(v.toString()))
      val fullNames= (json \\ nameUserKey).map(v=> unString(v.toString()))
      Success(List(ids, mentions, fullNames).transpose.map(v=> User(v(n=0), Some(v(n=1)), v(n=2))))
    }
    catch {
      case e:Throwable =>
        Failure(e)
    }
  }
}