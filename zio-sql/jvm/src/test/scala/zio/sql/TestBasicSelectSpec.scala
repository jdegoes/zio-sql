package zio.sql

import zio.test.Assertion._
import zio.test._

object TestBasicSelect {
  val userSql = new Sql { self =>
    import self.ColumnSet._

    val userTable =
      (string("user_id") ++ localDate("dob") ++ string("first_name") ++ string("last_name")).table("users")

    val userId :*: dob :*: fName :*: lName :*: _ = userTable.columns

    //todo this should compile using column names defined in the table
    val basicSelect = select { fName ++ lName } from userTable

    // fName and lName already have column names, shouldn't have to do this
    val basicSelectWithAliases = (select {
      (fName as "first_name") ++ (lName as "last_name")
    } from userTable)
  }
}

object TestBasicSelectSpec extends DefaultRunnableSpec {
  import TestBasicSelect.userSql._

  def spec = suite("TestBasicSelectSpec")(
    test("Selecting columns using existing column names") {
      assert(basicSelect)(equalTo(basicSelectWithAliases))
    }
  )
}
