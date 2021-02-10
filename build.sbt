import Dependencies._

ThisBuild / scalaVersion     := "2.13.4"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.hubertp"

// Works without any problems for JDK 11 and JDK 8
val log4j2_7Version = "2.7"
// Generates image only using GraalVM 21.0.0 for JDK 11. JDK 8 crashes.
val log4j2_11_2Version = "2.11.2"
val log4j2Version = log4j2_11_2Version

lazy val root = (project in file("."))
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(
    name := "graalvm-sandbox",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.slf4j"                %  "slf4j-api"        % "1.7.30",
      "org.apache.logging.log4j" %  "log4j-slf4j-impl" % log4j2Version % Runtime,
      "org.apache.logging.log4j" %  "log4j-api"        % log4j2Version % Runtime,
      "org.apache.logging.log4j" %  "log4j-core"       % log4j2Version % Runtime,
    ),
    // Based on:
    // - https://github.com/oracle/graal/issues/2875
    // - https://github.com/oracle/graal/issues/808
    // - https://github.com/oracle/graal/issues/2008
    // - https://how-to.vertx.io/graal-native-image-howto/
    // - https://www.graalvm.org/reference-manual/native-image/Logging/
    graalVMNativeImageOptions ++= {
      val base = Seq("--allow-incomplete-classpath",
        s"-H:ReflectionConfigurationFiles=${baseDirectory.value}/config/reflect-config-${log4j2Version}.json",
        s"-H:ResourceConfigurationFiles=${baseDirectory.value}/config/resource-config-${log4j2Version}.json",
        "-H:Log=registerResource:"
      )
      val versionSpecific = log4j2Version match {
        case `log4j2_11_2Version` =>
          Seq("-H:ServiceLoaderFeatureExcludeServiceProviders=com.oracle.truffle.js.scriptengine.GraalJSEngineFactory",
              "--features=org.graalvm.home.HomeFinderFeature")
        case _ =>
          Seq.empty[String]
      }
      base ++ versionSpecific
    },

    fork in run := true,

    // Generate resource files based on the run:
    // https://www.graalvm.org/reference-manual/native-image/BuildConfiguration/#assisted-configuration-of-native-image-builds
    // It will contain some bugs/missing reflection entries that have to be amended by hand
    javaOptions in run ++= Seq("-agentlib:native-image-agent=config-output-dir=target/native-image-resources")
  )
