package com.loosemond.orderservice

import cats.effect.{ConcurrentEffect, Timer}
import fs2.Stream
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import scala.concurrent.ExecutionContext.global
// import com.loosemond.orderservice.domain.Products
import com.loosemond.orderservice.domain.Items
import cats.syntax.all._
// import com.loosemond.orderservice.database.Migrations
import com.loosemond.orderservice.database._
// import com.comcast.ip4s._

object OrderserviceServer {

  def stream[F[_]: ConcurrentEffect](implicit
      T: Timer[F]
  ): Stream[F, Nothing] = {
    val httpApp = (
      OrderserviceRoutes
        .productRoutes[F](new ProductsRepository[F]()) <+>
        OrderserviceRoutes.itemRoutes[F](Items.impl[F]())
    ).orNotFound

    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    for {
      _ <- Migrations
        .migrate[F]() // Server Wont start if we cant start migrations
      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
