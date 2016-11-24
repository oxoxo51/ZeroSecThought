package models

import play.api.libs.json.Json

/**
  * Created on 16/10/21.
  */
case class Memo
(
  id: Option[Long],
  parentId: Option[Long],
  title: String,
  content: String,
  createDate: java.sql.Date
)
object Memo {
  implicit def jsonWrites = Json.writes[Memo]
  implicit def jsonReads = Json.reads[Memo]
}