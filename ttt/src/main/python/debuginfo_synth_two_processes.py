from debuginfo_trace_writer import DebugInfoTraceWriter
import sys

'''
Generate a trace with two processes, to make sure that the viewer properly
takes vpid into account.
'''

if len(sys.argv) < 2:
    print("Please provide trace output path.", file=sys.stderr)
    sys.exit(1)

def timestamp_generator():
    ts = 1
    while True:
        yield ts
        ts += 1

vpid1 = 1337
vpid2 = 2001

ts = timestamp_generator()
gen = DebugInfoTraceWriter(sys.argv[1])

baddr = 0x400000
memsz = 0x10000
function_addr = baddr + 0x100

gen.write_lttng_ust_statedump_start(next(ts), 0, vpid1)
gen.write_lttng_ust_statedump_start(next(ts), 0, vpid2)
gen.write_lttng_ust_statedump_bin_info(next(ts), 0, vpid1, baddr, memsz, "/tmp/foo", has_build_id=True, has_debug_link=True)
gen.write_lttng_ust_statedump_bin_info(next(ts), 0, vpid2, baddr, memsz, "/tmp/bar", has_build_id=True, has_debug_link=True)
gen.write_lttng_ust_statedump_build_id(next(ts), 0, vpid2, baddr, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
gen.write_lttng_ust_statedump_build_id(next(ts), 0, vpid1, baddr, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
gen.write_lttng_ust_statedump_debug_link(next(ts), 0, vpid1, baddr, 0, "/tmp/debuglink1")
gen.write_lttng_ust_statedump_debug_link(next(ts), 0, vpid2, baddr, 0, "/tmp/debuglink2")
gen.write_lttng_ust_statedump_end(next(ts), 0, vpid1)
gen.write_lttng_ust_statedump_end(next(ts), 0, vpid2)

gen.write_dummy_event(next(ts), 0, vpid1, function_addr)
gen.write_dummy_event(next(ts), 0, vpid2, function_addr)

gen.flush()
