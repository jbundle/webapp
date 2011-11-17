/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.base;

/**
 * @(#)DBServlet.java 0.00 12-Feb-97 Don Corley
 *
 * Copyright (c) 2009 tourapp.com. All Rights Reserved.
 *      don@tourgeek.com
 */

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.jbundle.util.webapp.osgi.OSGiFileServlet;

/**
 * RedirectServlet
 * 
 * This is the base servlet.
 */
public class BaseServlet extends OSGiFileServlet
{
	private static final long serialVersionUID = 1L;

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
}
