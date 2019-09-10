package com.fijimf.deepfij.news

import java.time.format.DateTimeFormatter

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.news.model.RssRefreshJob.Dao.JobParam
import com.fijimf.deepfij.news.model.{RssFeed, RssItem, RssRefreshJob}
import com.fijimf.deepfij.news.services.{RssFeedUpdateImpl, RssRepo}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.slf4j.{Logger, LoggerFactory}

object RssRoutes {
  val log: Logger =LoggerFactory.getLogger(RssRoutes.getClass)
  implicit val feedEncoder: Encoder[RssFeed] = deriveEncoder[RssFeed]
  implicit val itemEncoder: Encoder[RssItem] = deriveEncoder[RssItem]
  implicit val refreshJobEncoder: Encoder[RssRefreshJob] = deriveEncoder[RssRefreshJob]
  implicit val feedDecoder: Decoder[RssFeed] = deriveDecoder[RssFeed]
  implicit val itemDecoder: Decoder[RssItem] = deriveDecoder[RssItem]
  implicit val refreshJobDecoder: Decoder[RssRefreshJob] = deriveDecoder[RssRefreshJob]

  implicit def intEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Int] = jsonEncoderOf
  implicit def feedEntityEncoder[F[_] : Applicative]: EntityEncoder[F, RssFeed] = jsonEncoderOf
  implicit def itemEntityEncoder[F[_] : Applicative]: EntityEncoder[F, RssItem] = jsonEncoderOf
  implicit def refreshJobEntityEncoder[F[_] : Applicative]: EntityEncoder[F, RssRefreshJob] = jsonEncoderOf

  implicit def feedEntityDecoder[F[_] : Sync]: EntityDecoder[F, RssFeed] = jsonOf

  implicit def optFeedEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Option[RssFeed]] = jsonEncoderOf

  implicit def listFeedEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[RssFeed]] = jsonEncoderOf
  implicit def listItemEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[RssItem]] = jsonEncoderOf
  implicit def listRefreshJobEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[RssRefreshJob]] = jsonEncoderOf

  def rssFeedRoutes[F[_]](r: RssRepo[F], updater: RssFeedUpdateImpl[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "feeds" =>
        for {
          list <- r.listFeeds()
          resp <- Ok(list)
        } yield {
          resp
        }
      case GET -> Root / "feeds" / feedId =>
        for {
          opt <- r.findFeed(feedId.toLong)
          resp <- opt match {
            case None => NotFound()
            case Some(f) => Ok(f)
          }
        } yield {
          resp
        }
      case GET -> Root / "feeds" / feedId / "items" => // [after=yyyyMMddHHmmss] [skipMissing] [skipUnverified]
        for {
          items <- r.listItemsByFeed(feedId.toLong)
          resp <- items match {
            case Nil => NotFound()
            case list => Ok(list)
          }
        } yield {
          resp
        }
      case GET -> Root / "feeds" / feedId / "items" / itemId =>
        for {
          items <- r.listItemsByFeed(feedId.toLong)
          resp <- items.filter(_.id === itemId.toLong) match {
            case Nil => NotFound()
            case list => Ok(list)
          }
        } yield {
          resp
        }
      case req@POST -> Root / "feeds" =>
        for {
          feed <- req.as[RssFeed]
          strm = r.insertFeed(feed)
          list <- strm.compile[F, F, RssFeed].to[List]
          resp <- Ok(list)
        } yield {
          resp
        }
      case DELETE -> Root / "feeds" / feedId =>
        for {
          i <- r.deleteFeed(feedId.toLong)
          resp <- Ok(i)
        } yield {
          resp
        }
    }
  }

  def rssItemRoutes[F[_]](r: RssRepo[F], updater: RssFeedUpdateImpl[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "items" => //[after=yyyyMMddHHmmss] [skipMissing] [skipUnverified]
        for {
          items <- r.listItems()
          resp <- Ok(items)
        } yield {
          resp
        }
      case GET -> Root / "items" / itemId =>
        for {
          opt <- r.findItem(itemId.toLong)
          resp <- opt match {
            case None => NotFound()
            case Some(i) => Ok(i)
          }
        } yield {
          resp
        }
      case DELETE -> Root / "items" / itemId => for {
        i <- r.deleteItem(itemId.toLong)
        resp <- Ok(i)
      } yield {
        resp
      }


    }
  }

  def rssJobHistoryRoutes[F[_]](r: RssRepo[F], updater: RssFeedUpdateImpl[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root / "refreshJobs" =>
        val p=JobParam.fromReq(req)
        for {
          list<- r.listRefreshJobs(p)
          resp<-Ok(list)
        } yield {
          resp
        }

      case req@DELETE -> Root / "refreshJobs" => val p=JobParam.fromReq(req)
        for {
          num<- r.deleteRefreshJobs(p)
          resp<-Ok(num)
        } yield {
          resp
        }
    }
  }

  def rssActionRoutes[F[_]](r: RssRepo[F], updater: RssFeedUpdateImpl[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case POST -> Root / "refresh" / feedId =>
        for {
          items <- updater.updateFeed(feedId.toLong)
          _ <- F.delay(log.info(s"${items.size} new items saved."))
          resp <- Ok(items)
        } yield {
          resp
        }

      case POST -> Root / "verify" / feedId => NotImplemented() //[after=yyyyMMddHHmmss] [skipMissing] [skipUnverified]
    }
  }
}