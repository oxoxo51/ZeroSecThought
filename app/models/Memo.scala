package models

/**
  * Created on 16/10/21.
  */
case class Memo
(
  id: Option[Long],
  title: String,
  content: String,
  createDate: java.sql.Date
// TODO 親子関係
)
