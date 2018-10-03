Jabberwocky Trace Analysis Library
==================================

> 'Twas brillig, and the slithy toves  
> Did gyre and gimble in the wabe;  
> All mimsy were the borogoves,  
> And the mome raths outgrabe.  

Jabberwocky is a headless trace analysis library implemented in Java. Its goal
is to analyze traces by building indexes and state trackers, and produce
analysis results like data tables and graph/chart data models in the form of
command-line ouput, serialized data (JSON, etc.) or Java objects.

The Jabberwocky core framework is generic enough to support the implementation
of any file-based trace parser, although the initial goal of the project is to
focus on [CTF](http://diamon.org/ctf/) and [LTTng](https://lttng.org/) traces.

Jabberwocky is used as the analysis library of the
[LTTng Scope](https://github.com/lttng/lttng-scope) trace viewer.
The source code is licensed under the
[Eclipse Public License](https://www.eclipse.org/legal/epl-v10.html).

Requirements
------------

Jabberwocky requires the following items to be installed on the system:

* Java 1.8
* JavaFX

The JavaFX library is needed because of the use of
[JavaFX Properties](https://docs.oracle.com/javase/8/javafx/properties-binding-tutorial/binding.htm).
Not only it is a powerful programming tool, it also makes the integration with
JavaFX UIs slightly easier, although not mandatory.

Building from source
--------------------

Building Jabberwocky from source requires [Maven](http://maven.apache.org) 3.2
or later. Simply run

    mvn clean install

from the project's root directory to install the library into your local Maven
repository. This will also run the unit tests. To skip running the tests you can
use

    mvn clean install -Dmaven.test.skip=true

Using Jabberwocky in your project
---------------------------------

Jabberwocky is not (yet?) deployed to Maven Central. To add Jabberwocky as a
dependency to your Maven project, you first need to add the following
repositories to your project's `pom.xml`:

    <repositories>
      <repository>
        <id>efficios-releases-repo</id>
        <name>EfficiOS Releases</name>
        <url>https://mvn.efficios.com/repository/releases/</url>
      </repository>
      <repository>
        <id>efficios-snapshots-repo</id>
        <name>EfficiOS Snapshots</name>
        <url>https://mvn.efficios.com/repository/snapshots/</url>
      </repository>
    </repositories>


then add the module dependency inside the `<dependencies>` section:

    <dependency>
      <groupId>com.efficios.jabberwocky</groupId>
      <artifactId>jabberwocky-core</artifactId>
      <version>0.2.50-SNAPSHOT</version>
    </dependency>

Other modules like `jabberwocky-ctf` and `jabberwocky-lttng` contain parsers and
analyses specific to CTF and LTTng traces.
