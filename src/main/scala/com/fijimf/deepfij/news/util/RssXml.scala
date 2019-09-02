package com.fijimf.deepfij.news.util

import java.nio.charset.Charset
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZonedDateTime}

import com.fijimf.deepfij.news.model.RssItem

import scala.util.Try
import scala.xml.{Node, XML}

object RssXml {
  def loadXml(s:String): Either[Throwable,Node] = Try {
    XML.loadString(s)
  }.toEither

  def findChannel(n:Node) :Option[Node]={
    for {
      channel <- (n \ "channel").headOption
    } yield channel
  }

  @SuppressWarnings(Array("org.wartremover.warts.Option2Iterable"))
  def channelNodeToItems(id: Long, channel: Node): List[RssItem] = {
    (channel \\ "item").flatMap(item => {
      for {
        title <- (item \ "title").headOption
        link <- (item \ "link").headOption
        img = (item \ "image").headOption
        pubDate <- (item \ "pubDate").headOption
        date <- parseDate(pubDate.text)
      } yield {
        RssItem(0L, id, scrubText(title), text(link), img.map(text), date.toLocalDateTime, LocalDateTime.now())
      }
    }).toList
  }

  private def text(n: Node): String = n.text.trim

  private def scrubText(n: Node): String = {
    val charset: Charset = Charset.forName("UTF-8")
    charset.decode(charset.encode(text(n))).toString
  }

  private def parseDate(str: String): Option[ZonedDateTime] = {
    val formatters = List(
      DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss zzz"), //Espn
      DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z"), //CBS
      DateTimeFormatter.ISO_ZONED_DATE_TIME,
      DateTimeFormatter.RFC_1123_DATE_TIME
    )
    formatters.foldLeft(Option.empty[ZonedDateTime]) {
      case (o, f) => o.orElse(Try {
        ZonedDateTime.parse(str.trim, f)
      }.toOption)
    }
  }
}
