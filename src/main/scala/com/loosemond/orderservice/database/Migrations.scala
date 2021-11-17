package com.loosemond.orderservice.database

import org.flywaydb.core.api.output.MigrateResult
import cats.effect.Sync
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException

object Migrations {
  def migrate[F[_]: Sync](): fs2.Stream[F, MigrateResult] = {

    val flyway: Flyway = Flyway.configure
      .dataSource(
        "jdbc:postgresql://localhost:5432/orders",
        "postgres",
        "password"
      ).locations("classpath:db/migration")
      .load
      
    fs2.Stream.eval {
      try {
        Sync[F].delay(flyway.migrate())
      } catch {
        case e: FlywayException => Sync[F].raiseError(e)
      }
    }

  }
}
