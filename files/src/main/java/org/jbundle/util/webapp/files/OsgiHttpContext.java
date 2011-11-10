/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.files;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jbundle.util.osgi.finder.ClassServiceUtility;
import org.osgi.framework.Bundle;

/**
 * This File Context can retrieve files from the OSGi context including the obr index.
 * @author don
 *
 */
public class OsgiHttpContext extends org.jbundle.util.webapp.files.FileHttpContext {

    private URL urlCodeBase = null;

    public OsgiHttpContext(Bundle bundle, String urlCodeBase)
    {
    	super(bundle);
        try {
        	if (urlCodeBase != null)
        		this.urlCodeBase = new URL(urlCodeBase);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
    }

	@Override
    public URL getResource(String name)
    {
		if (aliases.containsKey(name))
			name = aliases.get(name);
    	if (urlCodeBase == null)
    		return super.getResource(name);
    	else
        {
    		if (urlCodeBase.getPath().endsWith("/"))
    			if (name.startsWith("/"))
    				name = name.substring(1);
    		URL url = null;
            try {
                url = ClassServiceUtility.getClassService().getResourceURL(name, urlCodeBase, null, this.getClass().getClassLoader());
            } catch (RuntimeException e) {
                e.printStackTrace();    // ???
			}
            return url;
        }
    }
	
	protected Map<String, String> aliases = new HashMap<String, String>();
	public void addPathAlias(String name, String target)
	{
		aliases.put(name, target);
	}
}
