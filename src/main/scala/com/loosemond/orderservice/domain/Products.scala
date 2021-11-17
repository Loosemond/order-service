package com.loosemond.orderservice.domain

import java.util.UUID

import cats.effect.Sync
import com.loosemond.orderservice.domain.Products.ProductMessage
import com.loosemond.orderservice.domain.Products.Product
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder}

// import scala.collection.mutable.ListBuffer
import java.util.Date
import java.text.DateFormat
// import scala.util.control.NonFatal
// import io.circe.Json
// import java.time.ZonedDateTime

trait Products[F[_]] {
  def create(product: Products.Product): F[Either[ProductMessage, Product]]
  def findById(id: String): F[Either[ProductMessage, Product]]
  def getAll(): F[List[Product]]
}

object Products {
  // def impl[F[_]: Sync](): Products[F] = new Products[F] {

  //   val products = new ListBuffer[Product]()

  //   override def create(
  //       product: Product
  //   ): F[Either[ProductMessage, Product]] = {
  //     // add one element at a time to the ListBuffer
  //     val createdProduct = product.copy(id = Option(UUID.randomUUID()))
  //     // val createdProduct2 = product.copy(creationDate = DateFormat.getDateInstance().parse(product.creationDate) )
  //     products += createdProduct
  //     Sync[F].pure(Right(createdProduct))
  //   }
  //   override def getAll(): F[List[Product]] = {
  //     Sync[F].pure(products.toList)

  //   }

  //   private def resolveId(id: String): Option[UUID] = {
  //     try {
  //       Option(UUID.fromString(id))
  //     } catch {
  //       case (_: IllegalArgumentException) => None
  //     }
  //   }

  //   override def findById(id: String): F[Either[ProductMessage, Product]] = {
  //     resolveId(id) match {
  //       case Some(resolvedId) =>
  //         Sync[F].pure {
  //           products.find(_.id.contains(resolvedId)) match {
  //             case Some(product) => Right(product)
  //             case None =>
  //               Left(
  //                 ProductMessage(
  //                   s"product did not exist with following identifier: $id"
  //                 )
  //               )
  //           }
  //         }
  //       case None =>
  //         Sync[F].pure(
  //           Left(ProductMessage(s"provided identifier was invalid: $id"))
  //         )
  //     }

  //   }
  // }
// finish
  case class Product(
      id: Option[UUID] = None,
      name: String,
      category: String,
      weight: Double,
      price: Double,
      creationDate: String
  ) {
    def getCreationDate(): Date =
      DateFormat.getDateInstance().parse(creationDate)

  }

  case class ProductDb(
      id: Option[UUID] = None,
      name: String,
      category: String,
      weight: Double,
      price: Double,
      creationDate: Date
  ) {}

  case class ProductMessage(message: String)

  object ProductMessage {
    implicit val productMessageDecoder: Decoder[ProductMessage] =
      deriveDecoder[ProductMessage]

    implicit def productMessageEntityDecoder[F[_]: Sync]
        : EntityDecoder[F, ProductMessage] = jsonOf

    implicit val productMessageEncoder: Encoder[ProductMessage] =
      deriveEncoder[ProductMessage]

    implicit def productMessageEntityEncoder[F[_]]
        : EntityEncoder[F, ProductMessage] = jsonEncoderOf

  }

  // implicit val decodeDate: Decoder[Date] = Decoder.decodeString.emap { s =>
  //   try {
  //     Right(DateFormat.getDateInstance().parse(s))
  //   } catch {
  //     case NonFatal(e) => Left(e.getMessage)
  //   }
  // }

  object Product {
    implicit val productDecoder: Decoder[Product] = deriveDecoder[Product]
    // implicit val productDecoder2: Decoder[Date] = deriveDecoder[Date]

    implicit def productEntityDecoder[F[_]: Sync]: EntityDecoder[F, Product] =
      jsonOf

    implicit val productEncoder: Encoder[Product] = deriveEncoder[Product]

    implicit def productEntityEncoder[F[_]]: EntityEncoder[F, Product] =
      jsonEncoderOf

    implicit def productEntityEncoder2[F[_]]: EntityEncoder[F, List[Product]] =
      jsonEncoderOf
  }

}
