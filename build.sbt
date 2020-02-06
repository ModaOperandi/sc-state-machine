import sbt.ExclusionRule
import sbt.Keys.scalaSource

lazy val commonSettings = Seq(
  ThisBuild / turbo := true,
  scalaVersion := "2.12.10",
  sources in(Compile, doc) := Seq(),
  scalaSource in Compile := baseDirectory.value / "app",
  scalaSource in Test := baseDirectory.value / "test",
  resourceDirectory in Compile := baseDirectory.value / "conf",
  resourceDirectory in Test := baseDirectory.value / "test_resources",
  Test / parallelExecution := false,
  resolvers += Resolver.bintrayRepo("ovotech", "maven"),
  fork in Test := true,
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8", // source files are in UTF-8
    "-deprecation", // warn about use of deprecated APIs
    "-unchecked", // warn about unchecked type parameters
    "-feature", // warn about misused language features
    "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
    "-language:implicitConversions", // allow use of implicit conversions
    "-language:postfixOps",
    "-Xlint", // enable handy linter warnings
    "-Xfatal-warnings", // turn compiler warnings into errors
    "-Ypartial-unification", // allow the compiler to unify type constructors of different arities
    "-Ywarn-macros:after" // allows the compiler to resolve implicit imports being flagged as unused
  )
)

lazy val artifactorySettings = Seq(
  resolvers += "Artifactory" at "https://moda.jfrog.io/moda/sbt",
  credentials += {
    (sys.env.get("JFROG_USER"), sys.env.get("JFROG_PASS")) match {
      case (Some(user), Some(pass)) => Credentials("Artifactory Realm", "moda.jfrog.io", user, pass)
      case _ => Credentials(Path.userHome / "credentials.sbt")
    }
  }
)

lazy val `state-machine` =
  (project in file("state-machine"))
    .settings(
      commonSettings,
      artifactorySettings,
      Dependencies.TestLib,
      Dependencies.Logging,
      addCommandAlias("format", ";scalafmt;test:scalafmt;scalafmtSbt")
    )
    .enablePlugins(GitVersioning)

lazy val examples =
  (project in file("examples"))
    .settings(
      commonSettings,
      artifactorySettings,
      Dependencies.TestLib,
      Dependencies.Logging,
      addCommandAlias("format", ";scalafmt;test:scalafmt;scalafmtSbt")
    )
    .enablePlugins(GitVersioning)
    .dependsOn(`state-machine`)
