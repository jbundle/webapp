/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.base;

import java.util.Dictionary;

import org.jbundle.util.osgi.bundle.BaseBundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends BaseBundleActivator
{
	/**
	 * Setup the application properties.
	 * Override this to set the properties.
	 * @param bundleContext BundleContext
	 */
	public void init()
	{
		super.init();
        this.setProperty(BaseWebappServlet.ALIAS, getWebAlias()); 		
	}
    /**
     * Start this service.
     * Override this to do all the startup.
     * @return true if successful.
     */
    @Override
    public Object startupService(BundleContext bundleContext)
    {
        service = this.createServiceTracker(context, getHttpContext(), this.getProperties());
        ((HttpServiceTracker)service).open();

        return service;
    }
    /**
     * Get the interface/service class name.
     * @return
     */
    public Class<?> getInterfaceClass()
    {
		return ServiceTracker.class;
    }
    
    /**
     * Shutdown this service.
     * Override this to do all the startup.
     * @return true if successful.
     */
    @Override
    public boolean shutdownService(Object service, BundleContext context)
    {
        if (this.service != null)
            ((HttpServiceTracker)this.service).close();
        return true;
    }
    
    /**
     * Create the service tracker.
     * @param context
     * @param httpContext
     * @param dictionary
     * @return
     */
    public HttpServiceTracker createServiceTracker(BundleContext context, HttpContext httpContext, Dictionary<String, String> dictionary)
    {
        if (httpContext == null)
            httpContext = getHttpContext();
        return new HttpServiceTracker(context, httpContext, dictionary);
    }
    /**
     * Get the web alias for this servlet.
     * @param context
     * @return
     */
    public String getWebAlias()
    {
        String contextPath = context.getProperty(BaseWebappServlet.ALIAS);
        if (contextPath == null)
            contextPath = context.getProperty(BaseWebappServlet.ALIAS.substring(BaseWebappServlet.PROPERTY_PREFIX.length()));
        if (contextPath == null)
        {
            contextPath = this.getServicePid();
            if (contextPath.lastIndexOf('.') != -1)
                contextPath = contextPath.substring(contextPath.lastIndexOf('.') + 1);
        }
        if (!contextPath.startsWith("/"))
            contextPath = "/" + contextPath;
        return contextPath;
    }
    /**
     * Get the Servlet context for this servlet.
     * Override if different from default context.
     * @return The httpcontext.
     */
    public HttpContext getHttpContext()
    {
        return null;    // Override this if you don't want to use the default http context
    }
    /**
     * Get my service tracker
     * @return
     */
    public HttpServiceTracker getServiceTracker()
    {
        return (HttpServiceTracker)service;
    }
    /**
     * Get this service tracker.
     * @param alias
     * @return
     */
    public static HttpServiceTracker getServiceTracker(BundleContext context, String key, String value)
    {
        String filter = "(" + key + "=" + value + ")";
        try {
            ServiceReference[] references = context.getServiceReferences(ServiceTracker.class.getName(), filter);
            if ((references == null) || (references.length == 0))
                references = context.getServiceReferences(ServiceTracker.class.getName(), null);   // Being paranoid
            if (references != null) {
                for (ServiceReference reference : references)
                {
                    Object service = context.getService(reference);
                    if (service instanceof HttpServiceTracker)    // Always
                        if (value.equals(((HttpServiceTracker)service).getProperty(key)))   // Being paranoid
                            return (HttpServiceTracker)service;
                }
            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        return null;    // Not found
    }
}
