/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.osgi;

import java.util.Dictionary;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;

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
            String filter = null; //??? "(" + Constants.OBJECTCLASS + "=" + HttpServiceTracker.class.getName() + ")"; 
            ServiceReference[] references = null;
            try {
                references = context.getServiceReferences(ServiceTracker.class.getName(), filter);
            } catch (InvalidSyntaxException e) {
                e.printStackTrace();
            }
            HttpServiceTracker httpService = null;
            if (references != null)
            {
                for (ServiceReference reference : references)
                {
                    if (context.getService(reference) instanceof HttpServiceTracker)
                    {
                        httpService = (HttpServiceTracker)context.getService(reference);
                        if (httpService instanceof HttpServiceTracker)
                            if (pid.equals(httpService.getProperty(HttpServiceTracker.SERVICE_PID)))
                            {  // This is the one!
                                httpService.updateConfigProperties(properties);
                                break;
                            }
                    }
                }
            }
        }
    }
}
