/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Mathieu Rail - Added functionality for getting a module's requirements
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.activator.internal.Activator;
import org.eclipse.tracecompass.tmf.core.analysis.internal.TmfAnalysisModuleSourceConfigElement;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.resource.Requirement;

/**
 * Analysis module helper for modules provided by a plugin's configuration
 * elements.
 *
 * @author Geneviève Bastien
 */
public class TmfAnalysisModuleHelperConfigElement implements IAnalysisModuleHelper {

    private final IConfigurationElement fCe;

    /**
     * Constructor
     *
     * @param ce
     *            The source {@link IConfigurationElement} of this module helper
     */
    public TmfAnalysisModuleHelperConfigElement(IConfigurationElement ce) {
        fCe = ce;
    }

    // ----------------------------------------
    // Wrappers to {@link IAnalysisModule} methods
    // ----------------------------------------

    @Override
    public String getId() {
        String id = fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.ID_ATTR);
        if (id == null) {
            throw new IllegalStateException();
        }
        return id;
    }

    @Override
    public String getName() {
        String name = fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.NAME_ATTR);
        if (name == null) {
            throw new IllegalStateException();
        }
        return name;
    }

    @Override
    public boolean isAutomatic() {
        return Boolean.parseBoolean(fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.AUTOMATIC_ATTR));
    }

    @Override
    public boolean appliesToExperiment() {
        return Boolean.parseBoolean(fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.APPLIES_EXP_ATTR));
    }

    @Override
    public String getHelpText() {
        /*
         * FIXME: No need to externalize this. A better solution will be found
         * soon and this string is just temporary
         */
        return "The trace must be opened to get the help message"; //$NON-NLS-1$
    }

    @Override
    public String getIcon() {
        return fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.ICON_ATTR);
    }

    @Override
    public Bundle getBundle() {
        return ContributorFactoryOSGi.resolve(fCe.getContributor());
    }

    private static Class<?> loadClassForBundle(Bundle bundle, String className) throws ClassNotFoundException {
        BundleRevision br = bundle.adapt(BundleRevision.class);
        if ((br.getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
            /*
             * The bundle is a fragment, we cannot use Bundle.loadClass()
             * directly (it returns null). We will retrieve the host bundle and
             * use that instead to load the class.
             */
            /* Fragments should have exactly 1 host requirement */
            Requirement req = br.getRequirements(BundleRevision.HOST_NAMESPACE).get(0);
            String directive = req.getDirectives().get("filter"); //$NON-NLS-1$
            directive = requireNonNull(directive);
            /*
             * The string will will look like this:
             * (osgi.wiring.host=org.eclipse.tracecompass.tmf.core)
             */
            String hostPluginName = directive.substring(18, directive.length() - 1);

            Bundle hostBundle = Platform.getBundle(hostPluginName);
            return hostBundle.loadClass(className);
        }
        return bundle.loadClass(className);
    }

    private boolean appliesToTraceClass(Class<? extends ITmfTrace> traceclass) {
        boolean applies = false;

        /* Get the module's applying tracetypes */
        final IConfigurationElement[] tracetypeCE = fCe.getChildren(TmfAnalysisModuleSourceConfigElement.TRACETYPE_ELEM);
        for (IConfigurationElement element : tracetypeCE) {
            String className = null;
            try {
                className = element.getAttribute(TmfAnalysisModuleSourceConfigElement.CLASS_ATTR);
                Class<?> applyclass = loadClassForBundle(getBundle(), className);
                String classAppliesVal = element.getAttribute(TmfAnalysisModuleSourceConfigElement.APPLIES_ATTR);
                boolean classApplies = true;
                if (classAppliesVal != null) {
                    classApplies = Boolean.parseBoolean(classAppliesVal);
                }
                if (classApplies) {
                    applies |= applyclass.isAssignableFrom(traceclass);
                } else {
                    /*
                     * If the trace type does not apply, reset the applies
                     * variable to false
                     */
                    if (applyclass.isAssignableFrom(traceclass)) {
                        applies = false;
                    }
                }
            } catch (ClassNotFoundException | InvalidRegistryObjectException e) {
                Activator.instance().logError("Error in applies to trace. Trying to load class " + className, e); //$NON-NLS-1$
            }
        }
        return applies;
    }

    @Override
    public boolean appliesToTraceType(Class<? extends ITmfTrace> traceclass) {
        boolean applies = appliesToTraceClass(traceclass);

        /* Check if it applies to an experiment */
        if (!applies && TmfExperiment.class.isAssignableFrom(traceclass)) {
            applies = appliesToExperiment();
        }
        return applies;
    }

    @Override
    public Iterable<Class<? extends ITmfTrace>> getValidTraceTypes() {
        Set<Class<? extends ITmfTrace>> traceTypes = new HashSet<>();

        for (TraceTypeHelper tth : TmfTraceType.getTraceTypeHelpers()) {
            if (appliesToTraceType(tth.getTraceClass())) {
                traceTypes.add(tth.getTraceClass());
            }
        }

        return traceTypes;
    }

    // ---------------------------------------
    // Functionalities
    // ---------------------------------------

    private IAnalysisModule createModule() {
        IAnalysisModule module = null;
        try {
            module = (IAnalysisModule) fCe.createExecutableExtension(TmfAnalysisModuleSourceConfigElement.ANALYSIS_MODULE_ATTR);
            module.setName(getName());
            module.setId(getId());
        } catch (CoreException e) {
            Activator.instance().logError("Error getting analysis modules from configuration files", e); //$NON-NLS-1$
        }
        return module;
    }

    @Override
    public IAnalysisModule newModule(ITmfTrace trace) throws TmfAnalysisException {

        /* Check if it applies to trace itself */
        boolean applies = appliesToTraceClass(trace.getClass());
        /*
         * If the trace is an experiment, check if this module would apply to an
         * experiment should it apply to one of its traces.
         */
        if (!applies && (trace instanceof TmfExperiment) && appliesToExperiment()) {
            for (ITmfTrace expTrace : TmfTraceManager.getTraceSet(trace)) {
                if (appliesToTraceClass(expTrace.getClass())) {
                    applies = true;
                    break;
                }
            }
        }

        if (!applies) {
            return null;
        }

        IAnalysisModule module = createModule();
        if (module == null) {
            return null;
        }

        module.setAutomatic(isAutomatic());

        /* Get the module's parameters */
        final IConfigurationElement[] parametersCE = fCe.getChildren(TmfAnalysisModuleSourceConfigElement.PARAMETER_ELEM);
        for (IConfigurationElement element : parametersCE) {
            String paramName = element.getAttribute(TmfAnalysisModuleSourceConfigElement.NAME_ATTR);
            if (paramName == null) {
                continue;
            }
            module.addParameter(paramName);
            String defaultValue = element.getAttribute(TmfAnalysisModuleSourceConfigElement.DEFAULT_VALUE_ATTR);
            if (defaultValue != null) {
                module.setParameter(paramName, defaultValue);
            }
        }
        if (module.setTrace(trace)) {
            TmfAnalysisManager.analysisModuleCreated(module);
        } else {
            module.dispose();
            module = null;
        }

        return module;

    }

    @Override
    public String getHelpText(@NonNull ITmfTrace trace) {
        IAnalysisModule module = createModule();
        if (module != null) {
            String ret = module.getHelpText(trace);
            module.dispose();
            return ret;
        }
        return getHelpText();

    }
}
