package no.mesan.hipchatparse.rooms

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class WikiRoomFormatterSpec extends FlatSpec {

  "WikiRoomFormatter" should "quote pipes" in {
      assert(WikiRoomFormatter.wash("| line | one |")==="\\| line \\| one \\|")
  }

  it should "quote code" in {
    assert(WikiRoomFormatter.wash("  /code some(a) { ... } ")==="{noformat}some(a) { ... } {noformat}")
  }

  it should "replace user mentions" in {
    assert(WikiRoomFormatter.wash("Hi, @larsr!")==="Hi, [~larsr]!")
  }
}
