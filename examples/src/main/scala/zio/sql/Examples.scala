package zio.sql

import zio.sql.sqlserver.SqlServerModule

object Examples extends App with ShopSchema with SqlServerModule {
  import this.AggregationDef._
  import this.FunctionDefStandard._
  import this.OrderDetails._
  import this.Orders._
  import this.Users._

  //select first_name, last_name from users
  val basicSelect = select(fName ++ lName) from users
  println(renderRead(basicSelect))

  //select first_name as first, last_name as last from users
  val basicSelectWithAliases = select {
    (fName as "first") ++ (lName as "last")
  } from users
  println(renderRead(basicSelectWithAliases))

  //select top 2 first_name, last_name from users order by last_name, first_name desc
  val selectWithRefinements =
    (select(fName ++ lName) from users)
      .orderBy(lName, fName.desc)
      .limit(2)
  println(renderRead(selectWithRefinements))

  case class Person(fname: String, lname: String)

  // execute(selectWithRefinements).to(Person)
  // execute(selectWithRefinements).to((_, _))

  //delete from users where first_name = 'Terrence'
  //val basicDelete = deleteFrom(users).where(fName === "Terrence")
  //println(renderDelete(basicDelete))

  /*
    val deleteFromWithSubquery = deleteFrom(orders).where(fkUserId in {
      select(userId as "id") from users where (fName === "Fred") //todo fix issue #36
    }) */

  //select first_name, last_name, order_date from users left join orders on users.usr_id = orders.usr_id
  val basicJoin = select {
    fName ++ lName ++ orderDate
  } from (users leftOuter orders).on(fkUserId === userId)
  println(renderRead(basicJoin))
  /*
    select users.usr_id, first_name, last_name, sum(quantity * unit_price) as "total_spend"
    from users
        left join orders on users.usr_id = orders.usr_id
        left join order_details on orders.order_id = order_details.order_id
    group by users.usr_id, first_name, last_name */

  val orderValues =
    (select {
      (Arbitrary(userId)) ++
        (Arbitrary(fName)) ++
        (Arbitrary(lName)) ++
        (Sum(quantity * unitPrice) as "total_spend") ++
        Sum(Abs(quantity))
    }
      from {
        users
          .join(orders)
          .on(userId === fkUserId)
          .leftOuter(orderDetails)
          .on(orderId === fkOrderId)
      })
      .groupBy(userId, fName /*, lName */ ) //shouldn't compile without lName todo fix #38
  println(renderRead(orderValues))

  import scala.language.postfixOps

  /*
   * select users.first_name, users.last_name from users where true and users.first_name is not null
   */
  val withPropertyOp = select(fName ++ lName) from users where (fName isNotNull)
  println(renderRead(withPropertyOp))
}
