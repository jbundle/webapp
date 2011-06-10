package org.jbundle.util.webapp.redirect;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends org.jbundle.util.webapp.osgi.HttpServiceActivator
{
    public String getServletClass()
    {
        return RegexRedirectServlet.class.getName();    // Override this to enable config admin.
    }
}