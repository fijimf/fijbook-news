package com.fijimf.deepfij.news.services

import java.util.concurrent.TimeUnit

import cats.effect._
import cats.implicits._
import com.fijimf.deepfij.news.model.RssItem
import com.fijimf.deepfij.news.model.RssItem.ItemParam
import org.http4s.client.{Client, UnexpectedStatus}
import org.slf4j.{Logger, LoggerFactory}

final case class RssFeedVerify[F[_]](httpClient: Client[F], repo: RssRepo[F])(implicit F: Async[F], clock: Clock[F]) {
  val log: Logger = LoggerFactory.getLogger(getClass)

  def verifyFeed(p: ItemParam): F[List[RssItem]] = {
    for {
      a <- repo.listItems(p)
      x <- verifyItems(a)
    } yield {
      x
    }
  }

  def verifyItems(li:List[RssItem]):F[List[RssItem]]={
    li.map(verifyItem).sequence.map(_.flatMap(_.toList))
  }

  def verifyItem(i: RssItem): F[Option[RssItem]] = {
    for {
      s <- clock.realTime(TimeUnit.MILLISECONDS)
      a <- httpClient.expect[String](i.url).map(b => (b.length, 200))
        .recover {
          case u: UnexpectedStatus => (0, u.status.code)
          case t: Throwable => (-1, -1)
        }
      e <- clock.realTime(TimeUnit.MILLISECONDS)
      i2 <- F.delay(i.verify(a._2, (e - s).toInt, a._1))
      u <- repo.updateItem(i2).compile[F, F, RssItem].last
    } yield {
      u
    }
  }

}
