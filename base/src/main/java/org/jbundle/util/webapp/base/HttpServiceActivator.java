/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.base;

import java.util.Dictionary;
import java.util.Hashtable;

import org.jbundle.util.osgi.BundleService;
import org.jbundle.util.osgi.bundle.BaseBundleService;
import org.jbundle.util.osgi.finder.ClassFinderActivator;
import org.jbundle.util.osgi.finder.ClassServiceUtility;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends BaseBundleService
{
    protected HttpServiceTracker httpServiceTracker = null;

    /**
     * Called when the http service tracker come up or is shut down.
     * Start or stop the bundle on start/stop.
     * @param event The service event.
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        BundleContext context = null;
        if (event != null)
            if (event.getServiceReference() != null)
                if (event.getServiceReference().getBundle() != null)
                    context = event.getServiceReference().getBundle().getBundleContext();
        if (event.getType() == ServiceEvent.REGISTERED)
        { // Osgi http Service is up, Okay to start the server
            ClassServiceUtility.log(context, LogService.LOG_INFO, "Starting the osgi Http Service tracker");
            
            if (httpServiceTracker == null)
    		    this.startupThisService(context);
        }
        if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            ClassServiceUtility.log(context, LogService.LOG_INFO, "Stopping the WebStart http service tracker");
            
            if (this.shutdownThisService(null, context))
                httpServiceTracker = null;        
        }        
    }
    /**
     * Start this service.
     * Override this to do all the startup.
     * @return true if successful.
     */
    @Override
    public boolean startupThisService(BundleContext bundleContext)
    {
        Dictionary<String, String> dictionary = new Hashtable<String, String>();
        dictionary.put(HttpServiceTracker.SERVICE_PID, getServicePid(context));
        dictionary.put(HttpServiceTracker.SERVLET_CLASS, getServletClass(context));
        dictionary.put(BaseWebappServlet.ALIAS, getWebAlias(context)); 
        httpServiceTracker = this.createServiceTracker(context, getHttpContext(), dictionary);
        httpServiceTracker.open();
        context.registerService(ServiceTracker.class.getName(), httpServiceTracker, dictionary);    // Why isn't this done automatically?

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
        if (httpServiceTracker != null)
            httpServiceTracker.close();
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
        return new HttpServiceTracker(context, getHttpContext(), dictionary);
    }
    
    /**
     * The service key in the config admin system.
     * @param context
     * @return By default the package name, else override this.
     */
    public String getServicePid(BundleContext context)
    {
        String servicePid = context.getProperty(HttpServiceTracker.SERVICE_PID);
        if (servicePid != null)
            return servicePid;
        servicePid = this.getServletClass(context);
        if ((servicePid == null)
                || (!servicePid.startsWith(HttpServiceActivator.PACKAGE_NAME)))
            servicePid = this.getClass().getName();
        return ClassFinderActivator.getPackageName(servicePid, false);
    }
    /**
     * Get the servlet class to activate.
     * @param context 
     * @return
     */
    public String getServletClass(BundleContext context)
    {
        return context.getProperty(HttpServiceTracker.SERVLET_CLASS);    // Override this to enable config admin.
    }
    /**
     * Get the web alias for this servlet.
     * @param context
     * @return
     */
    public String getWebAlias(BundleContext context)
    {
        String contextPath = context.getProperty(BaseWebappServlet.ALIAS);
        if (contextPath == null)
            contextPath = context.getProperty(BaseWebappServlet.ALIAS.substring(BaseWebappServlet.PROPERTY_PREFIX.length()));
        if (contextPath == null)
        {
            contextPath = this.getServicePid(context);
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
        return httpServiceTracker;
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
