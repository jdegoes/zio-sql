package zio.sql

import org.testcontainers.utility.DockerImageName
import com.dimafeng.testcontainers.{MSSQLServerContainer, SingleContainer}
import zio.{Has, Tag, ZLayer, ZManaged}
import zio.blocking.{Blocking, effectBlocking}

object TestContainer {
  def container[C <: SingleContainer[_]: Tag](c: C): ZLayer[Blocking, Throwable, Has[C]] =
    ZManaged.make {
      effectBlocking {
        c.start()
        c
      }
    }(container => effectBlocking(container.stop()).orDie).toLayer

  def mssql(imageName: String = "mcr.microsoft.com/azure-sql-edge:latest"): ZLayer[Blocking, Throwable, Has[MSSQLServerContainer]] =
    ZManaged.make {
      effectBlocking {
        val c = new MSSQLServerContainer(
          dockerImageName = DockerImageName.parse(imageName)
            .asCompatibleSubstituteFor("mcr.microsoft.com/mssql/server")
        ).configure { a =>
          a.withInitScript("shop_schema.sql")
          ()
        }

        c.start()
        c
      }
    } { container =>
      effectBlocking(container.stop()).orDie
    }.toLayer
}
