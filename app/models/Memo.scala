package models

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
