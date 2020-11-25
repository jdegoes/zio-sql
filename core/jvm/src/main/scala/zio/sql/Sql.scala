package zio.sql

trait Sql extends SelectModule with DeleteModule with UpdateModule with ExprModule with TableModule with InsertModule {
  self =>

  /*
   * (SELECT *, "foo", table.a + table.b AS sum... FROM table WHERE cond) UNION (SELECT ... FROM table)
   *   UNION ('1', '2', '3')
   *   ORDER BY table.a ASC, foo, sum DESC
   *   LIMIT 200
   *   OFFSET 100
   * UPDATE table SET ...
   * INSERT ... INTO table
   * DELETE ... FROM table
   *
   * SELECT ARBITRARY(age), COUNT(*) FROM person GROUP BY age
   */
  def select[F, A, B <: SelectionSet[A]](selection: Selection[F, A, B]): SelectBuilder[F, A, B] =
    SelectBuilder(selection)

  def deleteFrom[F[_], A, B](table: Table.Source.Aux[F, A, B]): DeleteBuilder[F, A, B] = DeleteBuilder(table)

  def update[A](table: Table.Aux[A]): UpdateBuilder[A] = UpdateBuilder(table)

  def renderRead(read: self.Read[_]): String

  def insert[A](table: Table.Aux[A], fieldList: SelectionSet[A]): InsertBuilder[A] = InsertBuilder(table, fieldList)
}
