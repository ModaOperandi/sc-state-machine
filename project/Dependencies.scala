import sbt.Keys.libraryDependencies
import sbt._

object Dependencies {
  val TestLib = Seq(
    libraryDependencies ++= Seq(
      "org.scalatest"  %% "scalatest"               % "3.2.0"  % Test, // ApacheV2
      "org.scalamock"  %% "scalamock"               % "4.4.0"  % Test,
      "org.mockito"    % "mockito-core"             % "3.3.3"  % Test,
      "org.mockito"    %% "mockito-scala-scalatest" % "1.14.8" % Test,
      "org.mockito"    %% "mockito-scala-cats"      % "1.14.8" % Test,
      "org.scalacheck" %% "scalacheck"              % "1.14.3" % Test
    )
  )

  val Logging = Seq(
    libraryDependencies ++= Seq(
      "ch.qos.logback"    % "logback-classic" % "1.2.3", // logging
      "ch.qos.logback"    % "logback-core"    % "1.2.3", // logging
      "org.slf4j"         % "jcl-over-slf4j"  % "1.7.30",
      "org.slf4j"         % "jul-to-slf4j"    % "1.7.30",
      "org.typelevel" %% "log4cats-slf4j"   % "1.3.1",
      "org.typelevel" %% "log4cats-testing" % "1.3.1" % Test
    )
  )

  val FS2 = Seq(
    libraryDependencies ++= Seq("co.fs2" %% "fs2-core" % "2.4.2")
  )

  val upperbound = Seq(libraryDependencies += "org.systemfw" %% "upperbound" % "0.2.0-M1")

}
