/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.redirect;

/**
 * @(#)DBServlet.java 0.00 12-Feb-97 Don Corley
 *
 * Copyright © 2012 tourgeek.com. All Rights Reserved.
 *      don@tourgeek.com
 */

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * RedirectServlet
 * 
 * This servlet is the redirect servlet.
 */
public class RegexRedirectServlet extends RedirectServlet
{
	private static final long serialVersionUID = 1L;

	public static final String REGEX = PROPERTY_PREFIX + "regex";
	public static final String REGEX_TARGET = PROPERTY_PREFIX + "regexTarget";

    /**
     * returns the servlet info
     */ 
    public String getServletInfo()
    {
        return "This the redirect servlet";
    }
    /**
     * init method.
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
     *  process an HTML get or post.
     * @exception ServletException From inherited class.
     * @exception IOException From inherited class.
     */
    public void service(HttpServletRequest req, HttpServletResponse res) 
        throws ServletException, IOException
    {
        String regex = this.getProperty(REGEX);
        String server = req.getServerName();
        if (regex != null)
        	if (server != null)
        {
        	String match = null;
        	if (regex.indexOf('/') != -1)
        	{
        	    match = regex.substring(regex.indexOf('/'));
        	    regex = regex.substring(0, regex.indexOf('/'));
        	}
        	if (server.matches(regex))
	        {
        		String target = this.getProperty(REGEX_TARGET);
        		if (target != null)
        		{
        	        if (logger != null)
        	        	logger.info("Redirect " + server + " to " + target);
                    String requestPath = req.getServletPath();
        	        if ((match == null) || (requestPath.matches(match)))
        	        	res.sendRedirect(target);
        	        else
        	        {
        	            boolean fileFound = sendResourceFile(req, res);
        	            if (!fileFound)
        	                res.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
        	        }
                    return;
        		}
	        }
        }
    	super.service(req, res);
    }
}
