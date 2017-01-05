Jabberwocky Trace Viewer
=======================

> 'Twas brillig, and the slithy toves
> Did gyre and gimble in the wabe;
> All mimsy were the borogoves,
> And the mome raths outgrabe.


This source tree contains the source code for the Trace Compass plugins for
Eclipse.

The plugins are categorized as follows:

    analysis/    | Generic extensions to the base framework
    btf/         | Best Trace Format (BTF) integration
    common/      | Generic utilities that can be used by other plugins
    ctf/         | Common Trace Format (CTF) reader library
    doc/         | Documentation and code examples
    gdbtrace/    | Support for reading and viewing GDB traces
    lttng/       | LTTng integration
    pcap/        | libpcap integration
    rcp/         | Code specific to the RCP version
    releng/      | Releng-related plugins
    statesystem/ | State System library
    tmf/         | Core framework


Setting up the development environment
--------------------------------------

To set up the environment to build Trace Compass from within Eclipse, see this
wiki page:
<http://wiki.eclipse.org/Trace_Compass/Development_Environment_Setup>


Compiling manually
------------------

The Maven project build requires version 3.3 or later. It can be downloaded from
<http://maven.apache.org> or from the package management system of your distro.

To build the project manually using Maven, simply run the following command from
the top-level directory:

    mvn clean install

The default command will compile and run the unit tests. Running the tests can
take some time, to skip them you can append `-Dmaven.test.skip=true` to the
`mvn` command:

    mvn clean install -Dmaven.test.skip=true

The stand-alone application (RCP) packages will be placed in
`rcp/org.eclipse.tracecompass.rcp.product/target/products`.


Maven profiles and properties
-----------------------------

The following Maven profiles and properties are defined in
the build system. You can set them by using `-P[profile name]` and
`-D[property name]=[value]` in `mvn` commands.

* `-Dtarget-platform=[target]`

  Defines which target to use. This is used to build against various versions of
  the Eclipse platform. Available ones are in
  `releng/org.eclipse.tracecompass.target`. The default is usually the latest
  stable platform. To use the staging target for example, use
  `-Dtarget-platform=tracecompass-eStaging`.

* `-Dskip-automated-ui-tests`

  Skips the automated UI integration tests. Not required when using
  `-Dmaven.test.skip=true`, which already skips all the tests.

* `-Pctf-grammar`

  Re-compiles the CTF grammar files. This should be enabled if you modify the
  `.g` files in the `ctf.parser` plugin.

* `-Pdeploy-rcp`

  Mainly for use on build servers. Copies the generated RCP archives, as well as
  the RCP-specific update site, to the paths specified by
  `-DrcpDestination=/absolute/path/to/destination` and
  `-DrcpSiteDestination=/absolute/path/to/destination`, respectively.

