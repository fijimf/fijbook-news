package com.fijimf.deepfij.news

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.news.model.{RssFeed, RssItem}
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
  implicit val feedDecoder: Decoder[RssFeed] = deriveDecoder[RssFeed]
  implicit val itemDecoder: Decoder[RssItem] = deriveDecoder[RssItem]

  implicit def feedEntityEncoder[F[_] : Applicative]: EntityEncoder[F, RssFeed] = jsonEncoderOf
  implicit def itemEntityEncoder[F[_] : Applicative]: EntityEncoder[F, RssItem] = jsonEncoderOf

  implicit def feedEntityDecoder[F[_] : Sync]: EntityDecoder[F, RssFeed] = jsonOf

  implicit def optFeedEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Option[RssFeed]] = jsonEncoderOf

  implicit def listFeedEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[RssFeed]] = jsonEncoderOf
  implicit def listItemEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[RssItem]] = jsonEncoderOf

  def rssFeedRoutes[F[_]](r: RssRepo[F], updater: RssFeedUpdateImpl[F])(implicit z: Sync[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "feeds" =>
        for {
          list <- r.listFeeds()
          resp <- Ok(list)
        } yield {
          resp
        }
      case GET -> Root / "feeds" / id =>
        for {
          list <- r.findFeed(id.toLong)
          resp <- Ok(list)
        } yield {
          resp
        }
      case req@POST -> Root / "feeds" / "new" =>
        for {
          feed <- req.as[RssFeed]
          strm = r.insertFeed(feed)
          list<-strm.compile[F,F,RssFeed].to[List]
          resp <- Ok(list)
        } yield {
          resp
        }
      case POST -> Root / "feeds" / id / "refresh" =>
        for {
          items <- updater.updateFeed(id.toLong)
          list <- items.compile[F,F,RssItem].to[List]
          _ <- z.delay(log.info(s"${list.size} new items saved."))
          resp <- Ok(list)
        } yield {
          resp
        }
    }
  }
}