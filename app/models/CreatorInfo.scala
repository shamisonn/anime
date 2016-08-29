package models

import org.joda.time.DateTime

case class CreatorInfo(
                        id: Option[Long],
                        name: String,
                        birthDay: DateTime,
                        lastUpdated: Option[DateTime]
                      )
