LTTng Scope
===========

LTTng Scope is a trace viewer and analyzer for [CTF](http://diamon.org/ctf/)
traces, with a focus on [LTTng](https://lttng.org/) kernel and userspace traces.
It supports Windows, Max OSX and Linux. The source code is available under the
[Eclipse Public License](https://www.eclipse.org/legal/epl-v10.html).


Compiling manually
------------------

Building the project requires Maven version 3.3 or later. It can be downloaded
from <http://maven.apache.org> or from the package management system of your
distribution.

To build, simply run the following command from the top-level directory:

    mvn clean install

Tests are skipped by default. To run the unit tests you can append
`-Dmaven.test.skip=false` to the `mvn` command:

    mvn clean install -Dmaven.test.skip=false

The platform-specific application packages will be placed in
`releng/org.lttng.scope.rcp.product/target/products`.


Maven profiles and properties
-----------------------------

The following Maven profiles and properties are defined in the build system.
You can set them by using `-P[profile name]` and `-D[property name]=[value]`
in `mvn` commands.

* `-Dtarget-platform=[target]`

  Defines which target to use. This is used to build against various versions of
  the Eclipse platform. Available ones are in
  `releng/org.lttng.scope.target`. The default is usually the latest
  stable platform.

