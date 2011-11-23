/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.osgi;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.Servlet;

import org.jbundle.util.osgi.finder.ClassServiceUtility;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * HttpServiceTracker - Wait for the http service to come up to add this servlet.
 * 
 * @author don
 * 
 */
public class HttpServiceTracker extends ServiceTracker {

    public static final String SERVICE_PID = "service.pid"; // The id of the data in the config registry
    public static final String SERVLET_CLASS = "servletClass"; // Optional class name for single servlets
    // Set this param to change root URL
    public static final String DEFAULT_WEB_ALIAS = "/webstart";
    public static final String WEB_ALIAS = BaseOsgiServlet.WEB_ALIAS;
    
    protected Dictionary<String, String> properties = null;
    protected Dictionary<String, String> configProperties = null; // properties saved in the configuration system
    protected HttpContext httpContext = null;
    protected Servlet servlet = null;    // The servlet that I am responsible for
    private ServiceRegistration serviceRegistration = null; // The configuration tracker

    /**
     * Constructor - Listen for HttpService.
     * 
     * @param context
     */
    public HttpServiceTracker(BundleContext context, HttpContext httpContext, Dictionary<String, String> dictionary) 
    {
        super(context, HttpService.class.getName(), null);
        this.httpContext = httpContext;
        this.properties = HttpServiceTracker.putAll(dictionary, null);
        if (context.getProperty(SERVLET_CLASS) != null)
            this.properties.put(SERVLET_CLASS, context.getProperty(SERVLET_CLASS));
        if (context.getProperty(SERVICE_PID) != null)
            this.properties.put(SERVICE_PID, context.getProperty(SERVICE_PID));
        if (context.getProperty(WEB_ALIAS) != null)
            this.properties.put(WEB_ALIAS, context.getProperty(WEB_ALIAS));
    }

    /**
     * Http Service is up, add my servlets.
     */
    public Object addingService(ServiceReference reference)
    {
        HttpService httpService = (HttpService) context.getService(reference);

        String alias = this.getAlias();
        this.properties = this.updateDictionaryConfig(this.properties, true);
        try {
            String servicePid = this.properties.get(SERVICE_PID);
            if (servlet == null)
            {
                servlet = this.makeServlet(alias, this.properties);
                if (servlet instanceof WebappServlet)
                    ((WebappServlet) servlet).init(context, servicePid, this.properties);
            }
            else
                if (servlet instanceof WebappServlet)
                    ((WebappServlet) servlet).setProperties(properties);
            if (servicePid != null)   // Listen for configuration changes
                serviceRegistration = context.registerService(ManagedService.class.getName(), new HttpConfigurator(context, servicePid), this.properties);

            httpService.registerServlet(alias, servlet, this.properties, httpContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return httpService;
    }
    
    /**
     * Create the servlet.
     * The SERVLET_CLASS property must be supplied.
     * @param alias
     * @param dictionary
     * @return
     */
    public Servlet makeServlet(String alias, Dictionary<String, String> dictionary)
    {
        String servletClass = dictionary.get(SERVLET_CLASS);
        return (Servlet)ClassServiceUtility.getClassService().makeObjectFromClassName(servletClass);        
    }

    /**
     * Http Service is down, remove my servlet.
     */
    public void removeService(ServiceReference reference, Object service) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }

        String alias = this.getAlias();
        ((HttpService) service).unregister(alias);
        if (servlet instanceof WebappServlet)
            if (((WebappServlet)servlet).restartRequired())
        {
            ((WebappServlet)servlet).free();
            servlet = null;
        }
    }

    /**
     * Get the (persistent) configuration dictionary from the service manager.
     * @return
     */
    @SuppressWarnings("unchecked")
    public Dictionary<String, String> updateDictionaryConfig(Dictionary<String, String> dictionary, boolean returnCopy)
    {
        if (returnCopy)
            dictionary = HttpServiceTracker.putAll(dictionary, null);
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

                    configProperties = config.getProperties();
                    if (configProperties == null)
                        configProperties = new Hashtable<String, String>();
                    // First, move all settings to dictionary
                    dictionary = HttpServiceTracker.putAll(configProperties, dictionary);
                    dictionary.put(BaseOsgiServlet.WEB_ALIAS, this.calculateWebAlias(dictionary));
                    // Next, move all saveable settings to the config dictionary (and save them)
                    Enumeration<String> keys = dictionary.keys();
                    while (keys.hasMoreElements())
                    {
                        String key = keys.nextElement();
                        if (isPersistentProperty(key))
                            configProperties.put(key, dictionary.get(key)); // Make sure all the fully qualified keys are persisted
                    }
                    // push the configuration dictionary to the ConfigAdminService
                    config.update(configProperties);
                }
            }
            if (dictionary.get(BaseOsgiServlet.WEB_ALIAS) == null)
                dictionary.put(BaseOsgiServlet.WEB_ALIAS, this.calculateWebAlias(dictionary));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    /**
     * Get the web context path from the service name.
     * @return
     */
    public String getAlias()
    {
        String alias = this.properties.get(WEB_ALIAS);
        return HttpServiceTracker.addURLPath(null, alias);
    }

    /**
     * Figure out the correct web alias.
     * @param dictionary
     * @return
     */
    public String calculateWebAlias(Dictionary<String, String> dictionary)
    {
        String alias = this.getAlias();
        if (alias == null)
            alias = dictionary.get(WEB_ALIAS);
        if (alias == null)
            alias = context.getProperty(BaseOsgiServlet.WEB_ALIAS);
        if (alias == null)
            alias = DEFAULT_WEB_ALIAS;
        return alias;
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
     * 
     * @param key
     * @return
     */
    public String getProperty(String key)
    {
        String value = this.properties.get(key);
        if (value == null)
            if (isPersistentProperty(key))
                value = this.properties.get(key.substring(BaseOsgiServlet.PROPERTY_PREFIX.length()));
        return value;
    }
    /**
     * Update the servlet's properties.
     * Called when the configuration changes.
     * 
     * @param contextPath
     */
    public void updateConfigProperties(Dictionary<String, String> properties)
    {
        if (HttpServiceTracker.propertiesEqual(properties, configProperties))
            return;
        configProperties = properties;
        ServiceReference reference = context.getServiceReference(HttpService.class.getName());
        if (reference == null)
            return;
        HttpService httpService = (HttpService) context.getService(reference);

        String oldAlias = this.getAlias();
        this.changeServletProperties(servlet, properties);
        String alias = properties.get(WEB_ALIAS);
        boolean restartRequired = false;
        if (!oldAlias.equals(alias))
            restartRequired = true;
        else if (servlet instanceof WebappServlet)
            restartRequired = ((WebappServlet)servlet).restartRequired();
        if (!restartRequired)
            return;
        httpService.unregister(oldAlias);
        this.properties.put(WEB_ALIAS, alias);
        
        if (servlet instanceof WebappServlet)
            if (((WebappServlet)servlet).restartRequired())
        {
            ((WebappServlet)servlet).free();
            servlet = null;
        }

        this.addingService(reference); // Start it back up
    }
    
    /**
     * Change the servlet properties to these properties.
     * @param servlet
     * @param properties
     * @return
     */
    public boolean changeServletProperties(Servlet servlet, Dictionary<String, String> properties)
    {
        if (servlet instanceof WebappServlet)
        {
            Dictionary<String, String> dictionary = ((WebappServlet)servlet).getProperties();
            properties = putAll(properties, dictionary);
        }
        return this.setServletProperties(servlet, properties);
    }
    
    /**
     * Set the serlvlet's properties.
     * @param servlet
     * @param properties
     * @return
     */
    public boolean setServletProperties(Servlet servlet, Dictionary<String, String> properties)
    {
        this.properties = properties;
        if (servlet instanceof WebappServlet)
            return ((WebappServlet)servlet).setProperties(properties);
        return true;    // Success
    }
    
    /**
     * Are these properties equal.
     * @param properties
     * @param dictionary
     * @return
     */
    public static boolean propertiesEqual(Dictionary<String, String> properties, Dictionary<String, String> dictionary)
    {
        Enumeration<String> props = properties.keys();
        while (props.hasMoreElements())
        {
            String key = props.nextElement();
            if (!properties.get(key).equals(dictionary.get(key)))
                return false;
        }
        props = dictionary.keys();
        while (props.hasMoreElements())
        {
            String key = props.nextElement();
            if (!dictionary.get(key).equals(properties.get(key)))
                return false;
        }
        return true;
    }

    /**
     * Copy all the values from one dictionary to another.
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
    
    /**
     * Is this a persistent property?
     * Override this to add more.
     * @param key
     * @return
     */
    public static boolean isPersistentProperty(String key)
    {
        return key.startsWith(BaseOsgiServlet.PROPERTY_PREFIX);
    }
    
    public void setServlet(Servlet servlet)
    {
        this.servlet = servlet;
    }
}
