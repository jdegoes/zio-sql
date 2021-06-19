package zio.sql.oracle

import org.testcontainers.utility.DockerImageName

import java.util.Properties
import zio.{ Has, ZEnv, ZLayer }
import zio.blocking.Blocking
import zio.sql.JdbcRunnableSpec
import zio.sql.TestContainer
import zio.test.environment.TestEnvironment

trait OracleRunnableSpec extends JdbcRunnableSpec with OracleModule {

  private def connProperties(user: String, password: String): Properties = {
    val props = new Properties
    props.setProperty("user", user)
    props.setProperty("password", password)
    props.setProperty("oracle.jdbc.timezoneAsRegion","false")
    props
  }

  private val executorLayer = {
    val poolConfigLayer = TestContainer
      .oracle(DockerImageName.parse("oracleinanutshell/oracle-xe-11g"))
      .map(a => Has(ConnectionPool.Config(a.get.jdbcUrl, connProperties(a.get.username, a.get.password))))

    val connectionPoolLayer = ZLayer.identity[Blocking] >+> poolConfigLayer >>> ConnectionPool.live

    (ZLayer.identity[
      Blocking
    ] ++ connectionPoolLayer >+> ReadExecutor.live >+> UpdateExecutor.live >+> DeleteExecutor.live >+> TransactionExecutor.live).orDie
  }

  override val jdbcTestEnvironment: ZLayer[ZEnv, Nothing, Environment] =
    TestEnvironment.live >+> executorLayer

}
