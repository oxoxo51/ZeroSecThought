package controllers

import javax.inject.{Inject, Singleton}

import constant.Constant
import forms.{MemoTemplateForm, MemoTemplateForms}
import models.MemoTemplate
import models.daos.{MemoDao, MemoTemplateDao}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/**
  * controller which controll a series of method in edit template screen.
  * Created on 2017/02/14.
  *
  * @param messagesApi
  * @param dao
  * @param memoDao
  * @param webJarAssets
  */
@Singleton
class EditMemoTemplateController @Inject() (
  val messagesApi: MessagesApi,
  dao: MemoTemplateDao,
  memoDao: MemoDao,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  def displayEdit(id: Long) = Action.async {
    implicit request =>
      // CREATE
      if (id == 0) {
        Future(Ok(views.html.editMemoTemplate(
          MemoTemplateForms.memoTemplateForm.fill(
            MemoTemplateForm(Some("C"), new MemoTemplate(None, None, "", ""))
          ), Await.result(memoDao.getMemos, Duration.Inf)
        )))
      } else {
        dao.byId(id).map(
          option => option match {
            case Some(memoTemplate) =>
              // UPDATE
              Logger.debug(memoTemplate.id + "/" + memoTemplate.title + "/" + memoTemplate.content)
              Ok(views.html.editMemoTemplate(
                MemoTemplateForms.memoTemplateForm.fill(
                  MemoTemplateForm(Some("U"), memoTemplate)
                ), Await.result(memoDao.getMemos, Duration.Inf)
              ))
            case None =>
              // can' find (with invalid id in url)

              Redirect(routes.ApplicationController.index())
                .flashing(Constant.MSG_ERROR -> Messages("error.notfound"))
          }
        )
      }
  }

  def register = Action.async {
    implicit request =>
      MemoTemplateForms.memoTemplateForm.bindFromRequest.fold(
        formWithErrors => {
          Logger.debug("***** tempate register: 入力不備 *****")
          Future(BadRequest(views.html.editMemoTemplate(
            formWithErrors, Await.result(memoDao.getMemos, Duration.Inf)
          )))
        },
        formValue => {
          formValue.command match {
            case Some("U") =>
              Logger.debug("***** template register: case update *****")
              dao.update(formValue.template).map(_ =>
                Redirect(routes.ApplicationController.index())
                  .flashing(Constant.MSG_SUCCESS
                    -> Messages("success.update", formValue.template.title))
              )
            case Some("C") =>
              Logger.debug("***** template register: case create *****")
              dao.create(formValue.template).map(_ =>
                Redirect(routes.ApplicationController.index())
                  .flashing(Constant.MSG_SUCCESS
                    -> Messages("success.create", formValue.template.title))
              )
            case _ =>
              Logger.debug("***** template register: case other")
              Future(Redirect(routes.ApplicationController.index()))
          }
        }
      )
  }

}
