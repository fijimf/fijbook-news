package com.fijimf.deepfij.news

import java.time.LocalDateTime

import cats.effect.{ContextShift, IO}
import com.fijimf.deepfij.news.model.RssRefreshJob.Dao.JobParam
import com.fijimf.deepfij.news.model.{RssItem, RssRefreshJob}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.util.{Colors, ExecutionContexts}
import org.scalatest.{FunSuite, Matchers}

class RssRefreshJobSpec extends FunSuite with Matchers with doobie.scalatest.IOChecker {

  override val colors: Colors.Ansi.type = doobie.util.Colors.Ansi // just for docs
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:deepfijdb", "fijuser", "mut()mb()"
  )


  test(s"create ddl") {
    check(RssRefreshJob.Dao.createDdl)
  }

  test(s"drop ddl") {
    check(RssRefreshJob.Dao.dropDdl)
  }

  test("insert") {
    check(RssRefreshJob.Dao.insert(RssRefreshJob (0L,1L,LocalDateTime.now(), LocalDateTime.now().plusSeconds(10L), 200, 12, 0)))
  }

  test("list all") {
    check(RssRefreshJob.Dao.list(JobParam(None, None, None)))
  }
  test("list feed") {
    check(RssRefreshJob.Dao.list(JobParam(Some(1L), None, None)))
  }
  test("list before") {
    check(RssRefreshJob.Dao.list(JobParam(None, Some(LocalDateTime.now()), None)))
  }
  test("list after") {
    check(RssRefreshJob.Dao.list(JobParam(None, None, Some(LocalDateTime.now()))))
  }
  test("list feed between") {
    check(RssRefreshJob.Dao.list(JobParam(Some(1L), Some(LocalDateTime.now().plusDays(1)), Some(LocalDateTime.now()))))
  }

  test("delete all") {
    check(RssRefreshJob.Dao.delete(JobParam(None, None, None)))
  }
  test("delete feed") {
    check(RssRefreshJob.Dao.delete(JobParam(Some(1L), None, None)))
  }
  test("delete before") {
    check(RssRefreshJob.Dao.delete(JobParam(None, Some(LocalDateTime.now()), None)))
  }
  test("delete after") {
    check(RssRefreshJob.Dao.delete(JobParam(None, None, Some(LocalDateTime.now()))))
  }
  test("delete feed between") {
    check(RssRefreshJob.Dao.delete(JobParam(Some(1L), Some(LocalDateTime.now().plusDays(1)), Some(LocalDateTime.now()))))
  }
}
