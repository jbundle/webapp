/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.webdav;

import org.osgi.framework.BundleContext;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends org.jbundle.util.webapp.base.HttpServiceActivator
{
    /**
     * Get the servlet class to activate.
     * @param context 
     * @return
     */
    @Override
    public String getServiceClassName()
    {
        return WebdavServlet.class.getName();    // Override this to enable config admin.
    }
    /**
     * Get the Servlet context for this servlet.
     * Override if different from default context.
     * @return The httpcontext.
     */
//+    public HttpContext getHttpContext()
  //  {
    //    return new FileHttpContext(context.getBundle());    // Override this
    //}
}
