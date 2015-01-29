import NativePackagerKeys._

organization := "me.lessis"

name := "jb"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "me.lessis" %% "fixie-grips-json4s" % "0.1.0",
  "org.json4s" %% "json4s-native" % "3.2.11"
)

proguardSettings

ProguardKeys.merge in Proguard := true

ProguardKeys.proguardVersion in Proguard := "5.0"

ProguardKeys.outputs in Proguard :=
  (ProguardKeys.proguardDirectory in Proguard).value /
   (name.value + "-" + version.value + ".jar") :: Nil

ProguardKeys.options in Proguard ++= Seq(
  "-keep class jb.* { *; }",
  "-keep class fixiegrips.* { *; }",
  "-keep class org.json4s.* { *; }",
  "-dontnote",
  "-dontwarn",
  "-dontobfuscate",
  "-dontoptimize",
  ProguardOptions.keepMain(
    (mainClass in Compile).value
    .getOrElse("jb.Main"))
)

ProguardKeys.mergeStrategies in Proguard ++= Seq(
  ProguardMerge.discard("META-INF/*".r),
  ProguardMerge.discard("rootdoc.txt")
)

javaOptions in (Proguard, ProguardKeys.proguard) := Seq("-Xmx2G")

packageArchetype.java_application

// http://www.scala-sbt.org/sbt-native-packager/formats/universal.html

scriptClasspath := (ProguardKeys.outputs in Proguard).value.map(_.getName)

(stage in Universal) <<= (stage in Universal).dependsOn(ProguardKeys.proguard in Proguard)

mappings in Universal := {
  val univ = (mappings in Universal).value.filter {
    case (_, name) => ! name.endsWith(".jar")
  }
  val pro = (ProguardKeys.outputs in Proguard).value.map {
    f => (f, "lib/" + f.getName)
  }
  univ ++ pro
}
