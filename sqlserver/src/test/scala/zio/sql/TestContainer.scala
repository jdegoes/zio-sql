package zio.sql

import com.dimafeng.testcontainers.SingleContainer
import com.dimafeng.testcontainers.MSSQLServerContainer
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

  def postgres(imageName: String = "mcr.microsoft.com/mssql/server:2017-latest"): ZLayer[Blocking, Throwable, Has[MSSQLServerContainer]] =
    ZManaged.make {
      effectBlocking {
        val c = new MSSQLServerContainer(
          dockerImageName = DockerImageName.parse(imageName)
        ).configure { a =>
          a.withInitScript("db_schema.sql")
          ()
        }
        c.start()
        c
      }
    } { container =>
      effectBlocking(container.stop()).orDie
    }.toLayer

}
