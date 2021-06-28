package zio.sql.sqlserver

import zio.test._
import zio.test.environment.TestEnvironment
import java.util.Properties
import zio.sql.{ ConnectionPoolConfig, JdbcRunnableSpec, TestContainer }
import zio.Has

trait SqlServerRunnableSpec extends JdbcRunnableSpec with SqlServerModule {

  def autoCommit: Boolean = true

  private def connProperties(user: String, password: String): Properties = {
    val props = new Properties
    props.setProperty("user", user)
    props.setProperty("password", password)
    props
  }

  val poolConfigLayer = TestContainer
    .postgres()
    .map(a =>
      Has(
        ConnectionPoolConfig(
          url = a.get.jdbcUrl,
          properties = connProperties(a.get.username, a.get.password),
          autoCommit = autoCommit
        )
      )
    )

  override def spec: Spec[TestEnvironment, TestFailure[Any], TestSuccess] =
    specLayered.provideCustomLayerShared(jdbcLayer)

  def specLayered: Spec[JdbcEnvironment, TestFailure[Object], TestSuccess]

}
