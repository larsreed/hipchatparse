package no.mesan.hipchatparse.utils

import java.util.regex.Pattern

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.global

/** Lousy name... TODO */
object Tools {

  private val urlRegexp = """(?i)\b((?:[a-z][\w-]+:(?:/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}/)(?:[^\s()<>]|\(([^\s()<>]+|(\([^\s()<>]\)))*\))+(?:\(([^\s()<>]|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))"""
  private lazy val urlPattern= Pattern.compile(urlRegexp)
  // Source: http://daringfireball.net/2010/07/improved_regex_for_matching_urls

  /** Parse a string possibly containing URLs. Each element is a pair of true (is URL)/false (not URL) with the corresponding text. */
  def urlParse(input: String): List[(Boolean, String)] = {
    val inputs= input.split("[\n\r]+").toList
    var res = List.empty[(Boolean, String)]
    for ( s <- inputs )  {
      val matcher = urlPattern.matcher(s)
      var lastIndex = 0

      while (matcher.find()) {
        val starts = matcher.start()
        val ends = matcher.end()
        if (starts > lastIndex) res ::=(false, s.substring(lastIndex, starts))
        res ::=(true, s.substring(starts, ends))
        lastIndex = ends
      }
      if (s.length > lastIndex) res ::=(false, s.substring(lastIndex))
      res ::= (false, "\n")
    }
    res.tail.reverse
  }

  /** Like urlParse, with a timeout. */
  def urlParseMaybe(input: String, duration: Duration)(implicit ec: ExecutionContext = ExecutionContext.global): Try[List[(Boolean, String)]] = {
    val fut= Future { urlParse(input) }
    try { Success(Await.result(fut, duration)) }
    catch { case e: Exception => Failure(e) }
  }

}
