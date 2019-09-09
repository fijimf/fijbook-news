package com.fijimf.deepfij.news.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.annotation.tailrec
import scala.util.Try

object DateUtil {
  val dateFmts: List[DateTimeFormatter] = List(DateTimeFormatter.ISO_LOCAL_DATE_TIME, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

  def stringToDateTime(s:String):Option[LocalDateTime] = {
    @tailrec
    def traverse(fs: List[DateTimeFormatter]): Option[LocalDateTime] = fs match {
      case Nil => None
      case head :: tail =>
        Try {
          LocalDateTime.parse(s, head)
        }.toOption match {
          case Some(d) => Some(d)
          case None => traverse(tail)
        }
    }
    traverse(dateFmts)
  }

}
