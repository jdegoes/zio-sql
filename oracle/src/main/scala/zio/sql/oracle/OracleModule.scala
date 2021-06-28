package zio.sql.oracle

import zio.sql.Jdbc

trait OracleModule extends Jdbc { self =>

  object OracleFunctionDef {
    val Sind = FunctionDef[Double, Double](FunctionName("sind"))
  }

  def buildExpr[A, B](expr: self.Expr[_, A, B], builder: StringBuilder): Unit = expr match {
    case Expr.Source(tableName, column)                                                       =>
      val _ = builder.append(tableName).append(".").append(column.name)
    case Expr.Unary(base, op)                                                                 =>
      val _ = builder.append(" ").append(op.symbol)
      buildExpr(base, builder)
    case Expr.Property(base, op)                                                              =>
      buildExpr(base, builder)
      val _ = builder.append(" ").append(op.symbol)
    case Expr.Binary(left, right, op)                                                         =>
      buildExpr(left, builder)
      builder.append(" ").append(op.symbol).append(" ")
      buildExpr(right, builder)
    case Expr.Relational(left, right, op)                                                     =>
      buildExpr(left, builder)
      builder.append(" ").append(op.symbol).append(" ")
      buildExpr(right, builder)
    case Expr.In(value, set)                                                                  =>
      buildExpr(value, builder)
      buildReadString(set, builder)
    case Expr.Literal(value)                                                                  =>
      val _ = builder.append(value.toString) //todo fix escaping
    case Expr.AggregationCall(param, aggregation)                                             =>
      builder.append(aggregation.name.name)
      builder.append("(")
      buildExpr(param, builder)
      val _ = builder.append(")")
    case Expr.ParenlessFunctionCall0(functionName)                                            =>
      val _ = builder.append(functionName.name)
    case Expr.FunctionCall0(function)                                                         =>
      builder.append(function.name.name)
      builder.append("(")
      val _ = builder.append(")")
    case Expr.FunctionCall1(param, function)                                                  =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param, builder)
      val _ = builder.append(")")
    case Expr.FunctionCall2(param1, param2, function)                                         =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param1, builder)
      builder.append(",")
      buildExpr(param2, builder)
      val _ = builder.append(")")
    case Expr.FunctionCall3(param1, param2, param3, function)                                 =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param1, builder)
      builder.append(",")
      buildExpr(param2, builder)
      builder.append(",")
      buildExpr(param3, builder)
      val _ = builder.append(")")
    case Expr.FunctionCall4(param1, param2, param3, param4, function)                         =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param1, builder)
      builder.append(",")
      buildExpr(param2, builder)
      builder.append(",")
      buildExpr(param3, builder)
      builder.append(",")
      buildExpr(param4, builder)
      val _ = builder.append(")")
    case Expr.FunctionCall5(param1, param2, param3, param4, param5, function)                 =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param1, builder)
      builder.append(",")
      buildExpr(param2, builder)
      builder.append(",")
      buildExpr(param3, builder)
      builder.append(",")
      buildExpr(param4, builder)
      builder.append(",")
      buildExpr(param5, builder)
      val _ = builder.append(")")
    case Expr.FunctionCall6(param1, param2, param3, param4, param5, param6, function)         =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param1, builder)
      builder.append(",")
      buildExpr(param2, builder)
      builder.append(",")
      buildExpr(param3, builder)
      builder.append(",")
      buildExpr(param4, builder)
      builder.append(",")
      buildExpr(param5, builder)
      builder.append(",")
      buildExpr(param6, builder)
      val _ = builder.append(")")
    case Expr.FunctionCall7(param1, param2, param3, param4, param5, param6, param7, function) =>
      builder.append(function.name.name)
      builder.append("(")
      buildExpr(param1, builder)
      builder.append(",")
      buildExpr(param2, builder)
      builder.append(",")
      buildExpr(param3, builder)
      builder.append(",")
      buildExpr(param4, builder)
      builder.append(",")
      buildExpr(param5, builder)
      builder.append(",")
      buildExpr(param6, builder)
      builder.append(",")
      buildExpr(param7, builder)
      val _ = builder.append(")")
  }

  def buildReadString(read: self.Read[_], builder: StringBuilder): Unit =
    read match {
      case Read.Mapped(read, _) => buildReadString(read, builder)

      case read0 @ Read.Select(_, _, _, _, _, _, _, _) =>
        object Dummy {
          type F
          type A
          type B <: SelectionSet[A]
        }
        val read = read0.asInstanceOf[Read.Select[Dummy.F, Dummy.A, Dummy.B]]
        import read._

        builder.append("SELECT ")
        buildSelection(selection.value, builder)
        table.foreach { t =>
          builder.append(" FROM ")
          buildTable(t, builder)
        }
        whereExpr match {
          case Expr.Literal(true) => ()
          case _                  =>
            builder.append(" WHERE ")
            buildExpr(whereExpr, builder)
        }
        groupBy match {
          case _ :: _ =>
            builder.append(" GROUP BY ")
            buildExprList(groupBy, builder)

            havingExpr match {
              case Expr.Literal(true) => ()
              case _                  =>
                builder.append(" HAVING ")
                buildExpr(havingExpr, builder)
            }
          case Nil    => ()
        }
        orderBy match {
          case _ :: _ =>
            builder.append(" ORDER BY ")
            buildOrderingList(orderBy, builder)
          case Nil    => ()
        }
        // NOTE: Limit doesn't exist in oracle 11g (>=12), for now replacing it with rownum keyword of oracle
        // Ref: https://przemyslawkruglej.com/archive/2013/11/top-n-queries-the-new-row-limiting-clause-11g-12c/
        limit match {
          case Some(limit) =>
            val _ = builder.append(" WHERE rownum <= ").append(limit)
          case None        => ()
        }
      // NOTE: Offset doesn't exist in oracle 11g (>=12)
      // offset match {
      //   case Some(offset) =>
      //     val _ = builder.append(" OFFSET ").append(offset).append(" ROWS ")
      //   case None         => ()
      // }

      case Read.Union(left, right, distinct) =>
        buildReadString(left, builder)
        builder.append(" UNION ")
        if (!distinct) builder.append("ALL ")
        buildReadString(right, builder)

      case Read.Literal(values) =>
        val _ = builder.append(" (").append(values.mkString(",")).append(") ") //todo fix needs escaping
    }

  def buildExprList(expr: List[Expr[_, _, _]], builder: StringBuilder): Unit               =
    expr match {
      case head :: tail =>
        buildExpr(head, builder)
        tail match {
          case _ :: _ =>
            builder.append(", ")
            buildExprList(tail, builder)
          case Nil    => ()
        }
      case Nil          => ()
    }
  def buildOrderingList(expr: List[Ordering[Expr[_, _, _]]], builder: StringBuilder): Unit =
    expr match {
      case head :: tail =>
        head match {
          case Ordering.Asc(value)  => buildExpr(value, builder)
          case Ordering.Desc(value) =>
            buildExpr(value, builder)
            builder.append(" DESC")
        }
        tail match {
          case _ :: _ =>
            builder.append(", ")
            buildOrderingList(tail, builder)
          case Nil    => ()
        }
      case Nil          => ()
    }

  def buildSelection[A](selectionSet: SelectionSet[A], builder: StringBuilder): Unit =
    selectionSet match {
      case cons0 @ SelectionSet.Cons(_, _) =>
        object Dummy {
          type Source
          type A
          type B <: SelectionSet[Source]
        }
        val cons = cons0.asInstanceOf[SelectionSet.Cons[Dummy.Source, Dummy.A, Dummy.B]]
        import cons._
        buildColumnSelection(head, builder)
        if (tail != SelectionSet.Empty) {
          builder.append(", ")
          buildSelection(tail, builder)
        }
      case SelectionSet.Empty              => ()
    }

  def buildColumnSelection[A, B](columnSelection: ColumnSelection[A, B], builder: StringBuilder): Unit =
    columnSelection match {
      case ColumnSelection.Constant(value, name) =>
        builder.append(value.toString()) //todo fix escaping
        name match {
          case Some(name) =>
            val _ = builder.append(" AS ").append(name)
          case None       => ()
        }
      case ColumnSelection.Computed(expr, name)  =>
        buildExpr(expr, builder)
        name match {
          case Some(name) =>
            Expr.exprName(expr) match {
              case Some(sourceName) if name != sourceName =>
                val _ = builder.append(" AS ").append(name)
              case _                                      => ()
            }
          case _          => () //todo what do we do if we don't have a name?
        }
    }
  def buildTable(table: Table, builder: StringBuilder): Unit                                           =
    table match {
      //The outer reference in this type test cannot be checked at run time?!
      case sourceTable: self.Table.Source          =>
        val _ = builder.append(sourceTable.name)
      case Table.SelectedTable(crossType, left, select, on) => {
          // CROSS and OUTER APPLY are only supported in SQL SERVER, so we rewrite them to JOINs
          //TODO write tests if thats a safe thing to do
          crossType match {
            case CrossType.CrossApply => buildTable(Table.Joined(JoinType.Inner, left, select.table.get, on), builder)
            case CrossType.OuterApply => buildTable(Table.Joined(JoinType.LeftOuter, left, select.table.get, on), builder)
          }
        }
      case Table.Joined(joinType, left, right, on) =>
        buildTable(left, builder)
        builder.append(joinType match {
          case JoinType.Inner      => " INNER JOIN "
          case JoinType.LeftOuter  => " LEFT JOIN "
          case JoinType.RightOuter => " RIGHT JOIN "
          case JoinType.FullOuter  => " OUTER JOIN "
        })
        buildTable(right, builder)
        builder.append(" ON ")
        buildExpr(on, builder)
        val _ = builder.append(" ")
    }

  override def renderRead(read: self.Read[_]): String = {
    val builder = new StringBuilder
    buildReadString(read, builder)
    builder.toString()
  }

  override def renderDelete(delete: self.Delete[_]): String = ???

  override def renderUpdate(update: self.Update[_]): String = ???
}
