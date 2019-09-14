package com.fijimf.deepfij.news.model

import java.sql.Timestamp
import java.time.LocalDateTime

import com.fijimf.deepfij.news.util.DateUtil
import doobie.implicits._
import doobie.util.Meta
import doobie.util.fragment.Fragment
import org.http4s.Request

final case class RssItem
(
  id: Long,
  rssFeedId: Long,
  title: String,
  url: String,
  image: Option[String],
  publishedAt: LocalDateTime,
  retrievedAt: LocalDateTime,
  verifiedAt:Option[LocalDateTime],
  statusCode:Option[Int],
  responseTime:Option[Int],
  responseSize:Option[Int]
){
  def verify(statusCode:Int, responseTime:Int, responseSize:Int): RssItem =
    copy(verifiedAt= Some(LocalDateTime.now()), statusCode=Some(statusCode), responseTime=Some(responseTime), responseSize=Some(responseSize))
}

object RssItem {

  object Dao {
    implicit val localDateTimeMeta: Meta[LocalDateTime] = Meta[Timestamp].imap(ts => ts.toLocalDateTime)(ldt => Timestamp.valueOf(ldt))

    def delete(id: Long): doobie.Update0 =
      sql"""
        DELETE FROM rss_item where id=${id}
      """.update

    def insert(item:RssItem): doobie.Update0 = sql"""
    INSERT INTO rss_item(feed_id, title, url, image, published_at, retrieved_at)
    VALUES  (${item.rssFeedId}, ${item.title}, ${item.url}, ${item.image}, ${item.publishedAt}, ${item.retrievedAt})
    ON CONFLICT (feed_id, url) DO UPDATE SET title=excluded.title, image=excluded.image, published_at=excluded.published_at, retrieved_at=excluded.retrieved_at WHERE excluded.published_at > rss_item.published_at
    RETURNING id,feed_id, title, url, image, published_at, retrieved_at, verified_at, status_code, response_time, response_size
    """.update

    def update(item:RssItem): doobie.Update0 = sql"""
    UPDATE rss_item SET
      feed_id=${item.rssFeedId},
      title=${item.title},
      url=${item.url},
      image=${item.image},
      published_at=${item.publishedAt},
      retrieved_at=${item.retrievedAt},
      verified_at=${item.verifiedAt},
      status_code=${item.statusCode},
      response_time=${item.responseTime},
      response_size=${item.responseSize}

    WHERE id=${item.id}
    RETURNING id,feed_id, title, url, image, published_at, retrieved_at,  verified_at, status_code, response_time, response_size
    """.update

    def find(id: Long): doobie.Query0[RssItem] = sql"""
       SELECT id,feed_id, title, url, image, published_at, retrieved_at, verified_at, status_code, response_time, response_size FROM rss_item Where id = $id
      """.query[RssItem]

    def list(p:ItemParam): doobie.Query0[RssItem] = (fr"""
                     SELECT id,feed_id, title, url, image, published_at, retrieved_at,  verified_at, status_code, response_time, response_size
                     FROM rss_item
      """++p.predicate).query[RssItem]

  }


  object ItemParam {
    def fromReq[F[_]](feedId:Option[Long],req: Request[F]): ItemParam = {
      ItemParam(feedId,
        req.params.get("before").flatMap(DateUtil.stringToDateTime),
        req.params.get("after").flatMap(DateUtil.stringToDateTime),
        req.params.contains("skipMissing"),
        req.params.contains("skipUnverified")
      )
    }
  }

  final case class ItemParam
  (
    feedId: Option[Long],
    before: Option[LocalDateTime],
    after: Option[LocalDateTime],
    skipMissing: Boolean,
    skipUnverified: Boolean
  ) {
    implicit val localDateTimeMeta: Meta[LocalDateTime] = Meta[Timestamp].imap(ts => ts.toLocalDateTime)(ldt => Timestamp.valueOf(ldt))

    def predicate: Fragment = {
      List(
        feedId.map(f=>fr"feed_id = $f").toList,
        before.map(s => fr"published_at < $s").toList,
        after.map(e => fr"published_at > $e").toList,
        if (skipMissing) List(fr"status_code <> 200") else List.empty[Fragment],
        if (skipUnverified) List(fr"status_code IS NOT NULL") else List.empty[Fragment]
      ).flatten match {
        case Nil => Fragment.empty
        case head :: tail =>
          fr"WHERE" ++ tail.foldLeft(head) { case (f: Fragment, g: Fragment) => f ++ fr"AND" ++ g }
      }
    }
  }

}
