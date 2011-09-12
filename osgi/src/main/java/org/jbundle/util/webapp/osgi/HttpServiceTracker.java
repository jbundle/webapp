/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.http.HttpServlet;

import org.jbundle.util.osgi.finder.ClassServiceUtility;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * HttpServiceTracker - Wait for the http service to come up to add servlets.
 * 
 * @author don
 *
 */
public class HttpServiceTracker extends ServiceTracker {

    public static final String DEFAULT_CONTEXT_PATH = "/webstart";
    
	String contextPath = null;
	
	HttpServlet servlet = null;
	String servicePid = null;
	String servletClassName = null;
	String defaultSystemContextPath = null;
	HttpContext httpContext = null;
	
    /**
	 * Constructor - Listen for HttpService.
	 * @param context
	 */
    public HttpServiceTracker(BundleContext context, String servicePid, String servletClassName, String defaultSystemContextPath, HttpContext httpContext) {
        super(context, HttpService.class.getName(), null);
        this.servicePid = servicePid;
        this.servletClassName = servletClassName;
        this.defaultSystemContextPath = defaultSystemContextPath;
        this.httpContext = httpContext;
    }
    
    /**
     * Http Service is up, add my servlets.
     */
    public Object addingService(ServiceReference reference) {
        HttpService httpService = (HttpService) context.getService(reference);
        
        try {
            Dictionary<String,String> dictionary = null;
            if (servicePid != null)
            {
                ServiceReference caRef = context.getServiceReference(ConfigurationAdmin.class.getName());
                if (caRef != null)
                {
                    ConfigurationAdmin configAdmin = (ConfigurationAdmin)context.getService(caRef);
                    Configuration config = configAdmin.getConfiguration(servicePid);
                 
                    dictionary = config.getProperties();
                    if (dictionary == null)
                       dictionary = new Hashtable<String,String>();
                    contextPath = (String)dictionary.get(BaseOsgiServlet.CONTEXT_PATH);
                    if (contextPath == null)
                        contextPath = calculateContextPath();
                    // configure the Dictionary
                    dictionary.put(BaseOsgiServlet.CONTEXT_PATH, contextPath);                 
                    //push the configuration dictionary to the ConfigAdminService
                    config.update(dictionary);
                }            
            }
            if (contextPath == null)
                contextPath = calculateContextPath();
            
            servlet = (HttpServlet)ClassServiceUtility.getClassService().makeObjectFromClassName(servletClassName);
            HttpContext httpContext = this.httpContext;
            if (dictionary == null)
                dictionary = new Hashtable<String,String>();
            if (servlet instanceof BaseOsgiServlet)
                ((BaseOsgiServlet)servlet).init(context, servicePid, dictionary);
	        httpService.registerServlet(contextPath, servlet, dictionary, httpContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return httpService;
    }
    
    /**
     * Http Service is down, remove my servlets.
     */
    public void removedService(ServiceReference reference, Object service) {
        ((HttpService)service).unregister(contextPath);
        if (servlet instanceof BaseOsgiServlet)
            ((BaseOsgiServlet)servlet).free();
        super.removedService(reference, service);
    }
    
    /**
     * Change the contextPath.
     * @param contextPath
     */
    public void setContextPath(String contextPath)
    {
        if (contextPath.equals(this.contextPath))
            return;
        ServiceReference reference = context.getServiceReference(HttpService.class.getName());
        if (reference == null)
            return;
        HttpService httpService = (HttpService) context.getService(reference);
        httpService.unregister(this.contextPath);
        this.contextPath = contextPath;
        
        this.addingService(reference);  // Start it back up
    }
    
    public String calculateContextPath()
    {
        String contextPath = null;
        contextPath = context.getProperty(BaseOsgiServlet.CONTEXT_PATH);
        if (contextPath == null)
            contextPath = defaultSystemContextPath;
        if (contextPath == null)
            contextPath = DEFAULT_CONTEXT_PATH;
        return contextPath;
    }
    
}
