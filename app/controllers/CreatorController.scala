package controllers

import com.google.inject.Inject
import daos.CreatorDAO
import models.Creator
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by sham_2 on 2016/08/30.
  */
class CreatorController @Inject()(val messagesApi: MessagesApi, dao: CreatorDAO)
  extends Controller with I18nSupport {

  val form = Form(
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText,
      "role" -> nonEmptyText,
      "productId" -> optional(longNumber),
      "creatorInfoId" -> optional(longNumber)
    )(Creator.apply)(Creator.unapply)
  )

  // GET /creators
  def index = Action.async { implicit req =>
    dao.all.map(ls => Ok(views.html.creators.index("", ls)))
  }

  // GET /creators/new
  def add = Action {
    Ok(views.html.creators.add("about page", form))
  }

  // POST /creators
  def create = Action.async { implicit req =>
    form.bindFromRequest.fold(
      error => {
        Future(BadRequest(views.html.creators.add("ERROR", error)))
      },
      creator => {
        dao.insert(creator).flatMap(cnt =>
          dao.all.map(ls => Ok(views.html.creators.index("", ls)))
        )
      }
    )
  }

  // GET /creators/{id}
  def getById(id: Long) = Action.async {
    dao.byId(id).flatMap {
      case Some(cre) =>
        Future(Ok(views.html.creators.about("", cre)))
      case None =>
        dao.all.map(ls => BadRequest(views.html.creators.index("", ls)))
    }
  }

  // GET /creators/{id}/edit
  def edit(id: Long) = Action.async {
    dao.byId(id).flatMap {
      case Some(cre) =>
        Future(Ok(views.html.creators.edit("", form.fill(cre))))
      case None =>
        dao.all.map(ls => BadRequest(views.html.creators.index("", ls)))
    }

  }

  // POST /creators/{id}
  def update(id: Long) = Action.async { implicit req =>
    form.bindFromRequest.fold(
      error => {
        Future(BadRequest(views.html.creators.edit("ERROR", error)))
      },
      creator => {
        dao.update(id, creator).flatMap(cnt =>
          Future(Ok(views.html.creators.about("", creator)))
        )
      }
    )
  }

  // POST /creators/{id}/delete
  def delete(id: Long) = Action.async {
    dao.byId(id).flatMap {
      case Some(pro) =>
        dao.delete(id)
        dao.all.map(ls => Ok(views.html.creators.index("", ls)))
      case None =>
        dao.all.map(ls => BadRequest(views.html.creators.index("", ls)))
    }
  }
}
