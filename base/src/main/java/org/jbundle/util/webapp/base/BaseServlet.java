package org.jbundle.util.webapp.base;

/**
 * @(#)DBServlet.java 0.00 12-Feb-97 Don Corley
 *
 * Copyright (c) 2009 tourapp.com. All Rights Reserved.
 *      don@tourgeek.com
 */

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.jbundle.util.webapp.osgi.BaseOsgiServlet;

/**
 * RedirectServlet
 * 
 * This is the base servlet.
 */
public class BaseServlet extends BaseOsgiServlet
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
    public static final String OTHER = "other";
    
    public static final String WINDOWS = "WINDOWS";
    public static final String LINUX = "LINUX";
    public static final String MAC = "MAC";
    
    public static String[][] OS = {
        {WINDOWS, WINDOWS},
        {LINUX, LINUX},
        {MAC, MAC},
    };

    public static String[][] BROWSER = {
        {IE, "MSIE"},
        {"chrome", "CHROME"},
        {"safari", "SAFARI"},
        {"opera", "OPERA"},
        {"java", "JAVA"},
        {FIREFOX, "MOZILLA/5"},
        {"webkit", "webkit"},
        {OTHER, ""}
    };
}
