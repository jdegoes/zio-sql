package zio.sql

import java.time._

import scala.language.implicitConversions

trait ExprModule extends NewtypesModule with FeaturesModule with OpsModule {
  self: SelectModule with TableModule =>

  /**
   * Models a function `A => B`.
   * SELECT product.price + 10
   */
  sealed trait Expr[F, -A, +B] { self =>

    def +[F2, A1 <: A, B1 >: B](that: Expr[F2, A1, B1])(implicit ev: IsNumeric[B1]): Expr[F :||: F2, A1, B1] =
      Expr.Binary(self, that, BinaryOp.Add[B1]())

    def -[F2, A1 <: A, B1 >: B](that: Expr[F2, A1, B1])(implicit ev: IsNumeric[B1]): Expr[F :||: F2, A1, B1] =
      Expr.Binary(self, that, BinaryOp.Sub[B1]())

    def *[F2, A1 <: A, B1 >: B](that: Expr[F2, A1, B1])(implicit ev: IsNumeric[B1]): Expr[F :||: F2, A1, B1] =
      Expr.Binary(self, that, BinaryOp.Mul[B1]())

    def /[F2, A1 <: A, B1 >: B](that: Expr[F2, A1, B1])(implicit ev: IsNumeric[B1]): Expr[F :||: F2, A1, B1] =
      Expr.Binary(self, that, BinaryOp.Div[B1]())

    def &&[F2, A1 <: A, B1 >: B](
      that: Expr[F2, A1, Boolean]
    )(implicit ev: B <:< Boolean): Expr[F :||: F2, A1, Boolean] =
      Expr.Binary(self.widen[Boolean], that, BinaryOp.AndBool)

    def ||[F2, A1 <: A, B1 >: B](
      that: Expr[F2, A1, Boolean]
    )(implicit ev: B <:< Boolean): Expr[F :||: F2, A1, Boolean] =
      Expr.Binary(self.widen[Boolean], that, BinaryOp.OrBool)

    def ===[F2, A1 <: A, B1 >: B](that: Expr[F2, A1, B1]): Expr[F :||: F2, A1, Boolean] =
      Expr.Relational(self, that, RelationalOp.Equals)

    def <>[F2, A1 <: A, B1 >: B](that: Expr[F2, A1, B1]): Expr[F :||: F2, A1, Boolean] =
      Expr.Relational(self, that, RelationalOp.NotEqual)

    def >[F2, A1 <: A, B1 >: B](that: Expr[F2, A1, B1]): Expr[F :||: F2, A1, Boolean] =
      Expr.Relational(self, that, RelationalOp.GreaterThan)

    def <[F2, A1 <: A, B1 >: B](that: Expr[F2, A1, B1]): Expr[F :||: F2, A1, Boolean] =
      Expr.Relational(self, that, RelationalOp.LessThan)

    def >=[F2, A1 <: A, B1 >: B](that: Expr[F2, A1, B1]): Expr[F :||: F2, A1, Boolean] =
      Expr.Relational(self, that, RelationalOp.GreaterThanEqual)

    def <=[F2, A1 <: A, B1 >: B](that: Expr[F2, A1, B1]): Expr[F :||: F2, A1, Boolean] =
      Expr.Relational(self, that, RelationalOp.LessThanEqual)

    def &[F2, A1 <: A, B1 >: B](that: Expr[F2, A1, B1])(implicit ev: IsIntegral[B1]): Expr[F :||: F2, A1, B1] =
      Expr.Binary(self, that, BinaryOp.AndBit[B1]())

    def |[F2, A1 <: A, B1 >: B](that: Expr[F2, A1, B1])(implicit ev: IsIntegral[B1]): Expr[F :||: F2, A1, B1] =
      Expr.Binary(self, that, BinaryOp.OrBit[B1]())

    def unary_~[B1 >: B](implicit ev: IsIntegral[B1]): Expr.Unary[F, A, B1] =
      Expr.Unary(self, UnaryOp.NotBit[B1]())

    def unary_-[B1 >: B](implicit ev: IsNumeric[B1]): Expr.Unary[F, A, B1] =
      Expr.Unary(self, UnaryOp.Negate[B1]())

    def not[A1 <: A](implicit ev: B <:< Boolean): Expr.Unary[F, A1, Boolean] =
      Expr.Unary(self.widen[Boolean], UnaryOp.NotBool)

    def isNull[A1 <: A]: Expr[F, A1, Boolean] =
      Expr.Property(self, PropertyOp.IsNull)

    def isNotNull[A1 <: A]: Expr[F, A1, Boolean] =
      Expr.Property(self, PropertyOp.IsNotNull)

    def isTrue[A1 <: A](implicit ev: B <:< Boolean): Expr[F, A1, Boolean] =
      Expr.Property(self, PropertyOp.IsTrue)

    def isNotTrue[A1 <: A](implicit ev: B <:< Boolean): Expr[F, A1, Boolean] =
      Expr.Property(self, PropertyOp.IsNotTrue)

    def as[B1 >: B](name: String): Selection[F, A, SelectionSet.Cons[A, B1, SelectionSet.Empty]] =
      Selection.computedAs(self, name)

    def ascending: Ordering[Expr[F, A, B]] = Ordering.Asc(self)

    def asc: Ordering[Expr[F, A, B]] = Ordering.Asc(self)

    def descending: Ordering[Expr[F, A, B]] = Ordering.Desc(self)

    def desc: Ordering[Expr[F, A, B]] = Ordering.Desc(self)

    def in[B1 >: B <: SelectionSet[_]](set: Read[B1]): Expr[F, A, Boolean] =
      Expr.In(self.asInstanceOf[Expr[F, A, SelectionSet[_]]], set)

    def widen[C](implicit ev: B <:< C): Expr[F, A, C] = {
      val _ = ev
      self.asInstanceOf[Expr[F, A, C]]
    }
  }

  object Expr {

    sealed trait InvariantExpr[F, -A, B] extends Expr[F, A, B] {
      def typeTag: TypeTag[B]
    }

    def typeTagOf[A](expr: Expr[_, _, A]): TypeTag[A] = expr.asInstanceOf[InvariantExpr[_, _, A]].typeTag

    implicit def literal[A](a: A)(implicit typeTag: TypeTag[A]): Expr[Features.Literal, Any, A] = typeTag match {
      case TypeTag.TLocalDateTime => Expr.literal(s"TIMESTAMP '$a'").asInstanceOf[Expr[Features.Literal, Any, A]]
      case _ => Expr.Literal(a)
    }


    def exprName[F, A, B](expr: Expr[F, A, B]): Option[String] =
      expr match {
        case Expr.Source(_, c) => Some(c.name)
        case _                 => None
      }

    implicit def expToSelection[F, A, B](
      expr: Expr[F, A, B]
    ): Selection[F, A, SelectionSet.Cons[A, B, SelectionSet.Empty]] =
      Selection.computedOption(expr, exprName(expr))

    sealed case class Source[A, B] private[sql] (tableName: TableName, column: Column[B])
        extends InvariantExpr[Features.Source, A, B] {
      def typeTag: TypeTag[B] = column.typeTag
    }

    sealed case class Unary[F, -A, B](base: Expr[F, A, B], op: UnaryOp[B]) extends InvariantExpr[F, A, B] {
      def typeTag: TypeTag[B] = typeTagOf(base)
    }

    sealed case class Property[F, -A, +B](base: Expr[F, A, B], op: PropertyOp) extends InvariantExpr[F, A, Boolean] {
      def typeTag: TypeTag[Boolean] = TypeTag.TBoolean
    }

    sealed case class Binary[F1, F2, A, B](left: Expr[F1, A, B], right: Expr[F2, A, B], op: BinaryOp[B])
        extends InvariantExpr[Features.Union[F1, F2], A, B] {
      def typeTag: TypeTag[B] = typeTagOf(left)
    }

    sealed case class Relational[F1, F2, A, B](left: Expr[F1, A, B], right: Expr[F2, A, B], op: RelationalOp)
        extends InvariantExpr[Features.Union[F1, F2], A, Boolean] {
      def typeTag: TypeTag[Boolean] = TypeTag.TBoolean
    }

    sealed case class In[F, A, B <: SelectionSet[_]](value: Expr[F, A, B], set: Read[B])
        extends InvariantExpr[F, A, Boolean] {
      def typeTag: TypeTag[Boolean] = TypeTag.TBoolean
    }

    sealed case class Literal[B: TypeTag](value: B) extends InvariantExpr[Features.Literal, Any, B] {
      def typeTag: TypeTag[B] = implicitly[TypeTag[B]]
    }

    sealed case class AggregationCall[F, A, B, Z: TypeTag](param: Expr[F, A, B], aggregation: AggregationDef[B, Z])
        extends InvariantExpr[Features.Aggregated[F], A, Z] {
      def typeTag: TypeTag[Z] = implicitly[TypeTag[Z]]
    }

    sealed case class FunctionCall1[F, A, B, Z: TypeTag](param: Expr[F, A, B], function: FunctionDef[B, Z])
        extends InvariantExpr[F, A, Z] {
      def typeTag: TypeTag[Z] = implicitly[TypeTag[Z]]
    }

    sealed case class FunctionCall2[F1, F2, A, B, C, Z: TypeTag](
      param1: Expr[F1, A, B],
      param2: Expr[F2, A, C],
      function: FunctionDef[(B, C), Z]
    ) extends InvariantExpr[Features.Union[F1, F2], A, Z] {
      def typeTag: TypeTag[Z] = implicitly[TypeTag[Z]]
    }

    sealed case class FunctionCall3[F1, F2, F3, A, B, C, D, Z: TypeTag](
      param1: Expr[F1, A, B],
      param2: Expr[F2, A, C],
      param3: Expr[F3, A, D],
      function: FunctionDef[(B, C, D), Z]
    ) extends InvariantExpr[Features.Union[F1, Features.Union[F2, F3]], A, Z] {
      def typeTag: TypeTag[Z] = implicitly[TypeTag[Z]]
    }

    sealed case class FunctionCall4[F1, F2, F3, F4, A, B, C, D, E, Z: TypeTag](
      param1: Expr[F1, A, B],
      param2: Expr[F2, A, C],
      param3: Expr[F3, A, D],
      param4: Expr[F4, A, E],
      function: FunctionDef[(B, C, D, E), Z]
    ) extends InvariantExpr[Features.Union[F1, Features.Union[F2, Features.Union[F3, F4]]], A, Z] {
      def typeTag: TypeTag[Z] = implicitly[TypeTag[Z]]
    }
  }

  sealed case class AggregationDef[-A, +B](name: FunctionName) { self =>

    def apply[F, Source, B1 >: B](expr: Expr[F, Source, A])(implicit
      typeTag: TypeTag[B1]
    ): Expr[Features.Aggregated[F], Source, B1] =
      Expr.AggregationCall[F, Source, A, B1](expr, self)
  }

  object AggregationDef {
    val Count                                            = AggregationDef[Any, Long](FunctionName("count"))
    val Sum                                              = AggregationDef[Double, Double](FunctionName("sum"))
    def Arbitrary[F, A, B: TypeTag](expr: Expr[F, A, B]) = AggregationDef[B, B](FunctionName("arbitrary"))(expr)
    val Avg                                              = AggregationDef[Double, Double](FunctionName("avg"))
    def Min[F, A, B: TypeTag](expr: Expr[F, A, B])       = AggregationDef[B, B](FunctionName("min"))(expr)
    def Max[F, A, B: TypeTag](expr: Expr[F, A, B])       = AggregationDef[B, B](FunctionName("max"))(expr)
  }

  sealed case class FunctionDef[-A, +B](name: FunctionName) { self =>

    def apply[F, Source, B1 >: B](param1: Expr[F, Source, A])(implicit typeTag: TypeTag[B1]): Expr[F, Source, B1] =
      Expr.FunctionCall1(param1, self: FunctionDef[A, B1])

    def apply[F1, F2, Source, P1, P2, B1 >: B](param1: Expr[F1, Source, P1], param2: Expr[F2, Source, P2])(implicit
      ev: (P1, P2) <:< A,
      typeTag: TypeTag[B1]
    ): Expr[F1 :||: F2, Source, B1] =
      Expr.FunctionCall2(param1, param2, self.narrow[(P1, P2)]: FunctionDef[(P1, P2), B1])

    def apply[F1, F2, F3, Source, P1, P2, P3, B1 >: B](
      param1: Expr[F1, Source, P1],
      param2: Expr[F2, Source, P2],
      param3: Expr[F3, Source, P3]
    )(implicit ev: (P1, P2, P3) <:< A, typeTag: TypeTag[B1]): Expr[F1 :||: F2 :||: F3, Source, B1] =
      Expr.FunctionCall3(param1, param2, param3, self.narrow[(P1, P2, P3)]: FunctionDef[(P1, P2, P3), B1])

    def apply[F1, F2, F3, F4, Source, P1, P2, P3, P4, B1 >: B](
      param1: Expr[F1, Source, P1],
      param2: Expr[F2, Source, P2],
      param3: Expr[F3, Source, P3],
      param4: Expr[F4, Source, P4]
    )(implicit ev: (P1, P2, P3, P4) <:< A, typeTag: TypeTag[B1]): Expr[F1 :||: F2 :||: F3 :||: F4, Source, B1] =
      Expr.FunctionCall4(
        param1,
        param2,
        param3,
        param4,
        self.narrow[(P1, P2, P3, P4)]: FunctionDef[(P1, P2, P3, P4), B1]
      )

    def narrow[C](implicit ev: C <:< A): FunctionDef[C, B] = {
      val _ = ev
      self.asInstanceOf[FunctionDef[C, B]]
    }
  }

  object FunctionDef {
    //math functions
    val Abs         = FunctionDef[Double, Double](FunctionName("abs"))
    val Acos        = FunctionDef[Double, Double](FunctionName("acos"))
    val Asin        = FunctionDef[Double, Double](FunctionName("asin"))
    val Atan        = FunctionDef[Double, Double](FunctionName("atan"))
    val Ceil        = FunctionDef[Double, Double](FunctionName("ceil"))
    val Cos         = FunctionDef[Double, Double](FunctionName("cos"))
    val Exp         = FunctionDef[Double, Double](FunctionName("exp"))
    val Floor       = FunctionDef[Double, Double](FunctionName("floor"))
    //val Log = FunctionDef[Double, Double](FunctionName("log")) //not part of SQL 2011 spec
    val Ln          = FunctionDef[Double, Double](FunctionName("ln"))
    val Mod         = FunctionDef[(Double, Double), Double](FunctionName("mod"))
    val Power       = FunctionDef[(Double, Double), Double](FunctionName("power"))
    val Round       = FunctionDef[(Double, Int), Double](FunctionName("round"))
    val Sign        = FunctionDef[Double, Double](FunctionName("sign"))
    val Sin         = FunctionDef[Double, Double](FunctionName("sin"))
    val Sqrt        = FunctionDef[Double, Double](FunctionName("sqrt"))
    val Tan         = FunctionDef[Double, Double](FunctionName("tan"))
    val WidthBucket = FunctionDef[(Double, Double, Double, Int), Int](FunctionName("width bucket"))

    //string functions
    val Ascii       = FunctionDef[String, Int](FunctionName("ascii"))
    val CharLength  = FunctionDef[String, Int](FunctionName("character length"))
    val Concat      = FunctionDef[(String, String), String](FunctionName("concat"))
    val Lower       = FunctionDef[String, String](FunctionName("lower"))
    val Ltrim       = FunctionDef[String, String](FunctionName("ltrim"))
    val OctetLength = FunctionDef[String, Int](FunctionName("octet length"))
    val Overlay     = FunctionDef[(String, String, Int, Option[Int]), String](FunctionName("overlay"))
    val Position    = FunctionDef[(String, String), Int](FunctionName("position"))
    val Replace     = FunctionDef[(String, String), String](FunctionName("replace"))
    val Rtrim       = FunctionDef[String, String](FunctionName("rtrim"))
    val Substring   = FunctionDef[(String, Int, Option[Int]), String](FunctionName("substring"))
    //TODO substring regex
    val Trim        = FunctionDef[String, String](FunctionName("trim"))
    val Upper       = FunctionDef[String, String](FunctionName("upper"))

    // date functions
    val CurrentTimestamp = FunctionDef[Nothing, Instant](FunctionName("current_timestamp"))
  }

  sealed trait Set[F, -A] {
    type Value

    def lhs: Expr[F, A, Value]
    def rhs: Expr[_, A, Value]

    def typeTag: TypeTag[Value]

  }

  object Set {
    type Aux[F, -A, Value0] = Set[F, A] { type Value = Value0 }

    def apply[F: Features.IsSource, A, Value0: TypeTag](
      lhs0: Expr[F, A, Value0],
      rhs0: Expr[_, A, Value0]
    ): Set.Aux[F, A, Value0] =
      new Set[F, A] {
        type Value = Value0

        def lhs = lhs0
        def rhs = rhs0

        def typeTag = implicitly[TypeTag[Value]]
      }
  }
}
