package controllers

import com.google.inject.Inject
import daos.CreatorInfoDAO
import models.CreatorInfo
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by sham_2 on 2016/08/30.
  */
class CreatorInfoController @Inject()(val messagesApi: MessagesApi, dao: CreatorInfoDAO)
  extends Controller with I18nSupport {

  val form = Form(
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText,
      "birthDay" -> jodaDate,
      "lastUpdate" -> optional(jodaDate)
    )(CreatorInfo.apply)(CreatorInfo.unapply)
  )

  // GET /creators_info
  def index = Action.async { implicit req =>
    dao.all.map(ls => Ok(views.html.creatorsInfo.index("", ls)))
  }

  // GET /creators_info/new
  def add = Action {
    Ok(views.html.creatorsInfo.add("about page", form))
  }

  // POST /creators_info
  def create = Action.async { implicit req =>
    form.bindFromRequest.fold(
      error => {
        Future(BadRequest(views.html.creatorsInfo.add("ERROR", error)))
      },
      creator => {
        dao.insert(creator).flatMap(cnt =>
          dao.all.map(ls => Ok(views.html.creatorsInfo.index("", ls)))
        )
      }
    )
  }

  // GET /creators_info/{id}
  def getById(id: Long) = Action.async {
    dao.byId(id).flatMap {
      case Some(cre) =>
        Future(Ok(views.html.creatorsInfo.about("", cre)))
      case None =>
        dao.all.map(ls => BadRequest(views.html.creatorsInfo.index("", ls)))
    }
  }

  // GET /creators_info/{id}/edit
  def edit(id: Long) = Action.async {
    dao.byId(id).flatMap {
      case Some(cre) =>
        Future(Ok(views.html.creatorsInfo.edit("", form.fill(cre))))
      case None =>
        dao.all.map(ls => BadRequest(views.html.creatorsInfo.index("", ls)))
    }

  }

  // POST /creators_info/{id}
  def update(id: Long) = Action.async { implicit req =>
    form.bindFromRequest.fold(
      error => {
        Future(BadRequest(views.html.creatorsInfo.edit("ERROR", error)))
      },
      creatorInfo => {
        dao.update(id, creatorInfo).flatMap(cnt =>
          Future(Ok(views.html.creatorsInfo.about("", creatorInfo)))
        )
      }
    )
  }

  // POST /creators_info/{id}/delete
  def delete(id: Long) = Action.async {
    dao.byId(id).flatMap {
      case Some(pro) =>
        dao.delete(id)
        dao.all.map(ls => Ok(views.html.creatorsInfo.index("", ls)))
      case None =>
        dao.all.map(ls => BadRequest(views.html.creatorsInfo.index("", ls)))
    }
  }
}
