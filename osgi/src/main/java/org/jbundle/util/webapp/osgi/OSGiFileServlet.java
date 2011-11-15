/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbundle.util.osgi.finder.ClassServiceUtility;

/**
 * A very simple servlet that just serves up resources from the OSGi bundle classpaths.
 * This servlet uses the OSGi utilities to find the resource.
 * @author don
 *
 */
public class OSGiFileServlet extends BaseOsgiServlet
{
	private static final long serialVersionUID = 1L;
	public static final String BASE_URL = "baseURL";
	public static final String BASE_PATH = "basePath";
	protected URL baseURL = null;
	protected String basePath = null;	// Prepend this to path

    /**
     * Constructor.
     * @param context
     */
    public void init(Object context, String servicePid, Dictionary<String, String> properties) {
    	super.init(context, servicePid, properties);
    	if (properties != null)
    	{
    		if (properties.get(BASE_URL) != null) {
				try {
					baseURL = new URL(properties.get(BASE_URL));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
    		}
    		basePath = properties.get(BASE_PATH);
    	}
    }

    /**
     * 
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    		throws ServletException, IOException
	{
	    boolean fileFound = sendResourceFile(req, resp);
		if (!fileFound)
		    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
	}
    /**
     *  process an HTML get or post.
     * @exception ServletException From inherited class.
     * @exception IOException From inherited class.
     */
    public void service(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException
    {
	    boolean fileFound = sendResourceFile(req, resp);
		if (!fileFound)
		    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
//    	super.service(req, resp);
    }
    /**
     * Send this is resource to the response stream.
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public boolean sendResourceFile(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String path = request.getPathInfo();
        if (path == null)
            return false;
        if (path.startsWith("/"))
        	path = path.substring(1);	// Resources already start from root/baseURL
        if (basePath != null)
        	path = basePath + path;
            
        URL url = null;
        try {
            url = ClassServiceUtility.getClassService().getResourceURL(path, baseURL, null, this.getClass().getClassLoader());
        } catch (RuntimeException e) {
            e.printStackTrace();    // ???
        }        	
        if (url == null)
            return false;   // Not found
        InputStream inStream = null;
        try {
            inStream = url.openStream();
        } catch (Exception e) {
            return false;   // Not found
        }

        // Todo may want to add cache info (using bundle lastModified).
        OutputStream writer = response.getOutputStream();
        copyStream(inStream, writer, true); // Ignore errors, as browsers do weird things
        writer.close();
        return true;
    }
};
