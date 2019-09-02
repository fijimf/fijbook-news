package com.fijimf.deepfij.news.util

import org.scalatest.FlatSpec

import scala.io.Source

class RssXmlSpec extends FlatSpec {

  "The RssXml object "should "load well formed XML from a string" in {
    val s = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("cbs-rss.xml")).mkString
    val t = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("espn-rss.xml")).mkString
    val u = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("si-rss.xml")).mkString
    assert(RssXml.loadXml(s).isRight)
    assert(RssXml.loadXml(t).isRight)
    assert(RssXml.loadXml(u).isRight)
  }

  it should "fail to load poorly formed XML from a string" in {
    val s = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("bad1-rss.xml")).mkString
    val t = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("bad1-rss.xml")).mkString
    val u = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("bad3-rss.xml")).mkString
    assert(RssXml.loadXml(s).isLeft)
    assert(RssXml.loadXml(t).isLeft)
    assert(RssXml.loadXml(u).isLeft)
  }

}
