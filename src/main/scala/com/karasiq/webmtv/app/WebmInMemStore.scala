package com.karasiq.webmtv.app

import java.util.concurrent.TimeUnit

import com.google.common.cache.CacheBuilder

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration

final class WebmInMemStore(threadTtl: FiniteDuration) extends WebmStore {
  private[this] val cache = CacheBuilder.newBuilder()
    .concurrencyLevel(1)
    .expireAfterWrite(threadTtl.toNanos, TimeUnit.NANOSECONDS)
    .maximumSize(10000)
    .build[ThreadId, Seq[String]]()

  override def get(id: ThreadId): Option[Seq[String]] =
    Option(cache.getIfPresent(id))

  override def update(id: ThreadId, files: Seq[String]): Unit =
    cache.put(id, files)

  override def iterator: Iterator[(ThreadId, Seq[String])] = {
    cache.cleanUp()
    cache.asMap().asScala.iterator
  }
}

object WebmInMemStore {
  def apply(threadTtl: FiniteDuration): WebmInMemStore = new WebmInMemStore(threadTtl)
}
