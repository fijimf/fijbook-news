package com.fijimf.deepfij.news

import java.time.LocalDateTime

import com.fijimf.deepfij.news.model.RssItem.ItemParam
import com.fijimf.deepfij.news.model.RssRefreshJob.Dao.JobParam
import com.fijimf.deepfij.news.model.{RssFeed, RssItem, RssRefreshJob}

class DoobieTypeCheckSpec extends DbIntegrationSpec {
  val containerName = "doobie-typecheck-spec"
  val port = "7374"

  describe("Doobie typechecking Dao's") {
    describe("RssFeed.Dao") {
      it("insert should typecheck") {
        check(RssFeed.Dao.insert(RssFeed(0L, "DUMMY", "https://junk.z.x.y/ppp/qqq")))
      }

      it("update should typecheck") {
        check(RssFeed.Dao.insert(RssFeed(19L, "DUMMY", "https://junk.z.x.y/ppp/qqq")))
      }

      it("delete should typecheck") {
        check(RssFeed.Dao.delete(0L))
      }

      it("find should typecheck") {
        check(RssFeed.Dao.find(0L))
      }

      it("list should typecheck") {
        check(RssFeed.Dao.list)
      }
    }

    describe("RssItem.Dao") {
      it("insert should typecheck") {

        check(RssItem.Dao.insert(RssItem(0L, 1L, "DUMMY", "https://junk.z.x.y/ppp/qqq", None, LocalDateTime.now(), LocalDateTime.now(), None, None, None, None)))
      }
      it("update should typecheck") {
        check(RssItem.Dao.insert(RssItem(34L, 1L, "DUMMY", "https://junk.z.x.y/ppp/qqq", None, LocalDateTime.now(), LocalDateTime.now(), None, None, None, None)))
      }

      it("delete should typecheck") {
        check(RssItem.Dao.delete(0L))
      }

      it("find should typecheck") {
        check(RssItem.Dao.find(0L))
      }

      it(s"list with parameter should typecheck") {
        for {
          feedId <- List(None, Some(1L))
          from <- List(None, Some(LocalDateTime.now()))
          to <- List(None, Some(LocalDateTime.now()))
          skipMissing <- List(true, false)
          skipUnverified <- List(true, false)
        } {
          check(RssItem.Dao.list(ItemParam(feedId, from, to, skipMissing, skipUnverified)))
        }
      }
    }
    describe("RssRefreshJob.Dao") {
      it("insert") {
        check(RssRefreshJob.Dao.insert(RssRefreshJob(0L, 1L, LocalDateTime.now(), LocalDateTime.now().plusSeconds(10L), 200, 12, 0)))
      }

      it("list all") {
        check(RssRefreshJob.Dao.list(JobParam(None, None, None)))
      }
      it("list feed") {
        check(RssRefreshJob.Dao.list(JobParam(Some(1L), None, None)))
      }
      it("list before") {
        check(RssRefreshJob.Dao.list(JobParam(None, Some(LocalDateTime.now()), None)))
      }
      it("list after") {
        check(RssRefreshJob.Dao.list(JobParam(None, None, Some(LocalDateTime.now()))))
      }
      it("list feed between") {
        check(RssRefreshJob.Dao.list(JobParam(Some(1L), Some(LocalDateTime.now().plusDays(1)), Some(LocalDateTime.now()))))
      }

      it("delete all") {
        check(RssRefreshJob.Dao.delete(JobParam(None, None, None)))
      }
      it("delete feed") {
        check(RssRefreshJob.Dao.delete(JobParam(Some(1L), None, None)))
      }
      it("delete before") {
        check(RssRefreshJob.Dao.delete(JobParam(None, Some(LocalDateTime.now()), None)))
      }
      it("delete after") {
        check(RssRefreshJob.Dao.delete(JobParam(None, None, Some(LocalDateTime.now()))))
      }
      it("delete feed between") {
        check(RssRefreshJob.Dao.delete(JobParam(Some(1L), Some(LocalDateTime.now().plusDays(1)), Some(LocalDateTime.now()))))
      }
    }
  }
}