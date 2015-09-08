package no.mesan.hipchatparse.roomlist

import no.mesan.hipchatparse.roomlist.RoomlistParser._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class RoomlistParserSpec extends FlatSpec with Matchers {
   val singleInput =
     """|{
       |  "rooms":[
       |    {
       |      "room_id":1055412,
       |      "name":"Mesan",
       |      "topic":"Alt generelt om det vi driver med i Mesan",
       |      "last_active":1441025084,
       |      "created":1424335148,
       |      "owner_user_id":1639001,
       |      "is_archived":false,
       |      "is_private":false,
       |      "guest_access_url":null,
       |      "xmpp_jid":"222885_mesan@conf.hipchat.com"
       |    }
       | ]
       |}""".stripMargin
   val tripleInput =
     """|{
       |  "rooms":[
       |    {
       |      "room_id":1055412,
       |      "name":"Mesan",
       |      "topic":"Alt generelt om det vi driver med i Mesan",
       |      "last_active":1441025084,
       |      "created":1424335148,
       |      "owner_user_id":1639001,
       |      "is_archived":false,
       |      "is_private":false,
       |      "guest_access_url":null,
       |      "xmpp_jid":"222885_mesan@conf.hipchat.com"
       |    },
       |    {
       |      "room_id":1511748,
       |      "name":"Mesan: Old",
       |      "last_active":1441006038,
       |      "created":1431429060,
       |      "owner_user_id":2041761,
       |      "is_archived":false,
       |      "is_private":true,
       |      "guest_access_url":null,
       |      "xmpp_jid":"222885_mesan_old@conf.hipchat.com"
       |    },
       |    {
       |      "room_id":1651836,
       |      "name":"Mesan Gaming",
       |      "topic":"Start the game, already! | Skriv inn din Steam ID her: https:\/\/dev.mesan.no\/confluence\/display\/sosial\/Gaming-profiler",
       |      "last_active":1440440994,
       |      "created":1434960066,
       |      "owner_user_id":1780390,
       |      "is_archived":false,
       |      "is_private":false,
       |      "guest_access_url":null,
       |      "xmpp_jid":"222885_mesan_gaming@conf.hipchat.com"
       |    }
       | ]
       |}""".stripMargin
   val missingInput =
     """|{
       |  "rooms":[
       |    {
       |      "room_id":1055412,
       |      "topic":"Alt generelt om det vi driver med i Mesan",
       |      "last_active":1441025084,
       |      "created":1424335148,
       |      "owner_user_id":1639001,
       |      "is_archived":false,
       |      "is_private":false,
       |      "guest_access_url":null,
       |      "xmpp_jid":"222885_mesan@conf.hipchat.com"
       |    }
       | ]
       |}""".stripMargin
  val specialInput =
    """|{
      |  "rooms":[
      |    {
      |      "room_id":1511748,
      |      "name":"Mesan: Old",
      |      "last_active":1441006038,
      |      "created":1431429060,
      |      "owner_user_id":2041761,
      |      "is_archived":false,
      |      "is_private":true,
      |      "guest_access_url":null,
      |      "xmpp_jid":"222885_mesan_old@conf.hipchat.com"
      |    }
      | ]
      |}""".stripMargin

   "jsonParse" should "fail on empty input" in {
     jsonParse("") shouldBe 'failure
   }

   it should "fail on invalid input" in {
     jsonParse("goble ][ freak") shouldBe 'failure
   }

   it should "accept a single input record" in {
     val result= jsonParse(singleInput).get
     result should have length 1

     val first= result.head
     first.id should equal("Mesan")
     first.name should equal("Mesan")
     first shouldBe 'public
   }

   it should "parse multiple rooms" in {
     jsonParse(tripleInput).get should have length 3
   }

  it should "handle special characters" in {
    val result= jsonParse(specialInput).get.head
    result.id shouldBe "Mesan_ Old"
    result.name shouldBe "Mesan: Old"
  }

  it should "detect private rooms" in {
    val result= jsonParse(specialInput).get.head
    result should not be 'public
  }

   it should "handle missing data, but it doesn't..." in {
     jsonParse(missingInput) shouldBe 'failure // TODO...
   }
 }
