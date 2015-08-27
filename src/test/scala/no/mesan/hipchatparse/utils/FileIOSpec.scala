package no.mesan.hipchatparse.utils

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import FileIO._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FileIOSpec extends FlatSpec {
  val dir= "src/test/resources/scanDirTest"

  "scanDir" should "return empty lists on non-existing directory" in {
    val res= scanDir("fooFight")
    assert(res.dirs.size===0, "dirs")
    assert(res.files.size===0, "files")
  }

  it should "find files and subdirectories" in {
    val res= scanDir(dir)
    assert(res.dirs.size===1, "dirs")
    assert(res.files.size===2, "files")
  }

  it should "return full path names" in {
    val res= scanDir(dir)
    val all= res.dirs ++ res.files
    all.foreach(p=> assert(p.matches(".*" + dir.replaceAll("/", ".") + ".*"), p + " contains"))
    all.foreach(p=> assert(!p.startsWith("src"), p + " startswith"))
  }

}
