package com.fijimf.deepfij.news

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, Timer}
import com.fijimf.deepfij.news.services.{RssFeedUpdateImpl, RssRepo}
import doobie.util.transactor.Transactor
import fs2.Stream
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

object RssServer {

  import scala.concurrent.ExecutionContext.Implicits.global

  @SuppressWarnings(Array("org.wartremover.warts.Nothing", "org.wartremover.warts.Any"))
  def stream[F[_] : ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, ExitCode] = {
    for {
      client <- BlazeClientBuilder[F](global).stream
      transactor = Transactor.fromDriverManager[F](
        "org.postgresql.Driver", "jdbc:postgresql:deepfijdb", "fijuser", "mut()mb()"
      )
      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      repo = new RssRepo(transactor)
      httpApp = RssRoutes.rssFeedRoutes(repo, RssFeedUpdateImpl(client, repo)).orNotFound
      finalHttpApp = Logger.httpApp[F](logHeaders = true, logBody = true)(httpApp)
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(port = 8080, host = "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
    }.drain
}