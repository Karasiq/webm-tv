package com.karasiq.webmtv.app

import java.util.concurrent.TimeUnit

import com.karasiq.mapdb.MapDbFile
import com.karasiq.mapdb.MapDbWrapper._
import com.karasiq.mapdb.serialization.MapDbSerializer
import com.karasiq.mapdb.serialization.MapDbSerializer.Default._
import org.mapdb.DBMaker

import scala.util.Random

private[app] object WebmStore {
  private val db = MapDbFile(DBMaker
    .memoryDirectDB()
    .transactionDisable()
    .compressionEnable()
    .make()
  )

  private val seen = db.createHashSet[String]("seen")(_
    .serializer(MapDbSerializer[String])
    .expireMaxSize(50)
    .expireAfterWrite(60, TimeUnit.MINUTES)
  )

  private val map = db.createHashMap[(String, Long), Seq[String]]("threads")(_
    .counterEnable()
    .keySerializer(MapDbSerializer[(String, Long)])
    .valueSerializer(MapDbSerializer[Seq[String]])
    .expireAfterWrite(20, TimeUnit.MINUTES)
  )

  def get(board: String, id: Long): Option[Seq[String]] = {
    map.get((board, id))
  }

  def update(board: String, id: Long, files: Seq[String]) = {
    map += (board, id) → files
  }

  def next(): Option[String] = {
    val result = Random.shuffle(map.values.flatten).find(url ⇒ !seen.contains(url))
    result.foreach(seen += _)
    result
  }
}
