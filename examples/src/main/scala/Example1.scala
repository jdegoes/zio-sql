import zio.sql.Sql

object Example1 extends Sql {
  import ColumnSet._

  def renderRead(read: Example1.Read[_]): String = ???

  val columnSet = int("age") ++ string("name")

  val table = columnSet.table("person")

  val table2 = columnSet.table("person2")

  val age :*: name :*: _ = table.columns

  val age2 :*: name2 :*: _ = table2.columns

  import FunctionDefStandard._
  import AggregationDef._

  val queried =
    (select {
      ((age + 2) as "age") ++ (name as "name") ++ (Abs(3.0) as "dummy")
    } from table)
      .limit(200)
      .offset(1000)
      .orderBy(age.descending)

  val joined =
    select {
      (age as "age") ++ (age2 as "age2")
    } from (table join table2).on(name === name2)

  val aggregated =
    (select {
      (Arbitrary(age) as "age") ++ (Count(1) as "count")
    } from table) groupBy age

  //val deleted = deleteFrom(table).where(age === 3)

  val updated =
    update(table)
      .set(age, age + 2)
      .set(name, "foo")
      .where(age > 100)
}
