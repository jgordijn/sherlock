name := "sherlock"
version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Dependencies.Compile.all
libraryDependencies ++= Dependencies.Test.all

enablePlugins(JavaAppPackaging)
