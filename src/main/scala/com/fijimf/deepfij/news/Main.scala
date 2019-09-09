package com.fijimf.deepfij.news

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    RssServer
      .stream[IO]
      .compile[IO, IO, ExitCode]
      .drain
      .as(ExitCode.Success)
  }
}