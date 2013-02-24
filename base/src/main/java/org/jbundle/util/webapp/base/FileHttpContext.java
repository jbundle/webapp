/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.base;

import java.io.IOException;
import java.net.URL;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

/**
 * Retrieve files from the bundle context.
 * @author don
 *
 */
public class FileHttpContext implements HttpContext {

    protected Bundle bundle;

    public FileHttpContext(Servlet servlet, Bundle bundle)
    {
        this.bundle = bundle;
        if (servlet instanceof BaseOsgiServlet)
            ((BaseOsgiServlet)servlet).setHttpContext(this);
    }

	@Override
	public boolean handleSecurity(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		return true;	// For now
	}

	@Override
    public URL getResource(String name)
    {
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        return this.bundle.getResource(name);
    }

	@Override
	public String getMimeType(String name) {
	    String mimeType = "text/html";
		if (name != null)
			if (name.lastIndexOf('.') != -1)
		{
			String extension = name.substring(name.lastIndexOf('.') + 1);
			if (isType(extension, IMAGE_EXTENSIONS))
			{
				if ("jpg".equalsIgnoreCase(extension))
					extension = "jpeg";
                if ("ico".equalsIgnoreCase(extension))
                    extension = "vnd.microsoft.icon";
				mimeType = "image/" + extension.toLowerCase();
			}
			else if (isType(extension, TEXT_EXTENSIONS))
			{
				if ("htm".equalsIgnoreCase(extension))
					extension = "html";
				if ("txt".equalsIgnoreCase(extension))
					extension = "plain";
				mimeType = "text/" + extension.toLowerCase();
			}
			else if (isType(extension, AUDIO_EXTENSIONS))
			{
				if ("mp3".equalsIgnoreCase(extension))
					extension = "mpeg";
				mimeType = "text/" + extension.toLowerCase();
			}
			else if (isType(extension, VIDEO_EXTENSIONS))
			{
				if ("mov".equalsIgnoreCase(extension))
					extension = "quicktime";
				mimeType = "text/" + extension.toLowerCase();
			}
			else if (isType(extension, APPLICATION_EXTENSIONS))
			{
				if ("jnlp".equalsIgnoreCase(extension))
					extension = "x-java-jnlp-file";
				if (("swf".equalsIgnoreCase(extension)) || ("cab".equalsIgnoreCase(extension)))
					extension = "x-shockwave-flash";
				if ("jar".equalsIgnoreCase(extension))
					extension = "java-archive";
				if ("js".equalsIgnoreCase(extension))
					extension = "x-javascript";
				mimeType = "application/" + extension.toLowerCase();
			}
		}
		return mimeType;
	}
	
	public static final String[] IMAGE_EXTENSIONS = {
		"gif", "png", "bmp", "jpg", "jpeg", "tiff", "ico"
		};
	public static final String[] TEXT_EXTENSIONS = {
		"css", "html", "htm", "txt", "xml", "xsl", "xslt", "csv"
		};
	public static final String[] APPLICATION_EXTENSIONS = {
		"json", "js", "pdf", "zip", "jnlp", "swf", "cab", "jar", "ogg", "gzip"
		};
	public static final String[] AUDIO_EXTENSIONS = {
		"mp3", "wav"
		};
	public static final String[] VIDEO_EXTENSIONS = {
		"mpeg", "mov", "mp4"
		};
	
	private boolean isType(String extension, String[] types)
	{
		for (String imageExtension : types)
		{
			if (imageExtension.equalsIgnoreCase(extension))
				return true;
		}
		return false;
	}

}
