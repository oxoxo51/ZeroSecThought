package controllers

import java.sql.Date
import javax.inject.{Inject, Singleton}

import constant.Constant
import forms.{MemoForm, MemoForms}
import models.Memo
import models.daos.{MemoDao, MemoTemplateDao}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/**
  * メモ編集画面における一連の処理を制御するコントローラー.
  * Created on 16/09/22.
  *
  * @param messagesApi
  * @param dao
  * @param webJarAssets
  */
@Singleton
class EditThoughtMemoController @Inject() (
  val messagesApi: MessagesApi,
  dao: MemoDao,
  templateDao: MemoTemplateDao,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  val today = new Date(new java.util.Date().getTime())

  /**
    * 引数で渡されたIDのメモ編集画面を表示する.
    * 新規の場合はID：0を引数に渡して呼び出す.
    * @param id
    * @return
    */
  def displayEdit(id: Long) = Action.async {
    implicit request =>
      // CREATE
      if (id==0) {
        Future(Ok(views.html.editThoughtMemo(
          MemoForms.memoForm.fill(
            MemoForm(Some("C"), new Memo(None, None, "", "", today, "0"))
          ), Option(dao.getCount(today)),
             Await.result(dao.getMemos(), Duration.Inf)
        )))
      } else {
        dao.byId(id).map(
          option => option match {
            case Some(memo) =>
              // UPDATE
              Logger.debug(memo.id + "/" + memo.title + "/" + memo.content + "/" + memo.createDate)
              Ok(views.html.editThoughtMemo(
                MemoForms.memoForm.fill(
                  MemoForm(Some("U"), memo)
                ), None, Await.result(dao.getMemos(), Duration.Inf)
              ))
            case None =>
              // 見つからない場合（IDをURLに適当に指定した場合）
              Redirect(routes.ApplicationController.index())
                .flashing(Constant.MSG_ERROR -> Messages("error.notfound"))
          }
        )
      }
  }

  /**
    * 引数で渡されたIDのテンプレートを元にメモ編集画面を表示する.
    * @param templateId
    * @return
    */
  def displayEditWithTemplate(templateId: Long) = Action.async {
    implicit request =>
      templateDao.byId(templateId).map(
        option => option match {
          case Some(template) =>
            Logger.debug(template.id + "/" + template.title + "/" + template.content)
            Ok(views.html.editThoughtMemo(
              MemoForms.memoForm.fill(
                MemoForm(Some("C"), new Memo(None, template.parentId, template.title, template.content, today, "0"))
              ), Option(dao.getCount(today)),
              Await.result(dao.getMemos, Duration.Inf)
            ))
          case None =>
            // 見つからない場合（IDをURLに適当に指定した場合）
            Redirect(routes.ApplicationController.index())
              .flashing(Constant.MSG_ERROR -> Messages("error.notfound"))
        }
      )
  }

  /**
    * メモ登録.
    * 入力内容に不備がなければDB登録.
    * @return
    */
  def register = Action.async {
    implicit request =>
      MemoForms.memoForm.bindFromRequest.fold(
        formWithErrors => {
          Logger.debug("***** register: 入力不備 *****")
          Future(BadRequest(views.html.editThoughtMemo(
            formWithErrors, Option(dao.getCount(today)), Await.result(dao.getMemos(), Duration.Inf)
          )))
        },
        formValue => {
          formValue.command match {
            case Some("U") =>
              Logger.debug("***** register: case update *****")
              dao.update(formValue.memo).map(_ =>
                Redirect(routes.ApplicationController.index())
                  .flashing(Constant.MSG_SUCCESS
                    ->  Messages("success.update", formValue.memo.title))
              )
            case Some("C") =>
              Logger.debug("***** register: case create *****")
              dao.create(formValue.memo).map(_ =>
                Redirect(routes.ApplicationController.index())
                  .flashing(Constant.MSG_SUCCESS
                    ->  Messages("success.create", formValue.memo.title))
              )
            case _ =>
              Logger.debug("***** register: case other *****")
              Future(Redirect(routes.ApplicationController.index()))
          }
        }
      )
  }
}
