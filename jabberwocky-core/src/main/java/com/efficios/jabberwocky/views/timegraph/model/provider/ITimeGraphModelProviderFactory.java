/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider;

import java.util.function.Supplier;

/**
 * Factory for {@link ITimeGraphModelProvider} objects.
 *
 * Used to register possible time graphs to the framework using the
 * {@link TimeGraphModelProviderManager}.
 *
 * @author Alexandre Montplaisir
 */
@FunctionalInterface
public interface ITimeGraphModelProviderFactory extends Supplier<ITimeGraphModelProvider> {

}
