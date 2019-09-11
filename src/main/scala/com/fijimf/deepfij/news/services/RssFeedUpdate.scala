package com.fijimf.deepfij.news.services

import java.time.LocalDateTime

import cats.effect._
import cats.implicits._
import com.fijimf.deepfij.news.model.{RssFeed, RssItem, RssRefreshJob}
import com.fijimf.deepfij.news.util.RssXml
import org.http4s.client.{Client, UnexpectedStatus}
import org.slf4j.{Logger, LoggerFactory}

final case class RssFeedUpdate[F[_]](httpClient: Client[F], repo: RssRepo[F])(implicit F: Async[F]) {
  val log:Logger=LoggerFactory.getLogger(getClass)

  case class FeedUpdateState(feedId: Long, startTime: LocalDateTime, feed: Option[RssFeed], body: Option[String], status: Int, parsedItems: List[RssItem], savedItems: List[RssItem]) {
    def load(): F[FeedUpdateState] = repo.findFeed(feedId) map {
      case Some(f) => copy(feed = Some(f))
      case None => this
    }

    def retrieve(): F[FeedUpdateState] = {
      feed match {
        case Some(f) =>
          httpClient.expect[String](f.url).attempt.map(_.fold({
            case u: UnexpectedStatus => copy(status = u.status.code)
            case _: Throwable => copy(status = -1)
          },
            str => copy(body = Some(str), status = 200)
          ))
        case None => F.pure(this)

      }
    }

    def createItems(): F[FeedUpdateState] = {
      body match {
        case Some(s) => RssXml.loadXml(s) match {
          case Left(_) => F.pure(this)
          case Right(n) =>
            F.pure(copy(parsedItems = (for {
              channel <- RssXml.findChannel(n)
            } yield {
              RssXml.channelNodeToItems(feedId, channel)
            }).getOrElse(List.empty[RssItem])))
        }
        case None => F.pure(this)
      }
    }

    def updateItems(): F[FeedUpdateState] = {
      repo
        .saveItems(parsedItems)
        .compile[F, F, RssItem]
        .fold(List.empty[RssItem]) { case (b, r) => r :: b }
        .map(l => copy(savedItems = l))
    }

    def recordJob(): F[RssRefreshJob] = {
      for {
        j <- F.delay {
          RssRefreshJob(0L, feedId, startTime, LocalDateTime.now() /* <- Is that an effect */ , status, parsedItems.size, savedItems.size)
        }
        k <- repo.insertRefreshJob(j)
      } yield {
        k
      }
    }
  }

  object FeedUpdateState {
    def init(id: Long): FeedUpdateState = FeedUpdateState(id, LocalDateTime.now(), None, None, 0, List.empty[RssItem], List.empty[RssItem])

  }

  def updateFeed(feedId: Long): F[List[RssItem]] = {
    for {
      a <- FeedUpdateState.init(feedId).load()
      b <- a.retrieve()
      c <- b.createItems()
      d <- c.updateItems()
      _ <- d.recordJob()
    } yield {
      d.savedItems
    }
  }


}
