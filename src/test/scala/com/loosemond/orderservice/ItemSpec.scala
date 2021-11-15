package com.loosemond.orderservice

// import java.util.UUID

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import munit.CatsEffectSuite
import org.http4s
import io.circe.syntax._
import org.http4s.circe._
import com.loosemond.orderservice.domain.Items.Item
import com.loosemond.orderservice.domain.Items
import com.loosemond.orderservice.domain.Products.Product

// import com.loosemond.orderservice.domain.Items.ItemMessage

class ItemSpecSpec extends CatsEffectSuite {

  test("Creating items") {
    val createdItemResponse: Response[IO] =
      createItem(
        Item(
          product = Product(
            name = "shoe",
            category = "clothes",
            weight = 0.300,
            price = 49.99,
            creationDate = "10-11-2021"
          ),
          shippingFee = 2.3,
          price = 20.2
        )
      )
    val createdItem: IO[Item] = createdItemResponse.as[Item]
    assert(
      createdItemResponse.status == Status.Created,
      s"\nExpected: ${Status.Created}, Actual: ${createdItemResponse.status}"
    )

    assertIOBoolean(createdItem.map(_.id.isDefined), "id was not defined")
    // assertIOBoolean(
    //   createdItem.map(_.price.equals(20.5)),
    //   "price did not match"
    // )
    assertIOBoolean(
      createdItem.map(_.shippingFee.equals(2.3)),
      "Shipping Fee did not match"
    )

    // assert(false)

  }

  val server: http4s.HttpApp[IO] = {
    OrderserviceRoutes.itemRoutes[IO](Items.impl[IO]()).orNotFound

  }

  private[this] def createItem(product: Item): Response[IO] = {
    val postItem: Request[IO] =
      Request[IO](Method.POST, uri"/items")
        .withEntity(product.asJson)
    this.server.run(postItem).unsafeRunSync()

  }

}
