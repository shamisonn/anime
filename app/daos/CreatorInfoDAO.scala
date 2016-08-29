package daos

import javax.inject.Inject

import com.github.tototoshi.slick.H2JodaSupport._
import models.CreatorInfo
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreatorInfoDAO @Inject()(private val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  class CreatorInfoTable(tag: Tag) extends Table[CreatorInfo](tag, "CREATORS_INFO") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def name = column[String]("NAME")

    def birthDay = column[DateTime]("BIRTH_DAY")

    def lastUpdated = column[DateTime]("LAST_UPDATED")

    def * = (id.?, name, birthDay, lastUpdated.?) <>
      (CreatorInfo.tupled, CreatorInfo.unapply)
  }

  val creatorsInfo = TableQuery[CreatorInfoTable]

  def all: Future[List[CreatorInfo]] =
    dbConfig.db.run(creatorsInfo.result).map(_.toList)

  def byId(id: Long): Future[Option[CreatorInfo]] =
    dbConfig.db.run(creatorsInfo.filter(_.id === id).result.headOption)

  def insert(creator: CreatorInfo): Future[Unit] = {
    val c = creator.copy(lastUpdated = Option(new DateTime()))
    dbConfig.db.run(creatorsInfo += c).map(_ => ())
  }

  def update(creator: CreatorInfo): Future[Unit] = {
    val c = creator.copy(lastUpdated = Option(new DateTime()))
    dbConfig.db.run(creatorsInfo.filter(_.id === c.id).update(c)).map(_ => ())
  }

  def delete(id: Long): Future[Unit] =
    dbConfig.db.run(creatorsInfo.filter(_.id === id).delete).map(_ => ())
}
