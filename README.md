LTTng Scope (Alpha Version)
===========================

[![Build Status](https://travis-ci.org/lttng/lttng-scope.svg?branch=master)](https://travis-ci.org/lttng/lttng-scope)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/12046/badge.svg)](https://scan.coverity.com/projects/lttng-scope)

**LTTng Scope** is a trace viewer and analyzer for
[CTF](http://diamon.org/ctf/) traces, with a focus on
[LTTng](https://lttng.org/) kernel and user space traces.

LTTng Scope supports Windows, macOS, and Linux. The source code is
available under the
[Eclipse Public License](https://www.eclipse.org/legal/epl-v10.html).

> NOTE: LTTng Scope is currently under heavy development. You should not be
> using it for anything serious, unless you know what you are doing!


Requirements
------------

The following items are required to run LTTng Scope:

* Java 8
* JavaFX

If you use Oracle's JVM, JavaFX is included. If you use OpenJDK, you might need
to install OpenJFX separately. On Debian/Ubuntu, it is part of the _openjfx_
package.


Build from source
-----------------

You need [Maven](http://maven.apache.org) version 3.3 or later to build
this project from source.

To build LTTng Scope from source, run the following from the project's root
directory:

    mvn clean install

The unit tests are run by default, to skip them you can use:

    mvn clean install -DskipTests

You can then run the program with:

    java -jar lttng-scope/target/lttng-scope-0.3.0-SNAPSHOT-jar-with-dependencies.jar

Remember that this is an early pre-release version, and a lot of core
functionality is still missing. You are still welcome to try it out and report
issues!


Relation to Trace Compass
-------------------------

LTTng Scope is based on some parts of the Trace Compass source code. However
it is meant to be a separate project with different design goals. While
Trace Compass aims to support any analysis for any trace type,
LTTng Scope aims to:

* Focus on CTF/LTTng use cases
* Streamline the user experience
* Clearly separate layers (trace reading, analysis, UI)

Running analyses on the command-line (in CI, etc.) with text-based output is
also within the realm of possibilities.


User documentation
------------------

Not available yet.
