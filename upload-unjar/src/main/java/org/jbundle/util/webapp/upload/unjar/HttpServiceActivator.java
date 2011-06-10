package org.jbundle.util.webapp.upload.unjar;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends org.jbundle.util.webapp.osgi.HttpServiceActivator
{
    public String getServletClass()
    {
        return UploadServletUnjar.class.getName();    // Override this to enable config admin.
    }
}