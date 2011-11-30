/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.base;

import java.util.Dictionary;
import java.util.Hashtable;

import org.jbundle.util.osgi.BundleService;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Start up and shutdown multiple web services.
 * @author don
 */
public class MultipleHttpServiceActivator extends org.jbundle.util.webapp.base.HttpServiceActivator
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
    public boolean startupThisService(BundleService bundleService, BundleContext context)
    {
        // Good, Environment activator is up. Time to start up http services trackers
        
        for (String alias : getAliases())
        {
            Dictionary<String, String> properties = new Hashtable<String, String>();
            ServiceTracker serviceTracker = this.makeServletTracker(alias, properties);
            serviceTracker.open();
            context.registerService(ServiceTracker.class.getName(), serviceTracker, properties);    // Why isn't this done automatically?
        }

        return true;
    }
    
    /**
     * Start this service.
     * Override this to do all the startup.
     * @return true if successful.
     */
    @Override
    public boolean shutdownThisService(BundleService bundleService, BundleContext context)
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
    public ServiceTracker makeServletTracker(String alias, Dictionary<String, String> properties)
    {
        return null;    // Override this
    }
    
}
