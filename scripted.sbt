scriptedSettings

scriptedLaunchOpts ++= Seq(
  "-Xmx512m",
  "-Dgatling.http.enableGA=false",
  "-Dplugin.version=" + version.value)

scriptedBufferLog   := true