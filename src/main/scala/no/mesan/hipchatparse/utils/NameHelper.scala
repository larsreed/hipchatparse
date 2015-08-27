package no.mesan.hipchatparse.utils

import scala.util.Random

/** Naming utilities*/
trait NameHelper {

  /** Remove path from string. */
  def stripPath(s: String): String= s.replaceAll("^.*[/\\\\]", "")

  /** Make a (hopefully) unique, valid actor name suffix. */
  def makeActorSuffix(s: String): String =
    s.filter(c=> c.toUpper >='A' && c.toUpper <= 'Z') + "_" + Random.nextLong()

}
