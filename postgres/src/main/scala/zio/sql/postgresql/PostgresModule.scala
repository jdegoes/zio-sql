package zio.sql.postgresql

import java.time._
import zio.sql.Jdbc
import zio.sql.rendering.{ Builder, RenderModule, Rendering }

import scala.language.implicitConversions

/**
 */
trait PostgresModule extends Jdbc with PostgressRenderModule { self =>

  override type SqlRenderer[-A] = PostgresRenderer[A]

  def renderSet[A <: SelectionSet[_]](set: List[Set[_, A]])(implicit render: Builder): Unit =
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

  private[zio] def renderReadImpl[A <: SelectionSet[_]](read: self.Read[_])(implicit render: Builder): Unit =
    read match {
      case read0 @ Read.Select(_, _, _, _, _, _, _, _) =>
        object Dummy { type F; type A; type B <: SelectionSet[A] }
        val read = read0.asInstanceOf[Read.Select[Dummy.F, Dummy.A, Dummy.B]]
        import read._

        render("SELECT ")
        renderSelection(selection.value)
        render(" FROM ")
        renderTable(table)
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

  def renderExprList(expr: List[Expr[_, _, _]])(implicit render: Builder): Unit =
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

  def renderOrderingList(expr: List[Ordering[Expr[_, _, _]]])(implicit render: Builder): Unit =
    expr match {
      case head :: tail =>
        head match {
          case Ordering.Asc(value)  => renderExpr(value)
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

  def renderSelection[A](selectionSet: SelectionSet[A])(implicit render: Builder): Unit =
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

  def renderColumnSelection[A, B](columnSelection: ColumnSelection[A, B])(implicit render: Builder): Unit =
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

  def renderTable(table: Table)(implicit render: Builder): Unit =
    table match {
      //The outer reference in this type test cannot be checked at run time?!
      case sourceTable: self.Table.Source          => render(sourceTable.name)
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

  override type ExprExtensionType[F, -A, B] = PostgresExprExtension[F, A, B]

  sealed trait PostgresExprExtension[F, -A, B]
  object PostgresExprExtension {
    implicit def postgresExpr2Expr[F, A, B: TypeTag](pgExpr: PostgresExprExtension[F, A, B]) =
      Expr.ExprDialectSpecific(pgExpr)

    implicit def postgresExpr2Selection[F, A, B: TypeTag](
      pgExpr: PostgresExprExtension[F, A, B]
    ): Selection[F, A, SelectionSet.Cons[A, B, SelectionSet.Empty]] =
      Selection.computedOption(pgExpr, Expr.exprName(pgExpr))

    sealed case class Overlay[F1, F2, F3, F4, Source, String](
      main: Expr[F1, Source, String],
      replacing: Expr[F2, Source, String],
      starting: Expr[F3, Source, Int],
      number: Expr[F4, Source, Int]
    ) extends PostgresExprExtension[F1 :||: F2 :||: F3 :||: F4, Source, String] {
      val functionName = "overlay"
    }
  }
  object PostgresFunctionDef   {
    val IsFinite                    = FunctionDef[Instant, Boolean](FunctionName("isfinite"))
    val TimeOfDay                   = FunctionDef[Any, String](FunctionName("timeofday"))
    val CurrentTime                 = Expr.ParenlessFunctionCall0[OffsetTime](FunctionName("current_time"))
    val CharLength                  = FunctionDef[String, Int](FunctionName("character_length"))
    val Localtime                   = Expr.ParenlessFunctionCall0[LocalTime](FunctionName("localtime"))
    val LocaltimeWithPrecision      = FunctionDef[Int, LocalTime](FunctionName("localtime"))
    val Localtimestamp              = Expr.ParenlessFunctionCall0[Instant](FunctionName("localtimestamp"))
    val LocaltimestampWithPrecision = FunctionDef[Int, Instant](FunctionName("localtimestamp"))
    val Md5                         = FunctionDef[String, String](FunctionName("md5"))
    val ParseIdent                  = FunctionDef[String, String](FunctionName("parse_ident"))
    val Chr                         = FunctionDef[Int, String](FunctionName("chr"))
    val CurrentDate                 = Expr.ParenlessFunctionCall0[LocalDate](FunctionName("current_date"))
    val Initcap                     = FunctionDef[String, String](FunctionName("initcap"))
    val Repeat                      = FunctionDef[(String, Int), String](FunctionName("repeat"))
    val Reverse                     = FunctionDef[String, String](FunctionName("reverse"))
    val TrimScale                   = FunctionDef[Double, Double](FunctionName("trim_scale"))
    val Hex                         = FunctionDef[Int, String](FunctionName("to_hex"))
    val Left                        = FunctionDef[(String, Int), String](FunctionName("left"))
    val Length                      = FunctionDef[String, Int](FunctionName("length"))
    val MinScale                    = FunctionDef[Double, Int](FunctionName("min_scale"))
    val Radians                     = FunctionDef[Double, Double](FunctionName("radians"))
    val Right                       = FunctionDef[(String, Int), String](FunctionName("right"))
    val StartsWith                  = FunctionDef[(String, String), Boolean](FunctionName("starts_with"))
    val Translate                   = FunctionDef[(String, String, String), String](FunctionName("translate"))
    val Trunc                       = FunctionDef[Double, Double](FunctionName("trunc"))
    val Sind                        = FunctionDef[Double, Double](FunctionName("sind"))
    val GCD                         = FunctionDef[(Double, Double), Double](FunctionName("gcd"))
    val LCM                         = FunctionDef[(Double, Double), Double](FunctionName("lcm"))
    val CBRT                        = FunctionDef[Double, Double](FunctionName("cbrt"))
    val Degrees                     = FunctionDef[Double, Double](FunctionName("degrees"))
    val Div                         = FunctionDef[(Double, Double), Double](FunctionName("div"))
    val Factorial                   = FunctionDef[Int, Int](FunctionName("factorial"))
    val Random                      = FunctionDef[Any, Double](FunctionName("random"))
    val LPad                        = FunctionDef[(String, Int, String), String](FunctionName("lpad"))
    val RPad                        = FunctionDef[(String, Int, String), String](FunctionName("rpad"))
    val ToTimestamp                 = FunctionDef[Long, ZonedDateTime](FunctionName("to_timestamp"))
    val PgClientEncoding            = FunctionDef[Any, String](FunctionName("pg_client_encoding"))
  }

  override def renderRead(read: self.Read[_]): String = {
    implicit val render: Builder = Builder()
    PostgresRenderer.renderReadImpl(read)
    println(render.toString)
    render.toString
  }

  def renderUpdate(update: Update[_]): String = {
    implicit val render: Builder = Builder()
    PostgresRenderer.renderUpdateImpl(update)
    println(render.toString)
    render.toString
  }

  override def renderDelete(delete: Delete[_]): String = {
    implicit val render: Builder = Builder()
    PostgresRenderer.renderDeleteImpl(delete)
    println(render.toString)
    render.toString
  }

}
