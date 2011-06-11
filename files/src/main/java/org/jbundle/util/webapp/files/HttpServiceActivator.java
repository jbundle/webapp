package org.jbundle.util.webapp.files;


import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends org.jbundle.util.webapp.osgi.HttpServiceActivator
{
    public String getServletClass()
    {
        return org.apache.catalina.servlets.DefaultServlet.class.getName();    // Override this to enable config admin.
    }
    /**
     * Get the Servlet context for this servlet.
     * Override if different from default context.
     * @return The httpcontext.
     */
    public HttpContext getHttpContext()
    {
        return new FileHttpContext(context.getBundle());    // Override this
    }
    
}