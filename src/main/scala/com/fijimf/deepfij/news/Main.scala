package com.fijimf.deepfij.news

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      conf <- configure()
      exitCode <- RssServer
        .stream[IO](conf)
        .compile[IO, IO, ExitCode]
        .drain
        .as(ExitCode.Success)
    } yield {
      exitCode
    }
  }

  def configure(): IO[Config] = {
    IO.delay {
      ConfigFactory.load()
    }
  }
}