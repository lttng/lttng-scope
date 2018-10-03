/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2014-2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.trace.layout

/**
 * Interface to define "concepts" present in the Linux kernel (represented by
 * its tracepoints), that can then be exposed by different tracers under
 * different names.
 */
interface LttngKernelEventLayout {

    // ------------------------------------------------------------------------
    // Common definitions
    // ------------------------------------------------------------------------

    companion object {
        /**
         * Whenever a process appears for the first time in a trace, we assume it
         * starts inside this system call. (The syscall prefix is defined by the
         * implementer of this interface.)
         */
        const val INITIAL_SYSCALL_NAME = "clone"
    }

    // ------------------------------------------------------------------------
    // Event names
    // ------------------------------------------------------------------------

    val eventIrqHandlerEntry: String
    val eventIrqHandlerExit: String
    val eventSoftIrqEntry: String
    val eventSoftIrqExit: String
    val eventSoftIrqRaise: String
    val eventSchedSwitch: String
    val eventSchedPiSetPrio: String
    val eventsSchedWakeup: Collection<String>
    val eventSchedProcessFork: String
    val eventSchedProcessExit: String
    val eventSchedProcessFree: String

    /** Optional event used by some tracers to deliver an initial state. */
    val eventStatedumpProcessState: String?

    /** System call entry prefix, something like "sys_open" or just "sys". */
    val eventSyscallEntryPrefix: String

    /** System call compatibility layer entry prefix, something like "compat_sys". */
    val eventCompatSyscallEntryPrefix: String

    /** System call exit prefix, something like "sys_exit". */
    val eventSyscallExitPrefix: String

    /** System call compatibility layer exit prefix, something like "compat_syscall_exit". */
    val eventCompatSyscallExitPrefix: String

    val eventSchedProcessExec: String
    val eventSchedProcessWakeup: String
    val eventSchedProcessWakeupNew: String
    val eventSchedProcessWaking: String
    val eventSchedMigrateTask: String
    val eventHRTimerStart: String
    val eventHRTimerCancel: String
    val eventHRTimerExpireEntry: String
    val eventHRTimerExpireExit: String
    val eventKmemPageAlloc: String
    val eventKmemPageFree: String

    val ipiIrqVectorsEntries: Collection<String>
    val ipiIrqVectorsExits: Collection<String>

    // ------------------------------------------------------------------------
    // Event field names
    // ------------------------------------------------------------------------

    val fieldIrq: String
    val fieldVec: String
    val fieldTid: String
    val fieldPrevTid: String
    val fieldPrevState: String
    val fieldNextComm: String
    val fieldNextTid: String
    val fieldChildComm: String
    val fieldParentTid: String
    val fieldChildTid: String
    val fieldComm: String
    val fieldName: String
    val fieldStatus: String
    val fieldPrevComm: String
    val fieldFilename: String
    val fieldPrio: String
    val fieldNewPrio: String
    val fieldPrevPrio: String
    val fieldNextPrio: String
    val fieldHRtimer: String
    val fieldHRtimerExpires: String
    val fieldHRtimerSoftexpires: String
    val fieldHRtimerFunction: String
    val fieldHRtimerNow: String
    val fieldSyscallRet: String
    val fieldTargetCpu: String
    val fieldDestCpu: String

    // ------------------------------------------------------------------------
    // I/O events and fields
    // ------------------------------------------------------------------------

    val eventBlockRqInsert: String
    val eventBlockRqIssue: String
    val eventBlockRqComplete: String
    val eventBlockBioFrontmerge: String
    val eventBlockBioBackmerge: String
    val eventBlockRqMerge: String
    val eventStatedumpBlockDevice: String?

    val fieldBlockDeviceId: String
    val fieldBlockSector: String
    val fieldBlockNrSector: String
    val fieldBlockRwbs: String
    val fieldBlockRqSector: String
    val fieldBlockNextRqSector: String
    val fieldDiskname: String
    val fieldIPIVector: String
    val fieldOrder: String?

    // ------------------------------------------------------------------------
    // Network events and fields
    // ------------------------------------------------------------------------

    val eventsNetworkSend: Collection<String>
    val eventsNetworkReceive: Collection<String>

    val fieldPathTcpSeq: Collection<String>
    val fieldPathTcpAckSeq: Collection<String>
    val fieldPathTcpFlags: Collection<String>

    // ------------------------------------------------------------------------
    // VirtualMachine events : kvm entry/exit events
    // ------------------------------------------------------------------------

    val eventsKVMEntry: Collection<String>
    val eventsKVMExit: Collection<String>

}
