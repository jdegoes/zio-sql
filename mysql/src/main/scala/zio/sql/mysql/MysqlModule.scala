package zio.sql.mysql

import zio.sql.{ Jdbc, Renderer }
import zio.Chunk
import java.time.Year
import java.sql.ResultSet

trait MysqlModule extends Jdbc { self =>

  override type TypeTagExtension[+A] = MysqlSpecific.MysqlTypeTag[A]

  object MysqlSpecific {
    trait MysqlTypeTag[+A] extends Tag[A] with Decodable[A]

    object MysqlTypeTag {
      implicit case object TYear extends MysqlTypeTag[Year] {
        override def decode(column: Either[Int, String], resultSet: ResultSet): Either[DecodingError, Year] =
          scala.util
            .Try(Year.of(column.fold(resultSet.getByte(_), resultSet.getByte(_)).toInt))
            .fold(
              _ => Left(DecodingError.UnexpectedNull(column)),
              r => Right(r)
            )
      }

    }
  }

  object MysqlFunctionDef {
    val Crc32   = FunctionDef[String, Long](FunctionName("crc32"))
    val Degrees = FunctionDef[Double, Double](FunctionName("degrees"))
    val Log2    = FunctionDef[Double, Double](FunctionName("log2"))
    val Log10   = FunctionDef[Double, Double](FunctionName("log10"))
    val Pi      = Expr.FunctionCall0[Double](FunctionDef[Any, Double](FunctionName("pi")))
  }

  override def renderRead(read: self.Read[_]): String = {
    implicit val render: Renderer = Renderer()
    MysqlRenderModule.renderReadImpl(read)
    render.toString
  }

  override def renderDelete(delete: self.Delete[_]): String = {
    implicit val render: Renderer = Renderer()
    MysqlRenderModule.renderDeleteImpl(delete)
    render.toString
  }

  override def renderUpdate(update: self.Update[_]): String = {
    implicit val render: Renderer = Renderer()
    MysqlRenderModule.renderUpdateImpl(update)
    render.toString
  }

  object MysqlRenderModule {
    def renderDeleteImpl(delete: Delete[_])(implicit render: Renderer) = {
      render("DELETE FROM ")
      renderTable(delete.table)
      delete.whereExpr match {
        case Expr.Literal(true) => ()
        case _                  =>
          render(" WHERE ")
          renderExpr(delete.whereExpr)
      }
    }

    def renderUpdateImpl(update: Update[_])(implicit render: Renderer) =
      update match {
        case Update(table, set, whereExpr) =>
          render("UPDATE ")
          renderTable(table)
          render(" SET ")
          renderSet(set)
          render(" WHERE ")
          renderExpr(whereExpr)
      }

    def renderReadImpl(read: self.Read[_])(implicit render: Renderer): Unit =
      read match {
        case Read.Mapped(read, _)                        =>
          renderReadImpl(read)
        case read0 @ Read.Select(_, _, _, _, _, _, _, _) =>
          object Dummy { type F; type A; type B <: SelectionSet[A] }
          val read = read0.asInstanceOf[Read.Select[Dummy.F, Dummy.A, Dummy.B]]
          import read._

          render("SELECT ")
          renderSelection(selection.value)
          table.foreach { t =>
            render(" FROM ")
            renderTable(t)
          }
          whereExpr match {
            case Expr.Literal(true) => ()
            case _                  =>
              render(" WHERE ")
              renderExpr(whereExpr)
          }
          groupBy match {
            case _ :: _ =>
              render(" GROUP BY ")
              renderExprList(groupBy)

              havingExpr match {
                case Expr.Literal(true) => ()
                case _                  =>
                  render(" HAVING ")
                  renderExpr(havingExpr)
              }
            case Nil    => ()
          }
          orderBy match {
            case _ :: _ =>
              render(" ORDER BY ")
              renderOrderingList(orderBy)
            case Nil    => ()
          }
          limit match {
            case Some(limit) => render(" LIMIT ", limit)
            case None        => ()
          }
          offset match {
            case Some(offset) => render(" OFFSET ", offset)
            case None         => ()
          }

        case Read.Union(left, right, distinct) =>
          renderReadImpl(left)
          render(" UNION ")
          if (!distinct) render("ALL ")
          renderReadImpl(right)

        case Read.Literal(values) =>
          render(" (", values.mkString(","), ") ") //todo fix needs escaping
      }

    private def renderSet(set: List[Set[_, _]])(implicit render: Renderer): Unit =
      set match {
        case head :: tail =>
          renderExpr(head.lhs)
          render(" = ")
          renderExpr(head.rhs)
          tail.foreach { setEq =>
            render(", ")
            renderExpr(setEq.lhs)
            render(" = ")
            renderExpr(setEq.rhs)
          }
        case Nil          => //TODO restrict Update to not allow empty set
      }

    private def renderTable(table: Table)(implicit render: Renderer): Unit =
      table match {
        //The outer reference in this type test cannot be checked at run time?!
        case sourceTable: self.Table.Source          =>
          render(sourceTable.name)
        case Table.SelectedTable(crossType, left, select, on) => {
          // CROSS and OUTER APPLY are only supported in SQL SERVER, so we rewrite them to JOINs
          //TODO write tests if thats a safe thing to do
          crossType match {
            case CrossType.CrossApply => renderTable(Table.Joined(JoinType.Inner, left, select.table.get, on))
            case CrossType.OuterApply => renderTable(Table.Joined(JoinType.LeftOuter, left, select.table.get, on))
          }
        }
        case Table.Joined(joinType, left, right, on) =>
          renderTable(left)
          render(joinType match {
            case JoinType.Inner      => " INNER JOIN "
            case JoinType.LeftOuter  => " LEFT JOIN "
            case JoinType.RightOuter => " RIGHT JOIN "
            case JoinType.FullOuter  => " OUTER JOIN "
          })
          renderTable(right)
          render(" ON ")
          renderExpr(on)
          render(" ")
      }

    private def renderExpr[A, B](expr: self.Expr[_, A, B])(implicit render: Renderer): Unit = expr match {
      case Expr.Source(tableName, column)                                               => render(tableName, ".", column.name)
      case Expr.Unary(base, op)                                                         =>
        render(" ", op.symbol)
        renderExpr(base)
      case Expr.Property(base, op)                                                      =>
        renderExpr(base)
        render(" ", op.symbol)
      case Expr.Binary(left, right, op)                                                 =>
        renderExpr(left)
        render(" ", op.symbol, " ")
        renderExpr(right)
      case Expr.Relational(left, right, op)                                             =>
        renderExpr(left)
        render(" ", op.symbol, " ")
        renderExpr(right)
      case Expr.In(value, set)                                                          =>
        renderExpr(value)
        renderReadImpl(set)
      case lit: Expr.Literal[_]                                                         => renderLit(lit)
      case Expr.AggregationCall(p, aggregation)                                         =>
        render(aggregation.name.name, "(")
        renderExpr(p)
        render(")")
      case Expr.ParenlessFunctionCall0(fn)                                              =>
        val _ = render(fn.name)
      case Expr.FunctionCall0(fn)                                                       =>
        render(fn.name.name)
        render("(")
        val _ = render(")")
      case Expr.FunctionCall1(p, fn)                                                    =>
        render(fn.name.name, "(")
        renderExpr(p)
        render(")")
      case Expr.FunctionCall2(p1, p2, fn)                                               =>
        render(fn.name.name, "(")
        renderExpr(p1)
        render(",")
        renderExpr(p2)
        render(")")
      case Expr.FunctionCall3(p1, p2, p3, fn)                                           =>
        render(fn.name.name, "(")
        renderExpr(p1)
        render(",")
        renderExpr(p2)
        render(",")
        renderExpr(p3)
        render(")")
      case Expr.FunctionCall4(p1, p2, p3, p4, fn)                                       =>
        render(fn.name.name, "(")
        renderExpr(p1)
        render(",")
        renderExpr(p2)
        render(",")
        renderExpr(p3)
        render(",")
        renderExpr(p4)
        render(")")
      case Expr.FunctionCall5(param1, param2, param3, param4, param5, function)         =>
        render(function.name.name)
        render("(")
        renderExpr(param1)
        render(",")
        renderExpr(param2)
        render(",")
        renderExpr(param3)
        render(",")
        renderExpr(param4)
        render(",")
        renderExpr(param5)
        render(")")
      case Expr.FunctionCall6(param1, param2, param3, param4, param5, param6, function) =>
        render(function.name.name)
        render("(")
        renderExpr(param1)
        render(",")
        renderExpr(param2)
        render(",")
        renderExpr(param3)
        render(",")
        renderExpr(param4)
        render(",")
        renderExpr(param5)
        render(",")
        renderExpr(param6)
        render(")")
      case Expr.FunctionCall7(
            param1,
            param2,
            param3,
            param4,
            param5,
            param6,
            param7,
            function
          ) =>
        render(function.name.name)
        render("(")
        renderExpr(param1)
        render(",")
        renderExpr(param2)
        render(",")
        renderExpr(param3)
        render(",")
        renderExpr(param4)
        render(",")
        renderExpr(param5)
        render(",")
        renderExpr(param6)
        render(",")
        renderExpr(param7)
        render(")")
    }

    private def renderLit[A, B](lit: self.Expr.Literal[_])(implicit render: Renderer): Unit = {
      import MysqlSpecific.MysqlTypeTag._
      import TypeTag._
      lit.typeTag match {
        case TDialectSpecific(tt) =>
          tt match {
            case tt @ TYear                       =>
              render(tt.cast(lit.value))
            case _: MysqlSpecific.MysqlTypeTag[_] => ???
          }
        case TByteArray           =>
          render(
            lit.value.asInstanceOf[Chunk[Byte]].map("""\%02X""" format _).mkString("x'", "", "'")
          ) // todo fix `cast` infers correctly but map doesn't work for some reason
        case tt @ TChar           =>
          render("'", tt.cast(lit.value), "'") //todo is this the same as a string? fix escaping
        case tt @ TInstant        =>
          render("TIMESTAMP '", tt.cast(lit.value), "'")
        case tt @ TLocalDate      =>
          render("DATE '", tt.cast(lit.value), "'")
        case tt @ TLocalDateTime  =>
          render("DATE '", tt.cast(lit.value), "'")
        case tt @ TLocalTime      =>
          render("TIME '", tt.cast(lit.value), "'")
        case tt @ TOffsetDateTime =>
          render("DATE '", tt.cast(lit.value), "'")
        case tt @ TOffsetTime     =>
          render("TIME '", tt.cast(lit.value), "'")
        case tt @ TUUID           =>
          render("'", tt.cast(lit.value), "'")
        case tt @ TZonedDateTime  =>
          render("DATE '", tt.cast(lit.value), "'")
        case TByte                =>
          render(lit.value)
        case TBigDecimal          =>
          render(lit.value)
        case TBoolean             =>
          render(lit.value)
        case TDouble              =>
          render(lit.value)
        case TFloat               =>
          render(lit.value)
        case TInt                 =>
          render(lit.value)
        case TLong                =>
          render(lit.value)
        case TShort               =>
          render(lit.value)
        case TString              =>
          render("'", lit.value, "'") //todo fix escaping
        case _                    =>
          render(lit.value) //todo fix add TypeTag.Nullable[_] =>
      }
    }

    private def renderSelection[A](selectionSet: SelectionSet[A])(implicit render: Renderer): Unit =
      selectionSet match {
        case cons0 @ SelectionSet.Cons(_, _) =>
          object Dummy {
            type Source
            type A
            type B <: SelectionSet[Source]
          }
          val cons = cons0.asInstanceOf[SelectionSet.Cons[Dummy.Source, Dummy.A, Dummy.B]]
          import cons._
          renderColumnSelection(head)
          if (tail != SelectionSet.Empty) {
            render(", ")
            renderSelection(tail)
          }
        case SelectionSet.Empty              => ()
      }

    private def renderColumnSelection[A, B](columnSelection: ColumnSelection[A, B])(implicit render: Renderer): Unit =
      columnSelection match {
        case ColumnSelection.Constant(value, name) =>
          render(value) //todo fix escaping
          name match {
            case Some(name) => render(" AS ", name)
            case None       => ()
          }
        case ColumnSelection.Computed(expr, name)  =>
          renderExpr(expr)
          name match {
            case Some(name) =>
              Expr.exprName(expr) match {
                case Some(sourceName) if name != sourceName => render(" AS ", name)
                case _                                      => ()
              }
            case _          => () //todo what do we do if we don't have a name?
          }
      }

    private def renderExprList(expr: List[Expr[_, _, _]])(implicit render: Renderer): Unit =
      expr match {
        case head :: tail =>
          renderExpr(head)
          tail match {
            case _ :: _ =>
              render(", ")
              renderExprList(tail)
            case Nil    => ()
          }
        case Nil          => ()
      }

    def renderOrderingList(expr: List[Ordering[Expr[_, _, _]]])(implicit render: Renderer): Unit =
      expr match {
        case head :: tail =>
          head match {
            case Ordering.Asc(value)  =>
              renderExpr(value)
            case Ordering.Desc(value) =>
              renderExpr(value)
              render(" DESC")
          }
          tail match {
            case _ :: _ =>
              render(", ")
              renderOrderingList(tail)
            case Nil    => ()
          }
        case Nil          => ()
      }
  }

}
