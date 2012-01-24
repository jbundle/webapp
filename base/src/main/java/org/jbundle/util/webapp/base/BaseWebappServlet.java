/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base OSGi Servlet.
 * Note: Even though this is an OsgiServlet, is must be able to run in a non-osgi environment,
 * so don't do any osgi imports.
 * Note: This is designed to override the JnlpDownloadServlet. I'm a little 
 * apprehensive about the licensing, so for now I wont wrap the (sun) code in an OSGi wrapper. 
 * @author don
 *
 */
public abstract class BaseWebappServlet extends HttpServlet /*JnlpDownloadServlet*/
    implements WebappServlet
{
	private static final long serialVersionUID = 1L;
    
    private Object bundleContext = null;
    protected String servicePid = null;
    protected Dictionary<String, String> properties = null;
    
    protected Logger logger = null;

    /**
     * Constructor.
     * @param bundleContext
     */
    public BaseWebappServlet() {
    	super();
    }
    
    /**
     * Constructor.
     * @param context
     */
    public BaseWebappServlet(Object bundleContext, String servicePid, Dictionary<String, String> dictionary) {
    	this();
    	init(bundleContext, servicePid, dictionary);
    }
    
    /**
     * Constructor.
     * @param context
     */
    public void init(Object bundleContext, String servicePid, Dictionary<String, String> dictionary) {
    	this.bundleContext = bundleContext;
    	this.servicePid = servicePid;
    	this.setProperties(dictionary);
    }
    /**
     * web servlet init method.
     * @exception ServletException From inherited class.
     */
    @SuppressWarnings("unchecked")
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        // Move init params to my properties
        Enumeration<String> paramNames = this.getInitParameterNames();
        while (paramNames.hasMoreElements())
        {
            String paramName = paramNames.nextElement();
            this.setProperty(paramName, this.getInitParameter(paramName));
        }
        if (Boolean.TRUE.toString().equalsIgnoreCase(this.getInitParameter(LOG_PARAM)))
            logger = Logger.getLogger(PROPERTY_PREFIX);
    }
    /**
     * Destroy this Servlet and any active applications.
     * This is only called when all users are done using this Servlet.
     */
    public void destroy()
    {
        super.destroy();
    }
     
    /**
     * Free my resources.
     */
    public void free()
    {
    }
    /**
     * 
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        this.service(req, resp);
    }
    /**
     * Convenience method.
     * Note: You will have to cast the class or override this in your actual OSGi servlet.
     */
    public Object getBundleContext()
    {
        return bundleContext;
    }
    /**
     * Get this param from the request or from the servlet's properties.
     * @param request
     * @param param
     * @param defaultValue
     * @return
     */
    public String getRequestParam(HttpServletRequest request, String param, String defaultValue)
    {
        String value = request.getParameter(servicePid + '.' + param);
        if ((value == null) && (properties != null))
            value = properties.get(servicePid + '.' + param);
        if (value == null)
            value = request.getParameter(param);
        if ((value == null) && (properties != null))
            value = properties.get(param);
        if (value == null)
                value = defaultValue;
        return value;
    }

	static final int BUFFER = 2048;
	/**
	 * 
	 * @param inStream
	 * @param outStream
	 * @param ignoreErrors
	 */
    public static void copyStream(InputStream inStream, OutputStream outStream, boolean ignoreErrors)
    {
    	byte[] data = new byte[BUFFER];
        int count;
        try {
			while((count = inStream.read(data, 0, BUFFER)) != -1)
			{
				outStream.write(data, 0, count);
			}
		} catch (IOException e) {
		    if (!ignoreErrors)
		        e.printStackTrace();
		}
    }
    /**
     * Get the browser type.
     */
    public String getBrowser(HttpServletRequest req)
    {
        String strAgent = req.getHeader("user-agent");
        if (strAgent == null)
            return OTHER;
        strAgent = strAgent.toUpperCase();
        for (int i = 0; i < BROWSER.length; i++)
        {
            if (strAgent.indexOf(BROWSER[i][1]) != -1)
                return BROWSER[i][0];
        }
        return OTHER;
    }
    /**
     * Get the browser type.
     */
    public String getOS(HttpServletRequest req)
    {
        String strAgent = req.getHeader("user-agent");
        if (strAgent == null)
            return OTHER;
        strAgent = strAgent.toUpperCase();
        for (int i = 0; i < OS.length; i++)
        {
            if (strAgent.indexOf(OS[i][1]) != -1)
                return OS[i][0];
        }
        return OTHER;
    }
    
    /**
     * Get the languages that the user would like to see.
     * @param req
     * @return
     */
    public String getLanguage(HttpServletRequest req)
    {
        return req.getHeader("Accept-Language");
    }
    
    public static final String IE = "ie";
    public static final String FIREFOX = "firefox";
    public static final String WEBKIT = "webkit";
    public static final String JAVA = "java";
    public static final String MOBILE = "mobile";
    public static final String OTHER = "other";
    
    public static final String WINDOWS = "WINDOWS";
    public static final String LINUX = "LINUX";
    public static final String MAC = "MAC";
    
    public static final String[][] OS = {
        {WINDOWS, WINDOWS},
        {LINUX, LINUX},
        {MAC, MAC},
    };

    public static final String[][] BROWSER = {
        {MOBILE, "WINDOWS CE"},
        {MOBILE, "AVANTGO"},
        {MOBILE, "MAZINGO"},
        {MOBILE, "MOBILE"},
        {MOBILE, "T68"},
        {MOBILE, "SYNCALOT"},
        {MOBILE, "BLAZER"},
        {IE, "MSIE"},
        {WEBKIT, "CHROME"},
        {WEBKIT, "SAFARI"},
        {WEBKIT, "OPERA"},
        {JAVA, "JAVA"},
        {FIREFOX, "MOZILLA/5"},
        {WEBKIT, "webkit"},
        {OTHER, ""}
    };

    /**
     * Set the properties. Override this to set any configuration up.
     */
    public boolean setProperties(Dictionary<String, String> properties)
    {
        this.properties = properties;
        return true;
    }
    /**
     * 
     */
    public Dictionary<String, String> getProperties()
    {
        return this.properties;
    }
    /**
     * Set this property.
     * @param key
     * @param value
     * @return
     */
    public String setProperty(String key, String value)
    {
        if (properties == null)
            properties = new Hashtable<String,String>();
        return properties.put(key, value);
    }
    /**
     * Get this property.
     * @param key
     * @return
     */
    public String getProperty(String key)
    {
        if (properties == null)
            return null;
        String value = properties.get(key);
        if (value == null)
        {
            if (key.startsWith(PROPERTY_PREFIX))
                value = properties.get(key.substring(PROPERTY_PREFIX.length()));
            else
                value = properties.get(PROPERTY_PREFIX + key);
        }
        return value;
    }
    /**
     * Do I have to restart the servlet after I change properties?
     * @return
     */
    public boolean restartRequired()
    {
        return false;   // Override this if you need to restart the server.
    }
}
