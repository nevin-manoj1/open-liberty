/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.jpa.overridepersistencecontext.web;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnit;
import javax.servlet.annotation.WebServlet;

import org.junit.Test;

import com.ibm.ws.jpa.overridepersistencecontext.testlogic.OverridePersistenceContextLogic;
import com.ibm.ws.testtooling.testinfo.JPAPersistenceContext;
import com.ibm.ws.testtooling.testinfo.JPAPersistenceContext.PersistenceContextType;
import com.ibm.ws.testtooling.testinfo.JPAPersistenceContext.PersistenceInjectionType;
import com.ibm.ws.testtooling.vehicle.web.JPADBTestServlet;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/TestOverridePersistenceContextServlet")
public class TestOverridePersistenceContextServlet extends JPADBTestServlet {
    // Container Managed Transaction Scope
    @PersistenceContext(unitName = "OverridePersistenceContext_JTA", properties = { @PersistenceProperty(name = "javax.persistence.lock.timeout", value = "34567") })
    private EntityManager cmtsEm;

    // Application Managed JTA
    @PersistenceUnit(unitName = "OverridePersistenceContext_JTA")
    private EntityManagerFactory amjtaEmf;

    // Application Managed Resource-Local
    @PersistenceUnit(unitName = "OverridePersistenceContext_RL")
    private EntityManagerFactory amrlEmf;

    @PostConstruct
    private void initFAT() {
        testClassName = OverridePersistenceContextLogic.class.getName();

        jpaPctxMap.put("test-jpa-resource-amjta",
                       new JPAPersistenceContext("test-jpa-resource-amjta", PersistenceContextType.APPLICATION_MANAGED_JTA, PersistenceInjectionType.FIELD, "amjtaEmf"));
        jpaPctxMap.put("test-jpa-resource-amrl",
                       new JPAPersistenceContext("test-jpa-resource-amrl", PersistenceContextType.APPLICATION_MANAGED_RL, PersistenceInjectionType.FIELD, "amrlEmf"));
        jpaPctxMap.put("test-jpa-resource-cmts",
                       new JPAPersistenceContext("test-jpa-resource-cmts", PersistenceContextType.CONTAINER_MANAGED_TS, PersistenceInjectionType.FIELD, "cmtsEm"));
    }

    // testOverridePersistenceContext Test
    @Test
    public void jpa_jpaconfig_testOverridePersistenceContext_AMJTA_Web() throws Exception {
        final String testName = "jpa_jpaconfig_testOverridePersistenceContext_AMJTA_Web";
        final String testMethod = "testOverridePersistenceContext";
        final String testResource = "test-jpa-resource-amjta";
        executeTest(testName, testMethod, testResource);
    }

    @Test
    public void jpa_jpaconfig_testOverridePersistenceContext_AMRL_Web() throws Exception {
        final String testName = "jpa_jpaconfig_testOverridePersistenceContext_AMRL_Web";
        final String testMethod = "testOverridePersistenceContext";
        final String testResource = "test-jpa-resource-amrl";
        executeTest(testName, testMethod, testResource);
    }

    @Test
    public void jpa_jpaconfig_testOverridePersistenceContext_CMTS_Web() throws Exception {
        final String testName = "jpa_jpaconfig_testOverridePersistenceContext_CMTS_Web";
        final String testMethod = "testOverridePersistenceContext";
        final String testResource = "test-jpa-resource-cmts";
        executeTest(testName, testMethod, testResource);
    }
}
