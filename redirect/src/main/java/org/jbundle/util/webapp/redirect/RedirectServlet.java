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

	public static final String MATCH = PROPERTY_PREFIX + "match";
	public static final String TARGET = PROPERTY_PREFIX + "target";

	/**
     * returns the servlet info
     */ 
    public String getServletInfo()
    {
        return "This the base servlet";
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
        String browser = this.getBrowser(req);
        String queryString = req.getQueryString();
        String path = req.getPathInfo();
        if (path == null)
        	path = "";
        String target = this.getProperty(browser);
        if (target == null)
            target = this.getProperty(TARGET);
        if (target == null)
            target = req.getParameter(TARGET);
        if (queryString != null)
        {
            char delimiter = '?';
            if (target.indexOf('?') != -1)
                delimiter = '&';
            target = target + delimiter + queryString;
        }

        if (logger != null)
        	logger.info("Redirect to " + target);
        String match = this.getProperty(MATCH);
        if ((target != null) && ((match == null) || path.matches(match)))
        	res.sendRedirect(target);
        else
        	super.service(req, res);
    }
}
