package com.fijimf.deepfij.news.model

import doobie.implicits._

final case class RssFeed(id: Long, name: String, url: String)

object RssFeed {

  object Dao {

    def createDdl: doobie.Update0 =
      sql"""
      CREATE TABLE rss_feed (
        id BIGSERIAL NOT NULL,
        name VARCHAR(36) NOT NULL,
        url VARCHAR(256) NOT NULL
      );
      CREATE UNIQUE INDEX ON rss_feed(url);
      CREATE UNIQUE INDEX ON rss_feed(name);
    """.update


    def dropDdl: doobie.Update0 =
      sql"""
    DROP TABLE IF EXISTS rss_feed;
      """.update

    def delete(id: Long): doobie.Update0 =
      sql"""
        DELETE FROM rss_feed where id=${id}
      """.update

    def insert(feed:RssFeed): doobie.Update0 =
      sql"""
    INSERT INTO rss_feed(name, url) VALUES (${feed.name}, ${feed.url})
    RETURNING id,name,url
    """.update

    def update(feed:RssFeed): doobie.Update0 =
      sql"""
    UPDATE rss_feed set name=${feed.name}, url=${feed.url}) WHERE id=${feed.id}
    RETURNING id,name,url
    """.update

    def list: doobie.Query0[RssFeed] = sql"""
                     SELECT id, name, url FROM rss_feed
      """.query[RssFeed]

    def find(id: Long): doobie.Query0[RssFeed] = sql"""
                     SELECT id, name, url FROM rss_feed Where id = $id
      """.query[RssFeed]

  }

}

