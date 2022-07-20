name := "kafka-pubsub"
organization := "de.th-koeln.inf.adv"
version := "0.1"
scalaVersion := "2.13.8"

resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

val kafkaVersion = "3.1.0"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.apache.kafka" % "kafka-streams" % kafkaVersion,
  "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion
)

publishTo := Some(
  "GitHub <THK-ADV> Apache Maven Packages" at "https://maven.pkg.github.com/THK-ADV/kafka-pubsub"
)

publishConfiguration := publishConfiguration.value.withOverwrite(true)

publishMavenStyle := true

credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "THK-ADV",
  ""
)
