/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2015 Ericsson
 * Copyright (C) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.trace.layout

/**
 * This file defines all the known event and field names for LTTng kernel
 * traces, for versions of lttng-modules 2.7 and above.
 */
open class LttngKernel27EventLayout protected constructor() : LttngKernel26EventLayout() {

    companion object {
        val instance = LttngKernel27EventLayout()

        private const val X86_IRQ_VECTORS_LOCAL_TIMER_ENTRY = "x86_irq_vectors_local_timer_entry"
        private const val X86_IRQ_VECTORS_LOCAL_TIMER_EXIT = "x86_irq_vectors_local_timer_exit"
        private const val X86_IRQ_VECTORS_RESCHEDULE_ENTRY = "x86_irq_vectors_reschedule_entry"
        private const val X86_IRQ_VECTORS_RESCHEDULE_EXIT = "x86_irq_vectors_reschedule_exit"
        private const val X86_IRQ_VECTORS_SPURIOUS_ENTRY = "x86_irq_vectors_spurious_apic_entry"
        private const val X86_IRQ_VECTORS_SPURIOUS_EXIT = "x86_irq_vectors_spurious_apic_exit"
        private const val X86_IRQ_VECTORS_ERROR_APIC_ENTRY = "x86_irq_vectors_error_apic_entry"
        private const val X86_IRQ_VECTORS_ERROR_APIC_EXIT = "x86_irq_vectors_error_apic_exit"
        private const val X86_IRQ_VECTORS_IPI_ENTRY = "x86_irq_vectors_ipi_entry"
        private const val X86_IRQ_VECTORS_IPI_EXIT = "x86_irq_vectors_ipi_exit"
        private const val X86_IRQ_VECTORS_IRQ_WORK_ENTRY = "x86_irq_vectors_irq_work_entry"
        private const val X86_IRQ_VECTORS_IRQ_WORK_EXIT = "x86_irq_vectors_irq_work_exit"
        private const val X86_IRQ_VECTORS_CALL_FUNCTION_ENTRY = "x86_irq_vectors_call_function_entry"
        private const val X86_IRQ_VECTORS_CALL_FUNCTION_EXIT = "x86_irq_vectors_call_function_exit"
        private const val X86_IRQ_VECTORS_CALL_FUNCTION_SINGLE_ENTRY = "x86_irq_vectors_call_function_single_entry"
        private const val X86_IRQ_VECTORS_CALL_FUNCTION_SINGLE_EXIT = "x86_irq_vectors_call_function_single_exit"
        private const val X86_IRQ_VECTORS_THRESHOLD_APIC_ENTRY = "x86_irq_vectors_threshold_apic_entry"
        private const val X86_IRQ_VECTORS_THRESHOLD_APIC_EXIT = "x86_irq_vectors_threshold_apic_exit"
        private const val X86_IRQ_VECTORS_DEFERRED_ERROR_APIC_ENTRY = "x86_irq_vectors_deferred_error_apic_entry"
        private const val X86_IRQ_VECTORS_DEFERRED_ERROR_APIC_EXIT = "x86_irq_vectors_deferred_error_apic_exit"
        private const val X86_IRQ_VECTORS_THERMAL_APIC_ENTRY = "x86_irq_vectors_thermal_apic_entry"
        private const val X86_IRQ_VECTORS_THERMAL_APIC_EXIT = "x86_irq_vectors_thermal_apic_exit"
    }

    // ------------------------------------------------------------------------
    // Updated event definitions in LTTng 2.7
    // ------------------------------------------------------------------------

    override val eventHRTimerStart = "timer_hrtimer_start"
    override val eventHRTimerCancel = "timer_hrtimer_cancel"
    override val eventHRTimerExpireEntry = "timer_hrtimer_expire_entry"
    override val eventHRTimerExpireExit = "timer_hrtimer_expire_exit"

    override val eventSoftIrqRaise = "irq_softirq_raise"
    override val eventSoftIrqEntry = "irq_softirq_entry"
    override val eventSoftIrqExit = "irq_softirq_exit"

    override val eventKmemPageAlloc =  "kmem_mm_page_alloc"
    override val eventKmemPageFree = "kmem_mm_page_free"

    override val eventsNetworkReceive = listOf("net_if_receive_skb")
    override val eventsKVMEntry = listOf("kvm_x86_entry")
    override val eventsKVMExit = listOf("kvm_x86_exit")

    override val ipiIrqVectorsEntries = listOf(
            X86_IRQ_VECTORS_LOCAL_TIMER_ENTRY,
            X86_IRQ_VECTORS_RESCHEDULE_ENTRY,
            X86_IRQ_VECTORS_SPURIOUS_ENTRY,
            X86_IRQ_VECTORS_ERROR_APIC_ENTRY,
            X86_IRQ_VECTORS_IPI_ENTRY,
            X86_IRQ_VECTORS_IRQ_WORK_ENTRY,
            X86_IRQ_VECTORS_CALL_FUNCTION_ENTRY,
            X86_IRQ_VECTORS_CALL_FUNCTION_SINGLE_ENTRY,
            X86_IRQ_VECTORS_THRESHOLD_APIC_ENTRY,
            X86_IRQ_VECTORS_DEFERRED_ERROR_APIC_ENTRY,
            X86_IRQ_VECTORS_THERMAL_APIC_ENTRY)

    override val ipiIrqVectorsExits = listOf(
            X86_IRQ_VECTORS_LOCAL_TIMER_EXIT,
            X86_IRQ_VECTORS_RESCHEDULE_EXIT,
            X86_IRQ_VECTORS_SPURIOUS_EXIT,
            X86_IRQ_VECTORS_ERROR_APIC_EXIT,
            X86_IRQ_VECTORS_IPI_EXIT,
            X86_IRQ_VECTORS_IRQ_WORK_EXIT,
            X86_IRQ_VECTORS_CALL_FUNCTION_EXIT,
            X86_IRQ_VECTORS_CALL_FUNCTION_SINGLE_EXIT,
            X86_IRQ_VECTORS_THRESHOLD_APIC_EXIT,
            X86_IRQ_VECTORS_DEFERRED_ERROR_APIC_EXIT,
            X86_IRQ_VECTORS_THERMAL_APIC_EXIT)

    // ------------------------------------------------------------------------
    // New event definitions
    // ------------------------------------------------------------------------

    open val x86IrqVectorsLocalTimerEntry = X86_IRQ_VECTORS_LOCAL_TIMER_ENTRY
    open val x86IrqVectorsLocalTimerExit = X86_IRQ_VECTORS_LOCAL_TIMER_EXIT
    open val x86IrqVectorsRescheduleEntry = X86_IRQ_VECTORS_RESCHEDULE_ENTRY
    open val x86IrqVectorsRescheduleExit = X86_IRQ_VECTORS_RESCHEDULE_EXIT
    open val x86IrqVectorsSpuriousApicEntry = X86_IRQ_VECTORS_SPURIOUS_ENTRY
    open val x86IrqVectorsSpuriousApicExit = X86_IRQ_VECTORS_SPURIOUS_EXIT
    open val x86IrqVectorsErrorApicEntry = X86_IRQ_VECTORS_ERROR_APIC_ENTRY
    open val x86IrqVectorsErrorApicExit = X86_IRQ_VECTORS_ERROR_APIC_EXIT
    open val x86IrqVectorsIpiEntry = X86_IRQ_VECTORS_IPI_ENTRY
    open val x86IrqVectorsIpiExit = X86_IRQ_VECTORS_IPI_EXIT
    open val x86IrqVectorsIrqWorkEntry =  X86_IRQ_VECTORS_IRQ_WORK_ENTRY
    open val x86IrqVectorsIrqWorkExit = X86_IRQ_VECTORS_IRQ_WORK_EXIT
    open val x86IrqVectorsCallFunctionEntry = X86_IRQ_VECTORS_CALL_FUNCTION_ENTRY
    open val x86IrqVectorsCallFunctionExit = X86_IRQ_VECTORS_CALL_FUNCTION_EXIT
    open val x86IrqVectorsCallFunctionSingleEntry = X86_IRQ_VECTORS_CALL_FUNCTION_SINGLE_ENTRY
    open val x86IrqVectorsCallFunctionSingleExit = X86_IRQ_VECTORS_CALL_FUNCTION_SINGLE_EXIT
    open val x86IrqVectorsThresholdApicEntry = X86_IRQ_VECTORS_THRESHOLD_APIC_ENTRY
    open val x86IrqVectorsThresholdApicExit = X86_IRQ_VECTORS_THRESHOLD_APIC_EXIT
    open val x86IrqVectorsDeferredErrorApicEntry = X86_IRQ_VECTORS_DEFERRED_ERROR_APIC_ENTRY
    open val x86IrqVectorsDeferredErrorApicExit = X86_IRQ_VECTORS_DEFERRED_ERROR_APIC_EXIT
    open val x86IrqVectorsThermalApicEntry = X86_IRQ_VECTORS_THERMAL_APIC_ENTRY
    open val x86IrqVectorsThermalApicExit = X86_IRQ_VECTORS_THERMAL_APIC_EXIT

    // ------------------------------------------------------------------------
    // New field definitions in LTTng 2.7
    // ------------------------------------------------------------------------

    open val fieldParentNSInum = "parent_ns_inum"
    open val fieldChildNSInum = "child_ns_inum"
    open val fieldChildVTids = "vtids"
    open val fieldNSInum = "ns_inum"
    open val fieldVTid = "vtid"
    open val fieldPPid = "ppid"
    open val fieldNSLevel = "ns_level"

}
