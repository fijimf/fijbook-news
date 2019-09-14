package com.fijimf.deepfij.news.model

import java.sql.Timestamp
import java.time.LocalDateTime

import com.fijimf.deepfij.news.util.DateUtil
import doobie.implicits._
import doobie.util.Meta
import doobie.util.fragment.Fragment
import org.http4s.Request


final case class RssRefreshJob(id: Long, feedId: Long, startTime: LocalDateTime, endTime: LocalDateTime, statusCode: Int, itemCount: Int, newItemCount: Int)

object RssRefreshJob {

  object Dao {
    implicit val localDateTimeMeta: Meta[LocalDateTime] = Meta[Timestamp].imap(ts => ts.toLocalDateTime)(ldt => Timestamp.valueOf(ldt))

    object JobParam {
      def fromReq[F[_]](req: Request[F]): JobParam = {
        JobParam(req.params.get("feedId") map (_.toLong),
          req.params.get("before").flatMap(DateUtil.stringToDateTime),
          req.params.get("after").flatMap(DateUtil.stringToDateTime)
        )
      }
    }

    final case class JobParam(feedId: Option[Long], before: Option[LocalDateTime], after: Option[LocalDateTime]) {
      def predicate: Fragment =
        List(
          feedId.map(f => fr"feed_id = $f").toList,
          before.map(s => fr"start_time < $s").toList,
          after.map(e => fr"end_time > $e").toList,
        ).flatten match {
          case Nil => Fragment.empty
          case head :: tail =>
            fr"WHERE" ++ tail.foldLeft(head) { case (f: Fragment, g: Fragment) => f ++ fr"AND" ++ g }
        }
    }

    def insert(j: RssRefreshJob): doobie.Update0=
      sql"""
        INSERT INTO rss_refresh_job(feed_id , start_time, end_time , status_code , item_count , new_item_count )
        VALUES  (${j.feedId}, ${j.startTime}, ${j.endTime}, ${j.statusCode}, ${j.itemCount}, ${j.newItemCount})
        RETURNING id,feed_id , start_time, end_time , status_code , item_count , new_item_count
    """.update

    def list(p: JobParam): doobie.Query0[RssRefreshJob] = {
      (fr"""
        SELECT id,
        feed_id,
        start_time,
        end_time,
        status_code,
        item_count,
        new_item_count
         FROM rss_refresh_job
    """ ++ p.predicate).query[RssRefreshJob]
    }

    def delete(p: JobParam): doobie.Update0 = {
      (fr"DELETE FROM rss_refresh_job" ++ p.predicate).update
    }
  }

}