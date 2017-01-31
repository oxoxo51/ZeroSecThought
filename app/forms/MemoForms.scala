package forms

import models.Memo
import play.api.data.Form
import play.api.data.Forms._


/**
  * メモ編集用フォームクラス.
  * Created on 16/10/24.
  *
  * @param command
  * @param memo
  */
case class MemoForm(command: Option[String], memo: Memo)

object MemoForms {
  def memoForm = Form(
    mapping(
      // 画面モード(”C":CREATE, ”U":UPDATE)
      "command" -> optional(text),
      "db" -> mapping(
        "id" -> optional(longNumber),
        "parentId" -> optional(longNumber),
        "title" -> nonEmptyText,
        "content" -> nonEmptyText,
        "createDate" -> sqlDate("yyyy-MM-dd"),
        "fav" -> text
      )
      (
        (id, parentId, title, content, createDate, fav)
        => new Memo(id, parentId, title, content, createDate, fav)
      )
      (
        (m: Memo)
        => Some(m.id, m.parentId, m.title, m.content, m.createDate, m.fav)
      )
    )(MemoForm.apply)(MemoForm.unapply)
  )
}
