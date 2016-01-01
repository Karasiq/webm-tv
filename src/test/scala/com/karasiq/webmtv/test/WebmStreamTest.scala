package com.karasiq.webmtv.test

import com.karasiq.webmtv.sosach.WebmStream
import org.scalatest.{FlatSpec, Matchers}

class WebmStreamTest extends FlatSpec with Matchers {
  "Webm stream" should "parse /b/" in {
    val stream = WebmStream("b")(scala.concurrent.ExecutionContext.global)
    assert(stream.hasNext, "Stream is empty")
    assert(stream.next().endsWith(".webm"), "Not webm")
  }
}
