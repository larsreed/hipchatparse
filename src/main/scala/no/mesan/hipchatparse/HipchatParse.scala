package no.mesan.hipchatparse

import akka.actor.ActorSystem
import no.mesan.hipchatparse.HipChatParseMain.Start


object HipchatParse {
  def main(args: Array[String]) {
    val system = ActorSystem("HipChatParse")
    val mainActor= system.actorOf(HipChatParseMain.props())
    mainActor ! Start("C:/Users/larsr_000/Desktop/hipchat_export",
      "C:/Users/larsr_000/Desktop/hipchat_export/res")
  }


}