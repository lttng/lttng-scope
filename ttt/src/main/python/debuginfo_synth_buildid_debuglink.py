from debuginfo_trace_writer import DebugInfoTraceWriter
import sys

'''
Generate bin_info events with various combinations of buildid/debuglink.
'''

if len(sys.argv) < 2:
    print("Please provide trace output path.", file=sys.stderr)
    sys.exit(1)

def timestamp_generator():
    ts = 1
    while True:
        yield ts
        ts += 1

# Variables are suffixed with _AB, where A = build_id presence (y/n) and
# B = debug link presence (y/n).

vpid_nn = 1337
vpid_yn = 1338
vpid_ny = 1339
vpid_yy = 1340

ts = timestamp_generator()
gen = DebugInfoTraceWriter(sys.argv[1])

baddr = 0x400000
memsz = 0x10000
func_addr = baddr + 0x100

def generate(vpid, build_id=None, debug_link=None):
    has_build_id = build_id is not None
    has_debug_link = debug_link is not None
    
    suffix = ('y' if has_build_id else 'n') + ('y' if has_debug_link else 'n')
    
    gen.write_lttng_ust_statedump_start(next(ts), 0, vpid)
    gen.write_lttng_ust_statedump_bin_info(next(ts), 0, vpid, baddr, memsz,
                                           '/tmp/foo_{}'.format(suffix),
                                           has_build_id=has_build_id,
                                           has_debug_link=has_debug_link)
    if has_build_id:
        gen.write_lttng_ust_statedump_build_id(next(ts), 0, vpid, baddr, build_id)

    if has_debug_link:
        gen.write_lttng_ust_statedump_debug_link(next(ts), 0, vpid, baddr, 0, debug_link)
        
    gen.write_lttng_ust_statedump_end(next(ts), 0, vpid)

generate(vpid_nn)
generate(vpid_yn, build_id='aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa')
generate(vpid_ny, debug_link='/tmp/debug_link1')
generate(vpid_yy, build_id='bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb', debug_link='/tmp/debug_link2')

gen.write_dummy_event(next(ts), 0, vpid_nn, func_addr)
gen.write_dummy_event(next(ts), 0, vpid_yn, func_addr)
gen.write_dummy_event(next(ts), 0, vpid_ny, func_addr)
gen.write_dummy_event(next(ts), 0, vpid_yy, func_addr)

gen.flush()
