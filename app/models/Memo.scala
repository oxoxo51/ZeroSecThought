package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * メモエンティティクラス.
  * Created on 16/10/21.
  *
  * @param id
  * @param parentId
  * @param title
  * @param content
  * @param createDate
  * @param fav
  */
case class Memo
(
  id: Option[Long],
  parentId: Option[Long],
  title: String,
  content: String,
  createDate: java.sql.Date,
  fav: String
)

/* メモエンティティ-JSONの変換 */
object Memo {
  val sqlDateWrite = Writes.sqlDateWrites("yyyy/MM/dd")
  implicit lazy val memoWrites: Writes[Memo] = (
    (__ \ "id").write[Option[Long]] and
    (__ \ "parentId").write[Option[Long]] and
    (__ \ "title").write[String] and
    (__ \ "content").write[String] and
    (__ \ "createDate").write[java.sql.Date](sqlDateWrite) and
    (__ \ "fav").write[String]
  )(unlift(Memo.unapply))
  implicit def jsonReads = Json.reads[Memo]
}

