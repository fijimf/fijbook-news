package com.fijimf.deepfij.news

import cats.effect.{ContextShift, IO}
import com.fijimf.deepfij.news.model.RssFeed
import doobie.util.{Colors, ExecutionContexts}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.scalatest.{FunSuite, Matchers}

class RssFeedSpec extends FunSuite with Matchers with doobie.scalatest.IOChecker {

  override val colors: Colors.Ansi.type = doobie.util.Colors.Ansi // just for docs
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:deepfijdb", "fijuser", "mut()mb()"
  )

  test("insert") {
    check(RssFeed.Dao.insert(RssFeed(0L, "DUMMY", "https://junk.z.x.y/ppp/qqq")))
  }

  test("update") {
    check(RssFeed.Dao.insert(RssFeed(19L, "DUMMY", "https://junk.z.x.y/ppp/qqq")))
  }

  test("delete") {
    check(RssFeed.Dao.delete(0L))
  }

  test("find") {
    check(RssFeed.Dao.find(0L))
  }

  test("list") {
    check(RssFeed.Dao.list)
  }
}
