/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.base;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.Servlet;

import org.jbundle.util.osgi.BundleConstants;
import org.jbundle.util.osgi.bundle.BaseBundleActivator;
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

    public static final String DEFAULT_WEB_ALIAS = "/webstart";
    
    protected Dictionary<String, Object> properties = null;
    protected Dictionary<String, Object> configProperties = null; // properties saved in the configuration system
    protected HttpContext httpContext = null;
    protected Servlet servlet = null;    // The servlet that I am responsible for
    private ServiceRegistration serviceRegistration = null; // The configuration tracker

    /**
     * Constructor - Listen for HttpService.
     * 
     * @param context
     */
    public HttpServiceTracker(BundleContext context, HttpContext httpContext, Dictionary<String, Object> dictionary)
    {
        super(context, HttpService.class.getName(), null);
        this.httpContext = httpContext;
        this.properties = BaseBundleActivator.putAll(dictionary, null);  // Copy properties
        if (context.getProperty(BundleConstants.SERVICE_CLASS) != null)
            this.properties.put(BundleConstants.SERVICE_CLASS, context.getProperty(BundleConstants.SERVICE_CLASS));
        if (context.getProperty(BundleConstants.SERVICE_PID) != null)
            this.properties.put(BundleConstants.SERVICE_PID, context.getProperty(BundleConstants.SERVICE_PID));
        if (context.getProperty(BaseWebappServlet.ALIAS) != null)
            this.properties.put(BaseWebappServlet.ALIAS, context.getProperty(BaseWebappServlet.ALIAS));
    }

    /**
     * Http Service is up, add my servlets.
     */
    public Object addingService(ServiceReference reference)
    {
        HttpService httpService = (HttpService) context.getService(reference);

        this.properties = this.updateDictionaryConfig(this.properties, true);
        try {
            String alias = this.getAlias();
            String servicePid = (String)this.properties.get(BundleConstants.SERVICE_PID);
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
    public Servlet makeServlet(String alias, Dictionary<String, Object> dictionary)
    {
        String servletClass = (String)dictionary.get(BundleConstants.SERVICE_CLASS);
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
            ((WebappServlet)servlet).free();
        servlet = null;
    }

    /**
     * Get the (persistent) configuration dictionary from the service manager.
     * @return
     */
    @SuppressWarnings("unchecked")
    public Dictionary<String, Object> updateDictionaryConfig(Dictionary<String, Object> dictionary, boolean returnCopy)
    {
        if (returnCopy)
            dictionary = BaseBundleActivator.putAll(dictionary, null);
        if (dictionary == null)
            dictionary = new Hashtable<String, Object>();
        try {
            String servicePid = (String)dictionary.get(BundleConstants.SERVICE_PID);
            if (servicePid != null)
            {
                ServiceReference caRef = context.getServiceReference(ConfigurationAdmin.class.getName());
                if (caRef != null)
                {
                    ConfigurationAdmin configAdmin = (ConfigurationAdmin)context.getService(caRef);
                    Configuration config = configAdmin.getConfiguration(servicePid);

                    configProperties = config.getProperties();
                    if (configProperties == null)
                        configProperties = new Hashtable<String, Object>();
                    // First, move all settings to dictionary
                    dictionary = BaseBundleActivator.putAll(configProperties, dictionary);
                    dictionary.put(BaseWebappServlet.ALIAS, this.calculateWebAlias(dictionary));
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
            if (dictionary.get(BaseWebappServlet.ALIAS) == null)
                dictionary.put(BaseWebappServlet.ALIAS, this.calculateWebAlias(dictionary));
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
        String alias = (String)this.properties.get(BaseWebappServlet.ALIAS);
        if (alias == null)
            alias = (String)this.properties.get(BaseWebappServlet.ALIAS.substring(BaseWebappServlet.PROPERTY_PREFIX.length()));
        return HttpServiceTracker.addURLPath(null, alias);
    }

    /**
     * Figure out the correct web alias.
     * @param dictionary
     * @return
     */
    public String calculateWebAlias(Dictionary<String, Object> dictionary)
    {
        String alias = (String)dictionary.get(BaseWebappServlet.ALIAS);
        if (alias == null)
            alias = (String)dictionary.get(BaseWebappServlet.ALIAS.substring(BaseWebappServlet.PROPERTY_PREFIX.length()));
        if (alias == null)
            alias = this.getAlias();
        if (alias == null)
            alias = context.getProperty(BaseWebappServlet.ALIAS);
        if (alias == null)
            alias = context.getProperty(BaseWebappServlet.ALIAS.substring(BaseWebappServlet.PROPERTY_PREFIX.length()));
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
    public Object getProperty(String key)
    {
        Object value = this.properties.get(key);
        if (value == null)
            if (isPersistentProperty(key))
                value = this.properties.get(key.substring(BaseWebappServlet.PROPERTY_PREFIX.length()));
        return value;
    }
    /**
     * Update the servlet's properties.
     * Called when the configuration changes.
     * 
     * @param properties
     */
    public void configPropertiesUpdated(Dictionary<String, Object> properties)
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
        String alias = (String)properties.get(BaseWebappServlet.ALIAS);
        boolean restartRequired = false;
        if (!oldAlias.equals(alias))
            restartRequired = true;
        else if (servlet instanceof WebappServlet)
            restartRequired = ((WebappServlet)servlet).restartRequired();
        if (!restartRequired)
            return;
        try {
            httpService.unregister(oldAlias);
        } catch (Exception e) {
            e.printStackTrace();    // Should not happen
        }
        this.properties.put(BaseWebappServlet.ALIAS, alias);
        
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
    public boolean changeServletProperties(Servlet servlet, Dictionary<String, Object> properties)
    {
        if (servlet instanceof WebappServlet)
        {
            Dictionary<String, Object> dictionary = ((WebappServlet)servlet).getProperties();
            properties = BaseBundleActivator.putAll(properties, dictionary);
        }
        return this.setServletProperties(servlet, properties);
    }
    
    /**
     * Set the serlvlet's properties.
     * @param servlet
     * @param properties
     * @return
     */
    public boolean setServletProperties(Servlet servlet, Dictionary<String, Object> properties)
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
    public static boolean propertiesEqual(Dictionary<String, Object> properties, Dictionary<String, Object> dictionary)
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
     * Is this a persistent property?
     * Override this to add more.
     * @param key
     * @return
     */
    public static boolean isPersistentProperty(String key)
    {
        return key.startsWith(BaseWebappServlet.PROPERTY_PREFIX);
    }
    
    public void setServlet(Servlet servlet)
    {
        this.servlet = servlet;
    }
    public Servlet getServlet()
    {
        return servlet;
    }
}
