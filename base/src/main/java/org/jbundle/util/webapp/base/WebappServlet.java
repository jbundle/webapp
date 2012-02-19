/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.base;

import java.util.Dictionary;

/**
 * WebApp OSGi Servlet.
 * Template for a servlet that can be changed on the fly.
 * @author don
 *
 */
public interface WebappServlet
{
    public static final String PROPERTY_PREFIX = "org.jbundle.util.webapp.";  // In Config Service
    public static final String ALIAS = PROPERTY_PREFIX + "alias";  // In Config Service
    public static final String LOG_PARAM = PROPERTY_PREFIX + "log";
    public static final String BASE_PATH = "basePath"; // Prepend URL or path to the resource path

    /**
     * Constructor.
     * @param context
     */
    public void init(Object bundleContext, String servicePid, Dictionary<String, String> dictionary);
    /**
     * Free my resources.
     */
    public void free();
    /**
     * Change the properties.
     * @param dictionary
     * @return TODO
     */
    public boolean setProperties(Dictionary<String, String> properties);
    /**
     * Get the properties.
     * @return
     */
    public Dictionary<String, String> getProperties();
    /**
     * Do I have to restart the servlet after I change properties?
     * @return
     */
    public boolean restartRequired();
}
