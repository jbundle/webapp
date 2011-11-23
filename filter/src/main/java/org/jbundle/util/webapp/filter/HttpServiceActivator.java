/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.filter;

import org.osgi.framework.BundleContext;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends org.jbundle.util.webapp.osgi.HttpServiceActivator
{
    public String getServletClass(BundleContext context)
    {
        return org.jbundle.util.webapp.redirect.RegexRedirectServlet.class.getName();    // Override this to enable config admin.
    }
}
