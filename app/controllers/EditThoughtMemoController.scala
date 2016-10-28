package controllers

import java.sql.Date
import javax.inject.{Inject, Singleton}

import constant.Constant
import forms.{MemoForms, MemoForm}
import models.Memo
import models.daos.MemoDao
import play.api.Logger
import play.api.i18n.{Messages, I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * Created on 16/09/22.
  */
@Singleton
class EditThoughtMemoController @Inject() (
  val messagesApi: MessagesApi,
  dao: MemoDao,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  def displayEdit(id: Long) = Action.async {
    implicit request =>
      dao.byId(id).map(
        option => option match {
          case Some(memo) =>
            // UPDATE
            Logger.debug(memo.id + "/" + memo.title + "/" + memo.content + "/" + memo.createDate)
            Ok(views.html.editThoughtMemo(MemoForms.memoForm.fill(
              MemoForm(Some("U"), memo))))
          case None =>
            // CREATE
            Ok(views.html.editThoughtMemo(MemoForms.memoForm.fill(
              MemoForm(Some("C"), new Memo(None, "", "", new Date(new java.util.Date().getTime()))))))
        }
      )
  }

  def register = Action.async {
    implicit request =>
      MemoForms.memoForm.bindFromRequest.fold(
        formWithErrors => {
          Logger.debug("***** register: 入力不備 *****")
          Future(BadRequest(views.html.editThoughtMemo(formWithErrors)))

        },
        formValue => {
          formValue.command match {
            case Some("U") =>
              Logger.debug("***** register: case update *****")
              dao.update(formValue.memo).map(_ =>
                Redirect(routes.ApplicationController.index())
                  .flashing(Constant.MSG_SUCCESS ->  Messages("success.update", formValue.memo.title))
              )
            case Some("C") =>
              Logger.debug("***** register: case create *****")
              dao.create(formValue.memo).map(_ =>
                Redirect(routes.ApplicationController.index())
                  .flashing(Constant.MSG_SUCCESS ->  Messages("success.create", formValue.memo.title))
              )            case _ =>
              Logger.debug("***** register: case other *****")
              Future(Redirect(routes.ApplicationController.index()))
          }
        }
      )
  }
}
