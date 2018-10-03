/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Jonathan Rajotte-Julien
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lami.module;

import org.lttng.scope.lami.aspect.LamiTableEntryAspect;

import java.util.Objects;

/**
 * Description of a series to use in LAMI charts.
 *
 * Contains the aspects to use on the X and Y axes (one per axis).
 *
 * @author Jonathan Rajotte-Julien
 */
public class LamiXYSeriesDescription {

    private final LamiTableEntryAspect fXAspect;
    private final LamiTableEntryAspect fYAspect;

    /**
     * Constructor
     *
     * @param xAspect
     *            The aspect to use on the X axis
     * @param yAspect
     *            The aspect to use on the Y axis
     */
    public LamiXYSeriesDescription(LamiTableEntryAspect xAspect, LamiTableEntryAspect yAspect) {
        fXAspect = xAspect;
        fYAspect = yAspect;
    }

    /**
     * Get the aspect corresponding to the X axis.
     *
     * @return The X-axis aspect
     */
    public LamiTableEntryAspect getXAspect() {
        return fXAspect;
    }

    /**
     * Get the aspect corresponding to the Y axis.
     *
     * @return The Y-axis aspect
     */
    public LamiTableEntryAspect getYAspect() {
        return fYAspect;
    }


    @Override
    public String toString() {
        return "x:" + fXAspect.getLabel() + " y:" + fYAspect.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int hashCode() {
        return Objects.hash(fXAspect, fYAspect);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LamiXYSeriesDescription other = (LamiXYSeriesDescription) obj;
        if (!fXAspect.equals(other.fXAspect)) {
            return false;
        }
        if (!fYAspect.equals(other.fYAspect)) {
            return false;
        }
        return true;
    }

}
