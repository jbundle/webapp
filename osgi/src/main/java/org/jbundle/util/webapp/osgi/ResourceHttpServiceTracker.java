/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.osgi;

import java.util.Dictionary;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * ResourceHttpServiceTracker - A tracker that registers (and unregisters) an alias to a resource name.
 * 
 * @author don
 *
 */
public class ResourceHttpServiceTracker extends HttpServiceTracker {

    public static final String RESOURCE_NAME = "name";
    
    public ResourceHttpServiceTracker(BundleContext context, HttpContext httpContext, Dictionary<String, String> dictionary) {
        super(context, httpContext, dictionary);
    }

    /**
     * Http Service is up, add my resource.
     */
    public Object addingService(ServiceReference reference)
    {
        HttpService httpService = (HttpService) context.getService(reference);

        String alias = this.getAlias();
        String name = this.getProperty(RESOURCE_NAME);
        if (name == null)
            name = alias;
        try {
            httpService.registerResources(alias, name, httpContext);
        } catch (NamespaceException e) {
            e.printStackTrace();
        }

        return httpService;
    }
}
