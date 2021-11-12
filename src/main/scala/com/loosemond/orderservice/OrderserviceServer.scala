package com.loosemond.orderservice

import cats.effect.{ConcurrentEffect, Timer}
import fs2.Stream
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import scala.concurrent.ExecutionContext.global

object OrderserviceServer {

  def stream[F[_]: ConcurrentEffect](implicit
      T: Timer[F]
  ): Stream[F, Nothing] = {
    val helloWorldAlg = HelloWorld.impl[F]
    val httpApp = (
      OrderserviceRoutes.helloWorldRoutes[F](helloWorldAlg)
    ).orNotFound
    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    for {
      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
