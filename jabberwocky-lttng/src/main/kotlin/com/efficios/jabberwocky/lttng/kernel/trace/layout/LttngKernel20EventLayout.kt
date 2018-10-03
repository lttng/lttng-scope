/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2012-2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.trace.layout

/**
 * This file defines all the known event and field names for LTTng kernel
 * traces, for versions of lttng-modules from 2.0 up to 2.5.
 *
 * These should not be externalized, since they need to match exactly what the
 * tracer outputs. If you want to localize them in a view, you should do a
 * mapping in the view itself.
 */
open class LttngKernel20EventLayout protected constructor() : LttngKernelEventLayout {

    companion object {
        val instance = LttngKernel20EventLayout()
    }

    /* Event names */
    override val eventIrqHandlerEntry = "irq_handler_entry"
    override val eventIrqHandlerExit = "irq_handler_exit"
    override val eventSoftIrqRaise = "softirq_raise"
    override val eventSoftIrqEntry = "softirq_entry"
    override val eventSoftIrqExit = "softirq_exit"
    override val eventHRTimerStart = "hrtimer_start"
    override val eventHRTimerCancel = "hrtimer_cancel"
    override val eventHRTimerExpireEntry = "hrtimer_expire_entry"
    override val eventHRTimerExpireExit = "hrtimer_expire_exit"
    override val eventSchedSwitch = "sched_switch"
    override val eventSchedPiSetPrio = "sched_pi_setprio"
    override val eventSchedMigrateTask = "sched_migrate_task"

    override val eventSchedProcessWakeup = "sched_wakeup"
    override val eventSchedProcessWakeupNew = "sched_wakeup_new"
    override val eventsSchedWakeup = listOf("sched_wakeup", "sched_wakeup_new")
    override val eventSchedProcessWaking = "sched_waking"

    override val eventSchedProcessFork = "sched_process_fork"
    override val eventSchedProcessExit = "sched_process_exit"
    override val eventSchedProcessFree = "sched_process_free"
    override val eventSchedProcessExec = "sched_process_exec"
    override val eventStatedumpProcessState = "lttng_statedump_process_state"

    override val eventSyscallEntryPrefix = "sys_"
    override val eventCompatSyscallEntryPrefix = "compat_sys_"
    override val eventSyscallExitPrefix = "exit_syscall"
    override val eventCompatSyscallExitPrefix = "exit_syscall"

    override val eventBlockRqInsert = "block_rq_insert"
    override val eventBlockRqIssue = "block_rq_issue"
    override val eventBlockRqMerge = "addons_elv_merge_requests"
    override val eventBlockRqComplete = "block_rq_complete"
    override val eventStatedumpBlockDevice = "lttng_statedump_block_device"
    override val eventBlockBioFrontmerge = "block_bio_frontmerge"
    override val eventBlockBioBackmerge = "block_bio_backmerge"

    override val eventKmemPageAlloc = "mm_page_alloc"
    override val eventKmemPageFree = "mm_page_free"

    override val ipiIrqVectorsEntries = emptyList<String>()
    override val ipiIrqVectorsExits = emptyList<String>()

    override val fieldIrq = "irq"
    override val fieldTid = "tid"
    override val fieldVec = "vec"
    override val fieldPrevTid = "prev_tid"
    override val fieldPrevState = "prev_state"
    override val fieldNextComm = "next_comm"
    override val fieldNextTid = "next_tid"
    override val fieldParentTid = "parent_tid"
    override val fieldChildComm = "child_comm"
    override val fieldChildTid = "child_tid"
    override val fieldPrio = "prio"
    override val fieldPrevPrio = "prev_prio"
    override val fieldNextPrio = "next_prio"
    override val fieldNewPrio = "newprio"
    override val fieldComm = "comm"
    override val fieldName = "name"
    override val fieldStatus = "status"
    override val fieldPrevComm = "prev_comm"
    override val fieldFilename = "filename"
    override val fieldTargetCpu = "target_cpu"
    override val fieldDestCpu = "dest_cpu"
    override val fieldIPIVector = "vector"

    override val fieldHRtimer = "hrtimer"
    override val fieldHRtimerFunction = "function"
    override val fieldHRtimerExpires = "expires"
    override val fieldHRtimerNow = "now"
    override val fieldHRtimerSoftexpires = "softexpires"

    override val fieldSyscallRet = "ret"
    override val fieldBlockRwbs = "rwbs"
    override val fieldDiskname = "diskname"
    override val fieldBlockDeviceId = "dev"
    override val fieldBlockSector = "sector"
    override val fieldBlockNrSector = "nr_sector"
    override val fieldBlockRqSector = "rq_sector"
    override val fieldBlockNextRqSector = "nextrq_sector"
    override val fieldOrder = "order"

    override val eventsNetworkSend = listOf("net_dev_queue")
    override val eventsNetworkReceive = listOf("netif_receive_skb")
    override val fieldPathTcpSeq = listOf("transport_fields", "thtype_tcp", "seq")
    override val fieldPathTcpAckSeq = listOf("transport_fields", "thtype_tcp", "ack_seq")
    override val fieldPathTcpFlags = listOf("transport_fields", "thtype_tcp", "flags")

    override val eventsKVMEntry = listOf("kvm_entry")
    override val eventsKVMExit = listOf("kvm_exit")
}
