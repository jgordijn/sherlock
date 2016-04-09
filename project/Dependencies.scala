import sbt._

object Dependencies {
  val akkaVersion = "2.4.2"

  object Compile {
    val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
    val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % akkaVersion
    val akkaHttp = "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion
    val akkaSprayJson = "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion
    val akkaDistData = "com.typesafe.akka" %% "akka-distributed-data-experimental" % akkaVersion

    val metrics = "io.dropwizard.metrics" % "metrics-core" % "3.1.0"
    val all = Seq(akkaActor, akkaHttp, akkaHttp, akkaHttpCore, akkaSprayJson, akkaDistData, metrics)
  }
  object Test {
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
    val scalatest = "org.scalatest" %% "scalatest" % "2.2.1" % "test"
    val all = Seq(akkaTestkit, scalatest)
  }
}
