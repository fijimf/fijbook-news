val Http4sVersion = "0.20.9"
val CirceVersion = "0.11.1"
val DoobieVersion = "0.7.0"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"

lazy val root = (project in file("."))
  .settings(
    organization := "com.fijimf.deepfij",
    name := "fijbook-news",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "io.circe"        %% "circe-generic"       % CirceVersion,
      "org.tpolecat"    %% "doobie-core"         % DoobieVersion,
      "org.tpolecat"    %% "doobie-postgres"     % DoobieVersion,
      "org.tpolecat"    %% "doobie-specs2"       % DoobieVersion,
      "org.tpolecat"    %% "doobie-scalatest"    % DoobieVersion,
      "org.specs2"      %% "specs2-core"         % Specs2Version % "test",
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.0")
  )
wartremoverWarnings ++= Warts.allBut(Warts.unsafe:_*)
wartremoverErrors ++= Warts.unsafe
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings",
)
