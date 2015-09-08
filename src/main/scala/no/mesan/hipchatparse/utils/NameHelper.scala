package no.mesan.hipchatparse.utils

import scala.util.Random

/** Naming utilities*/
trait NameHelper {

  /** Remove path from string. */
  def stripPath(s: String): String= s.replaceAll("^.*[/\\\\]", "")

  /** Map room name to room dir name. */
  def dirMap(roomName: String): String= roomName.replaceAll("[:/\\\\]", "_").trim

  /** Create HTML ID from name. */
  def name2id(roomName: String): String=
    roomName.filter(((('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9')).toSet + '_').contains)

  /** Make a (hopefully) unique, valid actor name suffix. */
  def makeActorSuffix(s: String): String =
    s.filter(c=> c.toUpper >='A' && c.toUpper <= 'Z') + "_" + Random.nextLong()

  /** Remove quotes from JSON names. */
  def washJson(s: String)= s.substring(1).replaceFirst(".$", "" )

}
