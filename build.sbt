organization in ThisBuild := "be.yannickdeturck"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.6"

val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "4.0.0"
val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.1" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
val cassandraDriverExtras = "com.datastax.cassandra" % "cassandra-driver-extras" % "3.5.0" // Adds extra codecs
val webjarsFoundation = "org.webjars" % "foundation" % "6.4.3"
val webjarsFoundationIconFonts = "org.webjars" % "foundation-icon-fonts" % "d596a3cfb3"

lazy val `lagom-shop-scala` = (project in file("."))
  .aggregate(itemApi, itemImpl, orderApi, orderImpl, frontend)
  .settings(commonSettings: _*)

lazy val common = (project in file("common"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslServer % Optional,
      playJsonDerivedCodecs,
      scalaTest
    )
  )

lazy val itemApi = (project in file("item-api"))
  .dependsOn(common)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs
    )
  )

lazy val itemImpl = (project in file("item-impl"))
  .dependsOn(itemApi)
  .settings(commonSettings: _*)
  .enablePlugins(LagomScala, SbtReactiveAppPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      lagomScaladslKafkaBroker,
      lagomScaladslPubSub,
      cassandraDriverExtras,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)

lazy val orderApi = (project in file("order-api"))
  .dependsOn(common)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs
    )
  )

lazy val orderImpl = (project in file("order-impl"))
  .dependsOn(orderApi, itemApi)
  .settings(commonSettings: _*)
  .enablePlugins(LagomScala, SbtReactiveAppPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      lagomScaladslKafkaBroker,
      cassandraDriverExtras,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)

lazy val frontend = (project in file("frontend"))
  .dependsOn(itemApi, orderApi)
  .settings(commonSettings: _*)
  .enablePlugins(PlayScala && LagomPlay, SbtReactiveAppPlugin)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomScaladslServer,
      lagomScaladslKafkaClient,
      macwire,
      scalaTest,
      webjarsFoundation,
      webjarsFoundationIconFonts
    ),
    httpIngressPaths := Seq("/")
  )

def commonSettings: Seq[Setting[_]] = Seq(
)

lagomCassandraCleanOnStart in ThisBuild := true
