package no.mesan.hipchatparse.rooms

import no.mesan.hipchatparse.rooms.RoomParser._
import org.junit.runner.RunWith
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class RoomParserSpec extends FlatSpec with Matchers {
   val singleInput =
     """[
       | {
       |    "date":"2015-08-21T06:32:37+0000",
       |    "from":{
       |      "name":"Lars Reed",
       |      "user_id":1537413
       |    },
       |    "message":"public final String name..."
       |  }
       |]
     """.stripMargin
   val tripleInput =
     """[
       | {
       |    "date":"2015-08-21T06:32:37+0000",
       |    "from":{
       |      "name":"Lars Reed",
       |      "user_id":1537413
       |    },
       |    "message":"public final String name..."
       |  },
       |  {
       |    "date":"2015-08-21T06:32:52+0000",
       |    "from":{
       |      "name":"\u00d8ystein Skadsem",
       |      "user_id":1639001
       |    },
       |    "message":":)"
       |  },
       |  {
       |    "date":"2015-08-21T06:32:59+0000",
       |    "from":{
       |      "name":"Knut Esten Melands\u00f8 Neks\u00e5",
       |      "user_id":1780230
       |    },
       |    "message":"da slipper vi java ee i samme slengen"
       |  }
       |]
     """.stripMargin
   val missingInput =
     """[
       | {
       |    "date":"2015-08-21T06:32:37+0000",
       |    "from":{
       |      "user_id":1537413
       |    }
       |]
     """.stripMargin

   "jsonParse" should "fail on empty input" in {
     jsonParse("") shouldBe 'failure
   }

   it should "fail on invalid input" in {
     jsonParse("goble ][ freak") shouldBe 'failure
   }

   it should "accept a single input record" in {
     val res= jsonParse(singleInput).get
     res should have length 1

     val u= res.head
     u.text shouldBe "public final String name..."
     u.user.fullName shouldBe "Lars Reed"
     u.user.ID shouldBe "1537413"
   }

   it should "parse multiple users" in {
     jsonParse(tripleInput).get should have length 3
   }

   it should "handle missing data, but it doesn't..." in {
     jsonParse(missingInput) shouldBe 'failure // TODO...
   }
 }
