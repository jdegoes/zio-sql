package zio.sql.oracle

import java.time.LocalDate
import java.util.UUID

import zio.Cause
import zio.test.Assertion._
import zio.test._

//import scala.language.postfixOps

object OracleModuleTest extends OracleRunnableSpec with ShopSchema {

  import Customers._
//  import Orders._

//  private def customerSelectJoseAssertion(condition: Expr[_, customers.TableType, Boolean]) = {
//    case class Customer(id: UUID, fname: String, lname: String, verified: Boolean, dateOfBirth: LocalDate)
//
//    val query =
//      select(customerId ++ fName ++ lName ++ verified ++ dob) from customers where (condition)
//
//    println(renderRead(query))
//
//    val expected =
//      Seq(
//        Customer(
//          UUID.fromString("636ae137-5b1a-4c8c-b11f-c47c624d9cdc"),
//          "Jose",
//          "Wiggins",
//          false,
//          LocalDate.parse("1987-03-23")
//        )
//      )
//
//    val testResult = execute(query)
//      .to[UUID, String, String, Boolean, LocalDate, Customer] { case row =>
//        Customer(row._1, row._2, row._3, row._4, row._5)
//      }
//
//    val assertion = for {
//      r <- testResult.runCollect
//    } yield assert(r)(hasSameElementsDistinct(expected))
//
//    assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
//  }

  val spec = suite("Oracle module")(
    testM("Can select from single table") {
      case class Customer(id: UUID, fname: String, lname: String, dateOfBirth: LocalDate)

      val query = select(customerId ++ fName ++ lName ++ dob) from customers

      println(renderRead(query))

      val expected =
        Seq(
          Customer(
            UUID.fromString("60b01fc9-c902-4468-8d49-3c0f989def37"),
            "Ronald",
            "Russell",
            LocalDate.parse("1983-01-05")
          ),
          Customer(
            UUID.fromString("f76c9ace-be07-4bf3-bd4c-4a9c62882e64"),
            "Terrence",
            "Noel",
            LocalDate.parse("1999-11-02")
          ),
          Customer(
            UUID.fromString("784426a5-b90a-4759-afbb-571b7a0ba35e"),
            "Mila",
            "Paterso",
            LocalDate.parse("1990-11-16")
          ),
          Customer(
            UUID.fromString("df8215a2-d5fd-4c6c-9984-801a1b3a2a0b"),
            "Alana",
            "Murray",
            LocalDate.parse("1995-11-12")
          ),
          Customer(
            UUID.fromString("636ae137-5b1a-4c8c-b11f-c47c624d9cdc"),
            "Jose",
            "Wiggins",
            LocalDate.parse("1987-03-23")
          )
        )

      val testResult = execute(query)
        .to[UUID, String, String, LocalDate, Customer] { case row =>
          Customer(row._1, row._2, row._3, row._4)
        }

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r)(hasSameElementsDistinct(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    }
  )
}
