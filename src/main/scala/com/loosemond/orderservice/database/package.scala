package com.loosemond.orderservice
import io.getquill.{PostgresJdbcContext, SnakeCase}

package object database {
  lazy val ctx = new PostgresJdbcContext(SnakeCase, "ctx")
}
