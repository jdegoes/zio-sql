package zio.sql.sqlserver
import zio.Cause
import zio.test.Assertion.equalTo
import zio.test.assert

object ReadSpec extends SqlServerRunnableSpec with ShopSchema {

  import Customers._

  override def specLayered = suite("SqlServer module read")(
    suite("Can handle bit type properly")(
      testM("handle isTrue to 1 bit type"){

      import AggregationDef._

      val query =
        select(Count(customerId))
          .from(customers)
          .where(verified.isTrue)

      val result = execute(query)

      val assertion = for {
        r  <- result
      } yield assert(r._1)(equalTo(4L))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced)).runHead.map(_.get)
    },
      testM("handle isNotTrue to 0 bit type"){
        import AggregationDef._

        val query =
          select(Count(customerId))
            .from(customers)
            .where(verified.isNotTrue)

        val result = execute(query)

        val assertion = for {
          r <- result
        } yield assert(r._1)(equalTo(1L))

        assertion.mapErrorCause(cause => Cause.stackless(cause.untraced)).runHead.map(_.get)
      }
    )
  )
}
