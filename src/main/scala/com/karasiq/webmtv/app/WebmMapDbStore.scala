package com.karasiq.webmtv.app

import java.io.Closeable
import java.nio.file.{Path, Paths}
import java.util.concurrent.TimeUnit

import com.karasiq.mapdb.MapDbWrapper._
import com.karasiq.mapdb.serialization.MapDbSerializer
import com.karasiq.mapdb.serialization.MapDbSerializer.Default._
import com.karasiq.mapdb.{MapDbFile, MapDbSingleFileProducer}
import com.typesafe.config.ConfigFactory
import org.mapdb.DBMaker
import org.mapdb.DBMaker.Maker

private[app] sealed trait WebmMapDbStore extends WebmStore with Closeable {
  protected val db: MapDbFile

  private def threadTtl: Long = ConfigFactory.load().getDuration("webm-tv.thread-ttl", TimeUnit.SECONDS)

  private lazy val map = db.createHashMap[ThreadId, Seq[String]]("threads")(_
    .counterEnable()
    .keySerializer(MapDbSerializer[ThreadId])
    .valueSerializer(MapDbSerializer[Seq[String]])
    .expireAfterWrite(threadTtl, TimeUnit.SECONDS)
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

  override def close(): Unit = {
    db.close()
  }
}

private[app] object WebmHeapStore extends WebmMapDbStore {
  override protected val db = MapDbFile(DBMaker
    .memoryDirectDB()
    .compressionEnable()
    .transactionDisable()
    .make()
  )
}

private[app] object WebmFileStore extends WebmMapDbStore {
  private val config = ConfigFactory.load().getConfig("webm-tv")

  private def dbPath: Path = Paths.get(config.getString("db"))

  private object Producer extends MapDbSingleFileProducer(dbPath) {
    override protected def setSettings(dbMaker: Maker): Maker = {
      dbMaker
        .compressionEnable()
        .transactionDisable()
        .asyncWriteEnable()
        .asyncWriteFlushDelay(2000)
        .executorEnable()
        .cacheSoftRefEnable()
    }
  }

  override protected val db: MapDbFile = Producer()
}
