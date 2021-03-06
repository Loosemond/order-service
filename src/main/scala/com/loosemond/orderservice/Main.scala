package com.loosemond.orderservice

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    OrderserviceServer.stream[IO].compile.drain.as(ExitCode.Success)
}
