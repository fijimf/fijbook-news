package com.fijimf.deepfij.news.services

import com.fijimf.deepfij.news.model.RssItem

trait RssFeedUpdate[F[_]] {
  def updateFeed(feedId: Long): F[fs2.Stream[F,RssItem]]
}
