import BuildHelper._
import InfrastructureHelper._
import explicitdeps.ExplicitDepsPlugin.autoImport.moduleFilterRemoveValue
import sbtcrossproject.CrossPlugin.autoImport.crossProject

inThisBuild(
  List(
    organization := "dev.zio",
    homepage := Some(url("https://zio.github.io/zio-sql/")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer("jdegoes", "John De Goes", "john@degoes.net", url("http://degoes.net"))
    ),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc"),
    scmInfo := Some(
      ScmInfo(url("https://github.com/zio/zio-sql/"), "scm:git:git@github.com:zio/zio-sql.git")
    )
  )
)

addCommandAlias("fmtOnce", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("fmt", "fmtOnce;fmtOnce")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

val zioVersion                 = "1.0.7"
val testcontainersVersion      = "1.15.3"
val testcontainersScalaVersion = "0.39.5"

lazy val startPostgres = taskKey[Unit]("Start up Postgres")
startPostgres := startService(Database.Postgres, streams.value)

lazy val stopPostgres = taskKey[Unit]("Shut down Postgres")
stopPostgres := stopService(Database.Postgres, streams.value)

lazy val startMySQL = taskKey[Unit]("Start up MySQL")
startMySQL := startService(Database.MySQL, streams.value)

lazy val stopMySQL = taskKey[Unit]("Shut down MySQL")
stopMySQL := stopService(Database.MySQL, streams.value)

lazy val startMsSQL = taskKey[Unit]("Start up Microsoft SQL Server")
startMsSQL := startService(Database.MSSQL, streams.value)

lazy val stopMsSQL = taskKey[Unit]("Shut down Microsoft SQL Server")
stopMsSQL := stopService(Database.MSSQL, streams.value)

lazy val startOracle = taskKey[Unit]("Start up Oracle")
startOracle := startService(Database.Oracle, streams.value)

lazy val stopOracle = taskKey[Unit]("Shut down Oracle")
stopOracle := stopService(Database.Oracle, streams.value)

lazy val root = project
  .in(file("."))
  .settings(
    publish / skip := true,
    unusedCompileDependenciesFilter -= moduleFilter("org.scala-js", "scalajs-library")
  )
  .aggregate(
    coreJVM,
    coreJS,
    docs,
    driver,
    examples,
    jdbc,
    mysql,
    oracle,
    postgres,
    sqlserver
  )

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .in(file("core"))
  .settings(stdSettings("zio-sql"))
  .settings(crossProjectSettings)
  .settings(buildInfoSettings("zio.sql"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % zioVersion,
      "dev.zio" %% "zio-streams"  % zioVersion,
      "dev.zio" %% "zio-test"     % zioVersion % "test",
      "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
    )
  )
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))

lazy val coreJS = core.js
  .settings(scalaJSUseMainModuleInitializer := true)

lazy val coreJVM = core.jvm
  .settings(dottySettings)

lazy val docs = project
  .in(file("zio-sql-docs"))
  .settings(
    publish / skip := true,
    moduleName := "zio-sql-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion
    )
  )
  .dependsOn(coreJVM)
  .enablePlugins(MdocPlugin, DocusaurusPlugin)

lazy val examples = project
  .in(file("examples"))
  .settings(stdSettings("examples"))
  .settings(
    publish / skip := true,
    moduleName := "examples"
  )
  .settings(dottySettings)
  .dependsOn(sqlserver)

lazy val driver = project
  .in(file("driver"))
  .settings(stdSettings("zio-sql-driver"))
  .settings(buildInfoSettings("zio.sql.driver"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % zioVersion,
      "dev.zio" %% "zio-test"     % zioVersion % "test",
      "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
    )
  )
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
  .settings(dottySettings)

lazy val jdbc = project
  .in(file("jdbc"))
  .settings(stdSettings("zio-sql-jdbc"))
  .settings(buildInfoSettings("zio.sql.jdbc"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % zioVersion,
      "dev.zio" %% "zio-streams"  % zioVersion,
      "dev.zio" %% "zio-test"     % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    )
  )
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
  .settings(dottySettings)
  .dependsOn(core.jvm)

lazy val mysql = project
  .in(file("mysql"))
  .dependsOn(jdbc % "compile->compile;test->test")
  .settings(stdSettings("zio-sql-mysql"))
  .settings(buildInfoSettings("zio.sql.mysql"))
  .settings(
    libraryDependencies ++= Seq(
      "org.testcontainers" % "testcontainers"             % testcontainersVersion      % Test,
      "org.testcontainers" % "database-commons"           % testcontainersVersion      % Test,
      "org.testcontainers" % "jdbc"                       % testcontainersVersion      % Test,
      "org.testcontainers" % "mysql"                      % testcontainersVersion      % Test,
      "mysql"              % "mysql-connector-java"       % "8.0.25"                   % Test,
      "com.dimafeng"      %% "testcontainers-scala-mysql" % testcontainersScalaVersion % Test
    )
  )
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
  .settings(dottySettings)

lazy val oracle = project
  .in(file("oracle"))
  .dependsOn(jdbc % "compile->compile;test->test")
  .settings(stdSettings("zio-sql-oracle"))
  .settings(buildInfoSettings("zio.sql.oracle"))
  .settings(
    libraryDependencies ++= Seq(
      "org.testcontainers"       % "testcontainers"                      % testcontainersVersion      % Test,
      "org.testcontainers"       % "database-commons"                    % testcontainersVersion      % Test,
      "org.testcontainers"       % "oracle-xe"                           % testcontainersVersion      % Test,
      "org.testcontainers"       % "jdbc"                                % testcontainersVersion      % Test,
      "com.oracle.database.jdbc" % "ojdbc8"                              % "21.1.0.0"                 % Test,
      "com.dimafeng"             % "testcontainers-scala-oracle-xe_2.13" % testcontainersScalaVersion % Test
    )
  )
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
  .settings(dottySettings)

lazy val postgres = project
  .in(file("postgres"))
  .dependsOn(jdbc % "compile->compile;test->test")
  .settings(stdSettings("zio-sql-postgres"))
  .settings(buildInfoSettings("zio.sql.postgres"))
  .settings(
    libraryDependencies ++= Seq(
      "org.testcontainers" % "testcontainers"                  % testcontainersVersion      % Test,
      "org.testcontainers" % "database-commons"                % testcontainersVersion      % Test,
      "org.testcontainers" % "postgresql"                      % testcontainersVersion      % Test,
      "org.testcontainers" % "jdbc"                            % testcontainersVersion      % Test,
      "org.postgresql"     % "postgresql"                      % "42.2.22"                  % Compile,
      "com.dimafeng"      %% "testcontainers-scala-postgresql" % testcontainersScalaVersion % Test
    )
  )
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
  .settings(dottySettings)

lazy val sqlserver = project
  .in(file("sqlserver"))
  .dependsOn(jdbc % "compile->compile;test->test")
  .settings(stdSettings("zio-sql-sqlserver"))
  .settings(buildInfoSettings("zio.sql.sqlserver"))
  .settings(
    libraryDependencies ++= Seq(
      "org.testcontainers" % "testcontainers"                    % testcontainersVersion      % Test,
      "org.testcontainers" % "database-commons"                  % testcontainersVersion      % Test,
      "org.testcontainers" % "mssqlserver"                       % testcontainersVersion      % Test,
      "org.testcontainers" % "jdbc"                              % testcontainersVersion      % Test,
      "com.microsoft.sqlserver" % "mssql-jdbc" % "9.2.1.jre11" % Test,
      "com.dimafeng"       %% "testcontainers-scala-mssqlserver" % testcontainersScalaVersion % Test
    )
  )
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
  .settings(dottySettings)
