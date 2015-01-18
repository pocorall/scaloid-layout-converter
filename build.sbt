name := "scaloid-converter"

description := "An Android layout converter from XML to Scaloid DSL"

version := "0.5"

scalaVersion := "2.11.5"

organization := "org.scaloid"

organizationHomepage := Some(new URL("http://blog.scaloid.org"))

scalacOptions ++= Seq("-feature", "-deprecation")


// Dependencies

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io",
  "spray nightlies" at "http://nightlies.spray.io"
)

val scaloidVersion = "3.6.1-10"
val sprayVersion = "1.3.2"
val akkaVersion = "2.3.8"

libraryDependencies ++= Seq(
  "org.scaloid" %% "scaloid" % scaloidVersion,
  "com.google.android" % "android" % "2.3.3" withSources,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
  "org.reflections" % "reflections" % "0.9.9",
  "org.apache.commons" % "commons-lang3" % "3.1",
  "io.spray" %% "spray-http" % sprayVersion,
  "io.spray" %% "spray-httpx" % sprayVersion,
  "io.spray" %% "spray-routing" % sprayVersion,
  "io.spray" %% "spray-servlet" % sprayVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.eclipse.jetty" % "jetty-webapp" % "9.2.6.v20141205" % "container",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" artifacts Artifact("javax.servlet", "jar", "jar"),
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "junit" % "junit" % "4.12" % "test"
)

seq(Revolver.settings: _*)

seq(webSettings: _*)

artifactPath in(Compile, packageWar) := {
  (target in Compile).value / "dist" / (artifactPath in(Compile, packageWar)).value.getName
}

fork in Test := true

initialCommands := Seq(
  "import org.scaloid.layout.converter._",
  "import ReflectionUtils._",
  "import StringUtils._"
).mkString("\n")
