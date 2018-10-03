TTT Test Traces Collection
==========================

*TTT* (originally *tracecompass-test-traces*) is a set of CTF test
traces for use in Trace Compass, LTTng Scope, and related projects.

To build the package and install it in your local Maven repo, simply
issue:

    mvn clean install


Adding a new CTF test trace
---------------------------

The modules follow the [Maven standard directory layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html).

To add a new CTF test trace, add it to the `ctf/src/main/resources` directory.
Make sure it is not archived or anything, as this will be exposed as-is to the
users.

Then update the `ctf/src/main/java/.../CtfTestTrace.java` file accordingly to
include the new trace.

Make sure the parameters (event count, etc.) are correct! This project does not
check those at the moment, but if they are incorrect they **will** fail some
Trace Compass unit tests. This is a known issue.

Finally, use `mvn versions:set` to bump the version number of the project.

