package controllers

import javax.inject.Inject

import forms.{MemoSearchForms, MemoSearchForm}
import models.daos.MemoDao
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created on 16/10/14.
  */
class ApplicationController @Inject() (
  val messagesApi: MessagesApi,
  dao: MemoDao,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  def index = Action.async { implicit request =>
    Logger.debug("***** access index *****")
    Future.successful(Ok(views.html.thoughtMemoList()))
  }

  def searchMemo = Action { implicit request =>
    Logger.debug(request.body.asFormUrlEncoded.get.toString)

    val tmpDateFrom = request.body.asFormUrlEncoded.get.get("conditionDateFrom").get.head
    val tmpDateTo = request.body.asFormUrlEncoded.get.get("conditionDateTo").get.head

    val conditionDateFrom = tmpDateFrom match {
      case "" => None
      case s => Some(java.sql.Date.valueOf(s))
    }
    val conditionDateTo = tmpDateTo match {
      case "" => None
      case s => Some(java.sql.Date.valueOf(s))
    }

    val conditionTitle = Option(request.body.asFormUrlEncoded.get.get("conditionTitle").get.head)
    val conditionContent = Option(request.body.asFormUrlEncoded.get.get("conditionContent").get.head)

    Logger.debug(conditionTitle + "/" + conditionContent + "/" + conditionDateFrom + "/" + conditionDateTo )
    val memos = Await.result(
      dao.findMemos(conditionDateFrom, conditionDateTo, conditionTitle, conditionContent),
      Duration.Inf)
    val jsonMemos = Json.toJson(memos)
    Logger.debug(jsonMemos.toString)
    Ok(jsonMemos)
  }

/*
  def search = Action.async { implicit request =>
    MemoSearchForms.memoSearchForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.thoughtMemoList(
          null, formWithErrors
        )))
      },
      formValue => {
        dao.findMemos(
          formValue.conditionDateFrom,
          formValue.conditionDateTo,
          formValue.conditionTitle,
          formValue.conditionContent
        ).flatMap(memos =>
          Future.successful(Ok(views.html.thoughtMemoList(
            memos,
            MemoSearchForms.memoSearchForm.fill(
              MemoSearchForm(
                formValue.conditionDateFrom,
                formValue.conditionDateTo,
                formValue.conditionTitle,
                formValue.conditionContent
              )
            ))))
        )
      }
    )
  }
*/

}