/*******************************************************************************
 * Copyright (c) 2015, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.cdi.internal.archive.liberty;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import com.ibm.ws.cdi.CDIException;
import com.ibm.ws.cdi.CDIServiceUtils;
import com.ibm.ws.cdi.internal.interfaces.Application;
import com.ibm.ws.cdi.internal.interfaces.ArchiveType;
import com.ibm.ws.cdi.internal.interfaces.CDIArchive;
import com.ibm.ws.cdi.internal.interfaces.ExtensionArchive;
import com.ibm.ws.container.service.app.deploy.ApplicationInfo;
import com.ibm.ws.container.service.app.deploy.ContainerInfo;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.kernel.service.utils.FileUtils;

/**
 *
 */
public class RuntimeFactory {

    private final CDILibertyRuntime services;
    private final ConcurrentHashMap<ApplicationInfo, Application> applications = new ConcurrentHashMap<ApplicationInfo, Application>();

    /**
     * @param services
     */
    public RuntimeFactory(CDILibertyRuntime services) {
        this.services = services;
    }

    private Container getContainerForBundle(Bundle bundle) {
        //for a bundle, we can use the bundles own private data storage as the cache..
        File cacheDir = bundle.getDataFile("cache");
        if (!FileUtils.ensureDirExists(cacheDir)) {
            return null;
        }
        File cacheDirAdapt = bundle.getDataFile("cacheAdapt");
        if (!FileUtils.ensureDirExists(cacheDirAdapt)) {
            return null;
        }
        File cacheDirOverlay = bundle.getDataFile("cacheOverlay");
        if (!FileUtils.ensureDirExists(cacheDirOverlay)) {
            return null;
        }
        // Create an artifact API and adaptable Container implementation for the bundle
        ArtifactContainer artifactContainer = getServices().getArtifactContainerFactory().getContainer(cacheDir, bundle);
        Container container = getServices().getAdaptableModuleFactory().getContainer(cacheDirAdapt, cacheDirOverlay, artifactContainer);

        return container;

    }

    /**
     * @param appInfo
     * @return
     * @throws CDIException
     */
    public Application newApplication(ApplicationInfo appInfo) throws CDIException {
        Application application = this.applications.get(appInfo);
        if (application == null) {
            application = new ApplicationImpl(appInfo, this);
            Application oldApplication = this.applications.putIfAbsent(appInfo, application);
            if (oldApplication != null) {
                application = oldApplication;
            }
        }

        return application;
    }

    public Application removeApplication(ApplicationInfo appInfo) {
        return this.applications.remove(appInfo);
    }

    public CDIArchive newArchive(ApplicationImpl application,
                                 ContainerInfo containerInfo,
                                 ArchiveType archiveType,
                                 ClassLoader classLoader) {
        CDIArchive archive = new CDIArchiveImpl(application, containerInfo, archiveType, classLoader, this);

        return archive;
    }

    /**
     * {@inheritDoc}
     *
     * @param extraExtensions
     *
     * @throws CDIException
     */
    public ExtensionArchive getExtensionArchiveForBundle(Bundle bundle,
                                                         Set<String> extraClasses,
                                                         Set<String> extraAnnotations,
                                                         boolean applicationBDAsVisible,
                                                         boolean extClassesOnly, Set<String> extraExtensionClasses) throws CDIException {

        ExtensionArchive extensionArchive = null;
        Container container = getContainerForBundle(bundle);
        //get hold of bundle classloader
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        ClassLoader loader = bundleWiring.getClassLoader();
        if (container != null) {
            ExtensionContainerInfo containerInfo = new ExtensionContainerInfo(container, loader, CDIServiceUtils.getSymbolicNameWithoutMinorOrMicroVersionPart(bundle.getSymbolicName())
                                                                                                 + "_"
                                                                                                 + CDIServiceUtils.getOSGIVersionForBndName(bundle.getVersion()), extraClasses, extraAnnotations, applicationBDAsVisible, extClassesOnly);

            extensionArchive = new ExtensionArchiveImpl(containerInfo, this, extraExtensionClasses);
        }
        return extensionArchive;
    }

    /**
     * @return
     */
    public CDILibertyRuntime getServices() {
        return services;
    }

}
