/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2012-2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean;

import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;

import java.util.List;

/**
 * This is the quark-read-only interface to the generic state system.
 * It only contains the quark-getting methods.
 *
 * @author Alexandre Montplaisir
 */
public interface IStateSystemQuarkResolver {

    /** Quark representing the root attribute */
    int ROOT_ATTRIBUTE = -1;

    /**
     * Get the ID of this state system.
     *
     * @return The state system's ID
     */
    String getSSID();

    /**
     * Return the current total amount of attributes in the system. This is also
     * equal to the quark that will be assigned to the next attribute that's
     * created.
     *
     * @return The current number of attributes in the system
     */
    int getNbAttributes();

    /**
     * Basic quark-retrieving method. Pass an attribute in parameter as an array
     * of strings, the matching quark will be returned.
     *
     * This version will NOT create any new attributes. If an invalid attribute
     * is requested, an exception will be thrown.
     *
     * @param attribute
     *            Attribute given as its full path in the Attribute Tree
     * @return The quark of the requested attribute, if it existed.
     * @throws AttributeNotFoundException
     *             This exception is thrown if the requested attribute simply
     *             did not exist in the system.
     */
    int getQuarkAbsolute(String... attribute)
            throws AttributeNotFoundException;

    /**
     * "Relative path" quark-getting method. Instead of specifying a full path,
     * if you know the path is relative to another attribute for which you
     * already have the quark, use this for better performance.
     *
     * This is useful for cases where a lot of modifications or queries will
     * originate from the same branch of the attribute tree : the common part of
     * the path won't have to be re-hashed for every access.
     *
     * This version will NOT create any new attributes. If an invalid attribute
     * is requested, an exception will be thrown.
     *
     * @param startingNodeQuark
     *            The quark of the attribute from which 'subPath' originates.
     * @param subPath
     *            "Rest" of the path to get to the final attribute
     * @return The matching quark, if it existed
     * @throws IndexOutOfBoundsException
     *             If the starting node quark is out of range
     * @throws AttributeNotFoundException
     *             If the sub-attribute does not exist
     */
    int getQuarkRelative(int startingNodeQuark, String... subPath)
            throws AttributeNotFoundException;

    /**
     * Return the sub-attributes of the target attribute, as a List of quarks.
     *
     * @param quark
     *            The attribute of which you want to sub-attributes. You can use
     *            "-1" here to specify the root node.
     * @param recursive
     *            True if you want all recursive sub-attributes, false if you
     *            only want the first level.
     * @return A List of integers, matching the quarks of the sub-attributes.
     * @throws AttributeNotFoundException
     *             If the quark was not existing or invalid.
     */
     List<Integer> getSubAttributes(int quark, boolean recursive)
            throws AttributeNotFoundException;

    /**
     * Return the sub-attributes of the target attribute, as a List of quarks,
     * similarly to {@link #getSubAttributes(int, boolean)}, but with an added
     * regex pattern to filter on the return attributes.
     *
     * @param quark
     *            The attribute of which you want to sub-attributes. You can use
     *            "-1" here to specify the root node.
     * @param recursive
     *            True if you want all recursive sub-attributes, false if you
     *            only want the first level. Note that the returned value will
     *            be flattened.
     * @param pattern
     *            The regular expression to match the attribute base name.
     * @return A List of integers, matching the quarks of the sub-attributes
     *         that match the regex. An empty list is returned if there is no
     *         matching attribute.
     * @throws AttributeNotFoundException
     *             If the 'quark' was not existing or invalid.
     */
     List<Integer> getSubAttributes(int quark, boolean recursive, String pattern)
            throws AttributeNotFoundException;

    /**
     * Batch quark-retrieving method. This method allows you to specify a path
     * pattern which includes a wildcard "*" somewhere. It will check all the
     * existing attributes in the attribute tree and return those who match the
     * pattern.
     *
     * For example, passing ("Threads", "*", "Exec_mode") will return the list
     * of quarks for attributes "Threads/1000/Exec_mode",
     * "Threads/1500/Exec_mode", and so on, depending on what exists at this
     * time in the attribute tree.
     *
     * If no wildcard is specified, the behavior is the same as
     * getQuarkAbsolute() (except it will return a List with one entry). This
     * method will never create new attributes.
     *
     * Only one wildcard "*" is supported at this time.
     *
     * @param pattern
     *            The array of strings representing the pattern to look for. It
     *            should ideally contain one entry that is only a "*".
     * @return A List of attribute quarks, representing attributes that matched
     *         the pattern. If no attribute matched, the list will be empty (but
     *         not null).
     */
    List<Integer> getQuarks(String... pattern);

    /**
     * Return the name assigned to this quark. This returns only the "basename",
     * not the complete path to this attribute.
     *
     * @param attributeQuark
     *            The quark for which we want the name
     * @return The name of the quark
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     */
    String getAttributeName(int attributeQuark);

    /**
     * This returns the slash-separated path of an attribute by providing its
     * quark
     *
     * @param attributeQuark
     *            The quark of the attribute we want
     * @return One single string separated with '/', like a filesystem path
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     */
    String getFullAttributePath(int attributeQuark);

    /**
     * Return the full attribute path, as an array of strings representing each
     * element.
     *
     * @param attributeQuark
     *            The quark of the attribute we want.
     * @return The array of path elements
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     */
    String [] getFullAttributePathArray(int attributeQuark);

    /**
     * Returns the parent quark of the attribute.
     *
     * @param attributeQuark
     *            The quark of the attribute
     * @return Quark of the parent attribute or <code>-1</code> if root quark or
     *         no parent.
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     */
    int getParentAttributeQuark(int attributeQuark);

}
