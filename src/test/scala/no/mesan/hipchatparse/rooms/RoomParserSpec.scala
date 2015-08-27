package no.mesan.hipchatparse.rooms

import no.mesan.hipchatparse.rooms.RoomParser._
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class RoomParserSpec extends FlatSpec {
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
     val res= jsonParse("")
     assert(res isFailure)
   }

   it should "fail on invalid input" in {
     val res= jsonParse("goble ][ freak")
     assert(res isFailure)
   }

   it should "accept a single input record" in {
     val res= jsonParse(singleInput).get
     assert(res.length===1)
     //noinspection ZeroIndexToHead
     val u= res(0)
     assert(u.user.ID==="1537413")
     assert(u.text==="public final String name...")
     assert(u.user.fullName==="Lars Reed")
   }

   it should "parse multiple users" in {
     val res= jsonParse(tripleInput).get
     assert(res.length===3)
   }

   it should "handle missing data, but it doesn't..." in {
     val res= jsonParse(missingInput)
     assert(res isFailure) // TODO...
   }
 }
