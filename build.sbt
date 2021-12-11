import sbt.ExclusionRule
import sbt.Keys.scalaSource

organization := "com.modaoperandi"
name         := "sc-state-machine"
scalaVersion := "2.13.7"

lazy val commonSettings = Seq(
  ThisBuild / turbo           := true,
  organization                := "com.modaoperandi",
  Compile / scalaSource       := baseDirectory.value / "app",
  Compile / resourceDirectory := baseDirectory.value / "conf",
  Test / scalaSource          := baseDirectory.value / "test",
  Test / resourceDirectory    := baseDirectory.value / "test_resources",
  Test / parallelExecution    := false,
  Test / fork                 := true,
  resolvers                   += Resolver.bintrayRepo("ovotech", "maven"),
  publishMavenStyle           := true,
  pomIncludeRepository        := (_ => false),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",                         // source files are in UTF-8
    "-deprecation",                  // warn about use of deprecated APIs
    "-unchecked",                    // warn about unchecked type parameters
    "-feature",                      // warn about misused language features
    "-language:higherKinds",         // allow higher kinded types without `import scala.language.higherKinds`
    "-language:implicitConversions", // allow use of implicit conversions
    "-language:postfixOps",          // enable postfix operators
    "-Xlint",                        // enable handy linter warnings
    "-Xfatal-warnings",              // turn compiler warnings into errors
    "-Ywarn-macros:after"            // allows the compiler to resolve implicit imports being flagged as unused
  )
)

lazy val root = (project in file("."))
  .aggregate(`state-machine`, examples)
  .settings(
    publishArtifact := false
  )

lazy val `state-machine` =
  (project in file("state-machine"))
    .settings(
      commonSettings,
      Dependencies.TestLib,
      Dependencies.Logging,
      addCommandAlias("format", ";scalafmt;test:scalafmt;scalafmtSbt")
    )

lazy val examples =
  (project in file("examples"))
    .settings(
      commonSettings,
      Dependencies.TestLib,
      Dependencies.Logging,
      Dependencies.FS2,
      addCommandAlias("format", ";scalafmt;test:scalafmt;scalafmtSbt")
    )
    .settings(
      publishArtifact := false
    )
    .dependsOn(`state-machine`)

inThisBuild(
  List(
    licenses := Seq(
      "MIT" -> url("https://raw.githubusercontent.com/laserdisc-io/fs2-aws/master/LICENSE")
    ),
    homepage := Some(url("https://github.com/modaoperandi/sc-state-machine/")),
    developers := List(
      Developer("semenodm", "Dmytro Semenov", "", url("https://github.com/semenodm"))
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/modaoperandi/sc-state-machine/tree/master"),
        "git@github.com:ModaOperandi/sc-state-machine.git",
        "git@github.com:ModaOperandi/sc-state-machine.git"
      )
    ),
    Test / publishArtifact          := true,
    pgpPublicRing                   := file(".circleci/local.pubring.asc"),
    pgpSecretRing                   := file(".circleci/local.secring.asc"),
    releaseEarlyWith                := SonatypePublisher,
    releaseEarlyEnableLocalReleases := true
  )
)
