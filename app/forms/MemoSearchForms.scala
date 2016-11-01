package forms

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created on 16/10/31.
  */
case class MemoSearchForm(
  conditionDateFrom: Option[java.sql.Date],
  conditionDateTo: Option[java.sql.Date],
  conditionTitle: Option[String],
  conditionContent: Option[String])

object MemoSearchForms {
  def memoSearchForm = Form(
    mapping(
      "conditionDateFrom" -> optional(sqlDate("yyyy-MM-dd")),
      "conditionDateTo" -> optional(sqlDate("yyyy-MM-dd")),
      "conditionTitle" -> optional(text),
      "conditionContent" -> optional(text)
    )(MemoSearchForm.apply)(MemoSearchForm.unapply)
  )
}