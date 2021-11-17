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
import com.loosemond.orderservice.database.Migrations
import com.loosemond.orderservice.database.ProductsRepository
// import cats.instances.double
// import com.loosemond.orderservice.domain.Products.ProductMessage

class ProductSpecSpec extends CatsEffectSuite {

  test("Creating products") {
    val uuid = UUID.randomUUID()
    val createdProductResponse: Response[IO] =
      createProduct(
        Product(
          name = s"shoe - ${uuid}",
          category = "clothes",
          weight = 0.300,
          price = 49.99,
          creationDate = "10-11-2021"
        )
      )
    val createdProduct = createdProductResponse.as[Product]
    assert(
      createdProductResponse.status == Status.Created,
      s"\nExpected: ${Status.Created}, Actual: ${createdProductResponse.status}"
    )
    assertIOBoolean(createdProduct.map(_.id.isDefined), "id was not defined")
    assertIOBoolean(
      createdProduct.map(_.name.contains(s"shoe - ${uuid}")),
      "Name did not match"
    )
    assertIOBoolean(
      createdProduct.map(_.category.contains("clothes")),
      "Category did not match"
    )
    assertIOBoolean(
      createdProduct.map(_.weight.equals(0.3)),
      "Weight did not match"
    )
    assertIOBoolean(
      createdProduct.map(_.price.equals(49.99)),
      "price did not match"
    )

    // assertIOBoolean(
    //   createdProduct.map(_.creationDate.contains("10-11-2021")),
    //   "price did not match"
    // )

  }

  test("Retrieving products") {
    // creating the obj to test
    val uuid = UUID.randomUUID()

    val createdProduct: Product = createProduct(
      Product(
        name = s"shoe - ${uuid}",
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
    assertIOBoolean(
      resolvedProduct.map(_.id.contains(productId)),
      "id did not match"
    )
    assertIO(resolvedProduct.map(_.name), s"shoe - ${uuid}")
    assertIO(resolvedProduct.map(_.category), "clothes")
    assertIO(resolvedProduct.map(_.weight), 0.3)
    assertIO(resolvedProduct.map(_.price), 49.99)
    assertIO(resolvedProduct.map(_.creationDate), "10-11-2021")

  }

  val server: http4s.HttpApp[IO] = {
    Migrations.migrate[IO]().compile.drain.unsafeRunSync()
    OrderserviceRoutes
      .productRoutes[IO](new ProductsRepository[IO]())
      .orNotFound
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
