package com.fijimf.deepfij.news.services

import com.fijimf.deepfij.news.model.RssItem

trait RssFeedUpdate[M[_]] {
  def updateFeed(feedId: Long): M[fs2.Stream[M,RssItem]]
}
