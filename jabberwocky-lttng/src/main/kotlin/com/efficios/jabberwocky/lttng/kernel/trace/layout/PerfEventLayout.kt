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
 * Event and field definitions for perf traces in CTF format.
 *
 * TODO The "by LttngKernel20EventLayout.instance" is to define the entries commented-out at the bottom.
 * This file was previously like this, but ideally the perf hierarchy should define its own events/fields.
 */
class PerfEventLayout : LttngKernelEventLayout by LttngKernel20EventLayout.instance {

    companion object {
        val instance = PerfEventLayout()
    }

    // ------------------------------------------------------------------------
    // Event names
    // ------------------------------------------------------------------------

    override val eventIrqHandlerEntry = "irq:irq_handler_entry"
    override val eventIrqHandlerExit = "irq:irq_handler_exit"
    override val eventSoftIrqEntry = "irq:softirq_entry"
    override val eventSoftIrqExit = "irq:softirq_exit"
    override val eventSoftIrqRaise = "irq:softirq_raise"
    override val eventSchedSwitch = "sched:sched_switch"
    override val eventSchedPiSetPrio = "sched:sched_pi_setprio"
    override val eventsSchedWakeup = listOf("sched:sched_wakeup", "sched:sched_wakeup_new")
    override val eventSchedProcessFork = "sched:sched_process_fork"
    override val eventSchedProcessExit = "sched:sched_process_exit"
    override val eventSchedProcessFree =  "sched:sched_process_free"
    /* Not present in perf traces */
    override val eventStatedumpProcessState: String? = null

    override val eventSyscallEntryPrefix = "raw_syscalls:sys_enter"
    override val eventCompatSyscallEntryPrefix = eventSyscallEntryPrefix
    override val eventSyscallExitPrefix = "raw_syscalls:sys_exit"
    override val eventCompatSyscallExitPrefix = eventSyscallExitPrefix
    override val eventSchedProcessExec = "sched:sched_process_exec"
    override val eventSchedProcessWakeup = "sched:sched_process_wakeup"
    override val eventSchedProcessWakeupNew = "sched:process_wakeup_new"
    override val eventSchedProcessWaking = "sched:sched_waking"
    override val eventSchedMigrateTask = "sched:sched_migrate_task"

    override val eventHRTimerStart = "timer:hrtimer_start"
    override val eventHRTimerCancel = "timer:hrtimer_cancel"
    override val eventHRTimerExpireEntry = "timer:hrtimer_expire_entry"
    override val eventHRTimerExpireExit = "timer:hrtimer_expire_exit"

    override val eventKmemPageAlloc = "kmem:page_alloc"
    override val eventKmemPageFree = "kmem:page_free"

    // ------------------------------------------------------------------------
    // Field names
    // ------------------------------------------------------------------------

    override val fieldIrq = "irq"
    override val fieldVec = "vec"
    override val fieldTid = "pid" // yes, "pid", what lttng calls a "tid" perf calls a "pid"
    override val fieldPrevTid = "prev_pid"
    override val fieldPrevState = "prev_state"
    override val fieldNextComm = "next_comm"
    override val fieldNextTid = "next_pid"
    override val fieldChildComm = "child_comm"
    override val fieldParentTid = "parent_pid"
    override val fieldChildTid = "child_pid"
    override val fieldPrio = "prio"
    override val fieldNewPrio = "newprio"
    override val fieldPrevPrio = "prev_prio"
    override val fieldNextPrio = "next_prio"
    override val fieldComm = "comm"
    override val fieldName = "name"
    override val fieldStatus = "status"
    override val fieldPrevComm = "prev_comm"
    override val fieldFilename = "filename"
    override val fieldHRtimer = "hrtimer"
    override val fieldHRtimerFunction = "function"
    override val fieldHRtimerExpires = "expires"
    override val fieldHRtimerSoftexpires = "softexpires"
    override val fieldHRtimerNow = "now"
    override val fieldOrder = "order"

    // ------------------------------------------------------------------------
    // I/O events and fields
    // ------------------------------------------------------------------------

    override val eventBlockRqInsert = "block:block_rq_insert"
    override val eventBlockRqIssue = "block:block_rq_issue"
    override val eventBlockRqComplete = "block:block_rq_complete"
    override val eventBlockBioFrontmerge = "block:block_bio_frontmerge"
    override val eventBlockBioBackmerge = "block:block_bio_backmerge"

    /* TODO Define these below specifically for perf */

//    override val eventBlockRqMerge: String
//    override val eventStatedumpBlockDevice: String?
//
//    override val eventsNetworkSend: Collection<String>
//    override val eventsNetworkReceive: Collection<String>
//    override val eventsKVMEntry: Collection<String>
//    override val eventsKVMExit: Collection<String>
//    override val ipiIrqVectorsEntries: Collection<String>
//    override val ipiIrqVectorsExits: Collection<String>
//
//    override val fieldSyscallRet: String
//    override val fieldTargetCpu: String
//    override val fieldDestCpu: String
//
//    override val fieldBlockDeviceId: String
//    override val fieldBlockSector: String
//    override val fieldBlockNrSector: String
//    override val fieldBlockRwbs: String
//    override val fieldBlockRqSector: String
//    override val fieldBlockNextRqSector: String
//    override val fieldDiskname: String
//    override val fieldIPIVector: String
//
//    override val fieldPathTcpSeq: Collection<String>
//    override val fieldPathTcpAckSeq: Collection<String>
//    override val fieldPathTcpFlags: Collection<String>

}
