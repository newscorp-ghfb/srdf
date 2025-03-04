lazy val scala212 = "2.12.13"
lazy val scala213 = "2.13.5"
lazy val scala3 = "3.0.0-RC2"
lazy val supportedScalaVersions = List(
  scala213, 
  scala212, 
  // scala3
)

val Java11 = "adopt@1.11"  

lazy val utilsVersion         = "0.1.85"

// Dependency versions
lazy val catsVersion           = "2.5.0"
lazy val catsEffectVersion     = "3.0.2"
lazy val circeVersion          = "0.14.0-M5"
lazy val declineVersion        = "2.0.0-RC1"
lazy val fs2Version            = "3.0.1"
lazy val http4sVersion         = "1.0.0-M21"
lazy val jenaVersion           = "3.16.0"
lazy val munitVersion          = "0.7.23"
lazy val munitEffectVersion    = "1.0.1"

lazy val rdf4jVersion          = "3.4.2"
lazy val scalacheckVersion     = "1.14.0"
// lazy val typesafeConfigVersion = "1.4.0"

// Compiler plugin dependency versions
lazy val simulacrumVersion       = "1.0.0"
lazy val scalaMacrosVersion      = "2.1.1"
lazy val scalaCollCompatVersion  = "2.4.3"

// Dependency modules

lazy val utils             = "es.weso"                    %% "utils"               % utilsVersion

lazy val catsCore          = "org.typelevel"              %% "cats-core"           % catsVersion
lazy val catsKernel        = "org.typelevel"              %% "cats-kernel"         % catsVersion
lazy val catsEffect        = "org.typelevel"              %% "cats-effect"         % catsEffectVersion
lazy val circeCore         = "io.circe"                   %% "circe-core"          % circeVersion
lazy val circeGeneric      = "io.circe"                   %% "circe-generic"       % circeVersion
lazy val circeParser       = "io.circe"                   %% "circe-parser"        % circeVersion
lazy val decline           = "com.monovore"               %% "decline"             % declineVersion
lazy val declineEffect     = "com.monovore"               %% "decline-effect"      % declineVersion 
lazy val fs2Core           = "co.fs2"                     %% "fs2-core"            % fs2Version
lazy val http4sEmberClient = "org.http4s"                 %% "http4s-ember-client" % http4sVersion
lazy val jenaArq           = "org.apache.jena"            % "jena-arq"             % jenaVersion
lazy val jenaFuseki        = "org.apache.jena"            % "jena-fuseki-main"     % jenaVersion
lazy val munit             = "org.scalameta"              %% "munit"               % munitVersion
lazy val munitEffects      = "org.typelevel"              %% "munit-cats-effect-3" % munitEffectVersion
lazy val rdf4j_runtime     = "org.eclipse.rdf4j"          % "rdf4j-runtime"        % rdf4jVersion
lazy val scalacheck        = "org.scalacheck"             %% "scalacheck"          % scalacheckVersion
lazy val scalaCollCompat   = "org.scala-lang.modules"     %% "scala-collection-compat" % scalaCollCompatVersion
// lazy val typesafeConfig    = "com.typesafe"               % "config"               % typesafeConfigVersion


ThisBuild / githubWorkflowJavaVersions := Seq(Java11)


def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _                              => false
  }

lazy val srdfMain = project
  .in(file("."))
  .settings(
    commonSettings, 
    publishSettings
  )
  .aggregate(srdfJena, srdf4j, srdf, docs)
  .dependsOn(srdfJena)
  .settings(
    libraryDependencies ++= Seq(
      decline, declineEffect
    ),
    publish / skip := true,
    ThisBuild / turbo := true
  )

lazy val srdf = project
  .in(file("modules/srdf"))
  .settings(commonSettings, publishSettings)
  .settings(
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      catsCore,
      catsKernel,
      // catsMacros,
      catsEffect,
      circeCore,
      circeGeneric,
      circeParser,
      fs2Core,
      utils,
//      scalaLogging,
//      scalaCollCompat,
    )
    )
    
  lazy val srdfJena = project
  .in(file("modules/srdfJena"))
  .dependsOn(srdf)
  .settings(commonSettings, publishSettings)
  .settings(
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
//      logbackClassic % Test,
//      scalaLogging,
      jenaFuseki % Test,
//      typesafeConfig % Test,
      utils,
      jenaArq,
      catsCore,
      catsKernel,
      catsEffect,
      fs2Core,
      http4sEmberClient
    ),
  )

lazy val srdf4j = project
  .in(file("modules/srdf4j"))
  .dependsOn(srdf)
  .settings(commonSettings, publishSettings)
  .settings(
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      utils,
      rdf4j_runtime,
      catsCore,
      catsKernel,
      catsEffect,
      fs2Core,
      scalaCollCompat
    )
  )

lazy val docs = project   
  .in(file("srdf-docs")) 
  .settings(
    noPublishSettings,
    mdocSettings,
    ScalaUnidoc / unidoc / unidocProjectFilter := inAnyProject -- inProjects(noDocProjects: _*)
   )
  .dependsOn(
    srdf, 
    srdfJena, 
//    srdf4j
    )
  .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)

lazy val mdocSettings = Seq(
  mdocVariables := Map(
    "VERSION" -> version.value
  ),
  ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(srdf, srdfJena, srdf4j),
  ScalaUnidoc / unidoc / target := (LocalRootProject / baseDirectory).value / "website" / "static" / "api",
  cleanFiles += (ScalaUnidoc / unidoc / target).value,
  docusaurusCreateSite := docusaurusCreateSite
    .dependsOn(Compile / unidoc)
    .value,
  docusaurusPublishGhpages :=
    docusaurusPublishGhpages
      .dependsOn(Compile / unidoc)
      .value,
  ScalaUnidoc / unidoc / scalacOptions ++= Seq(
    "-doc-source-url", s"https://github.com/weso/srdf/tree/v${(ThisBuild / version).value}€{FILE_PATH}.scala",
    "-sourcepath", (LocalRootProject / baseDirectory).value.getAbsolutePath,
    "-doc-title", "SRDF",
    "-doc-version", s"v${(ThisBuild / version).value}"
  )
)

lazy val noPublishSettings = publish / skip := true

/* ********************************************************
 ******************** Grouped Settings ********************
 **********************************************************/

lazy val noDocProjects = Seq[ProjectReference](
)

lazy val sharedDependencies = Seq(
  libraryDependencies ++= Seq(
    munit % Test,
    munitEffects % Test
  ),
  testFrameworks += new TestFramework("munit.Framework")
)

lazy val compilationSettings = Seq(
  // format: off
  scalacOptions ++= Seq(
    "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
    "-encoding", "utf-8",                // Specify character encoding used by source files.
    "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.  "-encoding", "UTF-8",
    "-language:_",
    "-Xlint"
  )
  // format: on
)

lazy val commonSettings = compilationSettings ++ sharedDependencies ++ Seq(
  organization := "es.weso",
  resolvers ++= Seq(Resolver.githubPackages("weso")), 
  coverageHighlighting := priorTo2_13(scalaVersion.value),
  githubOwner := "weso",
  githubRepository := "srdf"
 )

lazy val publishSettings = Seq(
  homepage        := Some(url("https://github.com/weso/srdf")),
  licenses        := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  scmInfo         := Some(ScmInfo(url("https://github.com/weso/srdf"), "scm:git:git@github.com:weso/srdf.git")),
  autoAPIMappings := true,
  pomExtra        := <developers>
                       <developer>
                         <id>labra</id>
                         <name>Jose Emilio Labra Gayo</name>
                         <url>https://github.com/labra/</url>
                       </developer>
                     </developers>,
  publishMavenStyle              := true,
)
