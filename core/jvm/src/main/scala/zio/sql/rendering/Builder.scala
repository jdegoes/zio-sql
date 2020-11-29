package zio.sql.rendering

class Builder(val builder: StringBuilder) extends AnyVal { self =>
  override def toString: String = builder.toString()
  private[rendering] def append(a: Any): Unit = {
    val _ = builder.append(a)
  }
}

object Builder {
  def apply(): Builder = new Builder(new StringBuilder)
}
