package zio.sql

import com.dimafeng.testcontainers.SingleContainer
import com.dimafeng.testcontainers.OracleContainer
import org.testcontainers.utility.DockerImageName
import zio._
import zio.blocking.{ effectBlocking, Blocking }

object TestContainer {

  def container[C <: SingleContainer[_]: Tag](c: C): ZLayer[Blocking, Throwable, Has[C]] =
    ZManaged.make {
      effectBlocking {
        c.start()
        c
      }
    }(container => effectBlocking(container.stop()).orDie).toLayer

  def oracle(imageName: DockerImageName): ZLayer[Blocking, Throwable, Has[OracleContainer]] =
    ZManaged.make {
      effectBlocking {
        val c = new OracleContainer(
          dockerImageName = imageName
        ).configure { a =>
          a.withInitScript("shop_schema.sql")
          ()
        }
        c.start()
        c
      }
    }(container => effectBlocking(container.stop()).orDie).toLayer

}
