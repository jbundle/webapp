/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.osgi;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Servlet;

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

    public static final String NAME = "name"; // The name of the servlet
    public static final String SERVICE_PID = "service.pid"; // The id of the data in the config registry
    public static final String SERVLET_CLASS = "servletClass"; // Optional class name for single servlets
    // Set this param to change root URL
    public static final String WEB_ALIAS_PREFIX = "org.jbundle.web.webalias"; // String to prefix web context with
    public static final String DEFAULT_WEB_ALIAS_PARAM = "org.jbundle.web.defaultwebalias"; // If no web url was passed in
    public static final String DEFAULT_WEB_ALIAS = "/webstart";
    public static final String WEB_ALIAS = BaseOsgiServlet.WEB_ALIAS;
    
    protected Dictionary<String, String> dictionary = null;

    protected HttpContext httpContext = null;

    /**
     * Constructor - Listen for HttpService.
     * 
     * @param context
     */
    public HttpServiceTracker(BundleContext context, HttpContext httpContext, Dictionary<String, String> dictionary) 
    {
        super(context, HttpService.class.getName(), null);
        this.httpContext = httpContext;
        this.dictionary = HttpServiceTracker.putAll(dictionary, null);
        if (context.getProperty(WEB_ALIAS_PREFIX) != null)
            this.dictionary.put(WEB_ALIAS_PREFIX, context.getProperty(WEB_ALIAS_PREFIX));
        if (context.getProperty(DEFAULT_WEB_ALIAS_PARAM) != null)
            this.dictionary.put(DEFAULT_WEB_ALIAS_PARAM, context.getProperty(DEFAULT_WEB_ALIAS_PARAM));
        if (context.getProperty(SERVLET_CLASS) != null)
            this.dictionary.put(SERVLET_CLASS, context.getProperty(SERVLET_CLASS));
        if (context.getProperty(SERVICE_PID) != null)
            this.dictionary.put(SERVICE_PID, context.getProperty(SERVICE_PID));
        if (context.getProperty(WEB_ALIAS) != null)
            this.dictionary.put(WEB_ALIAS, context.getProperty(WEB_ALIAS));
    }

    /**
     * Http Service is up, add my servlets.
     */
    public Object addingService(ServiceReference reference)
    {
        HttpService httpService = (HttpService) context.getService(reference);

        this.addServices(httpService);

        return httpService;
    }

    /**
     * Http Service is up, add my servlets.
     */
    public void addServices(HttpService httpService)
    {
        for (String name : getServletNames(dictionary)) {
            Servlet servlet = this.addService(name, dictionary, httpService); // Override this to add multiple http services
            servlets.put(name, servlet); // Null servlets are okay - they could be resource mappings
        }
    }

    /**
     * Http Service is up, add my servlet.
     * @param name TODO
     * @param dictionary
     */
    public Servlet addService(String name, Dictionary<String, String> dictionary, HttpService httpService)
    {
        Servlet servlet = null;
        String alias = this.getAliasFromName(name, dictionary);
        dictionary = this.updateDictionaryConfig(dictionary, true);
        try {
            servlet = this.makeServlet(dictionary);
            String servicePid = dictionary.get(SERVICE_PID);
            if (servlet instanceof BaseOsgiServlet)
                ((BaseOsgiServlet) servlet).init(context, servicePid, dictionary);
            if (servlet != null)
                httpService.registerServlet(alias, servlet, dictionary, httpContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return servlet;
    }
    
    /**
     * Create the servlet.
     * The SERVLET_CLASS property must be supplied.
     * @param dictionary
     * @return
     */
    public Servlet makeServlet(Dictionary<String, String> dictionary)
    {
        String servletClass = dictionary.get(SERVLET_CLASS);
        return (Servlet)ClassServiceUtility.getClassService().makeObjectFromClassName(servletClass);        
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
        for (String name : getServletNames(dictionary)) {
            Servlet servlet = this.getServletFromName(name);
            this.removeService(name, servlet, reference, service);
        }
    }

    /**
     * Http Service is down, remove my servlet.
     */
    public void removeService(String name, Servlet servlet, ServiceReference reference, Object service) {
        String alias = this.getAliasFromName(name, dictionary);
        ((HttpService) service).unregister(alias);
        if (servlet instanceof BaseOsgiServlet)
            ((BaseOsgiServlet) servlet).free();
    }

    /**
     * Get the (persistent) configuration dictionary from the service manager.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public Dictionary<String, String> updateDictionaryConfig(Dictionary<String, String> dictionary, boolean returnCopy)
    {
        if (dictionary == null)
            dictionary = new Hashtable<String, String>();
        try {
            String servicePid = dictionary.get(SERVICE_PID);
            if (servicePid != null)
            {
                ServiceReference caRef = context.getServiceReference(ConfigurationAdmin.class.getName());
                if (caRef != null)
                {
                    ConfigurationAdmin configAdmin = (ConfigurationAdmin)context.getService(caRef);
                    Configuration config = configAdmin.getConfiguration(servicePid);

                    Dictionary<String, String> configDictionary = config.getProperties();
                    if (configDictionary == null)
                        configDictionary = new Hashtable<String, String>();
                    // First, move all settings to dictionary
                    dictionary = HttpServiceTracker.putAll(configDictionary, dictionary);
                    dictionary.put(BaseOsgiServlet.WEB_ALIAS, this.calculateWebAlias(dictionary));
                    // Next, move all saveable settings to the config dictionary (and save them)
                    Enumeration<String> keys = dictionary.keys();
                    while (keys.hasMoreElements())
                    {
                        String key = keys.nextElement();
                        if (key.startsWith(BaseOsgiServlet.PROPERTY_PREFIX))
                            configDictionary.put(key, dictionary.get(key)); // Make sure all the fully qualified keys are persisted
                    }
                    // push the configuration dictionary to the ConfigAdminService
                    config.update(configDictionary);
                }
            }
            if (dictionary.get(BaseOsgiServlet.WEB_ALIAS) == null)
                dictionary.put(BaseOsgiServlet.WEB_ALIAS, this.calculateWebAlias(dictionary));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Dictionary<String, String> dictionaryOut = null;
        if (!returnCopy)
            dictionaryOut = dictionary;
        else
            dictionaryOut = HttpServiceTracker.putAll(dictionary, dictionaryOut);
        return dictionaryOut;
    }

    private Map<String, Servlet> servlets = new HashMap<String, Servlet>();

    /**
     * Look up the servlet.
     * @param path
     * @return
     */
    public Servlet getServletFromName(String path) {
        return servlets.get(path);
    }

    /**
     * Get all the web paths to add.
     * @param dictionary
     * 
     * @return
     */
    public String[] getServletNames(Dictionary<String, String> dictionary)
    { // Override this to supply more than one servlet
        String name = dictionary.get(NAME);
        if (name == null)
        {
            name = dictionary.get(WEB_ALIAS);
            if (name == null)
                return EMPTY_ARRAY;
            if (name.startsWith("/"))   // Always
                name = name.substring(1);
        }
        String[] paths = { name };
        return paths;
    }
    public static final String[] EMPTY_ARRAY = new String[0];

    /**
     * Get the web context path from the service name.
     * @param name
     * @param dictionary 
     * @return
     */
    public String getAliasFromName(String name, Dictionary<String, String> dictionary)
    {
        String alias = dictionary.get(WEB_ALIAS);
        if (alias == null)
            alias = HttpServiceTracker.addURLPath(dictionary.get(WEB_ALIAS_PREFIX), name);
        return alias;
    }

    /**
     * Figure out the correct web alias.
     * @param dictionary
     * @return
     */
    public String calculateWebAlias(Dictionary<String, String> dictionary)
    {
        String contextPath = dictionary.get(WEB_ALIAS);
        if (contextPath == null)
            contextPath = context.getProperty(BaseOsgiServlet.WEB_ALIAS);
        if (contextPath == null)
            contextPath = dictionary.get(DEFAULT_WEB_ALIAS_PARAM);
        if (contextPath == null)
            contextPath = DEFAULT_WEB_ALIAS;
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
     * Change the contextPath.
     * 
     * @param contextPath
     */
    public void setContextPath(String contextPath) {
        if (contextPath.equals(dictionary.get(WEB_ALIAS)))
            return;
        ServiceReference reference = context
                .getServiceReference(HttpService.class.getName());
        if (reference == null)
            return;
        HttpService httpService = (HttpService) context.getService(reference);
        httpService.unregister(dictionary.get(WEB_ALIAS));
        dictionary.put(WEB_ALIAS, contextPath);

        this.addingService(reference); // Start it back up
    }

    /**
     * 
     * @param sourceDictionary
     * @param destDictionary
     * @return
     */
    public static Dictionary<String, String> putAll(Dictionary<String, String> sourceDictionary, Dictionary<String, String> destDictionary)
    {
        if (destDictionary == null)
            destDictionary = new Hashtable<String, String>();
        if (sourceDictionary != null)
        {
            Enumeration<String> keys = sourceDictionary.keys();
            while (keys.hasMoreElements())
            {
                String key = keys.nextElement();
                destDictionary.put(key, sourceDictionary.get(key));
            }
        }
        return destDictionary;
    }
}
