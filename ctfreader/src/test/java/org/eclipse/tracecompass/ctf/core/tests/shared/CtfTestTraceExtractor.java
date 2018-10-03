/*******************************************************************************
 * Copyright (c) 2017 EfficiOS Inc.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.shared;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.lttng.scope.ttt.ctf.CtfTestTrace;

/**
 * Wrapper around {@link CtfTestTrace} that will extract the resources to a
 * temporary directory, and take care of deleting it once the {@link #close()}
 * method is invoked.
 *
 * If you keep references to these objects, make sure you call {@link #close()}
 * on them, or else you might leave dangling temporary files around!
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("nls")
public class CtfTestTraceExtractor implements AutoCloseable {

    private final Path fParentPath;
    private final CTFTrace fTrace;

    /**
     * Extract the given
     *
     * @param trace
     *            The test trace to extract
     * @return The wrapper object you can close() to delete the temporary trace
     */
    public static synchronized CtfTestTraceExtractor extractTestTrace(CtfTestTrace trace) {
        Path tracePath;
        try {
            Path parentPath = Files.createTempDirectory("test-trace"); //$NON-NLS-1$
            tracePath = extractUrlToTempDir(trace.getTraceURL(), parentPath);
            return new CtfTestTraceExtractor(tracePath, parentPath);
        } catch (IOException | CTFException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Constructor
     *
     * @param tracePath
     *            Path to the actual trace
     * @param parentPath
     *            Top-level extracted directory. Will be deleted on cleanup.
     * @throws CTFException
     *             If something goes wrong
     */
    private CtfTestTraceExtractor(Path tracePath, Path parentPath) throws CTFException {
        fParentPath = parentPath;
        fTrace = new CTFTrace(tracePath.toString());
    }

    /**
     * Retrieve the corresponding {@link CTFTrace} from this extracted test
     * trace.
     *
     * @return The CTFTrace
     */
    public CTFTrace getTrace() {
        return fTrace;
    }

    @Override
    public void close() {
        /* Delete the parent directory of this trace */
        deleteDirectoryRecursively(fParentPath);
    }

    private static Path extractUrlToTempDir(URL sourceURL, Path destinationPath) throws IOException {
        String urlString = sourceURL.getFile();
        String[] elems = urlString.split("!");
        // Remove the "file:"
        String jarFileName = elems[0].substring(5);
        // Remove the starting "/"
        String resourcePath = elems[1].substring(1);

        try (JarFile jar = new JarFile(jarFileName)) {
            final List<JarEntry> allEntries = Collections.list(jar.entries());

            /* Get the "top-level" directory of the trace we want to extract */
            List<JarEntry> entriesToExtract = allEntries.stream()
                    .filter(entry -> entry.getName().startsWith(resourcePath + "/")).collect(Collectors.toList());

            entriesToExtract.stream()
                .sorted(Comparator.comparing(JarEntry::getName))
                .forEach(entry -> {
                    String name = entry.getName();
                    try {
                        if (name.endsWith("/")) {
                            /*
                             * This a directory. We will create it on the file
                             * system
                             */
                            /*
                             * Note that in the list of JarEntries, directories are
                             * always placed first before the files, luckily.
                             */
                            String dirName = name.substring(0, name.length());
                            Path dir = destinationPath.resolve(dirName);
                            Files.createDirectories(dir);
                        } else {
                            /*
                             * This is a file, extract it in the corresponding
                             * location
                             */
                            try (InputStream is = jar.getInputStream(entry);) {
                                Path fileName = destinationPath.resolve(name);
                                Files.copy(is, fileName);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error extracting trace"); //$NON-NLS-1$
                        e.printStackTrace();
                    }
                });
        }

        return destinationPath.resolve(resourcePath);
    }

    /**
     * Delete a directory recursively.
     *
     * @param directory
     *            The directory to delete
     */
    public static void deleteDirectoryRecursively(Path directory) {
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
             });
        } catch (IOException e) {
        }
    }

}
