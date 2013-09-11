gatling-sbt
===========

Overview
--------

**This Gatling SBT plugin for Gatling >2.x**

For earlier versions try

https://github.com/andypetrella/gatling-sbt-plugin

More inspiration how this tend to be used is here

http://ska-la.blogspot.com/2012/07/gatling-tool-in-sbt-or-play-sample.html

... even that it is for 1.3.x Gatling, still useful.

Usage
-----

Try out on `sample`.

From root run `sbt`
```
> project {sample/quick-start}
> perf:test
```

What is also nice you may try test-only
```
> perf:test-only perf.GooglePerf
```

Local Install and Debug
-----------------------

**todo: this should be updated, when passed through publication**

Before start ensure that you have sbt-launcher at least 0.13.0

For earlier versions you may get
```
java.lang.IncompatibleClassChangeError: Found class jline.Terminal, but interface was expected
```

From root run `sbt`

Publish to local maven `test-framework`
```
> {test-framework}gatling-sbt/publish
```

Publish to local maven `gatling-sbt-plugin`
```
> {plugin}gatling-sbt/publish
```

Now you are ready to `perf`

Follow #Usage section

Todo
----

Must have:
* implement a Test Framework (DONE)
* share classpath with application
* user friendly configuration: editable conf file -- no hardcoded properties
* start recorder
* how to start the application?
* forked execution
* easy publish for different sbt-s and scala-s

Nice to have:
* sample for play2 framework
* show performance information in real time
* open results in browser (integration with the OS)
* integration in Activator
