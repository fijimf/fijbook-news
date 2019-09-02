package com.fijimf.deepfij.news.services

import java.sql.Timestamp
import java.time.LocalDateTime

import cats.effect.{Bracket, ContextShift, IO, Sync}
import cats.implicits._
import com.fijimf.deepfij.news.model.{RssFeed, RssItem}
import doobie.implicits._
import doobie.util.Meta
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import fs2.Pure


class RssRepo[M[_]](xa: Transactor[M])(implicit M: Bracket[M, Throwable]) {

  implicit val localDateTimeMeta: Meta[LocalDateTime] = Meta[Timestamp].imap(ts => ts.toLocalDateTime)(ldt => Timestamp.valueOf(ldt))

  def insertFeed(rssFeed: RssFeed): fs2.Stream[M, RssFeed] = {
    assert(rssFeed.id === 0L)
    RssFeed.Dao.insert(rssFeed).withGeneratedKeys[RssFeed]("id", "name", "url").transact(xa)
  }

  def updateFeed(rssFeed: RssFeed): fs2.Stream[M, RssFeed] = {
    assert(rssFeed.id =!= 0L)
    RssFeed.Dao.update(rssFeed).withGeneratedKeys[RssFeed]("id", "name", "url").transact(xa)
  }

  def deleteFeed(id: Long): M[Int] = RssFeed.Dao.delete(id).run.transact(xa)

  def listFeeds(): M[List[RssFeed]] = RssFeed.Dao.list.to[List].transact(xa)

  def findFeed(id: Long): M[Option[RssFeed]] = RssFeed.Dao.find(id).option.transact(xa)


  def insertItem(item: RssItem): fs2.Stream[M, RssItem] = {
    assert(item.id === 0L)
    RssItem.Dao.insert(item).withGeneratedKeys[RssItem]("id", "feed_id", "title", "url", "image", "published_at", "retrieved_at").transact(xa)
  }

  def updateItem(item: RssItem): fs2.Stream[M, RssItem] = {
    assert(item.id =!= 0L)
    RssItem.Dao.update(item).withGeneratedKeys[RssItem]("id", "feed_id", "title", "url", "image", "published_at", "retrieved_at").transact(xa)
  }

  def saveItems(items: List[RssItem]): fs2.Stream[M, RssItem] = {
    fs2.Stream.emits[Pure, RssItem](items).flatMap(i => upsertItem(i))
  }

  private def upsertItem(i: RssItem): fs2.Stream[M, RssItem] = {
    if (i.id === 0L)
      RssItem.Dao.insert(i).withGeneratedKeys[RssItem]("id", "feed_id", "title", "url", "image", "published_at", "retrieved_at").transact(xa)
    else
      RssItem.Dao.update(i).withGeneratedKeys[RssItem]("id", "feed_id", "title", "url", "image", "published_at", "retrieved_at").transact(xa)
  }

  def deleteItem(id: Long): M[Int] = RssItem.Dao.delete(id).run.transact(xa)

    def listItems(): M[List[RssItem]] = RssItem.Dao.list.to[List].transact(xa)

    def listItemsByFeed(feedId: Long): M[List[RssItem]] = RssItem.Dao.listById(feedId).to[List].transact(xa)

    def listRecentItems(since: LocalDateTime): M[List[RssItem]] = RssItem.Dao.listAfter(since).to[List].transact(xa)

    def listRecentItemsByFeed(feedId: Long, since: LocalDateTime): M[List[RssItem]] = RssItem.Dao.listByIdAfter(feedId, since).to[List].transact(xa)

    def findItem(id: Long): M[Option[RssItem]] = RssItem.Dao.find(id).option.transact(xa)


  }


  object Junk {
    def main(args: Array[String]): Unit = {

      import doobie.util.ExecutionContexts
      implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
      val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
        "org.postgresql.Driver", // driver classname
        "jdbc:postgresql://localhost:5432/deepfijdb", // connect URL (driver-specific)
        "fijuser", // user
        "mut()mb()", // password
        ExecutionContexts.synchronous // just for testing
      )

      val ints: List[Int] = List(RssItem.Dao.dropDdl.run, RssFeed.Dao.dropDdl.run, RssItem.Dao.createDdl.run, RssFeed.Dao.createDdl.run).sequence.transact(xa).unsafeRunSync()
      println(ints)

    }


  }