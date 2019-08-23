import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

val buildNumber = sys.env.get("BUILD_NUMBER").getOrElse("0-SNAPSHOT")

organization := "com.atscale.engine.akka-zk"
name := "akka-zk-cluster-seed"
version := s"0.1.11.${buildNumber}"

scalaVersion := "2.13.0"
crossScalaVersions := Seq(scalaVersion.value, "2.11.12", "2.12.8")

val akkaVersion = "2.5.25"
val akkaHttpVersion = "10.1.9"

val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
)

val exhibitorOptionalDependencies = Seq(
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "org.slf4j" % "log4j-over-slf4j" % "1.7.7",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
).map(_ % Provided)

val curatorVersion = "2.12.0.3"

val zkDependencies = Seq(
  "curator-framework",
  "curator-recipes"
).map {
  "com.atscale.engine.curator" % _ % curatorVersion exclude("log4j", "log4j") exclude("org.slf4j", "slf4j-log4j12")
}


val testDependencies = Seq(
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.8",
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.slf4j" % "log4j-over-slf4j" % "1.7.28",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.atscale.engine.curator" % "curator-test" % curatorVersion
).map(_ % Test)

val asReleases = "Artifactory Realm" at "http://artifactory.infra.atscale.com/release-local"

lazy val rootProject = (project in file(".")).
  enablePlugins(MultiJvmPlugin).
  configs(MultiJvm).
  settings(
    publishTo := Some(asReleases),
    resolvers += asReleases,

    libraryDependencies ++= (akkaDependencies ++ exhibitorOptionalDependencies ++ zkDependencies ++ testDependencies),
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint", "-language:postfixOps"),
    javacOptions in Compile ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    parallelExecution in Test := false,

    pomExtra := (
      <url>http://github.com/sclasen/akka-zk-cluster-seed</url>
      <licenses>
        <license>
          <name>The Apache Software License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:sclasen/akka-zk-cluster-seed.git</url>
        <connection>scm:git:git@github.com:sclasen/akka-zk-cluster-seed.git</connection>
      </scm>
      <developers>
        <developer>
          <id>sclasen</id>
          <name>Scott Clasen</name>
          <url>http://github.com/sclasen</url>
        </developer>
      </developers>)
  ).
  settings(Defaults.itSettings:_*).
  settings(SbtMultiJvm.multiJvmSettings:_*).
  // settings(compile in MultiJvm := (compile in MultiJvm) triggeredBy (compile in IntegrationTest)).
  // settings(executeTests in IntegrationTest := (executeTests in Test, executeTests in MultiJvm) map {
  //   case (testResults, multiNodeResults)  =>
  //     val overall =
  //       if (testResults.overall.id < multiNodeResults.overall.id)
  //         multiNodeResults.overall
  //       else
  //         testResults.overall
  //     Tests.Output(overall,
  //       testResults.events ++ multiNodeResults.events,
  //       testResults.summaries ++ multiNodeResults.summaries)
  // }).
  configs(IntegrationTest, MultiJvm)
