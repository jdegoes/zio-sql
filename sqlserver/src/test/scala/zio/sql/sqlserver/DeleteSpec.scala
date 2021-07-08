package zio.sql.sqlserver

import zio.Cause
import zio.test.Assertion._
import zio.test._

object DeleteSpec extends SqlServerRunnableSpec with ShopSchema {

  import Customers._

  override def specLayered = suite("SqlServer module delete")(
    testM("Can delete from single table with a condition") {
      val query = deleteFrom(customers).where(verified.isNotTrue)

      val result = execute(query)

      val assertion = for {
        r <- result
      } yield assert(r)(equalTo(1))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    }
  )
}
