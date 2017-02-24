package controllers

import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

import constant.Constant
import models.daos.{MemoDao, MemoTemplateDao}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, Flash}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * 一覧画面における一連の処理を制御するコントローラー.
  * Created on 16/10/14.
  *
  * @param messagesApi
  * @param dao
  * @param templateDao
  * @param webJarAssets
  */
class ApplicationController @Inject() (
  val messagesApi: MessagesApi,
  dao: MemoDao,
  templateDao: MemoTemplateDao,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  val utilToday = new java.util.Date

  /**
    * ルートアクセス時の処理.
    * 以下の情報を取得し、一覧画面を表示する.
    * 【取得内容】
    * ・セッションに保持された検索条件：検索時に保存されたもの
    * ・過去1週間、1か月・1年前の日付と日付毎のメモ件数
    * @return
    */
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

    val favChecked = request.session.get("favChecked") match {
      case None => Some("0")
      case Some (s) => Some(s)
    }

    var todayMemoCount = 0

    val sdf = new SimpleDateFormat("yyyy/MM/dd")
    var weekMemoList = List.empty[(String, String)]
    // 1週間分ループを回し、日付＋日付のmemo件数を取得しListに詰める
    for (i <- 0 to 6) {
      val cal = Calendar.getInstance
      cal.setTime(utilToday)
      cal.add(Calendar.DAY_OF_MONTH, i-6)
      val count = dao.getCount(new java.sql.Date(cal.getTime.getTime))
      weekMemoList :+= (sdf.format(cal.getTime), Integer.toString(count))
      if (i == 6) todayMemoCount = count
    }

    // テンプレートのIDとタイトルをリストに詰める
    val templates: List[(Long, String)] = (Await.result(templateDao.getMemoTemplates(), Duration.Inf)).map(
      template => (template.id.get, template.title)
    )
    // 本日のメモ件数:10件未満の場合はメッセージ表示する
    val message: Option[String] = {
      if (todayMemoCount < 10) Some(Messages("index.info", todayMemoCount))
      else None
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

    // 渡すパラメータをログ出力
    Logger.debug("session:" + conditionTitle + "/"
      + conditionContent + "/" + conditionDateFrom + "/"
      + conditionDateTo + "/" + sortKey + "/"
      + sortOrder + "/" + favChecked)
    // 一覧画面表示
    Future.successful(Ok(views.html.thoughtMemoList(
      conditionTitle,
      conditionContent,
      conditionDateFrom,
      conditionDateTo,
      sortKey,
      sortOrder,
      favChecked,
      weekMemoList,
      monthYearList,
      templates,
      message
    )))
  }

  /**
    * メモ検索.
    * requestで渡されたJSON形式の検索条件を元に、メモを検索し、
    * 検索結果をJSONで返却する.
    * request・responseJSONの形式は以下のとおり.
    * 【request】
    * {
    *   "conditionTitle": conditionTitle,
    *   "conditionContent": conditionContent,
    *   "conditionDateFrom": conditionDateFrom,
    *   "conditionDateTo": conditionDateTo,
    *   "sortKey": sortKey,
    *   "sortOrder": sortOrder,
    *   "favChecked": favChecked
    * }
    * 【response】
    * [
    *   {
    *     "id": id,
    *     "parentId": parentId,
    *     "title": title,
    *     "content": content,
    *     "createDate": createDate,
    *     "fav": fav
    *   }
    * ]
    * @return 検索結果一覧(JSON)
    */
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

    // 検索条件をログ出力
    Logger.debug(conditionTitle + "/" + conditionContent + "/" + conditionDateFrom + "/" + conditionDateTo + "/" + sortKey + "/" + sortOrder + "/" + favChecked)
    // 検索実行し、結果をJSON形式に変換
    val memos = Await.result(
      dao.findMemos(conditionDateFrom, conditionDateTo, conditionTitle, conditionContent, sortKey, sortOrder, favChecked),
      Duration.Inf)
    val jsonMemos = Json.toJson(memos)
    Logger.debug(jsonMemos.toString)
    // セッションに検索条件を保存し、検索結果を返す
    Ok(jsonMemos).withSession(
      "conditionTitle" -> conditionTitle.getOrElse("").toString,
      "conditionContent" -> conditionContent.getOrElse("").toString,
      "conditionDateFrom" -> conditionDateFrom.getOrElse("").toString,
      "conditionDateTo" -> conditionDateTo.getOrElse("").toString,
      "sortKey" -> sortKey.getOrElse("").toString,
      "sortOrder" -> sortKey.getOrElse("").toString,
      "favChecked" -> favChecked.getOrElse("0").toString
    )
  }

  /**
    * メモ削除.
    * requestで渡されたJSONにセットされたIDのメモを削除する.
    * @return
    */
  def deleteMemo = Action { implicit request =>
    Logger.debug(request.body.asFormUrlEncoded.get.toString)

    val id = request.body.asFormUrlEncoded.get.get("id").get.head
    val num = Await.result(
      dao.delete(id.toLong),
      Duration.Inf)
    Ok(request.body.asJson.orNull)
  }

  /**
    * テンプレート削除.
    * requestで渡されたJSONにセットされたIDのテンプレートを削除する.
    * @return
    */
  def deleteTemplate = Action { implicit request =>
    Logger.debug(request.body.asFormUrlEncoded.get.toString)

    val id = request.body.asFormUrlEncoded.get.get("id").get.head
    val num = Await.result(
      templateDao.delete(id.toLong),
      Duration.Inf)
    Ok(request.body.asJson.orNull)
  }

  /**
    * fav更新.
    * requestで渡されたJSONにセットされたID,更新後のフラグ値を元に
    * メモを更新する.
    * @return
    */
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