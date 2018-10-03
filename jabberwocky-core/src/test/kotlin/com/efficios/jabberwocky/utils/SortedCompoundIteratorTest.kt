/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.utils

import java.util.*

/**
 * Tests for the [SortedCompoundIterator].
 */
class SortedCompoundIteratorTest : SortedCompoundIteratorTestBase() {

    override fun <T> provideCompoundIterator(iterators: Collection<Iterator<T>>, comparator: Comparator<T>): Iterator<T> {
        return SortedCompoundIterator(iterators, comparator)
    }


}