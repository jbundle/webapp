/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.osgi;

import java.util.Dictionary;

/**
 * WebApp OSGi Servlet.
 * Template for a servlet that can be changed on the fly.
 * @author don
 *
 */
public interface WebappServlet
{
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
