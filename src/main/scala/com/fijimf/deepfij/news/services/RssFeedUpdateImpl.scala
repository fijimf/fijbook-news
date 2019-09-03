package com.fijimf.deepfij.news.services

import cats.data.OptionT
import cats.effect._
import cats.implicits._
import com.fijimf.deepfij.news.model.RssItem
import com.fijimf.deepfij.news.util.RssXml
import org.http4s.client.Client
import org.slf4j.{Logger, LoggerFactory}

final case class RssFeedUpdateImpl[F[_]](httpClient: Client[F], repo: RssRepo[F])(implicit F: Async[F])  extends RssFeedUpdate [F]{
  val log:Logger=LoggerFactory.getLogger(getClass)

  private def getUpdates(feedId: Long): F[List[RssItem]] = {
    def infoSafely(msg: String): OptionT[F, Unit] = OptionT.liftF(F.delay(log.info(msg)))
    def debugSafely(msg: String): OptionT[F, Unit] = OptionT.liftF(F.delay(log.info(msg)))

    (for {
      feed <- OptionT(repo.findFeed(feedId))
      _ <- infoSafely(s"Retrieving $feedId - ${feed.url}")
      xml <- OptionT.liftF(httpClient.expect[String](feed.url))
      _ <- infoSafely(s"Retrieved ${xml.length} bytes.")
      createdItems <- OptionT.fromOption[F](createItems(xml, feed.id))
      _ <- infoSafely(s"Created ${createdItems.length} items.")
      _ <- debugSafely("Items:\n\t"+createdItems.map(_.title.take(36)+"...").mkString(",\n\t"))
    } yield {
      createdItems
    }).getOrElseF(F.pure(List.empty[RssItem]))
  }

  def createItems(s: String, id: Long): Option[List[RssItem]] = {
    RssXml.loadXml(s) match {
      case Left(_) => None
      case Right(n) =>
        for {
          channel <- RssXml.findChannel(n)
        } yield {
          RssXml.channelNodeToItems(id, channel)
        }
    }

  }

  override def updateFeed(feedId: Long): F[fs2.Stream[F,RssItem]] = {
    for{
      items<-getUpdates(feedId)
      _ <- F.delay(log.info(s"Retrieved ${items.size} items from the feed"))
      res = repo.saveItems(items)
    } yield{
      res
    }
  }
}
