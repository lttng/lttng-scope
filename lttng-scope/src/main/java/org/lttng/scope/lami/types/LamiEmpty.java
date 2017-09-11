/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lami.types;

class LamiEmpty extends LamiData {

    public static final LamiEmpty INSTANCE = new LamiEmpty();

    private LamiEmpty() {}

    @Override
    public String toString() {
        return null;
    }
}
