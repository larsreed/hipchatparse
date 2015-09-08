package no.mesan.hipchatparse.utils

import org.junit.runner.RunWith
import org.scalatest._
import Tools._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ToolsSpec extends FlatSpec with Matchers {

  "ToolsSpec.urlParse" should "handle empty strings" in {
    urlParse("") shouldBe empty
  }

  it should "handle a string containing no URLs" in {
    val result= urlParse("no URLs")
    result should have length 1
    result.head should be (false, "no URLs")
  }

  it should "handle a pure URL" in {
    val result= urlParse("http://fagblogg.mesan.no/?s=test")
    result should have length 1
    result.head should be (true, "http://fagblogg.mesan.no/?s=test")
  }

  it should "handle a string that ends with a URL" in {
    val result= urlParse("search: https://fagblogg.mesan.no/?s=test")
    result should have length 2
    result.head should be (false, "search: ")
    result(1) should be (true, "https://fagblogg.mesan.no/?s=test")
  }

  it should "handle a string that starts with a URL" in {
    val result= urlParse("http://fagblogg.mesan.no/?s=test found")
    result should have length 2
    result.head should be (true, "http://fagblogg.mesan.no/?s=test")
    result(1) should be (false, " found")
  }

  it should "handle a string that contains a URL" in {
    val result= urlParse("search: http://fagblogg.mesan.no/?s=test found")
    result should have length 3
    result.head should be (false, "search: ")
    result(1) should be (true, "http://fagblogg.mesan.no/?s=test")
    result(2) should be (false, " found")
  }

  it should "handle multiple URLs" in {
    val result= urlParse("http://fagblogg.mesan.no/?s=test mailto:noone@nowhere.no ftp://files/1")
    result should have length 5
    result.head should be (true, "http://fagblogg.mesan.no/?s=test")
    result(1) should be (false, " ")
    result(2) should be (true, "mailto:noone@nowhere.no")
    result(3) should be (false, " ")
    result(4) should be (true, "ftp://files/1")
  }
}
