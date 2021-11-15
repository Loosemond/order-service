package com.loosemond.orderservice.domain

import java.util.UUID

import cats.effect.Sync
import com.loosemond.orderservice.domain.Items.ItemMessage
import com.loosemond.orderservice.domain.Items.Item
// import com.loosemond.orderservice.domain.Products.Product
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder}
// import com.loosemond.orderservice.domain.Products
import com.loosemond.orderservice.domain.Products.Product

import scala.collection.mutable.ListBuffer

trait Items[F[_]] {
  def create(item: Items.Item): F[Either[ItemMessage, Item]]
  def findById(id: String): F[Either[ItemMessage, Item]]
  def getAll(): F[List[Item]]
}

object Items {
  def impl[F[_]: Sync](): Items[F] = new Items[F] {

    val items = new ListBuffer[Item]()

    override def create(
        item: Item
    ): F[Either[ItemMessage, Item]] = {
      // add one element at a time to the ListBuffer
      val createdItem = item.copy(id = Option(UUID.randomUUID()))
      // val createdItem2 = item.copy(creationDate = DateFormat.getDateInstance().parse(item.creationDate) )
      items += createdItem
      Sync[F].pure(Right(createdItem))
    }
    override def getAll(): F[List[Item]] = {
      Sync[F].pure(items.toList)

    }

    private def resolveId(id: String): Option[UUID] = {
      try {
        Option(UUID.fromString(id))
      } catch {
        case (_: IllegalArgumentException) => None
      }
    }

    override def findById(id: String): F[Either[ItemMessage, Item]] = {
      resolveId(id) match {
        case Some(resolvedId) =>
          Sync[F].pure {
            items.find(_.id.contains(resolvedId)) match {
              case Some(item) => Right(item)
              case None =>
                Left(
                  ItemMessage(
                    s"item did not exist with following identifier: $id"
                  )
                )
            }
          }
        case None =>
          Sync[F].pure(
            Left(ItemMessage(s"provided identifier was invalid: $id"))
          )
      }

    }
  }
// finish
  case class Item(
      id: Option[UUID] = None,
      product: Product,
      shippingFee: Double,
      price: Double
  ) {
    var tax: Double = price * 0.23 // TODO make a variable for tax rate!!!
    def totalPrice(): Double = tax + shippingFee + price

  }

  // case class ItemF(
  //     id: Option[UUID] = None,
  //     product: Product,
  //     shippingFee: Double,
  //     price: Double
  // ) {
  //   var tax: Double = price * 0.23 // TODO make a variable for tax rate!!!
  //   def totalPrice(): Double = tax + shippingFee + price

  // }

  case class ItemMessage(message: String)

  object ItemMessage {
    implicit val itemMessageDecoder: Decoder[ItemMessage] =
      deriveDecoder[ItemMessage]

    implicit def itemMessageEntityDecoder[F[_]: Sync]
        : EntityDecoder[F, ItemMessage] = jsonOf

    implicit val itemMessageEncoder: Encoder[ItemMessage] =
      deriveEncoder[ItemMessage]

    implicit def itemMessageEntityEncoder[F[_]]: EntityEncoder[F, ItemMessage] =
      jsonEncoderOf

  }

  object Item {
    implicit val itemDecoder: Decoder[Item] = deriveDecoder[Item]

    // implicit val itemDecoder2: Decoder[Product] = deriveDecoder[Product]

    implicit def itemEntityDecoder[F[_]: Sync]: EntityDecoder[F, Item] =
      jsonOf

    implicit val itemEncoder: Encoder[Item] = deriveEncoder[Item]

    implicit def itemEntityEncoder[F[_]]: EntityEncoder[F, Item] =
      jsonEncoderOf

    implicit def itemEntityEncoder2[F[_]]: EntityEncoder[F, List[Item]] =
      jsonEncoderOf
  }

}
