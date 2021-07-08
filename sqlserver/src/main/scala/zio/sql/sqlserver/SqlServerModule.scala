package zio.sql.sqlserver

import scala.annotation.tailrec

import zio.sql.Jdbc

trait SqlServerModule extends Jdbc { self =>

  override def renderDelete(delete: self.Delete[_]): String = {
    implicit val builder: StringBuilder = new StringBuilder

    builder.append("delete from ")
    buildTable(delete.table)
    delete.whereExpr match {
      case Expr.Literal(true) => ()
      case _                  =>
        builder.append(" where ")
        buildExpr(delete.whereExpr)
    }

    builder.toString()
  }


  override def renderUpdate(update: self.Update[_]): String = ???

  override def renderRead(read: self.Read[_]): String = {
    implicit val builder: StringBuilder = new StringBuilder

    buildReadString(read)
    builder.toString()
  }

  private def buildExpr[A, B](expr: self.Expr[_, A, B])(implicit builder: StringBuilder): Unit = expr match {
    case Expr.Source(tableName, column)                                                       =>
      val _ = builder.append(tableName).append(".").append(column.name)
    case Expr.Unary(base, op)                                                                 =>
      val _ = builder.append(" ").append(op.symbol)
      buildExpr(base)
      // TODO remove when bit typetag will be added
    case Expr.Property(base, op) if op == PropertyOp.IsNotTrue                                =>
      buildExpr(base)
      val _ = builder.append(" ").append("= 0")
    case Expr.Property(base, op) if op == PropertyOp.IsTrue                                   =>
      buildExpr(base)
      val _ = builder.append(" ").append("= 1")
    case Expr.Property(base, op)                                                              =>
      buildExpr(base)
      val _ = builder.append(" ").append(op.symbol)
    case Expr.Binary(left, right, op)                                                         =>
      buildExpr(left)
      builder.append(" ").append(op.symbol).append(" ")
      buildExpr(right)
    case Expr.Relational(left, right, op)                                                     =>
      buildExpr(left)
      builder.append(" ").append(op.symbol).append(" ")
      buildExpr(right)
    case Expr.In(value, set)                                                                  =>
      buildExpr(value)
      buildReadString(set)
    case Expr.Literal(value)                                                                  =>
      val _ = builder.append(value.toString) //todo fix escaping
    case Expr.AggregationCall(param, aggregation)                                             =>
      builder.append(aggregation.name.name)
      builder.append("(")
      buildExpr(param)
      val _ = builder.append(")")
    case Expr.ParenlessFunctionCall0(function)                                                =>
      val _ = builder.append(function.name)
    case Expr.FunctionCall0(function)                                                         =>
      builder.append(function.name.name)
      builder.append("(")
      val _ = builder.append(")")
    case Expr.FunctionCall1(param, function)                                                  =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param)
      val _ = builder.append(")")
    case Expr.FunctionCall2(param1, param2, function)                                         =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param1)
      builder.append(",")
      buildExpr(param2)
      val _ = builder.append(")")
    case Expr.FunctionCall3(param1, param2, param3, function)                                 =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param1)
      builder.append(",")
      buildExpr(param2)
      builder.append(",")
      buildExpr(param3)
      val _ = builder.append(")")
    case Expr.FunctionCall4(param1, param2, param3, param4, function)                         =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param1)
      builder.append(",")
      buildExpr(param2)
      builder.append(",")
      buildExpr(param3)
      builder.append(",")
      buildExpr(param4)
      val _ = builder.append(")")
    case Expr.FunctionCall5(param1, param2, param3, param4, param5, function)                 =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param1)
      builder.append(",")
      buildExpr(param2)
      builder.append(",")
      buildExpr(param3)
      builder.append(",")
      buildExpr(param4)
      builder.append(",")
      buildExpr(param5)
      val _ = builder.append(")")
    case Expr.FunctionCall6(param1, param2, param3, param4, param5, param6, function)         =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param1)
      builder.append(",")
      buildExpr(param2)
      builder.append(",")
      buildExpr(param3)
      builder.append(",")
      buildExpr(param4)
      builder.append(",")
      buildExpr(param5)
      builder.append(",")
      buildExpr(param6)
      val _ = builder.append(")")
    case Expr.FunctionCall7(param1, param2, param3, param4, param5, param6, param7, function) =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param1)
      builder.append(",")
      buildExpr(param2)
      builder.append(",")
      buildExpr(param3)
      builder.append(",")
      buildExpr(param4)
      builder.append(",")
      buildExpr(param5)
      builder.append(",")
      buildExpr(param6)
      builder.append(",")
      buildExpr(param7)
      val _ = builder.append(")")
  }

  def buildReadString(read: self.Read[_])(implicit builder: StringBuilder): Unit = read match {
    case Read.Mapped(read, _) => buildReadString(read)

    //todo offset (needs orderBy, must use fetch _instead_ of top)
    case read0 @ Read.Select(_, _, _, _, _, _, _, _) =>
      object Dummy {
        type F
        type A
        type B <: SelectionSet[A]
      }
      val read = read0.asInstanceOf[Read.Select[Dummy.F, Dummy.A, Dummy.B]]
      import read._

      builder.append("select ")
      limit match {
        case Some(limit) =>
          builder.append("top ").append(limit).append(" ")
        case None        => ()
      }
      buildSelection(selection.value)
      table.foreach { t =>
        builder.append(" from ")
        buildTable(t)
      }
      whereExpr match {
        case Expr.Literal(true) => ()
        case _                  =>
          builder.append(" where ")
          buildExpr(whereExpr)
      }
      groupBy match {
        case _ :: _ =>
          builder.append(" group by ")
          buildExprList(groupBy)

          havingExpr match {
            case Expr.Literal(true) => ()
            case _                  =>
              builder.append(" having ")
              buildExpr(havingExpr)
          }
        case Nil    => ()
      }
      orderBy match {
        case _ :: _ =>
          builder.append(" order by ")
          buildOrderingList(orderBy)
        case Nil    => ()
      }

    case Read.Union(left, right, distinct) =>
      buildReadString(left)
      builder.append(" union ")
      if (!distinct) builder.append("all ")
      buildReadString(right)

    case Read.Literal(values) =>
      val _ = builder.append(" (").append(values.mkString(",")).append(") ") //todo fix needs escaping
  }

  @tailrec
  private def buildExprList(expr: List[Expr[_, _, _]])(implicit builder: StringBuilder): Unit = expr match {
    case head :: tail =>
      buildExpr(head)
      tail match {
        case _ :: _ =>
          builder.append(", ")
          buildExprList(tail)
        case Nil    => ()
      }
    case Nil          => ()
  }

  @tailrec
  private def buildOrderingList(expr: List[Ordering[Expr[_, _, _]]])(implicit builder: StringBuilder): Unit = expr match {
    case head :: tail =>
      head match {
        case Ordering.Asc(value)  => buildExpr(value)
        case Ordering.Desc(value) =>
          buildExpr(value)
          builder.append(" desc")
      }
      tail match {
        case _ :: _ =>
          builder.append(", ")
          buildOrderingList(tail)
        case Nil    => ()
      }
    case Nil          => ()
  }

  @tailrec
  private def buildSelection(selectionSet: SelectionSet[_])(implicit builder: StringBuilder): Unit = selectionSet match {
    case cons0 @ SelectionSet.Cons(_, _) =>
      object Dummy {
        type Source
        type A
        type B <: SelectionSet[Source]
      }
      val cons = cons0.asInstanceOf[SelectionSet.Cons[Dummy.Source, Dummy.A, Dummy.B]]
      import cons._
      buildColumnSelection(head)
      if (tail != SelectionSet.Empty) {
        builder.append(", ")
        buildSelection(tail)
      }
    case SelectionSet.Empty              => ()
  }

  private def buildColumnSelection[A, B](
    columnSelection: ColumnSelection[A, B]
  )(implicit builder: StringBuilder): Unit = columnSelection match {
    case ColumnSelection.Constant(value, name) =>
      builder.append(value.toString) //todo fix escaping
      name match {
        case Some(name) =>
          val _ = builder.append(" as ").append(name)
        case None       => ()
      }
    case ColumnSelection.Computed(expr, name)  =>
      buildExpr(expr)
      name match {
        case Some(name) =>
          Expr.exprName(expr) match {
            case Some(sourceName) if name != sourceName =>
              val _ = builder.append(" as ").append(name)
            case _                                      => ()
          }
        case _          => () //todo what do we do if we don't have a name?
      }
  }

  private def buildTable(table: Table)(implicit builder: StringBuilder): Unit = table match {
    //The outer reference in this type test cannot be checked at run time?!
    case sourceTable: self.Table.Source          =>
      val _ = builder.append(sourceTable.name)
    case Table.Joined(joinType, left, right, on) =>
      buildTable(left)
      builder.append(joinType match {
        case JoinType.Inner      => " inner join "
        case JoinType.LeftOuter  => " left join "
        case JoinType.RightOuter => " right join "
        case JoinType.FullOuter  => " outer join "
      })
      buildTable(right)
      builder.append(" on ")
      buildExpr(on)
      val _ = builder.append(" ")
  }
}
