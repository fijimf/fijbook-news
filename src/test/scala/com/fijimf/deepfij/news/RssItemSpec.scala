package com.fijimf.deepfij.news

import java.time.LocalDateTime

import cats.effect.{ContextShift, IO}
import com.fijimf.deepfij.news.model.RssItem
import doobie.util.{Colors, ExecutionContexts}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.scalatest.{FunSuite, Matchers}

class RssItemSpec extends FunSuite with Matchers with doobie.scalatest.IOChecker {

  override val colors: Colors.Ansi.type = doobie.util.Colors.Ansi // just for docs
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:deepfijdb", "fijuser", "mut()mb()"
  )


  test(s"create ddl") {
    check(RssItem.Dao.createDdl)
  }

  test(s"drop ddl") {
    check(RssItem.Dao.dropDdl)
  }

  test("insert") {
    check(RssItem.Dao.insert(RssItem(0L, 1L, "DUMMY", "https://junk.z.x.y/ppp/qqq", None, LocalDateTime.now(), LocalDateTime.now())))
  }
  test("update") {
    check(RssItem.Dao.insert(RssItem(34L, 1L, "DUMMY", "https://junk.z.x.y/ppp/qqq", None, LocalDateTime.now(), LocalDateTime.now())))
  }

  test("delete") {
    check(RssItem.Dao.delete(0L))
  }

  test("find") {
    check(RssItem.Dao.find(0L))
  }

  test("list") {
    check(RssItem.Dao.list)
  }

  test("list by feed") {
    check(RssItem.Dao.listById(0L))
  }

  test("list since") {
    check(RssItem.Dao.listAfter(LocalDateTime.now.minusDays(1)))
  }

  test("list by feed since") {
    check(RssItem.Dao.listByIdAfter(0L, LocalDateTime.now.minusDays(1)))
  }

}
