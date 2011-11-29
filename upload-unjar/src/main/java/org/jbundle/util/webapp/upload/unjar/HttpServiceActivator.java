/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.upload.unjar;

import org.osgi.framework.BundleContext;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends org.jbundle.util.webapp.base.HttpServiceActivator
{
    public String getServletClass(BundleContext context)
    {
        return UploadServletUnjar.class.getName();    // Override this to enable config admin.
    }
}
