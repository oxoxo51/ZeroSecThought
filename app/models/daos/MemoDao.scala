package models.daos

import java.sql.{Date, Timestamp}
import javax.inject.Inject

import models.Memo
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created on 16/10/24.
  */
class MemoDao @Inject()(dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig.driver.api._

  implicit def javaDateTimestampMapper = MappedColumnType.base[Date, Timestamp](
    dt => new Timestamp(dt.getTime),
    ts => new Date(ts.getTime)
  )

  private class MemoTable(tag: Tag) extends Table[Memo](tag, "MEMO") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE")
    def content = column[String]("CONTENT")
    def createDate = column[java.sql.Date]("CREATE_DATE")
    def * = (id.?, title, content, createDate) <> ((Memo.apply _).tupled, Memo.unapply)
  }

  private val memos = TableQuery[MemoTable]

  def getMemos(): Future[List[Memo]] =
    dbConfig.db.run(memos.result).map(_.toList)

  def getCount(today: Date): Int =
    Await.result(
      dbConfig.db.run(memos.filter(_.createDate === today).length.result),
      Duration.Inf
    )

  def byId(id: Long): Future[Option[Memo]] =
    dbConfig.db.run(memos.filter(_.id === id).result.headOption)

  def create(memo: Memo): Future[Int] = {
    dbConfig.db.run(memos += memo)
  }

  def update(memo: Memo): Future[Int] = {
    dbConfig.db.run(memos.filter(_.id === memo.id).map(
      m => (
        m.title,
        m.content
        )
    ).update(
      memo.title,
      memo.content
      )
    )
  }

  def delete(id: Long): Future[Int] =
    dbConfig.db.run(memos.filter(_.id === id).delete)
}
