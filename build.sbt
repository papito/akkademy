organization := "renegade-otter"
name := "akkademy"
version := "2.0.0"
scalaVersion := "2.13.15"
val pekkoVersion = "1.1.2"

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor-typed"         % pekkoVersion,
  "org.apache.pekko" %% "pekko-persistence-testkit" % pekkoVersion % Test,
  "org.scalatest"    %% "scalatest" % "3.2.19"      % Test,

  "ch.qos.logback"   % "logback-classic"            % "1.5.12",
  "org.slf4j"        % "slf4j-api"                  % "2.0.16",
)

scalacOptions := Seq(
  "-deprecation",
  "-language:postfixOps",
  "-opt:l:method",
  "-feature",
  "-Wunused:imports",
)


inThisBuild(
  List(
    scalaVersion := scalaVersion.value,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
  )
)

commands += Command.command("testFocused") { state =>
  "testOnly -- -n focused" :: state
}
