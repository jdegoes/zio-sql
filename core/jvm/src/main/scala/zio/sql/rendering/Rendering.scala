package zio.sql.rendering

trait Rendering[-A] extends LowPriorityRenderingImplicits {
  def apply(a: A)(implicit builder: Builder): Unit
}

abstract class LowPriorityRenderingImplicits {
  //todo add constants for common characters e.g. "'"
  implicit case object DefaultRenderer extends Rendering[String] {
    override def apply(a: String)(implicit builder: Builder): Unit = {
      //todo escaping
      val _ = builder.append(a)
    }
  }

  implicit case object YoloRenderer extends Rendering[Any] {
    override def apply(a: Any)(implicit builder: Builder): Unit = {
      //doesn't do anything
    }
  }
}
