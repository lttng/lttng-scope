/*******************************************************************************
 * Copyright (c) 2015  École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.event.aspect;

@SuppressWarnings("javadoc")
public class Messages {

    static final String ThreadPriorityAspect_Name = "Prio";
    static final String ThreadPriorityAspect_HelpText = "The priority of the thread this event belongs to";

    static final String KernelTidAspect_Name = "TID";
    static final String KernelTidAspect_HelpText = "The TID of the thread this event belongs to";


    private Messages() {
    }
}
