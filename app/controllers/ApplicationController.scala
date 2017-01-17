package controllers

import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

import models.daos.MemoDao
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created on 16/10/14.
  */
class ApplicationController @Inject() (
  val messagesApi: MessagesApi,
  dao: MemoDao,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  val utilToday = new java.util.Date

  def index = Action.async { implicit request =>
    Logger.debug("***** access index *****")
    // セッションから検索条件取得
    val conditionTitle = request.session.get("conditionTitle")
    val conditionContent = request.session.get("conditionContent")
    val conditionDateFrom = request.session.get("conditionDateFrom") match {
      case None => None
      case Some(s) => s match {
        case "" => None
        case _  => Some(java.sql.Date.valueOf(s))
    }

    }
    val conditionDateTo = request.session.get("conditionDateTo") match {
      case None => None
      case Some(s) =>  s match {
        case "" => None
        case _  => Some(java.sql.Date.valueOf(s))
      }
    }

    val sortKey = request.session.get("sortKey") match {
      case None => None
      case Some(s) => s match {
        case "" => None
        case _ => Some(s)
      }
    }
    val sortOrder = request.session.get("sortOrder") match {
      case None => None
      case Some(s) => s match {
        case "" => None
        case _ => Some(s)
      }
    }

    val sdf = new SimpleDateFormat("yyyy/MM/dd")
    var weekMemoList = List.empty[(String, String)]
    // 1週間分ループを回し、日付＋日付のmem0件数を取得しListに詰める
    for (i <- 0 to 6) {
      val cal = Calendar.getInstance
      cal.setTime(utilToday)
      cal.add(Calendar.DAY_OF_MONTH, i-6)
      val count = dao.getCount(new java.sql.Date(cal.getTime.getTime))
      weekMemoList :+= (sdf.format(cal.getTime), Integer.toString(count))
    }
    var monthYearList = List.empty[(String, String)]
    val cal = Calendar.getInstance
    cal.setTime(utilToday)
    cal.add(Calendar.MONTH, -1)
    val monthCount = dao.getCount(new java.sql.Date(cal.getTime.getTime))
    monthYearList :+= (sdf.format(cal.getTime), Integer.toString(monthCount))
    cal.add(Calendar.MONTH, 1)
    cal.add(Calendar.YEAR, -1)
    val yearCount = dao.getCount(new java.sql.Date(cal.getTime.getTime))
    monthYearList :+= (sdf.format(cal.getTime), Integer.toString(yearCount))

    Logger.debug("session:" + conditionTitle + "/" + conditionContent + "/" + conditionDateFrom + "/" + conditionDateTo + "/" + sortKey + "/" + sortOrder)
    Future.successful(Ok(views.html.thoughtMemoList(
      conditionTitle,
      conditionContent,
      conditionDateFrom,
      conditionDateTo,
      sortKey,
      sortOrder,
      weekMemoList,
      monthYearList
    )))
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
    val sortKey = Option(request.body.asFormUrlEncoded.get.get("sortKey").get.head)
    val sortOrder = Option(request.body.asFormUrlEncoded.get.get("sortOrder").get.head)
    val favChecked = Option(request.body.asFormUrlEncoded.get.get("favChecked").get.head)

    Logger.debug(conditionTitle + "/" + conditionContent + "/" + conditionDateFrom + "/" + conditionDateTo + "/" + sortKey + "/" + sortOrder + "/" + favChecked)
    val memos = Await.result(
      dao.findMemos(conditionDateFrom, conditionDateTo, conditionTitle, conditionContent, sortKey, sortOrder, favChecked),
      Duration.Inf)
    val jsonMemos = Json.toJson(memos)
    Logger.debug(jsonMemos.toString)
    Ok(jsonMemos).withSession(
      "conditionTitle" -> conditionTitle.getOrElse("").toString,
      "conditionContent" -> conditionContent.getOrElse("").toString,
      "conditionDateFrom" -> conditionDateFrom.getOrElse("").toString,
      "conditionDateTo" -> conditionDateTo.getOrElse("").toString,
      "sortKey" -> sortKey.getOrElse("").toString,
      "sortOrder" -> sortKey.getOrElse("").toString
    )
  }


  def deleteMemo = Action { implicit request =>
    Logger.debug(request.body.asFormUrlEncoded.get.toString)

    val id = request.body.asFormUrlEncoded.get.get("id").get.head
    val num = Await.result(
      dao.delete(id.toLong),
      Duration.Inf)
    Ok(request.body.asJson.orNull)
  }

  def updFav = Action { implicit request =>
    Logger.debug(request.body.asFormUrlEncoded.get.toString)

    val id = request.body.asFormUrlEncoded.get.get("memoId").get.head
    val flag = request.body.asFormUrlEncoded.get.get("favFlg").get.head

    val num = Await.result(
      dao.updateFav(id.toLong, flag),
      Duration.Inf)
    Ok(request.body.asJson.orNull)
  }
}