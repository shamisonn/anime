package models

case class Creator(
                    id: Option[Long],
                    name: String,
                    role: String,
                    productId: Option[Long],
                    creatorInfoId: Option[Long]
                  )
