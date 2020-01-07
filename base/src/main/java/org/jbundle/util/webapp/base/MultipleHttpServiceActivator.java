/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.base;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Start up and shutdown multiple web services.
 * @author don
 */
public class MultipleHttpServiceActivator extends HttpServiceActivator
{
    
    /**
     * Get all the web aliases to add http services for.
     *@return A list of the web services.
     */
    public String[] getAliases()
    {
        return EMPTY_ARRAY; // Override this!
    }
    private static final String[] EMPTY_ARRAY = new String[0];
    /**
     * Start this service.
     * Override this to do all the startup.
     * @return true if successful.
     */
    public Object startupService(BundleContext bundleContext)
    {
        // Good, Environment activator is up. Time to start up http services trackers
        
        for (String alias : getAliases())
        {
            Dictionary<String, Object> properties = new Hashtable<String, Object>();
            ServiceTracker serviceTracker = this.makeServletTracker(alias, properties);
            ((ServiceTracker)serviceTracker).open();
            context.registerService(ServiceTracker.class.getName(), serviceTracker, properties);    // Why isn't this done automatically?
        }

        return null;
    }
    
    /**
     * Start this service.
     * Override this to do all the startup.
     * @return true if successful.
     */
    @Override
    public boolean shutdownService(Object service, BundleContext context)
    {
        for (String alias : getAliases())
        {
            HttpServiceTracker serviceTracker = getServiceTracker(context, BaseWebappServlet.ALIAS, alias);
            if (serviceTracker != null)
                serviceTracker.close();
        }
        return true;
    }
    
    /**
     * Make a servlet tracker for the servlet at this alias.
     */
    public ServiceTracker makeServletTracker(String alias, Dictionary<String, Object> properties)
    {
        return null;    // Override this
    }
    
}
