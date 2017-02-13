package models

/**
  * memo Template class.
  * Created on 2017/02/13.
  *
  * @param id
  * @param parentId
  * @param title
  * @param content
  */
case class MemoTemplate
(
  id: Option[Long],
  parentId: Option[Long],
  title: String,
  content: String
)