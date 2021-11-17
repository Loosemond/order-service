package com.loosemond.orderservice

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import com.loosemond.orderservice.domain._
import com.loosemond.orderservice.domain.Products._
import com.loosemond.orderservice.domain.Items._
// import shapeless.ops.product

object OrderserviceRoutes {

  def productRoutes[F[_]: Sync](products: Products[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case request @ POST -> Root / "products" =>
        for {
          product <- request.as[Product]
          createdProductE <- products.create(product)
          resp <- createdProductE match {
            case Left(message)         => BadRequest(message)
            case Right(createdProduct) => Created(createdProduct)
          }
        } yield resp
      case GET -> Root / "products" / id =>
        for {
          resolvedProductO <- products.findById(id)
          resp <- resolvedProductO match {
            case Right(resolvedProduct) => Ok(resolvedProduct)
            case Left(productMessage @ ProductMessage(message))
                if message.startsWith(
                  "product did not exist with following identifier"
                ) =>
              NotFound(productMessage)
            case Left(productMessage) => BadRequest(productMessage)
          }
        } yield resp
      case GET -> Root / "products" => Ok(products.getAll())

    }

  }

  def itemRoutes[F[_]: Sync](items: Items[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}

    import dsl._
    HttpRoutes.of[F] {
      case request @ POST -> Root / "items" =>
        for {
          item <- request.as[ItemDTO]

          createdItemE <- items.create(item)
          // productById <- product2.findById(item.id.)

          resp <- createdItemE match {
            case Left(message)      => BadRequest(message)
            case Right(createdItem) => Created(createdItem)
          }
        } yield resp
      case GET -> Root / "items" / id =>
        for {
          resolvedItemO <- items.findById(id)
          resp <- resolvedItemO match {
            case Right(resolvedItem) => Ok(resolvedItem)
            case Left(itemMessage @ ItemMessage(message))
                if message.startsWith(
                  "item did not exist with following identifier"
                ) =>
              NotFound(itemMessage)
            case Left(itemMessage) => BadRequest(itemMessage)
          }
        } yield resp
      case GET -> Root / "items" => Ok(items.getAll())

    }

  }
}
