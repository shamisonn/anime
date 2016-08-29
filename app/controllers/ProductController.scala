package controllers

import com.google.inject.Inject
import daos.ProductDAO
import models.Product
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by sham_2 on 2016/08/28.
  */
class ProductController @Inject()(val messagesApi: MessagesApi, dao: ProductDAO)
  extends Controller with I18nSupport {

  val form = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText,
      "company" -> nonEmptyText,
      "publishDate" -> jodaDate,
      "lastUpdate" -> optional(jodaDate)
    )(Product.apply)(Product.unapply)
  )

  // GET /products
  def index = Action.async { implicit req =>
    dao.all.map(ls => Ok(views.html.products.index("", ls)))
  }

  // GET /products/new
  def add = Action {
    Ok(views.html.products.add("about page", form))
  }

  // POST /products
  def create = Action.async { implicit req =>
    form.bindFromRequest.fold(
      error => {
        Future(BadRequest(views.html.products.add("ERROR", error)))
      },
      product => {
        dao.insert(product).flatMap(cnt =>
          dao.all.map(ls => Ok(views.html.products.index("", ls)))
        )
      }
    )
  }

  // GET /products/{id}
  def getById(id: Long) = Action.async {
    dao.byId(id).flatMap {
      case Some(pro) =>
        Future(Ok(views.html.products.about("", pro)))
      case None =>
        dao.all.map(ls => BadRequest(views.html.products.index("", ls)))
    }
  }

  // GET /products/{id}/edit
  def edit(id: Long) = Action.async {
    dao.byId(id).flatMap {
      case Some(pro) =>
        Future(Ok(views.html.products.edit("", form.fill(pro))))
      case None =>
        dao.all.map(ls => BadRequest(views.html.products.index("", ls)))
    }

  }

  // POST /products/{id}
  def update(id: Long) = Action.async { implicit req =>
    form.bindFromRequest.fold(
      error => {
        Future(BadRequest(views.html.products.edit("ERROR", error)))
      },
      product => {
        dao.update(id, product).flatMap(cnt =>
          Future(Ok(views.html.products.about("", product)))
        )
      }
    )
  }

  // POST /products/{id}/delete
  def delete(id: Long) = Action.async {
    dao.byId(id).flatMap {
      case Some(pro) =>
        dao.delete(id)
        dao.all.map(ls => Ok(views.html.products.index("", ls)))
      case None =>
        dao.all.map(ls => BadRequest(views.html.products.index("", ls)))
    }
  }


}
