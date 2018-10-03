from debuginfo_trace_writer import DebugInfoTraceWriter
import sys

'''
Generate a trace simulating an exec.  When an exec happens, the address space
of the process is reset (previously loaded libraries are not there anymore,
and the main executable is replaced), so any know mapping should be forgotten.
In the trace, this is represented by a new statedump (when the liblttng-ust.so
library is loaded again in the new address space, its constructor is called
again, which initiates a new statedump).
'''

if len(sys.argv) < 2:
    print("Please provide trace output path.", file=sys.stderr)
    sys.exit(1)

def timestamp_generator():
    ts = 1
    while True:
        yield ts
        ts += 1

vpid = 1337
ts = timestamp_generator()
gen = DebugInfoTraceWriter(sys.argv[1])

baddr = 0x400000
memsz = 0x10000

gen.write_lttng_ust_statedump_start(next(ts), 0, vpid)
gen.write_lttng_ust_statedump_bin_info(next(ts), 0, vpid, baddr, memsz, "/tmp/foo", 0, 0, 0)
gen.write_lttng_ust_statedump_end(next(ts), 0, vpid)
gen.write_dummy_event(next(ts), 0, vpid, 0x400100)

baddr = 0x500000
memsz = 0x10000

gen.write_lttng_ust_statedump_start(next(ts), 0, vpid)
gen.write_lttng_ust_statedump_bin_info(next(ts), 0, vpid, baddr, memsz, "/tmp/bar", 0, 0, 0)
gen.write_lttng_ust_statedump_end(next(ts), 0, vpid)
# This event should not map to anything currently loaded.
gen.write_dummy_event(next(ts), 0, vpid, 0x400100)
gen.write_dummy_event(next(ts), 0, vpid, 0x500100)

gen.flush()
