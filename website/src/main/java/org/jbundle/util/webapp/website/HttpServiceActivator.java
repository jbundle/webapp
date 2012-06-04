/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.website;

import org.osgi.framework.BundleContext;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends org.jbundle.util.webapp.base.HttpServiceActivator
{
    public String getServiceClassName()
    {
        return org.jbundle.util.webapp.redirect.RegexRedirectServlet.class.getName();    // Override this to enable config admin.
    }
}
