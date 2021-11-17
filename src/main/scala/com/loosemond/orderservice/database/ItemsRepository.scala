package com.loosemond.orderservice.database
import cats.effect.Sync
import io.getquill.EntityQuery
import java.util.UUID
import com.loosemond.orderservice.database.ctx

import com.loosemond.orderservice.domain.Items
import com.loosemond.orderservice.domain.Items._
import com.loosemond.orderservice.domain.Products._

class ItemsRepository[F[_]: Sync] extends Items[F] {

  import ctx._

  val items: ctx.Quoted[EntityQuery[ItemDTO]] = quote {
    querySchema[ItemDTO](
      "items",
      // _.id -> "id",
      _.price -> "price",
      _.product -> "product_id",
      _.shippingFee -> "shipping_fee",
      _.price -> "price"
    )
  }

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

  override def create(item: ItemDTO): F[Either[ItemMessage, Item]] = {
    // ???
    // val itemDb: ItemDb = ItemDb(
    //   name = item.name,
    //   category = item.category,
    //   weight = item.weight,
    //   price = item.price,
    //   creationDate = item.getCreationDate()
    // )
    Sync[F].delay {
      ctx
        .run(
          items
            .insert(
              lift(item)
            )
            .returningGenerated(_.id)
        )
        .map { uiid =>
          Right(
            Item(
              id = Some(uiid),
              product = Product(
                name = s"shoe",
                category = "clothes",
                weight = 0.300,
                price = 49.99,
                creationDate = "10-11-2021"
              ),
              shippingFee = item.shippingFee,
              price = item.price
            )
          )
        }
        .getOrElse(Left(ItemMessage("")))
    }
  }

  def fromUUID(uuid: String): Option[UUID] = {
    try {
      Option(UUID.fromString(uuid))
    } catch {
      case _: IllegalArgumentException => None
    }
  }

  override def findById(id: String): F[Either[ItemMessage, Item]] = {
    // ???
    fromUUID(id)
      .map { id =>
        Sync[F].delay {
          //   ctx.run(items.filter(a => a.id.contains(lift(id))).flatMap(b => products.filter(c => c.id == b.id))) match {
          // ctx.run(items.join(products).on(_.id == _.id)) match {
          ctx.run(items.filter(a => a.id.contains(lift(id)))) match {
            // case Seq(item) => Right(item)
            case Seq(item: ItemDTO) =>
              ctx.run(
                products.filter(a => a.id.contains(lift(item.product)))
              ) match {
                case Seq(recProduct) =>
                  Right(
                    Item(
                      id = item.id,
                      product = recProduct,
                      shippingFee = item.shippingFee,
                      price = item.price
                    )
                  )
                case _ =>
                  Left(
                    ItemMessage(
                      s"Product did not exist with following identifier: ${item.product}"
                    )
                  )
              }
            case Seq() =>
              Left(
                ItemMessage(
                  s"item did not exist with following identifier: $id"
                )
              )
            case _ => Left(ItemMessage(""))
          }
        }
      }
      .getOrElse(
        Sync[F]
          .pure(Left(ItemMessage(s"provided identifier was invalid: $id")))
      )

  }

  def getAll(): F[List[Item]] = {
    // ???
    Sync[F]
      .delay {
        val itemDTOList = ctx.run(items)
        val productList: List[Product] = ctx.run(products)
        var itemList: List[Item] = List[Item]()

        itemDTOList.map(l =>
          itemList = itemList.appended(
            Item(
              id = l.id,
              product = productList.filter(p => p.id.contains(l.product)).head,
              shippingFee = l.shippingFee,
              price = l.price
            )
          )
        )
        itemList
      }

  }

}
