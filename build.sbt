// *****************************************************************************
// Build settings
// *****************************************************************************

inThisBuild(
  Seq(
    organization     := "example.com",
    organizationName := "ksilin",
    startYear        := Some(2022),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    // needs to be 2.12 as greyhound has not been built for 2.13 yet
    // A needed class was not found. This could be due to an error in your runpath. Missing class: scala/Serializable
    //java.lang.NoClassDefFoundError: scala/Serializable
    //	at java.base/java.lang.ClassLoader.defineClass1(Native Method)
    //	at java.base/java.lang.ClassLoader.defineClass(ClassLoader.java:1012)
    //	at java.base/java.security.SecureClassLoader.defineClass(SecureClassLoader.java:150)
    //	at java.base/jdk.internal.loader.BuiltinClassLoader.defineClass(BuiltinClassLoader.java:862)
    //	at java.base/jdk.internal.loader.BuiltinClassLoader.findClassOnClassPathOrNull(BuiltinClassLoader.java:760)
    //	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClassOrNull(BuiltinClassLoader.java:681)
    //	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:639)
    //	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188)
    //	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:520)
    //	at com.example.GreyhoundFutureInitTest.$anonfun$new$1(GreyhoundFutureInitTest.scala:35)
    scalaVersion := "2.12.15",
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-Ywarn-unused:imports",
    ),
    scalafmtOnCompile := true,
    dynverSeparator   := "_", // the default `+` is not compatible with docker tags
  )
)

resolvers ++= Seq(
  "confluent" at "https://packages.confluent.io/maven",
  "jitpack" at "https://jitpack.io",
)

// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `greyhound-init` =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.greyhound,
        library.greyhoundTestkit,
        library.config,
        library.airframeLog,
        library.scalatest,
        library.logback,
        library.cpTestcontainers,
      ),
    )

// *****************************************************************************
// Project settings
// *****************************************************************************

lazy val commonSettings =
  Seq(
    // Also (automatically) format build definition together with sources
    Compile / scalafmt := {
      val _ = (Compile / scalafmtSbt).value
      (Compile / scalafmt).value
    },
  )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val greyhound = "0.2.0"
      val config      = "1.4.1"
      val scopt       = "4.0.1"
      val airframeLog = "22.2.0"
      val logback     = "1.2.11"
      val scalatest   = "3.2.11"
      val cpTestcontainers = "0.2.1"
    }
    val greyhound   = "com.wix"             %% "greyhound-future" % Version.greyhound
    val greyhoundTestkit   = "com.wix" %% "greyhound-testkit" % Version.greyhound
    val config      = "com.typesafe"        % "config"                % Version.config
    val airframeLog = "org.wvlet.airframe" %% "airframe-log"          % Version.airframeLog
    val logback     = "ch.qos.logback"      % "logback-classic"       % Version.logback
    val scalatest   = "org.scalatest"      %% "scalatest"             % Version.scalatest
    val cpTestcontainers = "com.github.christophschubert" % "cp-testcontainers" % Version.cpTestcontainers
  }
