package com.karasiq.webmtv.test

import com.karasiq.webmtv.sosach.api.{Post, PostFile, Thread}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class JsonTest extends FlatSpec with Matchers {
  "JSON parser" should "parse file" in {
    import PostFile.format
    val json = """{"duration": "00:00:03",
                 |              "height": 510,
                 |              "md5": "a3b2b18a364ceb63aaff82f69a463924",
                 |              "name": "14515886954590.webm",
                 |              "path": "src/111694368/14515886954590.webm",
                 |              "size": 363,
                 |              "thumbnail": "thumb/111694368/14515886954590s.jpg",
                 |              "tn_height": 95,
                 |              "tn_width": 170,
                 |              "type": 6,
                 |              "width": 908
                 |            }""".stripMargin

    json.parseJson.convertTo[PostFile].path shouldBe "src/111694368/14515886954590.webm"
  }

  it should "parse post" in {
    import Post.format
    val json = """
        |{
        |          "banned": 0,
        |          "closed": 0,
        |          "comment": "Отмечаем новый год на дваче #2<br>",
        |          "date": "01/01/16 Птн 01:17:18",
        |          "email": "",
        |          "files": [
        |            {
        |              "duration": "",
        |              "height": 220,
        |              "md5": "8bfaafcf2f105ef3d5e5df65a9e9a002",
        |              "name": "14516002388560.jpg",
        |              "path": "src/111715493/14516002388560.jpg",
        |              "size": 6,
        |              "thumbnail": "thumb/111715493/14516002388560s.jpg",
        |              "tn_height": 220,
        |              "tn_width": 220,
        |              "type": 1,
        |              "width": 220
        |            }
        |          ],
        |          "files_count": 72,
        |          "hidden_num": "111715xxx",
        |          "lasthit": 1451603758,
        |          "name": "Аноним",
        |          "num": "111715493",
        |          "op": 0,
        |          "parent": "0",
        |          "posts_count": 452,
        |          "sticky": 0,
        |          "subject": "",
        |          "tags": "",
        |          "timestamp": 1451600238,
        |          "trip": ""
        |        }
      """.stripMargin

    json.parseJson.convertTo[Post].timestamp shouldBe 1451600238L
  }

  it should "parse thread" in {
    import Thread.format
    val boardJson = """{
                      |      "files_count": 624,
                      |      "posts": [
                      |        {
                      |          "banned": 0,
                      |          "closed": 0,
                      |          "comment": "С Новым годом, анончики!<br>Любви и добра вам, будьте няшками.<br><br><a href=\"https:&#47;&#47;www.youtube.com&#47;watch?v=lql0dS0M2j4\" target=\"_blank\">https:&#47;&#47;www.youtube.com&#47;watch?v=lql0dS0M2j4</a><br><br>На несколько дней мы отменяем все правила в &#47;b&#47;, кроме глобальных.<br><br>Хороших праздников, анон.",
                      |          "date": "31/12/15 Чтв 22:02:14",
                      |          "email": "",
                      |          "files": [
                      |            {
                      |              "duration": "",
                      |              "height": 935,
                      |              "md5": "1b65fb35a58599c3529e9fa89d8168a6",
                      |              "name": "14515885344270.png",
                      |              "path": "src/111694368/14515885344270.png",
                      |              "size": 30,
                      |              "thumbnail": "thumb/111694368/14515885344270s.jpg",
                      |              "tn_height": 220,
                      |              "tn_width": 220,
                      |              "type": 1,
                      |              "width": 935
                      |            }
                      |          ],
                      |          "files_count": 624,
                      |          "hidden_num": "111694xxx",
                      |          "lasthit": 1451590072,
                      |          "name": "",
                      |          "num": "111694368",
                      |          "op": 0,
                      |          "parent": "0",
                      |          "posts_count": 2676,
                      |          "sticky": 1,
                      |          "subject": "",
                      |          "tags": "",
                      |          "timestamp": 1451588534,
                      |          "trip": "!!%adm%!!"
                      |        },
                      |        {
                      |          "banned": 0,
                      |          "closed": 0,
                      |          "comment": "<a href=\"/b/res/111694368.html#111720326\" class=\"post-reply-link\" data-thread=\"111694368\" data-num=\"111720326\">>>111720326</a><br><span class=\"unkfunc\">&gt;2893</span><br><span class=\"unkfunc\">&gt;Кто такой медисон? </span>",
                      |          "date": "01/01/16 Птн 02:15:42",
                      |          "email": "",
                      |          "files": [],
                      |          "hidden_num": "111720xxx",
                      |          "lasthit": 1451590072,
                      |          "name": "Аноним",
                      |          "num": "111720384",
                      |          "op": 0,
                      |          "parent": "111694368",
                      |          "sticky": 1,
                      |          "subject": "",
                      |          "timestamp": 1451603742,
                      |          "trip": ""
                      |        },
                      |        {
                      |          "banned": 0,
                      |          "closed": 0,
                      |          "comment": "<a href=\"/b/res/111694368.html#111720263\" class=\"post-reply-link\" data-thread=\"111694368\" data-num=\"111720263\">>>111720263</a><br>Пикча откровенное гавно",
                      |          "date": "01/01/16 Птн 02:15:45",
                      |          "email": "",
                      |          "files": [],
                      |          "hidden_num": "111720xxx",
                      |          "lasthit": 1451590072,
                      |          "name": "Аноним",
                      |          "num": "111720387",
                      |          "op": 0,
                      |          "parent": "111694368",
                      |          "sticky": 1,
                      |          "subject": "",
                      |          "timestamp": 1451603745,
                      |          "trip": ""
                      |        },
                      |        {
                      |          "banned": 0,
                      |          "closed": 0,
                      |          "comment": "<a href=\"/b/res/111694368.html#111720369\" class=\"post-reply-link\" data-thread=\"111694368\" data-num=\"111720369\">>>111720369</a><br>Уебывай шкура тупая",
                      |          "date": "01/01/16 Птн 02:16:00",
                      |          "email": "",
                      |          "files": [],
                      |          "hidden_num": "111720xxx",
                      |          "lasthit": 1451590072,
                      |          "name": "Аноним",
                      |          "num": "111720403",
                      |          "op": 0,
                      |          "parent": "111694368",
                      |          "sticky": 1,
                      |          "subject": "",
                      |          "timestamp": 1451603760,
                      |          "trip": ""
                      |        }
                      |      ],
                      |      "posts_count": 2676,
                      |      "thread_num": "111694368"
                      |    }""".stripMargin
    boardJson.parseJson.convertTo[Thread].id shouldBe 111694368L

    val threadJson = """{
                       |      "posts": [
                       |        {
                       |          "banned": 0,
                       |          "closed": 0,
                       |          "comment": "С Новым годом, анончики!<br>Любви и добра вам, будьте няшками.<br><br><a href=\"https:&#47;&#47;www.youtube.com&#47;watch?v=lql0dS0M2j4\" target=\"_blank\">https:&#47;&#47;www.youtube.com&#47;watch?v=lql0dS0M2j4</a><br><br>На несколько дней мы отменяем все правила в &#47;b&#47;, кроме глобальных.<br><br>Хороших праздников, анон.",
                       |          "date": "31/12/15 Чтв 22:02:14",
                       |          "email": "",
                       |          "files": [
                       |            {
                       |              "duration": "",
                       |              "height": 935,
                       |              "md5": "1b65fb35a58599c3529e9fa89d8168a6",
                       |              "name": "14515885344270.png",
                       |              "path": "src/111694368/14515885344270.png",
                       |              "size": 30,
                       |              "thumbnail": "thumb/111694368/14515885344270s.jpg",
                       |              "tn_height": 220,
                       |              "tn_width": 220,
                       |              "type": 1,
                       |              "width": 935
                       |            }
                       |          ],
                       |          "hidden_num": "111694xxx",
                       |          "lasthit": 1451590072,
                       |          "name": "",
                       |          "num": 111694368,
                       |          "number": 1,
                       |          "op": 0,
                       |          "parent": "0",
                       |          "sticky": 1,
                       |          "subject": "",
                       |          "tags": "",
                       |          "timestamp": 1451588534,
                       |          "trip": "!!%adm%!!"
                       |        }
                       |]}""".stripMargin
    threadJson.parseJson.convertTo[Thread].id shouldBe 111694368L
  }
}
