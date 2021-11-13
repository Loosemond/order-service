package com.loosemond.orderservice

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import com.loosemond.orderservice.domain.Products
import com.loosemond.orderservice.domain.Products.Product
import com.loosemond.orderservice.domain.Products.ProductMessage

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
}
