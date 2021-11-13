package com.loosemond.orderservice

import java.util.UUID

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import munit.CatsEffectSuite
import org.http4s
import io.circe.syntax._
import org.http4s.circe._
import com.loosemond.orderservice.domain.Products.Product
import com.loosemond.orderservice.domain.Products
// import com.loosemond.orderservice.domain.Products.ProductMessage

class OrderSpecSpec extends CatsEffectSuite {

  test("Creating products") {
    val createdProductResponse: Response[IO] =
      createProduct(
        Product(
          name = "shoe",
          category = "clothes",
          weight = 0.300,
          price = 49.99,
          creationDate = "10-11-2021"
        )
      )
    val createdProduct: IO[Product] = createdProductResponse.as[Product]
    assert(
      createdProductResponse.status == Status.Created,
      s"\nExpected: ${Status.Created}, Actual: ${createdProductResponse.status}"
    )
    assertIO(createdProduct.map(_.name), "shoe")
    assertIO(createdProduct.map(_.category), "clothes")
    assertIO(createdProduct.map(_.weight), 0.300)
    assertIO(createdProduct.map(_.price), 49.99)
    assertIO(createdProduct.map(_.creationDate), "10-11-2021")

    assertIOBoolean(createdProduct.map(_.id.isDefined), "id was not defined")

  }

  test("Retrieving products") {
    // creating the obj to test
    val createdProduct: Product = createProduct(
      Product(
        name = "shoe",
        category = "clothes",
        weight = 0.300,
        price = 49.99,
        creationDate = "10-11-2021"
      )
    ).as[Product].unsafeRunSync()
    val productId: UUID =
      createdProduct.id.getOrElse(fail("identifier was not provided"))
    val resolvedProductResponse = getProduct(productId.toString)
    val resolvedProduct = resolvedProductResponse.as[Product]
    assert(
      resolvedProductResponse.status == Status.Ok,
      s"Expected: ${Status.Ok}, Actual: ${resolvedProductResponse.status}"
    )
    assertIO(resolvedProduct.map(_.name), "foo")
    assertIOBoolean(
      resolvedProduct.map(_.id.contains(productId)),
      "id did not match"
    )

  }

  val server: http4s.HttpApp[IO] = {
    OrderserviceRoutes.productRoutes[IO](Products.impl[IO]()).orNotFound
  }

  private[this] def createProduct(product: Product): Response[IO] = {
    val postProduct: Request[IO] =
      Request[IO](Method.POST, uri"/products")
        .withEntity(product.asJson)
    this.server.run(postProduct).unsafeRunSync()

  }

  private[this] def getProduct(id: String): Response[IO] = {
    val getProduct: Request[IO] =
      Request[IO](Method.GET, uri"/products" / id)
    this.server.run(getProduct).unsafeRunSync()
  }

  // private[this] def createProduct(product: Product): Response[IO] = {
  //   val postProduct: Request[IO] =
  //     Request[IO](Method.POST, uri"/products").withEntity(product.asJson)
  //   this.server.run(postProduct).unsafeRunSync()
  // }

}
