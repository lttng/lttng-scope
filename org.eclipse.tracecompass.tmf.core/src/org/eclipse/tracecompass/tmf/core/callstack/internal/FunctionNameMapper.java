/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Marc-Andre Laperle - Map from binary file
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.callstack.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

/**
 * Class containing the different methods to import an address->name mapping.
 *
 * @author Alexandre Montplaisir
 */
public final class FunctionNameMapper {

    private FunctionNameMapper() {}

    private static final Pattern REMOVE_ZEROS_PATTERN = Pattern.compile("^0+(?!$)"); //$NON-NLS-1$

    /**
     * Get the function name mapping from a text file obtained by doing
     *
     * <pre>
     * nm[--demangle][binary] &gt; file.txt
     * </pre>
     *
     * @param mappingFile
     *            The file to import
     * @return A map&lt;address, function name&gt; of the results
     */
    public static @Nullable Map<String, String> mapFromNmTextFile(File mappingFile) {
        Map<String, String> map = new HashMap<>();

        try (FileReader fr = new FileReader(mappingFile);
                BufferedReader reader = new BufferedReader(fr);) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                /* Only lines with 3 elements contain addresses */
                String[] elems = line.split(" ", 3); //$NON-NLS-1$
                if (elems.length == 3) {
                    String address = stripLeadingZeros(elems[0]);
                    String name = elems[elems.length - 1];
                    map.put(address, name);
                }
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            /* Stop reading the file at this point */
        }

        if (map.isEmpty()) {
            return null;
        }
        return ImmutableMap.copyOf(map);
    }

    /**
     * Strip the leading zeroes from the address
     * */
    private static String stripLeadingZeros(String address) {
        return REMOVE_ZEROS_PATTERN.matcher(address).replaceFirst(""); //$NON-NLS-1$
    }

}
