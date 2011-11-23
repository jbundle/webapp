/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Dictionary;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base OSGi Servlet.
 * Note: Even though this is called OsgiServlet, is must be able to run in a non-osgi environment,
 * so don't do any osgi imports.
 * Note: This is designed to override the JnlpDownloadServlet. I'm a little 
 * apprehensive about the licensing so for now I wont wrap the (sun) code in an OSGi wrapper. 
 * @author don
 *
 */
public abstract class BaseOsgiServlet extends HttpServlet /*JnlpDownloadServlet*/
    implements WebappServlet
{
	private static final long serialVersionUID = 1L;
    
    private Object bundleContext = null;
    String servicePid = null;
    Dictionary<String, String> properties = null;
    
    public static final String PROPERTY_PREFIX = "org.jbundle.util.webapp.";  // In Config Service
    public static final String WEB_ALIAS = PROPERTY_PREFIX + "webalias";  // In Config Service

    /**
     * Constructor.
     * @param bundleContext
     */
    public BaseOsgiServlet() {
    	super();
    }
    
    /**
     * Constructor.
     * @param context
     */
    public BaseOsgiServlet(Object bundleContext, String servicePid, Dictionary<String, String> dictionary) {
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
    	this.properties = dictionary;
    }
    /**
     * web servlet init method.
     * @exception ServletException From inherited class.
     */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
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
        {IE, "MSIE"},
        {WEBKIT, "CHROME"},
        {WEBKIT, "SAFARI"},
        {WEBKIT, "OPERA"},
        {JAVA, "JAVA"},
        {FIREFOX, "MOZILLA/5"},
        {WEBKIT, "webkit"},
        {OTHER, ""}
    };

    public boolean setProperties(Dictionary<String, String> properties)
    {
        this.properties = properties;
        return true;
    }
    public Dictionary<String, String> getProperties()
    {
        return this.properties;
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
