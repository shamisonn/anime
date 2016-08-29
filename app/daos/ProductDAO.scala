package daos

import javax.inject.Inject

import com.github.tototoshi.slick.H2JodaSupport._
import models.Product
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProductDAO @Inject()(private val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  class ProductTable(tag: Tag) extends Table[Product](tag, "PRODUCTS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def title = column[String]("TITLE")

    def company = column[String]("COMPANY")

    def publishDate = column[DateTime]("PUBLISH_DATE")

    def lastUpdated = column[DateTime]("LAST_UPDATED")

    def * = (id.?, title, company, publishDate, lastUpdated.?) <>
      (Product.tupled, Product.unapply)
  }

  val products = TableQuery[ProductTable]

  def all: Future[List[Product]] =
    dbConfig.db.run(products.result).map(_.toList)

  def byId(id: Long): Future[Option[Product]] =
    dbConfig.db.run(products.filter(_.id === id).result.headOption)

  def insert(product: Product): Future[Unit] = {
    val p = product.copy(lastUpdated = Option(new DateTime()))
    dbConfig.db.run(products += p).map(_ => ())
  }

  def update(id: Long, product: Product): Future[Unit] = {
    val p = product.copy(id = Some(id), lastUpdated = Option(new DateTime()))
    dbConfig.db.run(products.filter(_.id === id).update(p))
      .map(_ => ())
  }

  def delete(id: Long): Future[Unit] =
    dbConfig.db.run(products.filter(_.id === id).delete).map(_ => ())

}