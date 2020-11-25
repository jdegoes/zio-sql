package zio.sql

trait InsertModule {
  self: ExprModule with TableModule with SelectModule =>

  sealed case class InsertBuilder[A](table: Table.Aux[A], filedList: SelectionSet[A]) {
    def values[Value: TypeTag](valueList: Expr[_, A, Value]): Insert[A] =
      Insert(table, filedList, valueList :: Nil)
  }

  sealed case class Insert[A](table: Table.Aux[A], intoFields: SelectionSet[A], valuesField: List[Expr[_, A, _]]) {

    def values[F: Features.IsSource, Value: TypeTag](newValue: Expr[_, A, Value]): Insert[A] =
      copy(valuesField = valuesField :+ newValue)
  }
}
