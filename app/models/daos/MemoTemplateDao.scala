package models.daos

import javax.inject.Inject

import models.{Memo, MemoTemplate}
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * memo template data access object.
  * Created on 2017/02/13.
  *
  * @param dbConfigProvider
  */
class MemoTemplateDao @Inject()(dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig.driver.api._

  /**
    * memo tamplate table.
    * @param tag
    */
  private class MemoTemplateTable(tag: Tag) extends Table[MemoTemplate](tag, "memoTemplate") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def parentId = column[Long]("parent_id")
    def title = column[String]("title")
    def content = column[String]("content")
    def * = (id.?, parentId.?, title, content) <> ((MemoTemplate.apply _).tupled, MemoTemplate.unapply)
  }

  private val memoTemplates = TableQuery[MemoTemplateTable]

  /**
    * get all memo templates
    * @return
    */
  def getMemoTemplates(): Future[List[MemoTemplate]] =
    dbConfig.db.run(memoTemplates.sortBy(row => row.title).result).map(_.toList)

  /**
    * get memo template by id
    * @param id
    * @return
    */
  def byId(id: Long): Future[Option[MemoTemplate]] =
    dbConfig.db.run(memoTemplates.filter(_.id === id).result.headOption)

  def create(memoTemplate: MemoTemplate): Future[Int] = {
    dbConfig.db.run(memoTemplates += memoTemplate)
  }

  /**
    * update.
    * @param memoTemplate
    * @return
    */
  def update(memoTemplate: MemoTemplate): Future[Int] = {
    dbConfig.db.run(memoTemplates.filter(_.id === memoTemplate.id).map(
      m => (
        m.parentId,
        m.title,
        m.content
        )
    ).update(
      // maybe parentId can't be None
      memoTemplate.parentId.getOrElse(0),
      memoTemplate.title,
      memoTemplate.content
      )
    )
  }

  /**
    * delete.
    * @param id
    * @return
    */
  def delete(id: Long): Future[Int] =
    dbConfig.db.run(memoTemplates.filter(_.id === id).delete)
}
