package zio.sql.postgresql

import zio.sql.rendering.{ Builder, LowPriorityRenderingImplicits, Rendering }

trait PostgressRenderModule { self: PostgresModule =>
  trait PostgresRenderer[-A] extends LowPriorityRenderingImplicits with Rendering[A] {}
  //todo split out to separate module
  object PostgresRenderer {
    implicit case object DeleteRenderer extends PostgresRenderer[Delete[_]] {
      override def apply(delete: Delete[_])(implicit builder: Builder): Unit = {
        render("DELETE FROM ", delete.table)
        delete.whereExpr match {
          case Expr.Literal(true) => ()
          case _                  => render(" WHERE ", delete.whereExpr)
        }
      }
    }

    implicit case object UpdateRenderer extends PostgresRenderer[Update[_]] {
      override def apply(update: Update[_])(implicit builder: Builder): Unit = update match {
        case Update(table, set, whereExpr) =>
          render("UPDATE ", table, " SET ", set, " WHERE ", whereExpr)
      }
    }

    implicit case object ExprRenderer   extends PostgresRenderer[Expr.Literal[_]]    {
      override def apply(lit: Expr.Literal[_])(implicit builder: Builder): Unit = {
        import TypeTag._
        lit.typeTag match {
          case tt @ TByteArray      => render(tt.cast(lit.value))                     // todo still broken
          //something like? render(tt.cast(lit.value).map("\\\\%03o" format _).mkString("E\'", "", "\'"))
          case tt @ TChar           =>
            render("'", tt.cast(lit.value), "'") //todo is this the same as a string? fix escaping
          case tt @ TInstant        => render("TIMESTAMP '", tt.cast(lit.value), "'") //todo test
          case tt @ TLocalDate      => render(tt.cast(lit.value))                     // todo still broken
          case tt @ TLocalDateTime  => render(tt.cast(lit.value))                     // todo still broken
          case tt @ TLocalTime      => render(tt.cast(lit.value))                     // todo still broken
          case tt @ TOffsetDateTime => render(tt.cast(lit.value))                     // todo still broken
          case tt @ TOffsetTime     => render(tt.cast(lit.value))                     // todo still broken
          case tt @ TUUID           => render(tt.cast(lit.value))                     // todo still broken
          case tt @ TZonedDateTime  => render(tt.cast(lit.value))                     // todo still broken

          case TByte       => render(lit.value)           //default toString is probably ok
          case TBigDecimal => render(lit.value)           //default toString is probably ok
          case TBoolean    => render(lit.value)           //default toString is probably ok
          case TDouble     => render(lit.value)           //default toString is probably ok
          case TFloat      => render(lit.value)           //default toString is probably ok
          case TInt        => render(lit.value)           //default toString is probably ok
          case TLong       => render(lit.value)           //default toString is probably ok
          case TShort      => render(lit.value)           //default toString is probably ok
          case TString     => render("'", lit.value, "'") //todo fix escaping

          case _ => render(lit.value) //todo fix add TypeTag.Nullable[_] =>
        }
      }
    }
    implicit case object UpdateRenderer extends PostgresRenderer[self.Expr[_, _, _]] {
      override def apply(expr: Expr[_, _, _])(implicit builder: Builder): Unit = expr match {
        case Expr.Source(tableName, column)         => render(tableName, ".", column.name)
        case Expr.Unary(base, op)                   => render(" ", op.symbol, base)
        case Expr.Property(base, op)                => render(base, " ", op.symbol)
        case Expr.Binary(left, right, op)           => render(left, " ", op.symbol, " ", right)
        case Expr.Relational(left, right, op)       => render(left, " ", op.symbol, " ", right)
        case Expr.In(value, set)                    => render(value, set)
        case lit: Expr.Literal[_]                   => render(lit)
        case Expr.AggregationCall(p, aggregation)   => render(aggregation.name.name, "(", p, ")")
        case Expr.ParenlessFunctionCall0(fn)        => render(fn.name)
        case Expr.FunctionCall0(fn)                 => render(fn.name.name, "()")
        case Expr.FunctionCall1(p, fn)              => render(fn.name.name, "(", p, ")")
        case Expr.FunctionCall2(p1, p2, fn)         => render(fn.name.name, "(", p1, ",", p2, ")")
        case Expr.FunctionCall3(p1, p2, p3, fn)     => render(fn.name.name, "(", p1, ",", p2, ",", p3, ")")
        case Expr.FunctionCall4(p1, p2, p3, p4, fn) => render(fn.name.name, "(", p1, ",", p2, ",", p3, ",", p4, ")")
      }
    }

    implicit case object UpdateRenderer extends PostgresRenderer[Nothing] {}
    implicit case object UpdateRenderer extends PostgresRenderer[Nothing] {}
    implicit case object UpdateRenderer extends PostgresRenderer[Nothing] {}
    implicit case object UpdateRenderer extends PostgresRenderer[Nothing] {}
    implicit case object UpdateRenderer extends PostgresRenderer[Nothing] {}
  }
}
