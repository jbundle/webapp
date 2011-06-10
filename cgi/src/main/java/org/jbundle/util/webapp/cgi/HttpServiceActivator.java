package org.jbundle.util.webapp.cgi;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends org.jbundle.util.webapp.osgi.HttpServiceActivator
{
    public String getServletClass()
    {
        return org.apache.catalina.servlets.CGIServlet.class.getName();    // Override this to enable config admin.
    }
}