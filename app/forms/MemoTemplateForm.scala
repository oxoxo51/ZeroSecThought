package forms

import models.MemoTemplate
import play.api.data.Form
import play.api.data.Forms._

/**
  * edit form for memo template.
  * Created on 2017/02/14.
  *
  * @param command
  * @param template
  */
case class MemoTemplateForm(command: Option[String], template: MemoTemplate)

object MemoTemplateForms {
  def memoTemplateForm = Form(
    mapping(
      // screen mode("C":CREATE, "U":UPDATE)
      "command" -> optional(text),
      "db" -> mapping(
        "id" -> optional(longNumber),
        "parentId" -> optional(longNumber),
        "title" -> nonEmptyText(maxLength=30),
        "content" -> text(maxLength=400)
      )
      (
        (id, parentId, title, content)
        => new MemoTemplate(id, parentId, title, content)
      )
      (
        (m: MemoTemplate)
        => Some(m.id, m.parentId, m.title, m.content)
      )
    )(MemoTemplateForm.apply)(MemoTemplateForm.unapply)
  )

}
