package zio.sql.sqlserver

import java.time._
import java.util.UUID

import zio._
import zio.test.Assertion._
import zio.test._

import zio.test.TestAspect.ignore
import zio.test.TestAspect.sequential

import scala.language.postfixOps

object PostgresModuleSpec extends SqlServerRunnableSpec with DbSchema {

  import AggregationDef._
  import Customers._
  import Orders._

  private def customerSelectJoseAssertion(condition: Expr[_, customers.TableType, Boolean]) = {
    case class Customer(id: UUID, fname: String, lname: String, verified: Boolean, dateOfBirth: LocalDate)

    val query =
      select(customerId ++ fName ++ lName ++ verified ++ dob).from(customers).where(condition)

    val expected =
      Seq(
        Customer(
          UUID.fromString("636ae137-5b1a-4c8c-b11f-c47c624d9cdc"),
          "Jose",
          "Wiggins",
          false,
          LocalDate.parse("1987-03-23")
        )
      )

    val testResult = execute(
      query
        .to[UUID, String, String, Boolean, LocalDate, Customer] { case row =>
          Customer(row._1, row._2, row._3, row._4, row._5)
        }
    )

    val assertion = for {
      r <- testResult.runCollect
    } yield assert(r)(hasSameElementsDistinct(expected))

    assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
  }

  override def specLayered = suite("MSSQL Server module")(
    testM("Can select from single table") {
      case class Customer(id: UUID, fname: String, lname: String, dateOfBirth: LocalDate)

      val query = select(customerId ++ fName ++ lName ++ dob).from(customers)

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

      val testResult = execute(
        query
          .to[UUID, String, String, LocalDate, Customer] { case row =>
            Customer(row._1, row._2, row._3, row._4)
          }
      )

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r)(hasSameElementsDistinct(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    //TODO fix tests -> they were just copy pasted from postgres module to find bugs
    testM("Can select with property unary operator") {
      customerSelectJoseAssertion(verified isNotTrue)
    } @@ ignore,
    testM("Can select with property binary operator with UUID") {
      customerSelectJoseAssertion(customerId === UUID.fromString("636ae137-5b1a-4c8c-b11f-c47c624d9cdc"))
    } @@ ignore,
    testM("Can select with property binary operator with String") {
      customerSelectJoseAssertion(fName === "Jose")
    } @@ ignore,
    testM("Can select with property binary operator with LocalDate") {
      customerSelectJoseAssertion(dob === LocalDate.parse("1987-03-23"))
    } @@ ignore,
    testM("Can select with property binary operator with LocalDateTime") {
      customerSelectJoseAssertion(dob === LocalDateTime.parse("1987-03-23T00:00:00"))
    } @@ ignore,
    testM("Can select with property binary operator with OffsetDateTime") {
      customerSelectJoseAssertion(dob === OffsetDateTime.parse("1987-03-23T00:00:00Z"))
    } @@ ignore,
    testM("Can select with property binary operator with ZonedLocalDate") {
      customerSelectJoseAssertion(dob === ZonedDateTime.parse("1987-03-23T00:00:00Z"))
    } @@ ignore,
    testM("Can select with property binary operator with Instant") {
      customerSelectJoseAssertion(dob === Instant.parse("1987-03-23T00:00:00Z"))
    } @@ ignore,
    testM("Can select from single table with limit, offset and order by") {
      case class Customer(id: UUID, fname: String, lname: String, dateOfBirth: LocalDate)

      val query = select(customerId ++ fName ++ lName ++ dob).from(customers).limit(1).offset(1).orderBy(fName)

      val expected =
        Seq(
          Customer(
            UUID.fromString("636ae137-5b1a-4c8c-b11f-c47c624d9cdc"),
            "Jose",
            "Wiggins",
            LocalDate.parse("1987-03-23")
          )
        )

      val testResult = execute(
        query
          .to[UUID, String, String, LocalDate, Customer] { case row =>
            Customer(row._1, row._2, row._3, row._4)
          }
      )

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r)(hasSameElementsDistinct(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    } @@ ignore,
    testM("Can count rows") {
      val query = select(Count(customerId)).from(customers)

      val expected = 5L

      val result = execute(query.to[Long, Long](identity))

      for {
        r <- result.runCollect
      } yield assert(r.head)(equalTo(expected))
    },
    testM("cross apply") {
      val query = select(fName ++ lName ++ orderDate).from(customers.crossApply(select(orderDate).from(orders)).where(fkCustomerId === customerId))

      case class Row(firstName: String, lastName: String, orderDate: LocalDate)

      val expected = Seq(
        Row("Ronald", "Russell", LocalDate.parse("2019-03-25")),
        Row("Ronald", "Russell", LocalDate.parse("2018-06-04")),
        Row("Alana", "Murray", LocalDate.parse("2019-08-19")),
        Row("Jose", "Wiggins", LocalDate.parse("2019-08-30")),
        Row("Jose", "Wiggins", LocalDate.parse("2019-03-07")),
        Row("Ronald", "Russell", LocalDate.parse("2020-03-19")),
        Row("Alana", "Murray", LocalDate.parse("2020-05-11")),
        Row("Alana", "Murray", LocalDate.parse("2019-02-21")),
        Row("Ronald", "Russell", LocalDate.parse("2018-05-06")),
        Row("Mila", "Paterso", LocalDate.parse("2019-02-11")),
        Row("Terrence", "Noel", LocalDate.parse("2019-10-12")),
        Row("Ronald", "Russell", LocalDate.parse("2019-01-29")),
        Row("Terrence", "Noel", LocalDate.parse("2019-02-10")),
        Row("Ronald", "Russell", LocalDate.parse("2019-09-27")),
        Row("Alana", "Murray", LocalDate.parse("2018-11-13")),
        Row("Jose", "Wiggins", LocalDate.parse("2020-01-15")),
        Row("Terrence", "Noel", LocalDate.parse("2018-07-10")),
        Row("Mila", "Paterso", LocalDate.parse("2019-08-01")),
        Row("Alana", "Murray", LocalDate.parse("2019-12-08")),
        Row("Mila", "Paterso", LocalDate.parse("2019-11-04")),
        Row("Mila", "Paterso", LocalDate.parse("2018-10-14")),
        Row("Terrence", "Noel", LocalDate.parse("2020-04-05")),
        Row("Jose", "Wiggins", LocalDate.parse("2019-01-23")),
        Row("Terrence", "Noel", LocalDate.parse("2019-05-14")),
        Row("Mila", "Paterso", LocalDate.parse("2020-04-30"))
      )

      val result = execute(
        query
          .to[String, String, LocalDate, Row] { case row =>
            Row(row._1, row._2, row._3)
          }
      )

      val assertion = for {
        r <- result.runCollect
      } yield assert(r)(hasSameElementsDistinct(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))


      //TODO SHOULD NOT COMPILE because orderDate is not selected in "tabled valued function" select(orderId).from(orders)
      // possible solution is to need to extract "orderId" column out of orders and create a new table 
      // select(fName ++ lName ++ orderDate).from(customers.crossApply(select(orderId).from(orders)).where(fkCustomerId === customerId))
    },
     testM("outer apply") {
      val query = select(fName ++ lName ++ orderDate).from(customers.outerApply(select(orderDate).from(orders)).where(fkCustomerId === customerId))

      val _ = query
      ???
    } @@ ignore,
    testM("Can select from joined tables (inner join)") {
      val query = select(fName ++ lName ++ orderDate).from(customers.join(orders).on(fkCustomerId === customerId))

      case class Row(firstName: String, lastName: String, orderDate: LocalDate)

      val expected = Seq(
        Row("Ronald", "Russell", LocalDate.parse("2019-03-25")),
        Row("Ronald", "Russell", LocalDate.parse("2018-06-04")),
        Row("Alana", "Murray", LocalDate.parse("2019-08-19")),
        Row("Jose", "Wiggins", LocalDate.parse("2019-08-30")),
        Row("Jose", "Wiggins", LocalDate.parse("2019-03-07")),
        Row("Ronald", "Russell", LocalDate.parse("2020-03-19")),
        Row("Alana", "Murray", LocalDate.parse("2020-05-11")),
        Row("Alana", "Murray", LocalDate.parse("2019-02-21")),
        Row("Ronald", "Russell", LocalDate.parse("2018-05-06")),
        Row("Mila", "Paterso", LocalDate.parse("2019-02-11")),
        Row("Terrence", "Noel", LocalDate.parse("2019-10-12")),
        Row("Ronald", "Russell", LocalDate.parse("2019-01-29")),
        Row("Terrence", "Noel", LocalDate.parse("2019-02-10")),
        Row("Ronald", "Russell", LocalDate.parse("2019-09-27")),
        Row("Alana", "Murray", LocalDate.parse("2018-11-13")),
        Row("Jose", "Wiggins", LocalDate.parse("2020-01-15")),
        Row("Terrence", "Noel", LocalDate.parse("2018-07-10")),
        Row("Mila", "Paterso", LocalDate.parse("2019-08-01")),
        Row("Alana", "Murray", LocalDate.parse("2019-12-08")),
        Row("Mila", "Paterso", LocalDate.parse("2019-11-04")),
        Row("Mila", "Paterso", LocalDate.parse("2018-10-14")),
        Row("Terrence", "Noel", LocalDate.parse("2020-04-05")),
        Row("Jose", "Wiggins", LocalDate.parse("2019-01-23")),
        Row("Terrence", "Noel", LocalDate.parse("2019-05-14")),
        Row("Mila", "Paterso", LocalDate.parse("2020-04-30"))
      )

      val result = execute(
        query
          .to[String, String, LocalDate, Row] { case row =>
            Row(row._1, row._2, row._3)
          }
      )

      val assertion = for {
        r <- result.runCollect
      } yield assert(r)(hasSameElementsDistinct(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    }
    
  ) @@ sequential
}
