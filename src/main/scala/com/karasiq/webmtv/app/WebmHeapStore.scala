package com.karasiq.webmtv.app

import java.util.concurrent.TimeUnit

import com.karasiq.mapdb.MapDbFile
import com.karasiq.mapdb.MapDbWrapper._
import com.karasiq.mapdb.serialization.MapDbSerializer
import com.karasiq.mapdb.serialization.MapDbSerializer.Default._
import org.mapdb.DBMaker

private[app] object WebmHeapStore extends WebmStore {
  private val db = MapDbFile(DBMaker
    .heapDB()
    .transactionDisable()
    .make()
  )

  private val map = db.createHashMap[ThreadId, Seq[String]]("threads")(_
    .counterEnable()
    .keySerializer(MapDbSerializer[ThreadId])
    .valueSerializer(MapDbSerializer[Seq[String]])
    .expireAfterWrite(10, TimeUnit.MINUTES)
  )

  override def get(id: ThreadId): Option[Seq[String]] = {
    map.get(id)
  }

  override def update(id: ThreadId, files: Seq[String]): Unit = {
    map += id → files
  }

  override def iterator: Iterator[(ThreadId, Seq[String])] = {
    map.iterator
  }
}
