package controllers

import javax.inject.{Inject, Singleton}

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

/**
  * Created on 16/09/22.
  */
@Singleton
class EditThoughtMemoController @Inject() (
  val messagesApi: MessagesApi,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  def edit(id: Long) = Action.async {
    implicit request =>
      Future.successful(Ok(views.html.editThoughtMemo(id)))
  }

  def register = Action.async {
    implicit request =>
      Future.successful(Redirect(routes.ApplicationController.index()))
  }
}
