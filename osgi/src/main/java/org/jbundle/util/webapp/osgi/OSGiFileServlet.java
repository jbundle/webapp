/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

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

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    		throws ServletException, IOException
	{
	    boolean fileFound = getResourceFile(req, resp, true);
		if (!fileFound)
		    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
	}
    /**
     * Send this is resource to the response stream.
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public boolean getResourceFile(HttpServletRequest request, HttpServletResponse response, boolean useOSGiLookup) throws IOException
    {
        URL url = null;
        if (!useOSGiLookup)
        {
            String path = request.getPathInfo();
            if (path == null)
                return false;
            if (!path.startsWith("/"))
                path = "/" + path;  // Must start from root
        	url = this.getClass().getResource(path);
        }
        else
        {
        	String contextPath = this.getServletContext().getContextPath();
            String path = request.getRequestURI();
            if ((contextPath != null) && (contextPath.length() > 0))
            	if (path.startsWith(contextPath))
            		path = path.substring(contextPath.length());
            if (path.startsWith("/"))
            	path = path.substring(1);	// Can't start from root
            
            try {
                url = ClassServiceUtility.getClassService().getResourceURL(path, null, null, this.getClass().getClassLoader());
            } catch (RuntimeException e) {
                e.printStackTrace();    // ???
            }        	
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
