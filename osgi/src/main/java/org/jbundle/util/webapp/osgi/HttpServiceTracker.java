/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.osgi;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Servlet;
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

    public static final String SERVICE_PID = "service.pid"; // The id of the data in the config registry
    public static final String SERVLET_CLASS = "servletClass"; // Optional class name for single servlets
    // Set this param to change root URL
    public static final String WEB_CONTEXT = "org.jbundle.web.webcontext";
    public static final String DEFAULT_CONTEXT_PATH_PARAM = "defaultContextPath";

    public static final String DEFAULT_CONTEXT_PATH = "/webstart";

    protected String webContextPath = null; // The web url (if this is a single servlet)

    protected String defaultContextPath = null; // If no web url was passed in

    protected HttpContext httpContext = null;

    String contextPath = null; // The path that this http service is registered to

    String servicePid = null; // The registration key that my configuration data is stored under (typically the package name)
    String servletClassName = null; // The servlet class name to create

    /**
     * Constructor - Listen for HttpService.
     * 
     * @param context
     */
    public HttpServiceTracker(BundleContext context, HttpContext httpContext, Dictionary<String, String> dictionary) {
        super(context, HttpService.class.getName(), null);
        this.httpContext = httpContext;
        if (dictionary != null) {
            defaultContextPath = getProperty(DEFAULT_CONTEXT_PATH_PARAM,
                    context, dictionary);
            webContextPath = getProperty(WEB_CONTEXT, context, dictionary);
            servicePid = getProperty(SERVICE_PID, context, dictionary);
            servletClassName = getProperty(SERVLET_CLASS, context, dictionary);
        }
    }

    /**
     * Http Service is up, add my servlets.
     */
    public Object addingService(ServiceReference reference) {
        HttpService httpService = (HttpService) context.getService(reference);

        this.addServices(httpService);

        return httpService;
    }

    /**
     * Http Service is up, add my servlets.
     */
    public void addServices(HttpService httpService) {
        for (String name : getServletNames()) {
            Servlet servlet = this.addService(name, httpService); // Override this to add multiple http services
            servlets.put(name, servlet); // Null servlets are okay - they could be resource mappings
        }
    }

    /**
     * Http Service is up, add my servlet.
     */
    public Servlet addService(String name, HttpService httpService) {
        HttpServlet servlet = null;
        Dictionary<String, String> dictionary = this.getDictionary();
        try {
            servlet = (HttpServlet) ClassServiceUtility.getClassService().makeObjectFromClassName(servletClassName);
            if (servlet instanceof BaseOsgiServlet)
                ((BaseOsgiServlet) servlet).init(context, servicePid, dictionary);
            if (servlet != null)
                httpService.registerServlet(contextPath, servlet, dictionary, httpContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return servlet;
    }

    /**
     * Http Service is down, remove my servlets.
     */
    public void removedService(ServiceReference reference, Object service) {
        this.removeServices(reference, service);
        super.removedService(reference, service);
    }

    /**
     * Http Service is down, remove my servlets.
     */
    public void removeServices(ServiceReference reference, Object service) {
        for (String name : getServletNames()) {
            Servlet servlet = this.getServletFromName(name);
            this.removeService(name, servlet, reference, service);
        }
    }

    /**
     * Http Service is down, remove my servlet.
     */
    public void removeService(String name, Servlet servlet,
            ServiceReference reference, Object service) {
        name = this.getPathFromName(name);
        ((HttpService) service).unregister(name);
        if (servlet instanceof BaseOsgiServlet)
            ((BaseOsgiServlet) servlet).free();
    }

    /**
     * Get the (persistent) configuration dictionary from the service manager.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public Dictionary<String, String> getDictionary()
    {
        Dictionary<String, String> dictionary = null;
        try {
            if (servicePid != null) {
                ServiceReference caRef = context.getServiceReference(ConfigurationAdmin.class.getName());
                if (caRef != null) {
                    ConfigurationAdmin configAdmin = (ConfigurationAdmin) context.getService(caRef);
                    Configuration config = configAdmin.getConfiguration(servicePid);

                    dictionary = config.getProperties();
                    if (dictionary == null)
                        dictionary = new Hashtable<String, String>();
                    contextPath = (String) dictionary.get(BaseOsgiServlet.CONTEXT_PATH);
                    if (contextPath == null)
                        contextPath = calculateContextPath();
                    // configure the Dictionary
                    dictionary.put(BaseOsgiServlet.CONTEXT_PATH, contextPath);
                    // push the configuration dictionary to the ConfigAdminService
                    config.update(dictionary);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (dictionary == null)
            dictionary = new Hashtable<String, String>();
        return dictionary;
    }

    private Map<String, Servlet> servlets = new HashMap<String, Servlet>();

    public Servlet getServletFromName(String path) {
        return servlets.get(path);
    }

    /**
     * 
     * @param name
     * @return
     */
    public String getPathFromName(String name) {
        return HttpServiceTracker.addURLPath(webContextPath, name);
    }

    /**
     * Get all the web paths to add.
     * 
     * @return
     */
    public String[] getServletNames() { // Override this to supply more than one servlet
        if (contextPath == null)
            contextPath = calculateContextPath();
        if (contextPath == null)
            return EMPTY_ARRAY;
        String[] paths = { contextPath };
        return paths;
    }

    public static final String[] EMPTY_ARRAY = new String[0];

    public String calculateContextPath() {
        String contextPath = null;
        contextPath = context.getProperty(BaseOsgiServlet.CONTEXT_PATH);
        if (contextPath == null)
            contextPath = defaultContextPath;
        if (contextPath == null)
            contextPath = DEFAULT_CONTEXT_PATH;
        return contextPath;
    }

    /**
     * Add the base path to get an http path (**Move this to Util?**)
     * 
     * @param basePath
     * @param path
     * @return
     */
    public static String addURLPath(String basePath, String path) {
        if (basePath == null)
            basePath = "";
        if ((!basePath.endsWith("/")) && (!path.startsWith("/")))
            path = "/" + path;
        if (basePath.length() > 0)
            path = basePath + path;
        if (path.length() == 0)
            path = "/";
        else if ((path.length() > 1) && (path.endsWith("/")))
            path = path.substring(0, path.length() - 1);
        return path;
    }

    /**
     * Get a property from the context or dictionary.
     * 
     * @param key
     * @param context
     * @param dictionary
     * @return
     */
    public static String getProperty(String key, BundleContext context,
            Dictionary<String, String> dictionary) {
        String value = null;
        if (context != null)
            value = context.getProperty(key);
        if (value == null)
            if (dictionary != null)
                value = dictionary.get(key);
        return value;
    }

    /**
     * Change the contextPath.
     * 
     * @param contextPath
     */
    public void setContextPath(String contextPath) {
        if (contextPath.equals(this.contextPath))
            return;
        ServiceReference reference = context
                .getServiceReference(HttpService.class.getName());
        if (reference == null)
            return;
        HttpService httpService = (HttpService) context.getService(reference);
        httpService.unregister(this.contextPath);
        this.contextPath = contextPath;

        this.addingService(reference); // Start it back up
    }

}
