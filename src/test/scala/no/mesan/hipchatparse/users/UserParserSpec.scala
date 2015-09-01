package no.mesan.hipchatparse.users

import no.mesan.hipchatparse.users.UserParser._
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class UserParserSpec extends FlatSpec {
  val singleInput = """{
            |  "users":[
            |    {
            |      "user_id":1537413,
            |      "email":"lars.reed@mesan.no",
            |      "is_deleted":0,
            |      "is_group_admin":1,
            |      "name":"Lars Reed",
            |      "mention_name":"larsr",
            |      "photo_url":"https:\/\/s3.amazonaws.com\/uploads.hipchat.com\/photos\/1537413\/BbxRw9wnhNellBO_125.png",
            |      "last_active":1440139130,
            |      "created":1418644198,
            |      "status":"available",
            |      "status_message":"",
            |      "timezone":"Europe\/Berlin",
            |      "title":"Konsulentsjef"
            |    }
            |  ]
            |}""".stripMargin
  val tripleInput = """{
            |  "users":[
            |    {
            |      "user_id":1537413,
            |      "email":"lars.reed@mesan.no",
            |      "is_deleted":0,
            |      "is_group_admin":1,
            |      "name":"Lars Reed",
            |      "mention_name":"larsr",
            |      "photo_url":"https:\/\/s3.amazonaws.com\/uploads.hipchat.com\/photos\/1537413\/BbxRw9wnhNellBO_125.png",
            |      "last_active":1440139130,
            |      "created":1418644198,
            |      "status":"available",
            |      "status_message":"",
            |      "timezone":"Europe\/Berlin",
            |      "title":"Konsulentsjef"
            |    },
            |    {
            |      "user_id":1537369,
            |      "email":"trondmo@mesan.no",
            |      "is_deleted":0,
            |      "is_group_admin":1,
            |      "name":"Trond Marius \u00d8vstetun",
            |      "mention_name":"ovstetun",
            |      "photo_url":"https:\/\/secure.gravatar.com\/avatar\/b2687e9b37159bf149c3285abb288bd2?s=125&d=https%3A%2F%2Fwww.hipchat.com%2Fimg%2Fsilhouette_125.png&r=g",
            |      "last_active":1440138905,
            |      "created":1418643142,
            |      "status":"available",
            |      "status_message":"",
            |      "timezone":"Europe\/Berlin",
            |      "title":"CTO"
            |    },
            |    {
            |      "user_id":1639001,
            |      "email":"oysteins@mesan.no",
            |      "is_deleted":0,
            |      "is_group_admin":1,
            |      "name":"\u00d8ystein Skadsem",
            |      "mention_name":"oysteins",
            |      "photo_url":"https:\/\/secure.gravatar.com\/avatar\/8b3e5ca5e3c389c19ad24e6b34f461e5?s=125&d=https%3A%2F%2Fwww.hipchat.com%2Fimg%2Fsilhouette_125.png&r=g",
            |      "last_active":1440138772,
            |      "created":1421393601,
            |      "status":"available",
            |      "status_message":"",
            |      "timezone":"Europe\/Berlin",
            |      "title":"Fagsjef"
            |    }
            |  ]
            |}""".stripMargin
  val missingInput = """{
            |  "users":[
            |    {
            |      "user_id":1537413,
            |      "email":"lars.reed@mesan.no",
            |      "is_deleted":0,
            |      "is_group_admin":1,
            |      "photo_url":"https:\/\/s3.amazonaws.com\/uploads.hipchat.com\/photos\/1537413\/BbxRw9wnhNellBO_125.png",
            |      "last_active":1440139130,
            |      "created":1418644198,
            |      "status":"available",
            |      "status_message":"",
            |      "timezone":"Europe\/Berlin",
            |      "title":"Konsulentsjef"
            |    }
            |  ]
            |}""".stripMargin

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
    assert(u.ID==="1537413")
    assert(u.mention.get==="larsr")
    assert(u.fullName==="Lars Reed")
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
