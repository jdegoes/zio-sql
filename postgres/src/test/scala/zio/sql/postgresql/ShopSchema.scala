package zio.sql.postgresql

import zio.sql.Jdbc

trait ShopSchema extends Jdbc { self =>
  import self.ColumnSet._

  object Customers     {
    //https://github.com/zio/zio-sql/issues/320 Once Insert is supported, we can remove created_timestamp_string
    val customers =
      (uuid("id") ++ localDate("dob") ++ string("first_name") ++ string("last_name") ++ boolean(
        "verified"
      ) ++ string("created_timestamp_string") ++ zonedDateTime("created_timestamp"))
        .table("customers")

    val customerId :*: dob :*: fName :*: lName :*: verified :*: createdString :*: createdTimestamp :*: _ =
      customers.columns

      val test = customerId ++ fName ++ lName ++ dob
  }
  object Orders        {
    val orders = (uuid("id") ++ uuid("customer_id") ++ localDate("order_date")).table("orders")

    val orderId :*: fkCustomerId :*: orderDate :*: _ = orders.columns
  }
  object Products      {
    val products =
      (uuid("id") ++ string("name") ++ string("description") ++ string("image_url")).table("products")

    val productId :*: description :*: imageURL :*: _ = products.columns
  }
  object ProductPrices {
    val productPrices =
      (uuid("product_id") ++ offsetDateTime("effective") ++ bigDecimal("price")).table("product_prices")

    val fkProductId :*: effective :*: price :*: _ = productPrices.columns
  }

  object OrderDetails {
    val orderDetails =
      (uuid("order_id") ++ uuid("product_id") ++ int("quantity") ++ bigDecimal("unit_price"))
        .table(
          "order_details"
        )

    val fkOrderId :*: fkProductId :*: quantity :*: unitPrice :*: _ = orderDetails.columns
  }
}
