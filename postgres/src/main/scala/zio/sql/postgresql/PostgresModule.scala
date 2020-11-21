package zio.sql.postgresql

import java.time.LocalDate

import zio.sql.Jdbc

/**
 */
trait PostgresModule extends Jdbc { self =>

  sealed trait TimestampPart
  case object Century         extends TimestampPart
  case object Day             extends TimestampPart
  case object Decade          extends TimestampPart
  case object Dow             extends TimestampPart
  case object Doy             extends TimestampPart
  case object Epoch           extends TimestampPart
  case object Hour            extends TimestampPart
  case object IsoDow          extends TimestampPart
  case object IsoYear         extends TimestampPart
  case object Microseconds    extends TimestampPart
  case object Millennium      extends TimestampPart
  case object Milliseconds    extends TimestampPart
  case object Minute          extends TimestampPart
  case object Month           extends TimestampPart
  case object Quarter         extends TimestampPart
  case object Second          extends TimestampPart
  case object Timezone        extends TimestampPart
  case object Timezone_hour   extends TimestampPart
  case object Timezone_minute extends TimestampPart
  case object Week            extends TimestampPart
  case object Year            extends TimestampPart

  sealed trait PostgresTypeTagExtension[+A]

  object PostgresTypeTagExtension {
    implicit case object TTimestampPartCentury         extends PostgresTypeTagExtension[Century.type]
    implicit case object TTimestampPartDay             extends PostgresTypeTagExtension[Day.type]
    implicit case object TTimestampPartDecade          extends PostgresTypeTagExtension[Decade.type]
    implicit case object TTimestampPartDow             extends PostgresTypeTagExtension[Dow.type]
    implicit case object TTimestampPartDoy             extends PostgresTypeTagExtension[Doy.type]
    implicit case object TTimestampPartEpoch           extends PostgresTypeTagExtension[Epoch.type]
    implicit case object TTimestampPartHour            extends PostgresTypeTagExtension[Hour.type]
    implicit case object TTimestampPartIsoDow          extends PostgresTypeTagExtension[IsoDow.type]
    implicit case object TTimestampPartIsoYear         extends PostgresTypeTagExtension[IsoYear.type]
    implicit case object TTimestampPartMicroseconds    extends PostgresTypeTagExtension[Microseconds.type]
    implicit case object TTimestampPartMillennium      extends PostgresTypeTagExtension[Millennium.type]
    implicit case object TTimestampPartMilliseconds    extends PostgresTypeTagExtension[Milliseconds.type]
    implicit case object TTimestampPartMinute          extends PostgresTypeTagExtension[Minute.type]
    implicit case object TTimestampPartMonth           extends PostgresTypeTagExtension[Month.type]
    implicit case object TTimestampPartQuarter         extends PostgresTypeTagExtension[Quarter.type]
    implicit case object TTimestampPartSecond          extends PostgresTypeTagExtension[Second.type]
    implicit case object TTimestampPartTimezone        extends PostgresTypeTagExtension[Timezone.type]
    implicit case object TTimestampPartTimezone_hour   extends PostgresTypeTagExtension[Timezone_hour.type]
    implicit case object TTimestampPartTimezone_minute extends PostgresTypeTagExtension[Timezone_minute.type]
    implicit case object TTimestampPartWeek            extends PostgresTypeTagExtension[Week.type]
    implicit case object TTimestampPartYear            extends PostgresTypeTagExtension[Year.type]
  }

  type TypeTagExtension[+A] = PostgresTypeTagExtension[A]

  object PostgresFunctionDef {
    val Sind    = FunctionDefStandard[Double, Double](FunctionName("sind"))
    // FIXME: extract should be available also for other date like types (e.g. LocalDateTime, Instant, etc.)
    val Extract = FunctionDefWithDelimiter[(TimestampPart, LocalDate), Double](FunctionName("extract"), " FROM ")
  }

  override def renderRead(read: self.Read[_]): String = {
    val builder = new StringBuilder

    def buildExpr[A, B](expr: self.Expr[_, A, B]): Unit = expr match {
      case Expr.Source(tableName, column)                               =>
        val _ = builder.append(tableName).append(".").append(column.name)
      case Expr.Unary(base, op)                                         =>
        val _ = builder.append(" ").append(op.symbol)
        buildExpr(base)
      case Expr.Property(base, op)                                      =>
        buildExpr(base)
        val _ = builder.append(" ").append(op.symbol)
      case Expr.Binary(left, right, op)                                 =>
        buildExpr(left)
        builder.append(" ").append(op.symbol).append(" ")
        buildExpr(right)
      case Expr.Relational(left, right, op)                             =>
        buildExpr(left)
        builder.append(" ").append(op.symbol).append(" ")
        buildExpr(right)
      case Expr.In(value, set)                                          =>
        buildExpr(value)
        buildReadString(set)
      case Expr.Literal(value)                                          =>
        val _ = builder.append(value.toString) //todo fix escaping
      case Expr.AggregationCall(param, aggregation)                     =>
        builder.append(aggregation.name.name)
        builder.append("(")
        buildExpr(param)
        val _ = builder.append(")")
      case Expr.FunctionCall1(param, function)                          =>
        builder.append(function.name.name)
        builder.append("(")
        buildExpr(param)
        val _ = builder.append(")")
      case Expr.FunctionCall2(param1, param2, paramDelimiter, function) =>
        builder.append(function.name.name)
        builder.append("(")
        buildExpr(param1)
        builder.append(paramDelimiter)
        buildExpr(param2)
        val _ = builder.append(")")
      case Expr.FunctionCall3(param1, param2, param3, function)         =>
        builder.append(function.name.name)
        builder.append("(")
        buildExpr(param1)
        builder.append(",")
        buildExpr(param2)
        builder.append(",")
        buildExpr(param3)
        val _ = builder.append(")")
      case Expr.FunctionCall4(param1, param2, param3, param4, function) =>
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
    }

    def buildReadString[A <: SelectionSet[_]](read: self.Read[_]): Unit =
      read match {
        case read0 @ Read.Select(_, _, _, _, _, _, _, _) =>
          object Dummy {
            type F
            type A
            type B <: SelectionSet[A]
          }
          val read = read0.asInstanceOf[Read.Select[Dummy.F, Dummy.A, Dummy.B]]
          import read._

          builder.append("SELECT ")
          buildSelection(selection.value)
          builder.append(" FROM ")
          buildTable(table)
          whereExpr match {
            case Expr.Literal(true) => ()
            case _                  =>
              builder.append(" WHERE ")
              buildExpr(whereExpr)
          }
          groupBy match {
            case _ :: _ =>
              builder.append(" GROUP BY ")
              buildExprList(groupBy)

              havingExpr match {
                case Expr.Literal(true) => ()
                case _                  =>
                  builder.append(" HAVING ")
                  buildExpr(havingExpr)
              }
            case Nil    => ()
          }
          orderBy match {
            case _ :: _ =>
              builder.append(" ORDER BY ")
              buildOrderingList(orderBy)
            case Nil    => ()
          }
          limit match {
            case Some(limit) =>
              builder.append(" LIMIT ").append(limit)
            case None        => ()
          }
          offset match {
            case Some(offset) =>
              val _ = builder.append(" OFFSET ").append(offset)
            case None         => ()
          }

        case Read.Union(left, right, distinct) =>
          buildReadString(left)
          builder.append(" UNION ")
          if (!distinct) builder.append("ALL ")
          buildReadString(right)

        case Read.Literal(values) =>
          val _ = builder.append(" (").append(values.mkString(",")).append(") ") //todo fix needs escaping
      }

    def buildExprList(expr: List[Expr[_, _, _]]): Unit               =
      expr match {
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
    def buildOrderingList(expr: List[Ordering[Expr[_, _, _]]]): Unit =
      expr match {
        case head :: tail =>
          head match {
            case Ordering.Asc(value)  => buildExpr(value)
            case Ordering.Desc(value) =>
              buildExpr(value)
              builder.append(" DESC")
          }
          tail match {
            case _ :: _ =>
              builder.append(", ")
              buildOrderingList(tail)
            case Nil    => ()
          }
        case Nil          => ()
      }

    def buildSelection[A](selectionSet: SelectionSet[A]): Unit =
      selectionSet match {
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

    def buildColumnSelection[A, B](columnSelection: ColumnSelection[A, B]): Unit =
      columnSelection match {
        case ColumnSelection.Constant(value, name) =>
          builder.append(value.toString()) //todo fix escaping
          name match {
            case Some(name) =>
              val _ = builder.append(" AS ").append(name)
            case None       => ()
          }
        case ColumnSelection.Computed(expr, name)  =>
          buildExpr(expr)
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
    def buildTable(table: Table): Unit                                           =
      table match {
        //The outer reference in this type test cannot be checked at run time?!
        case sourceTable: self.Table.Source          =>
          val _ = builder.append(sourceTable.name)
        case Table.Joined(joinType, left, right, on) =>
          buildTable(left)
          builder.append(joinType match {
            case JoinType.Inner      => " INNER JOIN "
            case JoinType.LeftOuter  => " LEFT JOIN "
            case JoinType.RightOuter => " RIGHT JOIN "
            case JoinType.FullOuter  => " OUTER JOIN "
          })
          buildTable(right)
          builder.append(" ON ")
          buildExpr(on)
          val _ = builder.append(" ")
      }
    buildReadString(read)
    builder.toString()
  }
}
