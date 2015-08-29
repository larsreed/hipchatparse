package no.mesan.hipchatparse

import java.nio.file.{Paths, Path, Files}

import akka.actor.ActorSystem
import no.mesan.hipchatparse.HipChatParseMain.Start


object HipchatParse {

  def help(exitCode: Int) {
    println("usage: java -jar hipchatparse.jar baseDirectory [resultDirectory]")
    System.exit(exitCode)
  }

  def err(exitCode: Int, msg: String) = {
    println(msg)
    System.exit(exitCode)
  }

  def main(args: Array[String]) {
    if (args.length < 1) help(1)
    if (args(0)=="--help" || args(0)=="-h") help(0)
    val baseDir= args(0)
    if (!Files.exists(Paths.get(baseDir))) err(2, s"$baseDir does not exist")
    val resultDir= if (args.length<2) baseDir else args(1)
    if (!Files.exists(Paths.get(resultDir))) err(2, s"$resultDir does not exist")

    val system = ActorSystem("HipChatParse")
    val mainActor= system.actorOf(HipChatParseMain.props())
    mainActor ! Start(baseDir, resultDir)
  }


}