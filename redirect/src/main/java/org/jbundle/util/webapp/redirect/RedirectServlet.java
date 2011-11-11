/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.redirect;

/**
 * @(#)DBServlet.java 0.00 12-Feb-97 Don Corley
 *
 * Copyright (c) 2009 tourapp.com. All Rights Reserved.
 *      don@tourgeek.com
 */

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbundle.util.webapp.base.BaseServlet;

/**
 * RedirectServlet
 * 
 * This servlet is the redirect servlet.
 */
public class RedirectServlet extends BaseServlet
{
	private static final long serialVersionUID = 1L;

	public static final String LOG_PARAM = "log";
	public static final String MATCH_PARAM = "allow";
	public static final String TARGET = "target";
    public static final String DEFAULT_TARGET_URL = "http://www.jbundle.org";

	protected Logger logger = null;
	protected String match = null;

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
    	if (Boolean.TRUE.toString().equalsIgnoreCase(this.getInitParameter(LOG_PARAM)))
    		logger = Logger.getLogger("org.jbundle.util.webapp.redirect");
    	match = this.getInitParameter(MATCH_PARAM);
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
        String browser = this.getBrowser(req);
        String queryString = req.getQueryString();
        String path = req.getPathInfo();
        if (path == null)
        	path = "";
        String target = this.getInitParameter(browser);
        if (target == null)
            target = this.getInitParameter(TARGET);
        if (target == null)
            target = req.getParameter(TARGET);
        if ((target == null) || (target.length() == 0))
            target = DEFAULT_TARGET_URL;
        if (queryString != null)
        {
            char delimiter = '?';
            if (target.indexOf('?') != -1)
                delimiter = '&';
            target = target + delimiter + queryString;
        }

        if (logger != null)
        	logger.info("Redirect to " + target);
        if ((match == null) || path.matches(match))
        	res.sendRedirect(target);
        else
        	super.service(req, res);
    }
}
