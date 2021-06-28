package zio.sql.sqlserver

import zio.sql.Jdbc

trait DbSchema extends Jdbc { self =>
  import self.ColumnSet._

  object Customers {

    val customers =
      (uuid("id") ++ string("first_name") ++ string("last_name") ++ boolean("verified") ++ localDate("dob"))
        .table("customers")

    val customerId :*: fName :*: lName :*: verified :*: dob :*: _ =
      customers.columns

    val z = Cons(Column[Int]("1"), Cons(Column[Int]("2"), Empty)).table("whoo")
  }

  object Orders {
    val orders = (uuid("id") ++ uuid("customer_id") ++ localDate("order_date")).table("orders")

    val orderId :*: fkCustomerId :*: orderDate :*: _ = orders.columns
  }
}
