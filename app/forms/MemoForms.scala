package forms

import models.Memo
import play.api.data.Form
import play.api.data.Forms._


/**
  * Created on 16/10/24.
  */
case class MemoForm(command: Option[String], memo: Memo)

object MemoForms {
  def memoForm = Form(
    mapping(
      "command" -> optional(text),
      "db" -> mapping(
        "id" -> optional(longNumber),
        "title" -> nonEmptyText,
        "content" -> nonEmptyText,
        "createDate" -> sqlDate("yyyy-MM-dd")
      )
      (
        (id, title, content, createDate)
        => new Memo(id, title, content, createDate)
      )
      (
        (m: Memo)
        => Some(m.id, m.title, m.content, m.createDate)
      )
    )(MemoForm.apply)(MemoForm.unapply)
  )
}
