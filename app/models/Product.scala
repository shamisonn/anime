package models

import org.joda.time.DateTime

case class Product(
                    id: Option[Long],
                    title: String,
                    company: String,
                    publishDate: DateTime,
                    lastUpdated: Option[DateTime]
                  )
