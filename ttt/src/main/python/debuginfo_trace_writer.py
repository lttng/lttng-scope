from babeltrace import CTFWriter
import sys
import binascii

class DebugInfoTraceWriter:

    def __init__(self, trace_path, num_cpus=1):
        self.trace_path = trace_path
        
        # Create CTF writer object and boring administrative stuff.
        self._create_writer()
        
        # Create some useful basic types.
        self._define_base_types()
        
        # Define a stream class and its events.
        self._define_stream_class()
        self._define_events()

        # Create one stream for each CPU.
        self._streams = {}
        for i in range(num_cpus):
            self._create_stream(i)

    def _create_writer(self):
        self.writer = CTFWriter.Writer(self.trace_path)

        self.clock = CTFWriter.Clock("A_clock")
        self.clock.description = "Simple clock"
        self.writer.add_clock(self.clock)

        self.writer.add_environment_field("domain", "ust")
        self.writer.add_environment_field("tracer_name", "lttng-ust")
        self.writer.add_environment_field("tracer_major", 2)
        self.writer.add_environment_field("tracer_minor", 8)

    def _define_stream_class(self):
        self.stream_class = CTFWriter.StreamClass("test_stream")
        self.stream_class.clock = self.clock

        # Add cpu_ip to existing stream packet context type
        t = self.stream_class.packet_context_type
        t.add_field(self.uint32_type, "cpu_id")
        
        # Define stream event context type
        t = CTFWriter.StructureFieldDeclaration()
        t.add_field(self.uint64_hex_type, "_ip")
        t.add_field(self.int32_type, "_vpid")
        self.stream_class.event_context_type = t

    def _define_base_types(self):
        self.int8_type = CTFWriter.IntegerFieldDeclaration(8)
        self.int8_type.signed = True
        self.int8_type.alignment = 8
        
        self.uint8_type = CTFWriter.IntegerFieldDeclaration(8)
        self.uint8_type.signed = False
        self.uint8_type.alignment = 8

        self.int32_type = CTFWriter.IntegerFieldDeclaration(32)
        self.int32_type.signed = True
        self.int32_type.alignment = 8
        
        self.uint32_type = CTFWriter.IntegerFieldDeclaration(32)
        self.uint32_type.signed = False
        self.uint32_type.alignment = 8

        self.uint64_type = CTFWriter.IntegerFieldDeclaration(64)
        self.uint64_type.signed = False
        self.uint64_type.alignment = 8

        self.uint64_hex_type = CTFWriter.IntegerFieldDeclaration(64)
        self.uint64_hex_type.signed = False
        self.uint64_hex_type.alignment = 8
        self.uint64_hex_type.base = 16
        
        self.string_type = CTFWriter.StringFieldDeclaration()
        
    def _define_events(self):
        # Simply call all methods that start with define_
        for name, func in sorted(self.__class__.__dict__.items()):
            if name.startswith("define_"):
                func(self)

    def _create_stream(self, cpu_id):
        stream = self.writer.create_stream(self.stream_class)
        stream.packet_context.field("cpu_id").value = cpu_id
        
        self._streams[cpu_id] = stream

    def _define_event(self, event):
        self.stream_class.add_event_class(event)

    def flush(self):
        self.writer.flush_metadata()
        
        for stream in self._streams.values():
            stream.flush()
        
    def _write_event(self, event, time_ms, cpu_id, vpid, ip):
        stream = self._streams[cpu_id]

        self.clock.time = time_ms * 1000000

        event.stream_context.field("_vpid").value = vpid
        event.stream_context.field("_ip").value = ip
        
        stream.append_event(event)

    def define_lttng_ust_statedump_start(self):
        self._lttng_ust_statedump_start = CTFWriter.EventClass("lttng_ust_statedump:start")
        self._define_event(self._lttng_ust_statedump_start)

    def write_lttng_ust_statedump_start(self, time_ms, cpu_id, vpid):
        event = CTFWriter.Event(self._lttng_ust_statedump_start)
        self._write_event(event, time_ms, cpu_id, vpid, 0)

    def define_lttng_ust_statedump_bin_info(self):
        self.lttng_ust_statedump_bin_info = CTFWriter.EventClass("lttng_ust_statedump:bin_info")
        self.lttng_ust_statedump_bin_info.add_field(self.uint64_hex_type, "_baddr")
        self.lttng_ust_statedump_bin_info.add_field(self.uint64_hex_type, "_memsz")
        self.lttng_ust_statedump_bin_info.add_field(self.string_type, "_path")
        self.lttng_ust_statedump_bin_info.add_field(self.uint8_type, "_is_pic")
        self.lttng_ust_statedump_bin_info.add_field(self.uint8_type, "_has_build_id")
        self.lttng_ust_statedump_bin_info.add_field(self.uint8_type, "_has_debug_link")
        self._define_event(self.lttng_ust_statedump_bin_info)

    def write_lttng_ust_statedump_bin_info(self, time_ms, cpu_id, vpid, baddr,
                                           memsz, path, is_pic=False,
                                           has_build_id=False,
                                           has_debug_link=False):
        event = CTFWriter.Event(self.lttng_ust_statedump_bin_info)

        event.payload("_baddr").value = baddr
        event.payload("_memsz").value = memsz
        event.payload("_path").value = path
        event.payload("_is_pic").value = 1 if is_pic else 0
        event.payload("_has_build_id").value = 1 if has_build_id else 0
        event.payload("_has_debug_link").value = 1 if has_debug_link else 0

        self._write_event(event, time_ms, cpu_id, vpid, 0)

    def define_lttng_ust_statedump_build_id(self):
        self.lttng_ust_statedump_build_id = CTFWriter.EventClass("lttng_ust_statedump:build_id")
        self.lttng_ust_statedump_build_id.add_field(self.uint64_hex_type, "_baddr")
        self.lttng_ust_statedump_build_id.add_field(self.uint64_type, "__build_id_length")
        
        build_id_type = CTFWriter.SequenceFieldDeclaration(self.uint8_type, "__build_id_length")
        
        self.lttng_ust_statedump_build_id.add_field(build_id_type, "_build_id")
        self._define_event(self.lttng_ust_statedump_build_id)

    def write_lttng_ust_statedump_build_id(self, time_ms, cpu_id, vpid, baddr,
                                           build_id):
        build_id = list(binascii.unhexlify(build_id))
    
        event = CTFWriter.Event(self.lttng_ust_statedump_build_id)

        event.payload("_baddr").value = baddr
        event.payload("__build_id_length").value = len(build_id)
        
        build_id_field = event.payload("_build_id")
        build_id_field.length = event.payload("__build_id_length")
        
        for i, value in enumerate(build_id):
            build_id_field.field(i).value = value
        
        self._write_event(event, time_ms, cpu_id, vpid, 0)

    def define_lttng_ust_statedump_debug_link(self):
        self.lttng_ust_statedump_debug_link = CTFWriter.EventClass("lttng_ust_statedump:debug_link")
        self.lttng_ust_statedump_debug_link.add_field(self.uint64_hex_type, "_baddr")
        self.lttng_ust_statedump_debug_link.add_field(self.uint32_type, "_crc")
        self.lttng_ust_statedump_debug_link.add_field(self.string_type, "_filename")
        self._define_event(self.lttng_ust_statedump_debug_link)

    def write_lttng_ust_statedump_debug_link(self, time_ms, cpu_id, vpid, baddr, crc, filename):
        event = CTFWriter.Event(self.lttng_ust_statedump_debug_link)

        event.payload("_baddr").value = baddr
        event.payload("_crc").value = crc
        event.payload("_filename").value = filename
        
        self._write_event(event, time_ms, cpu_id, vpid, 0)

    def define_lttng_ust_statedump_end(self):
        self._lttng_ust_statedump_end = CTFWriter.EventClass("lttng_ust_statedump:end")
        self._define_event(self._lttng_ust_statedump_end)

    def write_lttng_ust_statedump_end(self, time_ms, cpu_id, vpid):
        event = CTFWriter.Event(self._lttng_ust_statedump_end)
        self._write_event(event, time_ms, cpu_id, vpid, 0)

    def define_dummy_event(self):
        self._dummy_event = CTFWriter.EventClass("dummy_event")
        self._define_event(self._dummy_event)

    def write_dummy_event(self, time_ms, cpu_id, vpid, ip):
        event = CTFWriter.Event(self._dummy_event)
        self._write_event(event, time_ms, cpu_id, vpid, ip)
