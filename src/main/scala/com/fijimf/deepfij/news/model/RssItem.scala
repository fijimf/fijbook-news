package com.fijimf.deepfij.news.model

import java.sql.Timestamp
import java.time.LocalDateTime

import doobie.implicits._
import doobie.util.Meta

final case class
RssItem(id: Long, rssFeedId: Long, title: String, url: String, image: Option[String], publishedAt: LocalDateTime, retrievedAt: LocalDateTime)

object RssItem {

  object Dao {
    implicit val localDateTimeMeta: Meta[LocalDateTime] = Meta[Timestamp].imap(ts => ts.toLocalDateTime)(ldt => Timestamp.valueOf(ldt))

    def createDdl: doobie.Update0 =
      sql"""
      CREATE TABLE rss_item (
        id BIGSERIAL NOT NULL,
        feed_id BIGINT NOT NULL,
        title VARCHAR(144) NOT NULL,
        url VARCHAR(256) NOT NULL,
        image VARCHAR(256) NULL,
        published_at TIMESTAMP NOT NULL,
        retrieved_at TIMESTAMP NOT NULL
      );
      CREATE UNIQUE INDEX ON rss_item(feed_id, url);
       """.update

    def dropDdl: doobie.Update0 =
      sql"""
       DROP TABLE IF EXISTS rss_item;
       """.update


    def delete(id: Long): doobie.Update0 =
      sql"""
        DELETE FROM rss_item where id=${id}
      """.update

    def insert(item:RssItem): doobie.Update0 = sql"""
    INSERT INTO rss_item(feed_id, title, url, image, published_at, retrieved_at)
    VALUES  (${item.rssFeedId}, ${item.title}, ${item.url}, ${item.image}, ${item.publishedAt}, ${item.retrievedAt})
    ON CONFLICT (feed_id, url) DO UPDATE SET title=excluded.title, image=excluded.image, published_at=excluded.published_at, retrieved_at=excluded.retrieved_at WHERE excluded.published_at > rss_item.published_at
    RETURNING id,feed_id, title, url, image, published_at, retrieved_at
    """.update

    def update(item:RssItem): doobie.Update0 = sql"""
    UPDATE rss_item SET feed_id=${item.rssFeedId}, title=${item.title}, url=${item.url}, image=${item.image}, published_at=${item.publishedAt}, retrieved_at=${item.retrievedAt}
    WHERE id=${item.id}
    RETURNING id,feed_id, title, url, image, published_at, retrieved_at
    """.update

    def find(id: Long): doobie.Query0[RssItem] = sql"""
       SELECT id,feed_id, title, url, image, published_at, retrieved_at FROM rss_item Where id = $id
      """.query[RssItem]

    def list: doobie.Query0[RssItem] = sql"""
                     SELECT id,feed_id, title, url, image, published_at, retrieved_at FROM rss_item
      """.query[RssItem]

    def listById(feedId: Long):  doobie.Query0[RssItem]  = sql"""
                     SELECT id,feed_id, title, url, image, published_at, retrieved_at FROM rss_item WHERE feed_id=$feedId
      """.query[RssItem]

    def listAfter(since: LocalDateTime):  doobie.Query0[RssItem]  = sql"""
                     SELECT id,feed_id, title, url, image, published_at, retrieved_at FROM rss_item WHERE published_at > $since
      """.query[RssItem]

    def listByIdAfter(feedId: Long, since: LocalDateTime): doobie.Query0[RssItem] = sql"""
                     SELECT id,feed_id, title, url, image, published_at, retrieved_at FROM rss_item WHERE published_at > $since and feed_id=$feedId
      """.query[RssItem]
  }

}
