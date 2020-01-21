package com.fijimf.deepfij.news

import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.news.model.RssItem.ItemParam
import com.fijimf.deepfij.news.model.RssRefreshJob.Dao.JobParam
import com.fijimf.deepfij.news.model.{RssFeed, _}
import com.fijimf.deepfij.news.services.{RssFeedUpdate, RssFeedVerify, RssRepo}
import com.fijimf.deepfij.news.util.ServerInfo
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.slf4j.{Logger, LoggerFactory}

object RssRoutes {
  val log: Logger = LoggerFactory.getLogger(RssRoutes.getClass)

  def rssHealthcheckRoutes[F[_]](r: RssRepo[F], updater: RssFeedUpdate[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "status" =>
        for {
          status<-r.healthcheck.map(isOk=>ServerInfo.fromStatus(isOk))
          resp <- if (status.isOk) Ok(status) else InternalServerError(status)
        } yield {
          resp
        }
    }
  }
 def rssFeedRoutes[F[_]](r: RssRepo[F], updater: RssFeedUpdate[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "feeds" =>
        for {
          list <- r.listFeeds()
          resp <- Ok(list)
        } yield {
          resp
        }
      case GET -> Root / "feeds" / feedId =>
        for {
          opt <- r.findFeed(feedId.toLong)
          resp <- opt match {
            case None => NotFound()
            case Some(f) => Ok(f)
          }
        } yield {
          resp
        }
      case req@GET -> Root / "feeds" / feedId / "items" =>
        val p: ItemParam = ItemParam.fromReq(Some(feedId.toLong), req)
        for {
          items <- r.listItems(p)
          resp <- items match {
            case Nil => NotFound()
            case list => Ok(list)
          }
        } yield {
          resp
        }
      case req@GET -> Root / "feeds" / feedId / "items" / itemId =>
        val p: ItemParam = ItemParam.fromReq(Some(feedId.toLong), req)
        for {
          items <- r.listItems(p)
          resp <- items.filter(_.id === itemId.toLong) match {
            case Nil => NotFound()
            case list => Ok(list)
          }
        } yield {
          resp
        }
      case req@POST -> Root / "feeds" =>
        for {
          feed <- req.as[RssFeed]
          strm = r.insertFeed(feed)
          list <- strm.compile[F, F, RssFeed].to[List]
          resp <- Ok(list)
        } yield {
          resp
        }
      case DELETE -> Root / "feeds" / feedId =>
        for {
          i <- r.deleteFeed(feedId.toLong)
          resp <- Ok(i)
        } yield {
          resp
        }
    }
  }

  def rssItemRoutes[F[_]](r: RssRepo[F], updater: RssFeedUpdate[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root / "items" =>
        val p: ItemParam = ItemParam.fromReq(None, req)
        for {
          items <- r.listItems(p)
          resp <- Ok(items)
        } yield {
          resp
        }
      case GET -> Root / "items" / itemId =>
        for {
          opt <- r.findItem(itemId.toLong)
          resp <- opt match {
            case None => NotFound()
            case Some(i) => Ok(i)
          }
        } yield {
          resp
        }
      case DELETE -> Root / "items" / itemId => for {
        i <- r.deleteItem(itemId.toLong)
        resp <- Ok(i)
      } yield {
        resp
      }
    }
  }

  def rssJobHistoryRoutes[F[_]](r: RssRepo[F], updater: RssFeedUpdate[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root / "refreshJobs" =>
        val p: JobParam =JobParam.fromReq(req)
        for {
          list<- r.listRefreshJobs(p)
          resp<-Ok(list)
        } yield {
          resp
        }

      case req@DELETE -> Root / "refreshJobs" =>
        val p: JobParam =JobParam.fromReq(req)
        for {
          num<- r.deleteRefreshJobs(p)
          resp<-Ok(num)
        } yield {
          resp
        }
    }
  }

  def rssActionRoutes[F[_]](r: RssRepo[F], updater: RssFeedUpdate[F], verifier: RssFeedVerify[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case POST -> Root / "refresh" / feedId =>
        for {
          items <- updater.updateFeed(feedId.toLong)
          _ <- F.delay(log.info(s"${items.size} new items saved."))
          resp <- Ok(items)
        } yield {
          resp
        }

      case req@POST -> Root / "verify" / feedId =>
        val p: ItemParam = ItemParam.fromReq(Some(feedId.toLong), req)
        for {
          items <- verifier.verifyFeed(p)
          resp <- Ok(items)
        } yield {
          resp
        }
    }
  }
}