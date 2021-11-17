package com.loosemond.orderservice.database

import cats.effect.Sync
import com.loosemond.orderservice.domain.Products
import com.loosemond.orderservice.domain.Products._
import com.loosemond.orderservice.database.ctx
import io.getquill.EntityQuery

import java.util.UUID

class ProductsRepository[F[_]: Sync] extends Products[F] {

  import ctx._

  val products: ctx.Quoted[EntityQuery[Product]] = quote {
    querySchema[Product](
      "products",
      // _.id -> "id",
      _.name -> "name",
      _.category -> "category",
      _.weight -> "weight",
      _.price -> "price",
      _.creationDate -> "creationdate"
    )
  }

  override def create(product: Product): F[Either[ProductMessage, Product]] = {
    // ???
    // val productDb: ProductDb = ProductDb(
    //   name = product.name,
    //   category = product.category,
    //   weight = product.weight,
    //   price = product.price,
    //   creationDate = product.getCreationDate()
    // )
    Sync[F].delay {
      ctx
        .run(
          products
            .insert(
              _.name -> lift(product.name),
              _.category -> lift(product.category),
              _.weight -> lift(product.weight),
              _.price -> lift(product.price),
              _.creationDate -> lift(product.creationDate)
            )
            .returningGenerated(_.id)
        )
        .map { uiid =>
          Right(product.copy(id = Some(uiid)))
        }
        .getOrElse(Left(ProductMessage("")))
    }
  }

  def fromUUID(uuid: String): Option[UUID] = {
    try {
      Option(UUID.fromString(uuid))
    } catch {
      case _: IllegalArgumentException => None
    }
  }

  override def findById(id: String): F[Either[ProductMessage, Product]] = {
    // ???
    fromUUID(id)
      .map { id =>
        Sync[F].delay {
          ctx.run(products.filter(_.id.contains(lift(id)))) match {
            case Seq(product) => Right(product)
            case Seq() =>
              Left(
                ProductMessage(
                  s"product did not exist with following identifier: $id"
                )
              )
            case _ => Left(ProductMessage(""))
          }
        }
      }
      .getOrElse(
        Sync[F]
          .pure(Left(ProductMessage(s"provided identifier was invalid: $id")))
      )

  }

  def getAll(): F[List[Product]] = {
    // ???
    Sync[F].delay {
      ctx.run(products)

    }
  }

}
