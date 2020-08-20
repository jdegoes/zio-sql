package zio.sql

import zio.sql._

object Examples {
  import ShopSchema._
  import ShopSchema.AggregationDef._
  import ShopSchema.Users._
  import ShopSchema.Orders._
  import ShopSchema.OrderDetails._

  //select first_name, last_name from users
  val basicSelect = select { fName ++ lName } from users

  //select first_name as first, last_name as last from users
  val basicSelectWithAliases = select {
    (fName as "first") ++ (lName as "last")
  } from users

  //select top 2 first_name, last_name from users order by last_name, first_name desc
  val selectWithRefinements =
    select { fName ++ lName } from users orderBy (lName, fName.desc) limit 2

  //delete from users where first_name = 'Terrence'
  val basicDelete = deleteFrom(users).where(fName === "Terrence")

  /*
  val singleColumnSelect = select(userId) from users where (fName === "Fred")

  val deleteFromWithSubquery = deleteFrom(orders).where(fkUserId in {
    singleColumnSelect //todo fix issue #36
  })
  */

  //delete from users where first_name in ('Fred', 'Terrance')
  val deleteFromWithLiteral =
    deleteFrom(users) where (fName in {
      Read.lit("Fred", "Terrance") //todo make syntax nicer #52
    })

  //select first_name, last_name, order_date from users left join orders on users.usr_id = orders.usr_id
  val basicJoin = select {
    fName ++ lName ++ orderDate
  } from (users leftOuter orders).on(fkUserId === userId)

  /*
    select users.usr_id, first_name, last_name, sum(quantity * unit_price) as "total_spend"
    from users
        left join orders on users.usr_id = orders.usr_id
        left join order_details on orders.order_id = order_details.order_id
    group by users.usr_id, first_name, last_name */

  val orderValues =
    (select {
      Arbitrary(userId) ++
        Arbitrary(fName) ++
        Arbitrary(lName) ++
        (Sum(quantity * unitPrice) as "total_spend")
    }
      from {
        users
          .join(orders)
          .on(userId === fkUserId)
          .leftOuter(orderDetails)
          .on(orderId == fkOrderId)
      })
      .groupBy(userId, fName /*, lName */ ) //shouldn't compile without lName todo fix #38
}
object ShopSchema extends Sql { self =>
  import self.ColumnSet._

  object Users {
    val users = (int("usr_id") ++ localDate("dob") ++ string("first_name") ++ string("last_name")).table("users")

    val userId :*: dob :*: fName :*: lName :*: _ = users.columns
  }
  object Orders {
    val orders = (int("order_id") ++ int("usr_id") ++ localDate("order_date")).table("orders")

    val orderId :*: fkUserId :*: orderDate :*: _ = orders.columns
  }
  object Products {
    val products =
      (int("product_id") ++ string("name") ++ string("description") ++ string("image_url")).table("products")

    val productId :*: description :*: imageURL :*: _ = products.columns
  }
  object ProductPrices {
    val productPrices =
      (int("product_id") ++ offsetDateTime("effective") ++ bigDecimal("price")).table("product_prices")

    val fkProductId :*: effective :*: price :*: _ = productPrices.columns
  }

  object OrderDetails {
    val orderDetails =
      (int("order_id") ++ int("product_id") ++ double("quantity") ++ double("unit_price"))
        .table("order_details") //todo fix #3 quantity should be int, unit price should be bigDecimal, numeric operators only support double ATM.

    val fkOrderId :*: fkProductId :*: quantity :*: unitPrice :*: _ = orderDetails.columns
  }
}
