/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.base;

import java.util.Dictionary;

import org.jbundle.util.osgi.BundleConstants;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 * Call me when my configuration data changes.
 * @author don
 *
 */
public class HttpConfigurator implements ManagedService {
    BundleContext context = null;
    String pid = null;
    
    public HttpConfigurator(BundleContext context, String pid)
    {
        super();
        this.context = context;
        this.pid = pid;
    }
    
    /**
     * 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void updated(Dictionary properties) throws ConfigurationException
    {
        // Stop and start the service with the new context.
        if (properties == null) {
            // no configuration from configuration admin
            // or old configuration has been deleted
        } else {
            // apply configuration from config admin
            HttpServiceTracker httpService = HttpServiceActivator.getServiceTracker(context, BundleConstants.SERVICE_PID, pid);
            if (httpService != null)
                httpService.configPropertiesUpdated(properties);
        }
    }
}
