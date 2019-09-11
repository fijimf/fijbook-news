package com.fijimf.deepfij.news

import java.time.LocalDateTime

import cats.effect.{ContextShift, IO}
import com.fijimf.deepfij.news.model.RssItem
import com.fijimf.deepfij.news.model.RssItem.ItemParam
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.util.{Colors, ExecutionContexts}
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
    check(RssItem.Dao.insert(RssItem(0L, 1L, "DUMMY", "https://junk.z.x.y/ppp/qqq", None, LocalDateTime.now(), LocalDateTime.now(), None,None,None,None)))
  }
  test("update") {
    check(RssItem.Dao.insert(RssItem(34L, 1L, "DUMMY", "https://junk.z.x.y/ppp/qqq", None, LocalDateTime.now(), LocalDateTime.now(), None,None,None,None)))
  }

  test("delete") {
    check(RssItem.Dao.delete(0L))
  }

  test("find") {
    check(RssItem.Dao.find(0L))
  }

  List(
    ItemParam(None, None, None, skipMissing = true, skipUnverified = true),
    ItemParam(None, Some(LocalDateTime.now()), None, skipMissing = true, skipUnverified = true),
    ItemParam(None, None, Some(LocalDateTime.now()), skipMissing = true, skipUnverified = true),
    ItemParam(None, Some(LocalDateTime.now()), Some(LocalDateTime.now()), skipMissing = true, skipUnverified = true),

    ItemParam(None, None, None, skipMissing = true, skipUnverified = false),
    ItemParam(None, Some(LocalDateTime.now()), None, skipMissing = true, skipUnverified = false),
    ItemParam(None, None, Some(LocalDateTime.now()), skipMissing = true, skipUnverified = false),
    ItemParam(None, Some(LocalDateTime.now()), Some(LocalDateTime.now()), skipMissing = true, skipUnverified = false),

    ItemParam(None, None, None, skipMissing = false, skipUnverified = true),
    ItemParam(None, Some(LocalDateTime.now()), None, skipMissing = false, skipUnverified = true),
    ItemParam(None, None, Some(LocalDateTime.now()), skipMissing = false, skipUnverified = true),
    ItemParam(None, Some(LocalDateTime.now()), Some(LocalDateTime.now()), skipMissing = false, skipUnverified = true),

    ItemParam(None, None, None, skipMissing = false, skipUnverified = false),
    ItemParam(None, Some(LocalDateTime.now()), None, skipMissing = false, skipUnverified = false),
    ItemParam(None, None, Some(LocalDateTime.now()), skipMissing = false, skipUnverified = false),
    ItemParam(None, Some(LocalDateTime.now()), Some(LocalDateTime.now()), skipMissing = false, skipUnverified = false),

    ItemParam(Some(1L), None, None, skipMissing = true, skipUnverified = true),
    ItemParam(Some(1L), Some(LocalDateTime.now()), None, skipMissing = true, skipUnverified = true),
    ItemParam(Some(1L), None, Some(LocalDateTime.now()), skipMissing = true, skipUnverified = true),
    ItemParam(Some(1L), Some(LocalDateTime.now()), Some(LocalDateTime.now()), skipMissing = true, skipUnverified = true),

    ItemParam(Some(1L), None, None, skipMissing = true, skipUnverified = false),
    ItemParam(Some(1L), Some(LocalDateTime.now()), None, skipMissing = true, skipUnverified = false),
    ItemParam(Some(1L), None, Some(LocalDateTime.now()), skipMissing = true, skipUnverified = false),
    ItemParam(Some(1L), Some(LocalDateTime.now()), Some(LocalDateTime.now()), skipMissing = true, skipUnverified = false),

    ItemParam(Some(1L), None, None, skipMissing = false, skipUnverified = true),
    ItemParam(Some(1L), Some(LocalDateTime.now()), None, skipMissing = false, skipUnverified = true),
    ItemParam(Some(1L), None, Some(LocalDateTime.now()), skipMissing = false, skipUnverified = true),
    ItemParam(Some(1L), Some(LocalDateTime.now()), Some(LocalDateTime.now()), skipMissing = false, skipUnverified = true),

    ItemParam(Some(1L), None, None, skipMissing = false, skipUnverified = false),
    ItemParam(Some(1L), Some(LocalDateTime.now()), None, skipMissing = false, skipUnverified = false),
    ItemParam(Some(1L), None, Some(LocalDateTime.now()), skipMissing = false, skipUnverified = false),
    ItemParam(Some(1L), Some(LocalDateTime.now()), Some(LocalDateTime.now()), skipMissing = false, skipUnverified = false)
  ).foreach(p => {
    test(s"list $p") {
      check(RssItem.Dao.list(p))
    }
  })




}
