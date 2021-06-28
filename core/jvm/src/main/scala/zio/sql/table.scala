package zio.sql

import java.time._
import java.util.UUID

import zio.Chunk

trait TableModule { self: ExprModule with SelectModule =>

  sealed case class ColumnSchema[A](value: A)

  sealed trait ColumnSet {
    type ColumnsRepr[T]
    type Append[That <: ColumnSet] <: ColumnSet

    def ++[That <: ColumnSet](that: That): Append[That]

    def columnsUntyped: List[Column.Untyped]

    protected def mkColumns[T](name: TableName): ColumnsRepr[T]
  }

  object ColumnSet {
    type Empty                  = Empty.type
    type :*:[A, B <: ColumnSet] = Cons[A, B]
    type Singleton[A]           = Cons[A, Empty]

    case object Empty extends ColumnSet {
      type ColumnsRepr[_]            = Unit
      type Append[That <: ColumnSet] = That

      override def ++[That <: ColumnSet](that: That): Append[That] = that

      override def columnsUntyped: List[Column.Untyped] = Nil

      override protected def mkColumns[T](name: TableName): ColumnsRepr[T] = ()
    }

    sealed case class Cons[A, B <: ColumnSet](head: Column[A], tail: B) extends ColumnSet { self =>
      type ColumnsRepr[T]            = (Expr[Features.Source, T, A], tail.ColumnsRepr[T])
      type Append[That <: ColumnSet] = Cons[A, tail.Append[That]]

      override def ++[That <: ColumnSet](that: That): Append[That] = Cons(head, tail ++ that)

      override def columnsUntyped: List[Column.Untyped] = head :: tail.columnsUntyped

      def table(name0: TableName): Table.Source.Aux_[ColumnsRepr, A :*: B] =
        new Table.Source {
          type Repr[C] = ColumnsRepr[C]
          type Cols    = A :*: B
          val name: TableName                      = name0
          val columnSchema: ColumnSchema[A :*: B]  = ColumnSchema(self)
          val columns: ColumnsRepr[TableType]      = mkColumns[TableType](name0)
          val columnsUntyped: List[Column.Untyped] = self.columnsUntyped

        }

      override protected def mkColumns[T](name: TableName): ColumnsRepr[T] =
        (Expr.Source(name, head), tail.mkColumns(name))
    }

    def bigDecimal(name: String): Singleton[BigDecimal]         = singleton[BigDecimal](name)
    def boolean(name: String): Singleton[Boolean]               = singleton[Boolean](name)
    def byteArray(name: String): Singleton[Chunk[Byte]]         = singleton[Chunk[Byte]](name)
    def char(name: String): Singleton[Char]                     = singleton[Char](name)
    def double(name: String): Singleton[Double]                 = singleton[Double](name)
    def float(name: String): Singleton[Float]                   = singleton[Float](name)
    def instant(name: String): Singleton[Instant]               = singleton[Instant](name)
    def int(name: String): Singleton[Int]                       = singleton[Int](name)
    def localDate(name: String): Singleton[LocalDate]           = singleton[LocalDate](name)
    def localDateTime(name: String): Singleton[LocalDateTime]   = singleton[LocalDateTime](name)
    def localTime(name: String): Singleton[LocalTime]           = singleton[LocalTime](name)
    def long(name: String): Singleton[Long]                     = singleton[Long](name)
    def offsetDateTime(name: String): Singleton[OffsetDateTime] = singleton[OffsetDateTime](name)
    def offsetTime(name: String): Singleton[OffsetTime]         = singleton[OffsetTime](name)
    def short(name: String): Singleton[Short]                   = singleton[Short](name)
    def singleton[A: TypeTag](name: String): Singleton[A]       = Cons(Column[A](name), Empty)
    def string(name: String): Singleton[String]                 = singleton[String](name)
    def uuid(name: String): Singleton[UUID]                     = singleton[UUID](name)
    def zonedDateTime(name: String): Singleton[ZonedDateTime]   = singleton[ZonedDateTime](name)
  }

  object :*: {
    def unapply[A, B](tuple: (A, B)): Some[(A, B)] = Some(tuple)
  }

  sealed case class Column[A: TypeTag](name: String) {
    def typeTag: TypeTag[A] = implicitly[TypeTag[A]]
  }

  object Column {
    type Untyped = Column[_]
  }

  sealed trait JoinType

  object JoinType {
    case object Inner      extends JoinType
    case object LeftOuter  extends JoinType
    case object RightOuter extends JoinType
    case object FullOuter  extends JoinType
  }

  sealed trait CrossType 

  object CrossType {
    case object CrossApply extends CrossType
    case object OuterApply extends CrossType
  }

  /**
   * (left join right) on (...)
   */
  sealed trait Table { self =>
    type TableType

    final def fullOuter[That](that: Table.Aux[That]): Table.JoinBuilder[self.TableType, That] =
      new Table.JoinBuilder[self.TableType, That](JoinType.FullOuter, self, that)

    final def join[That](that: Table.Aux[That]): Table.JoinBuilder[self.TableType, That] =
      new Table.JoinBuilder[self.TableType, That](JoinType.Inner, self, that)

    final def leftOuter[That](that: Table.Aux[That]): Table.JoinBuilder[self.TableType, That] =
      new Table.JoinBuilder[self.TableType, That](JoinType.LeftOuter, self, that)

    final def rightOuter[That](that: Table.Aux[That]): Table.JoinBuilder[self.TableType, That] =
      new Table.JoinBuilder[self.TableType, That](JoinType.RightOuter, self, that)

    final def crossApply[F, Cols, TableSelectionType](select: Read.Select[F, Cols, TableSelectionType]): Table.SelectedTableBuilder[F, self.TableType, TableSelectionType, Cols] = 
      new Table.SelectedTableBuilder[F, self.TableType, TableSelectionType, Cols](CrossType.CrossApply, self, select)

    final def outerApply[F, Cols, TableSelectionType](select: Read.Select[F, Cols, TableSelectionType]): Table.SelectedTableBuilder[F, self.TableType, TableSelectionType, Cols] = 
      new Table.SelectedTableBuilder[F, self.TableType, TableSelectionType, Cols](CrossType.OuterApply, self, select)

    val columnsUntyped: List[Column.Untyped]
  }

  object Table {

    class JoinBuilder[A, B](joinType: JoinType, left: Table.Aux[A], right: Table.Aux[B]) {
      def on[F](expr: Expr[F, A with B, Boolean]): Table.Aux[A with B] =
        Joined(joinType, left, right, expr)
    }

    case class SelectedTableBuilder[F1, A, B, Cols](crossType: CrossType, left: Table.Aux[A], select: Read.Select[F1, Cols, B]) {
      def where[F2](expr: Expr[F2, A with B, Boolean]): Table.Aux[A with B] =
        SelectedTable[F1, F2, Cols, A, B](crossType, left, select, expr)
    }

    /**
      * Table where right side is a selection out of another table. 
      * TODO do we want to support "table valued function" for sql server and replace Select with a function?
      * 
      * example
      * elect(fName ++ lName ++ orderDate).from(customers.crossApply(select(orderDate).from(orders)).where(fkCustomerId === customerId))
      */
    sealed case class SelectedTable[F1, F2, Cols, A, B](
            crossType: CrossType,
            left: Table.Aux[A],
            select: Read.Select[F1, Cols, B], 
            expr: Expr[F2, A with B, Boolean]) extends Table {
      type TableType = A with B
      
      val columnsUntyped: List[Column.Untyped] = left.columnsUntyped ++ select.table.get.columnsUntyped
    }

    type Aux[A] = Table { type TableType = A }

    //you need insanity in your life
    trait Insanity {
      def ahhhhhhhhhhhhh[A]: A
    }
    sealed trait Source extends Table with Insanity {
      type Repr[_]
      type Cols
      val name: TableName
      val columnSchema: ColumnSchema[Cols]
      val columns: Repr[TableType]

      override def ahhhhhhhhhhhhh[A]: A = ??? //don't remove or it'll break
    }
    object Source {
      type Aux_[F[_], B]   = Table.Source {
        type Repr[X] = F[X]
        type Cols    = B
      }
      type Aux[F[_], A, B] = Table.Source {
        type Repr[X]   = F[X]
        type TableType = A
        type Cols      = B
      }
    }

    sealed case class Joined[F, A, B](
      joinType: JoinType,
      left: Table.Aux[A],
      right: Table.Aux[B],
      on: Expr[F, A with B, Boolean]
    ) extends Table {
      type TableType = left.TableType with right.TableType

      val columnsUntyped: List[Column.Untyped] = left.columnsUntyped ++ right.columnsUntyped
    }
  }
}
