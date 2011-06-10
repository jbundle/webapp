package org.jbundle.util.webapp.webdav;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends org.jbundle.util.webapp.osgi.HttpServiceActivator
{
    public String getServletClass()
    {
        return org.apache.catalina.servlets.WebdavServlet.class.getName();    // Override this to enable config admin.
    }
}