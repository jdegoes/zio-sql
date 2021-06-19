import sbt._
import sbt.Keys._

import explicitdeps.ExplicitDepsPlugin.autoImport._
import sbtcrossproject.CrossPlugin.autoImport._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbtbuildinfo._
import BuildInfoKeys._
import scalafix.sbt.ScalafixPlugin.autoImport.scalafixSemanticdb

object BuildHelper {
  val SilencerVersion = "1.7.5"
  val Scala212        = "2.12.14"
  val Scala213        = "2.13.6"
  val ScalaDotty      = "3.0.1-RC1"

  def buildInfoSettings(packageName: String) =
    Seq(
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, isSnapshot),
      buildInfoPackage := packageName,
      buildInfoObject := "BuildInfo"
    )

  private val stdOptions = Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings"
  )

  private val std2xOptions = Seq(
    "-language:higherKinds",
    "-language:existentials",
    "-explaintypes",
    "-Yrangepos",
    "-Xlint:_,-missing-interpolator,-type-parameter-shadow",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )

  private def propertyFlag(property: String, default: Boolean) =
    sys.props.get(property).map(_.toBoolean).getOrElse(default)

  private def optimizerOptions(optimize: Boolean) =
    if (optimize)
      Seq(
        "-opt:l:inline",
        "-opt-inline-from:zio.internal.**"
      )
    else Nil

  def extraOptions(scalaVersion: String, optimize: Boolean) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((0, _))  =>
        Seq(
          "-language:implicitConversions",
          "-Xignore-scala2-macros"
        )
      case Some((2, 13)) =>
        Seq(
          "-Ywarn-unused:params,-implicits"
        ) ++ std2xOptions ++ optimizerOptions(optimize)
      case Some((2, 12)) =>
        Seq(
          "-opt-warnings",
          "-Ywarn-extra-implicit",
          "-Ywarn-unused:_,imports",
          "-Ywarn-unused:imports",
          "-Ypartial-unification",
          "-Yno-adapted-args",
          "-Ywarn-inaccessible",
          "-Ywarn-infer-any",
          "-Ywarn-nullary-override",
          "-Ywarn-nullary-unit",
          "-Ywarn-unused:params,-implicits",
          "-Xfuture",
          "-Xsource:2.13",
          "-Xmax-classfile-name",
          "242"
        ) ++ std2xOptions ++ optimizerOptions(optimize)
      case _             => Seq.empty
    }

  def platformSpecificSources(platform: String, conf: String, baseDirectory: File)(versions: String*) =
    List("scala" :: versions.toList.map("scala-" + _): _*).map { version =>
      baseDirectory.getParentFile / platform.toLowerCase / "src" / conf / version
    }.filter(_.exists)

  def crossPlatformSources(scalaVer: String, platform: String, conf: String, baseDir: File) = {
    val versions = CrossVersion.partialVersion(scalaVer) match {
      case Some((2, 12)) =>
        List("2.12", "2.11+", "2.12+", "2.11-2.12", "2.12-2.13", "2.x")
      case Some((2, 13)) =>
        List("2.13", "2.11+", "2.12+", "2.13+", "2.12-2.13", "2.x")
      case Some((3, 0))  =>
        List("dotty", "2.11+", "2.12+", "2.13+", "3.x")
      case _             =>
        Nil
    }
    platformSpecificSources(platform, conf, baseDir)(versions: _*)
  }

  val dottySettings = Seq(
    crossScalaVersions += ScalaDotty,
    scalacOptions ++= {
      if (scalaVersion.value == ScalaDotty)
        Seq("-noindent")
      else
        Seq()
    },
    Compile / doc / sources := {
      val old = (Compile / doc / sources).value
      if (scalaVersion.value == ScalaDotty) {
        Nil
      } else {
        old
      }
    },
    Test / parallelExecution := {
      val old = (Test / parallelExecution).value
      if (scalaVersion.value == ScalaDotty) {
        false
      } else {
        old
      }
    }
  )

  val scalaReflectSettings = Seq(
    libraryDependencies ++=
      (if (scalaVersion.value == ScalaDotty) Seq()
       else
         Seq(
           "dev.zio" %%% "izumi-reflect" % "1.1.0"
         ))
  )

  lazy val crossProjectSettings = Seq(
    Compile / unmanagedSourceDirectories ++= {
      val platform = crossProjectPlatform.value.identifier
      val baseDir  = baseDirectory.value
      val scalaVer = scalaVersion.value

      crossPlatformSources(scalaVer, platform, "main", baseDir)
    },
    Test / unmanagedSourceDirectories ++= {
      val platform = crossProjectPlatform.value.identifier
      val baseDir  = baseDirectory.value
      val scalaVer = scalaVersion.value

      crossPlatformSources(scalaVer, platform, "test", baseDir)
    }
  )

  def stdSettings(prjName: String) = Seq(
    name := s"$prjName",
    scalacOptions := stdOptions,
    crossScalaVersions := Seq(Scala213, Scala212),
    ThisBuild / scalaVersion := Scala213, //ScalaDotty,
    scalacOptions := stdOptions ++ extraOptions(scalaVersion.value, optimize = !isSnapshot.value),
    libraryDependencies ++= {
      if (scalaVersion.value == ScalaDotty)
        Seq(
          "com.github.ghik"                 % s"silencer-lib_2.13.6" % "1.7.5"         % Provided
        )
      else
        Seq(
          ("com.github.ghik"                % "silencer-lib"         % SilencerVersion % Provided).cross(CrossVersion.full),
          compilerPlugin(("com.github.ghik" % "silencer-plugin"      % SilencerVersion).cross(CrossVersion.full))
        )
    },
    Test / parallelExecution := true,
    incOptions ~= (_.withLogRecompileOnMacro(false)),
    autoAPIMappings := true,
    unusedCompileDependenciesFilter -= moduleFilter("org.scala-js", "scalajs-library")
  )

  def macroExpansionSettings = Seq(
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) => Seq("-Ymacro-annotations")
        case _             => Seq.empty
      }
    },
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, x)) if x <= 12 =>
          Seq(compilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full)))
        case _                       => Seq.empty
      }
    }
  )

  def macroDefinitionSettings = Seq(
    scalacOptions += "-language:experimental.macros",
    libraryDependencies ++= {
      if (scalaVersion.value == ScalaDotty) Seq()
      else
        Seq(
          "org.scala-lang" % "scala-reflect"  % scalaVersion.value % "provided",
          "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
        )
    }
  )

  implicit class ModuleHelper(p: Project) {
    def module: Project = p.in(file(p.id)).settings(stdSettings(p.id))
  }
}
