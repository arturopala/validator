ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

addSbtPlugin("org.scalameta"     % "sbt-scalafmt"  % "2.5.2")
addSbtPlugin("org.scoverage"     % "sbt-scoverage" % "2.0.9")
addSbtPlugin("de.heikoseeberger" % "sbt-header"    % "5.10.0")
addSbtPlugin("ch.epfl.scala"     % "sbt-scalafix"  % "0.11.1")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.12")

addSbtPlugin("com.typesafe.sbt" % "sbt-site"         % "1.4.1")
addSbtPlugin("io.kevinlee"      % "sbt-github-pages" % "0.13.0")

addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.5.1")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.3.2")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "1.14.0")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.4.16")
