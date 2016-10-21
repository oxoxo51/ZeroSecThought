package controllers

import javax.inject.Inject

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future

/**
  * Created on 16/10/14.
  */
class ApplicationController @Inject() (
  val messagesApi: MessagesApi,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  def index = Action.async { implicit request =>
    Logger.debug("***** access index *****")
    Future.successful(Ok(views.html.thoughtMemoList()))
  }

}