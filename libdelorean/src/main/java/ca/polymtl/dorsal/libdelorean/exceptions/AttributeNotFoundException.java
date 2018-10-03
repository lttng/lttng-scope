/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2012-2015 Ericsson
 * Copyright (C) 2010-2011 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.exceptions;

/**
 * This exception gets thrown when the user tries to access an attribute which
 * doesn't exist in the system, of if the quark is simply invalid (ie, < 0).
 *
 * @author Alexandre Montplaisir
 */
public class AttributeNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 7964275803369706145L;

    /**
     * Default constructor
     */
    public AttributeNotFoundException() {
        super();
    }

    /**
     * Constructor with a message
     *
     * @param message
     *            Message to attach to this exception
     */
    public AttributeNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with both a message and a cause.
     *
     * @param message
     *            Message to attach to this exception
     * @param e
     *            Cause of this exception
     */
    public AttributeNotFoundException(String message, Throwable e) {
        super(message, e);
    }
}
