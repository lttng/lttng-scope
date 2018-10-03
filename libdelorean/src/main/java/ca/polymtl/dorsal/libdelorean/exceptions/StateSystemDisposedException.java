/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2012-2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.exceptions;

/**
 * Exception thrown by the state system if a query is done on it after it has
 * been disposed.
 *
 * @author Alexandre Montplaisir
 */
public class StateSystemDisposedException extends RuntimeException {

    private static final long serialVersionUID = 7896041701818620084L;

    /**
     * Create a new simple StateSystemDisposedException.
     */
    public StateSystemDisposedException() {
        super();
    }

    /**
     * Create a new StateSystemDisposedException based on a previous one.
     *
     * @param e
     *            The previous exception
     */
    public StateSystemDisposedException(Throwable e) {
        super(e);
    }

}
