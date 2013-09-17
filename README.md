# Scaloid Layout Converter

This program converts an Android XML layout into a [Scaloid](http://scaloid.org) layout.

### Try this!

http://layout.scaloid.org

## Project layout

This project has a standard Maven/[SBT](http://scala-sbt.org) web application directory structure.
We leverages [Spray](http://spray.io) HTTP toolkit and [Scala language](http://scala-lang.org).

To run on a local machine:
```
$ sbt container:start shell
```

To make a `.war` file:
```
$ sbt package
```

For layout converter, check out the class `org.scaloid.layout.Converter`.

## Let's make it together!

This project is in early stages, and I will grow it constantly. If you have any idea to improve Scaloid, feel free to open issues or post patches.

### License

This software is licensed under the [Apache 2 license](http://www.apache.org/licenses/LICENSE-2.0.html).
