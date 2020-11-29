package zio.sql.rendering

import zio.sql.Sql

trait RenderModule { self: Sql =>
  //not using vararg to avoid allocating `Seq`s
  def render[A](a: A)(implicit r: SqlRenderer[A], builder: Builder): Unit = r.apply(a)(builder)

  def render[A, B](a: A, b: B)(implicit ra: SqlRenderer[A], rb: SqlRenderer[B], builder: Builder): Unit = {
    render(a)
    render(b)
  }

  def render[A, B, C](a: A, b: B, c: C)(implicit
    ra: SqlRenderer[A],
    rb: SqlRenderer[B],
    rc: SqlRenderer[C],
    builder: Builder
  ): Unit = {
    render(a)
    render(b)
    render(c)
  }

  def render[A, B, C, D](a: A, b: B, c: C, d: D)(implicit
    ra: SqlRenderer[A],
    rb: SqlRenderer[B],
    rc: SqlRenderer[C],
    rd: SqlRenderer[D],
    builder: Builder
  ): Unit = {
    render(a)
    render(b)
    render(c)
    render(d)
  }
  def render[A, B, C, D, E](a: A, b: B, c: C, d: D, e: E)(implicit
    ra: SqlRenderer[A],
    rb: SqlRenderer[B],
    rc: SqlRenderer[C],
    rd: SqlRenderer[D],
    re: SqlRenderer[E],
    builder: Builder
  ): Unit = {
    render(a)
    render(b)
    render(c)
    render(d)
    render(e)
  }
  def render[A, B, C, D, E, F](a: A, b: B, c: C, d: D, e: E, f: F)(implicit
    ra: SqlRenderer[A],
    rb: SqlRenderer[B],
    rc: SqlRenderer[C],
    rd: SqlRenderer[D],
    re: SqlRenderer[E],
    rf: SqlRenderer[F],
    builder: Builder
  ): Unit = {
    render(a)
    render(b)
    render(c)
    render(d)
    render(e)
    render(f)
  }
  def render[A, B, C, D, E, F, G](a: A, b: B, c: C, d: D, e: E, f: F, g: G)(implicit
    ra: SqlRenderer[A],
    rb: SqlRenderer[B],
    rc: SqlRenderer[C],
    rd: SqlRenderer[D],
    re: SqlRenderer[E],
    rf: SqlRenderer[F],
    rg: SqlRenderer[G],
    builder: Builder
  ): Unit = {
    render(a)
    render(b)
    render(c)
    render(d)
    render(e)
    render(f)
    render(g)
  }
  def render[A, B, C, D, E, F, G, H, I](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H)(implicit
    ra: SqlRenderer[A],
    rb: SqlRenderer[B],
    rc: SqlRenderer[C],
    rd: SqlRenderer[D],
    re: SqlRenderer[E],
    rf: SqlRenderer[F],
    rg: SqlRenderer[G],
    rh: SqlRenderer[H],
    builder: Builder
  ): Unit = {
    render(a)
    render(b)
    render(c)
    render(d)
    render(e)
    render(f)
    render(g)
    render(h)
  }
  def render[A, B, C, D, E, F, G, H, I](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I)(implicit
    ra: SqlRenderer[A],
    rb: SqlRenderer[B],
    rc: SqlRenderer[C],
    rd: SqlRenderer[D],
    re: SqlRenderer[E],
    rf: SqlRenderer[F],
    rg: SqlRenderer[G],
    rh: SqlRenderer[H],
    ri: SqlRenderer[I],
    builder: Builder
  ): Unit = {
    render(a)
    render(b)
    render(c)
    render(d)
    render(e)
    render(f)
    render(g)
    render(h)
    render(i)
  }
  def render[A, B, C, D, E, F, G, H, I, J](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J)(implicit
    ra: SqlRenderer[A],
    rb: SqlRenderer[B],
    rc: SqlRenderer[C],
    rd: SqlRenderer[D],
    re: SqlRenderer[E],
    rf: SqlRenderer[F],
    rg: SqlRenderer[G],
    rh: SqlRenderer[H],
    ri: SqlRenderer[I],
    rj: SqlRenderer[J],
    builder: Builder
  ): Unit = {
    render(a)
    render(b)
    render(c)
    render(d)
    render(e)
    render(f)
    render(g)
    render(h)
    render(i)
    render(j)
  }
}
