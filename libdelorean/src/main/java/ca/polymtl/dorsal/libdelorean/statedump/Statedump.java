/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.statedump;

import ca.polymtl.dorsal.libdelorean.IStateSystemReader;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.*;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Wrapper object representing a full query, along with its corresponding
 * attributes. It allows to reconstruct an initial state from scratch.
 *
 * @author Alexandre Montplaisir
 * @author Philippe Proulx
 */
public class Statedump {

    private static final Logger LOGGER = Logger.getLogger(Statedump.class.getName());

    /** File format version. Bump if the format changes */
    private static final int STATEDUMP_FORMAT_VERSION = 1;

    private static final String STATEDUMP_DIRECTORY = ".tc-states"; //$NON-NLS-1$
    private static final String FILE_SUFFIX = ".statedump.json"; //$NON-NLS-1$

    private final List<String[]> fAttributes;
    private final List<StateValue> fStates;
    private final int fStatedumpVersion;

    /**
     * Baseline constructor. Builds a statedump by passing elements directly.
     *
     * @param attributes
     *            The list of state system attributes
     * @param states
     *            The corresponding states. The indices should match the
     *            previous list.
     * @param version
     *            Version of the statedump
     * @throws IllegalArgumentException
     *             If the two arrays don't have the same size
     */
    public Statedump(List<String[]> attributes, List<StateValue> states, int version) {
        if (attributes.size() != states.size()) {
            throw new IllegalArgumentException("Both lists should have the same number of elements"); //$NON-NLS-1$
        }
        fAttributes = ImmutableList.copyOf(attributes);
        fStates = ImmutableList.copyOf(states);
        fStatedumpVersion = version;
    }

    /**
     * "Online" constructor. Builds a statedump from a given state system and
     * timestamp.
     *
     * @param ss
     *            The state system for which to build the state dump
     * @param timestamp
     *            The timestamp at which to query the state to dump
     * @param version
     *            Version of the statedump
     */
    public Statedump(IStateSystemReader ss, long timestamp, int version) {
        List<StateInterval> fullQuery;
        try {
            fullQuery = ss.queryFullState(timestamp);
        } catch (StateSystemDisposedException e1) {
            fAttributes = Collections.EMPTY_LIST;
            fStates = Collections.EMPTY_LIST;
            fStatedumpVersion = -1;
            return;
        }

        ImmutableList.Builder<String[]> attributesBuilder = ImmutableList.builder();
        for (int quark = 0; quark < ss.getNbAttributes(); quark++) {
            attributesBuilder.add(ss.getFullAttributePathArray(quark));
        }
        fAttributes = attributesBuilder.build();

        List<StateValue> states = fullQuery.stream()
                .map(StateInterval::getStateValue)
                .collect(Collectors.toList());
        fStates = ImmutableList.copyOf(states);

        fStatedumpVersion = version;
    }

    /**
     * Get the list of attributes of this state dump.
     *
     * @return The attributes
     */
    public List<String[]> getAttributes() {
        return fAttributes;
    }

    /**
     * Get the state values of this state dump.
     *
     * @return The state values
     */
    public List<StateValue> getStates() {
        return fStates;
    }

    /**
     * Get the version of this statedump. Can be used to consider if a statedump
     * should be read or not if the analysis changed since it was written.
     *
     * @return The statedump's version
     */
    public int getVersion() {
        return fStatedumpVersion;
    }

    /**
     * Save this statedump at the given location.
     *
     * @param parentPath
     *            The location where to save the statedump file, usually in or
     *            close to its corresponding trace. It will be put under a Trace
     *            Compass-specific sub-directory.
     * @param ssid
     *            The state system ID of the state system we are saving. This
     *            will be used for restoration.
     * @throws IOException
     *             If there are problems creating or writing to the target
     *             directory
     */
    public void dumpState(Path parentPath, String ssid) throws IOException {
        /* Create directory if it does not exist */
        Path sdPath = parentPath.resolve(STATEDUMP_DIRECTORY);
        if (!Files.exists(sdPath)) {
            Files.createDirectory(sdPath);
        }

        /* Create state dump file */
        String fileName = ssid + FILE_SUFFIX;
        Path filePath = sdPath.resolve(fileName);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        Files.createFile(filePath);

        JSONObject root = new JSONObject();

        try (Writer bw = Files.newBufferedWriter(filePath, Charsets.UTF_8)) {
            /* Create the root object */
            root.put(Serialization.FORMAT_VERSION_KEY, STATEDUMP_FORMAT_VERSION);
            root.put(Serialization.ID_KEY, ssid);
            root.put(Serialization.STATEDUMP_VERSION_KEY, getVersion());

            /* Create the root state node */
            JSONObject rootNode = new JSONObject();
            rootNode.put(Serialization.CHILDREN_KEY, new JSONObject());
            root.put(Serialization.STATE_KEY, rootNode);

            /* Insert all the paths, types, and values */
            for (int i = 0; i < getAttributes().size(); i++) {
                String[] attribute = getAttributes().get(i);
                StateValue sv = getStates().get(i);

                Serialization.insertFrom(rootNode, attribute, 0, sv);
            }

            bw.write(root.toString(2));

        } catch (JSONException e) {
            /*
             * This should never happen. Any JSON exception means that there's a
             * bug in this code.
             */
            throw new IllegalStateException(e);
        }
    }

    /**
     * Retrieve a previously-saved statedump.
     *
     * @param parentPath
     *            The expected location of the statedump file. Like the
     *            corresponding parameter in {@link #dumpState}, this is the
     *            parent path of the TC-specific subdirectory.
     * @param ssid
     *            The ID of the state system to retrieve
     * @return The corresponding de-serialized statedump. Returns null if there
     *         are no statedump for this state system ID (or no statedump
     *         directory at all).
     */
    public static @Nullable Statedump loadState(Path parentPath, String ssid) {
        /* Find the state dump directory */
        Path sdPath = parentPath.resolve(STATEDUMP_DIRECTORY);
        if (!Files.isDirectory(sdPath)) {
            return null;
        }

        /* Find the state dump file */
        String fileName = ssid + FILE_SUFFIX;
        Path filePath = sdPath.resolve(fileName);
        if (!Files.exists(filePath)) {
            return null;
        }

        try (InputStreamReader in = new InputStreamReader(Files.newInputStream(filePath, StandardOpenOption.READ))) {
            BufferedReader bufReader = new BufferedReader(in);
            String json = bufReader.lines().collect(Collectors.joining("\n")); //$NON-NLS-1$
            JSONObject root = new JSONObject(json);

            return Serialization.stateDumpFromJsonObject(root, ssid);
        } catch (IOException | JSONException e) {
            return null;
        }
    }

    /**
     * Inner utility class for serialization-related values and methods.
     */
    private static final class Serialization {

        private Serialization() {}

        private static final String VALUE_KEY = "value"; //$NON-NLS-1$
        private static final String TYPE_KEY = "type"; //$NON-NLS-1$
        private static final String CHILDREN_KEY = "children"; //$NON-NLS-1$
        private static final String STATE_KEY = "state"; //$NON-NLS-1$
        private static final String ID_KEY = "id"; //$NON-NLS-1$
        private static final String FORMAT_VERSION_KEY = "format-version"; //$NON-NLS-1$
        private static final String STATEDUMP_VERSION_KEY = "statedump-version"; //$NON-NLS-1$
        private static final String BOOLEAN_TYPE = "boolean"; //$NON-NLS-1$
        private static final String DOUBLE_TYPE = "double"; //$NON-NLS-1$
        private static final String INT_TYPE = "int"; //$NON-NLS-1$
        private static final String LONG_TYPE = "long"; //$NON-NLS-1$
        private static final String NULL_TYPE = "null"; //$NON-NLS-1$
        private static final String STRING_TYPE = "string"; //$NON-NLS-1$
        private static final String UNKNOWN_TYPE = "unknown"; //$NON-NLS-1$
        private static final String DOUBLE_NAN = "nan"; //$NON-NLS-1$
        private static final String DOUBLE_POS_INF = "+inf"; //$NON-NLS-1$
        private static final String DOUBLE_NEG_INF = "-inf"; //$NON-NLS-1$

        private static void insertStateValueInStateNode(JSONObject stateNode, StateValue stateValue) throws JSONException {

            @NotNull String type;
            Object value;

            if (stateValue instanceof BooleanStateValue) {
                type = BOOLEAN_TYPE;
                value = ((BooleanStateValue) stateValue).getValue();
            } else if (stateValue instanceof DoubleStateValue) {
                type = DOUBLE_TYPE;
                double doubleValue = ((DoubleStateValue) stateValue).getValue();

                if (Double.isNaN(doubleValue)) {
                    value = DOUBLE_NAN;
                } else if (Double.isInfinite(doubleValue)) {
                    if (doubleValue < 0) {
                        value = DOUBLE_NEG_INF;
                    } else {
                        value = DOUBLE_POS_INF;
                    }
                } else {
                    value = doubleValue;
                }
            } else if (stateValue instanceof IntegerStateValue) {
                type = INT_TYPE;
                value = ((IntegerStateValue) stateValue).getValue();
            } else if (stateValue instanceof LongStateValue) {
                type = LONG_TYPE;
                value = ((LongStateValue) stateValue).getValue();
            } else if (stateValue instanceof NullStateValue) {
                type = NULL_TYPE;
                value = null;
            } else if (stateValue instanceof StringStateValue) {
                type = STRING_TYPE;
                value = ((StringStateValue) stateValue).getValue();
            } else {
                type = UNKNOWN_TYPE;
                value = stateValue.toString();
            }

            stateNode.put(TYPE_KEY, type);
            if (value != null) {
                stateNode.put(VALUE_KEY, value);
            }
        }

        private static void insertFrom(JSONObject stateNode, String[] attr, int attrIndex, StateValue stateValue) throws JSONException {
            if (attr.length == 0 || !stateNode.has(CHILDREN_KEY)) {
                throw new IllegalStateException();
            }

            JSONObject nodeChildren = stateNode.getJSONObject(CHILDREN_KEY);
            String curAttrElement = attr[attrIndex];

            if (!nodeChildren.has(curAttrElement)) {
                JSONObject newNode = new JSONObject();
                newNode.put(CHILDREN_KEY, new JSONObject());
                nodeChildren.put(curAttrElement, newNode);
            }

            JSONObject nearestChild = requireNonNull(nodeChildren.getJSONObject(curAttrElement));

            if (attrIndex == attr.length - 1) {
                /* end of recursion! */
                insertStateValueInStateNode(nearestChild, stateValue);
                return;
            }

            insertFrom(nearestChild, attr, attrIndex + 1, stateValue);
        }

        private static @Nullable StateValue stateValueFromJsonObject(JSONObject node) {
            StateValue stateValue;

            String type = node.optString(TYPE_KEY);
            if (type == null) {
                LOGGER.warning(() -> "Missing \"" + TYPE_KEY + "\" property in state node object"); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }

            switch (type) {
            case NULL_TYPE:
                stateValue = StateValue.nullValue();
                break;

            case BOOLEAN_TYPE:
                boolean boolValue;
                try {
                    boolValue = node.getBoolean(VALUE_KEY);
                } catch (JSONException e) {
                    LOGGER.warning(() -> String.format("Invalid or missing \"%s\" property (expecting a boolean) in state node object", VALUE_KEY)); //$NON-NLS-1$
                    return null;
                }
                stateValue = StateValue.newValueBoolean(boolValue);
                break;

            case INT_TYPE:
            case LONG_TYPE:
                long longValue;

                try {
                    longValue = node.getLong(VALUE_KEY);
                } catch (JSONException e) {
                    LOGGER.warning(() -> String.format("Invalid or missing \"%s\" property (expecting a number) in state node object", VALUE_KEY)); //$NON-NLS-1$
                    return null;
                }

                if (type.equals(INT_TYPE)) {
                    stateValue = StateValue.newValueInt((int) longValue);
                } else {
                    stateValue = StateValue.newValueLong(longValue);
                }
                break;

            case DOUBLE_TYPE:
                Double doubleValue;

                Object nodeValue = node.opt(VALUE_KEY);
                if (nodeValue == null) {
                    LOGGER.warning(() -> String.format("Missing \"%s\" property in state node object", VALUE_KEY)); //$NON-NLS-1$
                    return null;
                }

                if (nodeValue instanceof Double) {
                    doubleValue = (Double) nodeValue;
                } else if (nodeValue instanceof String) {
                    String strValue = (String) nodeValue;
                    switch (strValue) {
                    case DOUBLE_NAN:
                        doubleValue = Double.NaN;
                        break;
                    case DOUBLE_NEG_INF:
                        doubleValue = Double.NEGATIVE_INFINITY;
                        break;
                    case DOUBLE_POS_INF:
                        doubleValue = Double.POSITIVE_INFINITY;
                        break;
                    default:
                        doubleValue = null;
                    }
                } else {
                    doubleValue = null;
                }

                if (doubleValue == null) {
                    LOGGER.warning(() -> "Invalid \"" + VALUE_KEY + "\" property in state node object"); //$NON-NLS-1$ //$NON-NLS-2$
                    return null;
                }

                stateValue = StateValue.newValueDouble(doubleValue);
                break;

            case STRING_TYPE:
            case UNKNOWN_TYPE:
                String stringValue = node.optString(VALUE_KEY);
                if (stringValue == null) {
                    LOGGER.warning(() -> "Invalid or missing \"" + VALUE_KEY + "\" property (expecting a string) in state node object"); //$NON-NLS-1$ //$NON-NLS-2$
                    return null;
                }

                stateValue = StateValue.newValueString(stringValue);
                break;

            default:
                LOGGER.warning(() -> String.format("Unknown \"" + TYPE_KEY + "\" property (\"" + type + "\") in state node object")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                return null;
            }

            return stateValue;
        }

        private static boolean visitStateNode(JSONObject stateNode, List<String> attrStack,
                List<String[]> attributes, List<StateValue> values) {
            StateValue stateValue = null;
            String[] attribute = attrStack.stream().toArray(String[]::new);

            /* Ignore if it's the root node */
            if (attribute.length > 0) {
                stateValue = stateValueFromJsonObject(stateNode);

                if (stateValue == null) {
                    LOGGER.warning(() -> "Cannot rebuild state value for attribute \"" + Arrays.toString(attribute) + '\"'); //$NON-NLS-1$
                    return false;
                }

                /* Insert at the same position */
                attributes.add(attribute);
                values.add(stateValue);
            }

            if (stateNode.has(CHILDREN_KEY)) {
                JSONObject childrenNode;

                try {
                    childrenNode = stateNode.getJSONObject(CHILDREN_KEY);
                } catch (JSONException e) {
                    LOGGER.warning(() -> String.format("At attribute \"%s\": expecting an object for the \"%s\" property", //$NON-NLS-1$
                            Arrays.toString(attribute), CHILDREN_KEY));
                    return false;
                }

                Iterator<String> keyIt = childrenNode.keys();

                while (keyIt.hasNext()) {
                    String key = keyIt.next();
                    JSONObject childStateNode;

                    try {
                        childStateNode = requireNonNull(childrenNode.getJSONObject(key));
                    } catch (JSONException e) {
                        LOGGER.warning(() -> String.format("At attribute \"%s\": in \"%s\" node: expecting an object for the \"%s\" property", //$NON-NLS-1$
                                Arrays.toString(attribute), CHILDREN_KEY, key));
                        return false;
                    }

                    attrStack.add(key);

                    if (!visitStateNode(childStateNode, attrStack, attributes, values)) {
                        LOGGER.warning(() -> String.format("At attribute \"%s\": in \"%s\" node: failed to visit the \"%s\" property", //$NON-NLS-1$
                                Arrays.toString(attribute), CHILDREN_KEY, key));
                        return false;
                    }

                    attrStack.remove(attrStack.size() - 1);
                }
            }

            return true;
        }

        private static @Nullable Statedump stateDumpFromJsonObject(JSONObject root, String expectedSsid) {
            List<String[]> attributes = new ArrayList<>();
            List<StateValue> values = new ArrayList<>();

            int statedumpVersion;
            JSONObject rootStateNode;
            String keyToLookup = null;

            try {
                /* Read the file format version and ensure we can read it */
                keyToLookup = FORMAT_VERSION_KEY;
                int formatVersion = root.getInt(FORMAT_VERSION_KEY);

                if (formatVersion != STATEDUMP_FORMAT_VERSION) {
                    LOGGER.warning(() -> "Skipping statedump file with unknown version " + formatVersion); //$NON-NLS-1$
                    return null;
                }

                /*
                 * Read state system ID property and check if it matches the
                 * expected one.
                 */
                keyToLookup = ID_KEY;
                String ssid = root.getString(ID_KEY);

                if (!expectedSsid.equals(ssid)) {
                    LOGGER.warning(() -> "State system ID mismatch: expecting \"" + expectedSsid + "\", got \"" + ssid + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return null;
                }

                /* Read statedump version property */
                keyToLookup = STATEDUMP_VERSION_KEY;
                statedumpVersion = root.getInt(STATEDUMP_VERSION_KEY);


                /* Read state property (root state node) */
                keyToLookup = STATE_KEY;
                rootStateNode = requireNonNull(root.getJSONObject(STATE_KEY));

            } catch (JSONException e) {
                LOGGER.warning("Missing \"" + keyToLookup + "\" property in state dump (root) object"); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }

            if (!visitStateNode(rootStateNode, new ArrayList<String>(), attributes, values)) {
                LOGGER.warning(() -> "Failed to visit the root state node object"); //$NON-NLS-1$
                return null;
            }

            return new Statedump(attributes, values, statedumpVersion);
        }
    }

}
