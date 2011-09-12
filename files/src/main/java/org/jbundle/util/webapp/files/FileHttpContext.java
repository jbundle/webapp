/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.files;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

/**
 * Jnlp http context.
 * @author don
 *
 */
public class FileHttpContext implements HttpContext {

    private Bundle bundle;

    public FileHttpContext(Bundle bundle)
    {
        this.bundle = bundle;
    }

	@Override
	public boolean handleSecurity(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		return true;
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
		}
		return mimeType;
	}
	
	public static final String[] IMAGE_EXTENSIONS = {
		"gif", "png", "bmp", "jpg", "jpeg", "tiff"
		};
	public static final String[] TEXT_EXTENSIONS = {
		"css", "html", "htm", "txt"
		};
	public static final String[] APPLICATION_EXTENSIONS = {
		"json", "js", "pdf", "zip", "jnlp"
		};
	public static final String[] AUDIO_EXTENSIONS = {
		"mp3", "wav"
		};
	public static final String[] VIDEO_EXTENSIONS = {
		"mpeg", "mov"
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
