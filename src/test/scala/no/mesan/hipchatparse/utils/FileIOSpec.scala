package no.mesan.hipchatparse.utils

import org.junit.runner.RunWith
import org.scalatest.{Matchers, FlatSpec}
import FileIO._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FileIOSpec extends FlatSpec with Matchers {
  val dir= "src/test/resources/scanDirTest"

  "scanDir" should "return empty lists on non-existing directory" in {
    val res= scanDir("fooFight")
    res.dirs should have length 0
    res.files should have length 0
  }

  it should "find files and subdirectories" in {
    val res= scanDir(dir)
    res.dirs should have length 1
    res.files should have length 2
  }

  it should "return full path names" in {
    val res= scanDir(dir)
    val all= res.dirs ++ res.files
    all.foreach(p=> assert(p.matches(".*" + dir.replaceAll("/", ".") + ".*"), p + " contains"))
    all.foreach(p=> assert(!p.startsWith("src"), p + " startswith"))
  }
}
