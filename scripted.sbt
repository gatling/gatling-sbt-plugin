scriptedSettings

scriptedLaunchOpts ++= Seq(
  "-Xmx512m",
  "-XX:MaxPermSize=256m",
  "-Dgatling.http.enableGA=false",
  "-Dplugin.version=" + version.value)

scriptedBufferLog   := true