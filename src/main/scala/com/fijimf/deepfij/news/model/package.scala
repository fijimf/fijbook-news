package com.fijimf.deepfij.news

import cats.Applicative
import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

package object model {
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

}
