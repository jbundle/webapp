/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Dictionary;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Base OSGi Servlet.
 * Note: Even though this is called OsgiServlet, is must be able to run in a non-osgi environment,
 * so don't do any osgi imports.
 * Note: This is designed to override the JnlpDownloadServlet. I'm a little 
 * apprehensive about the licensing if I wrap the (sun) code in an OSGi wrapper. 
 * @author don
 *
 */
public abstract class BaseOsgiServlet extends HttpServlet /*JnlpDownloadServlet*/ {
	private static final long serialVersionUID = 1L;
    
    private Object context = null;
    String servicePid = null;
    Dictionary<String, String> properties = null;
    
    public static final String CONTEXT_PATH = "org.jbundle.util.osgi.contextpath";  // In Config Service

    /**
     * Constructor.
     * @param context
     */
    public BaseOsgiServlet() {
    	super();
    }
    
    /**
     * Constructor.
     * @param context
     */
    public BaseOsgiServlet(Object context, String servicePid, Dictionary<String, String> properties) {
    	this();
    	init(context, servicePid, properties);
    }
    
    /**
     * Constructor.
     * @param context
     */
    public void init(Object context, String servicePid, Dictionary<String, String> properties) {
    	this.context = context;
    	this.servicePid = servicePid;
    	this.properties = properties;
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
        return context;
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
}
