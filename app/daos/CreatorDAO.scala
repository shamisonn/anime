package daos

import javax.inject.Inject

import models.Creator
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreatorDAO @Inject()(private val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  private class CreatorTable(tag: Tag) extends Table[Creator](tag, "CREATORS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def name = column[String]("NAME")

    def role = column[String]("ROLE")

    def productId = column[Long]("PRODUCT_ID")

    def creatorInfoId = column[Long]("CREATOR_INFO_ID")

    def * = (id.?, name, role, productId.?, creatorInfoId.?) <>
      (Creator.tupled, Creator.unapply)

    def product = foreignKey("FK_PRODUCT", productId,
      new ProductDAO(dbConfigProvider).products)(_.id)

    def creatorInfo = foreignKey("FK_CREATOR_INFO", creatorInfoId,
      new CreatorInfoDAO(dbConfigProvider).creatorsInfo)(_.id)
  }

  private val creators = TableQuery[CreatorTable]

  def all: Future[List[Creator]] =
    dbConfig.db.run(creators.result).map(_.toList)

  def byId(id: Long): Future[Option[Creator]] =
    dbConfig.db.run(creators.filter(_.id === id).result.headOption)

  def insert(creator: Creator): Future[Unit] =
    dbConfig.db.run(creators += creator).map(_ => ())

  def update(id: Long, creator: Creator): Future[Unit] =
    dbConfig.db.run(creators.filter(_.id === id).update(creator))
      .map(_ => ())

  def delete(id: Long): Future[Unit] =
    dbConfig.db.run(creators.filter(_.id === id).delete)
      .map(_ => ())
}
