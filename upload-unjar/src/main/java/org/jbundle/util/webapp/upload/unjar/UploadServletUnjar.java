/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.upload.unjar;

/**
 * @(#)DBServlet.java	0.00 12-Feb-97 Don Corley
 *
 * Copyright (c) 2009 tourapp.com. All Rights Reserved.
 *		don@tourgeek.com
 */
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbundle.util.webapp.upload.UploadServlet;


/** 
 * Upload Servlet.
 * This Servlet is used to upload a file to a server and unjar them in a directory.
 * @author  Don Corley
 * @version 1.0.0
 */
public class UploadServletUnjar extends UploadServlet
{
	public static final String SOURCE_PARAM = "source";
	// Filesytem
	public static final String DEST_ROOT_PATHNAME_PARAM = "destination.filesystem.rootpathname";
		// Zip
	public static final String ZIPIN_FILENAME_PARAM = "source.zip.filename";

	
	private static final long serialVersionUID = 1L;

	/**
	  * Creates new Servlet
	  */
	public UploadServletUnjar()
	{
		super();
	}
	/**
	 * The file was uploaded successfully, return an HTML string to display.
	 * NOTE: This is supplied to provide a convenient place to override this servlet and
	 * do some processing or supply a different (or no) return string.
	 */
	public String successfulFileUpload(File file, Properties properties)
	{
		String strHTML = super.successfulFileUpload(file, properties);
		strHTML = "<a href=\"/\">Home</a>" + RETURN + strHTML;
			// Create a properties object to describe where to move these files
		String strPath = file.getPath();
			// May as well use the passed-in properties object (No properties are usable)
		properties.setProperty(SOURCE_PARAM, "Zip");
		properties.setProperty(ZIPIN_FILENAME_PARAM, strPath);	// Source jar/zip

		properties.setProperty(DESTINATION, "Filesystem");
		String strDest = properties.getProperty(DESTINATION, "C:\\TEMP\\");
		properties.setProperty(DEST_ROOT_PATHNAME_PARAM, strDest);	// Destination jar/zip
			// Queue up the task to move them!
		org.jbundle.jbackup.Scanner scanner = new org.jbundle.jbackup.Scanner(properties);
		new Thread(scanner).start();
		
		strHTML += "<p>Unjar thread successfully started</p>";
		return strHTML;
	}
	/*
	 * Parse the properties.
	 * Override this to do extra stuff.
	 */
	public void sendForm(HttpServletRequest req, HttpServletResponse res, String strReceiveMessage, Properties properties)
		throws ServletException, IOException
	{
		if (properties.getProperty(DESTINATION) == null)
		{		// Check to see if a param was set in get (ie., ?dest=xyz)
			String[] strParamDest = req.getParameterValues(DESTINATION);
			if ((strParamDest != null)
				&& (strParamDest.length > 0)
					&& (strParamDest[0] != null)
						&& (strParamDest[0].length() > 0))
							properties.setProperty(DESTINATION, strParamDest[0]);
		}
		super.sendForm(req, res, strReceiveMessage, properties);
	}
	/*
	 * Write HTML after the form (Override this to do something).
	 */
	public void writeAfterHTML(PrintWriter out, HttpServletRequest req, Properties properties)
	{
		String strDefault = properties.getProperty(DESTINATION, "");
		out.write("<input type=\"hidden\" value=\"" + strDefault + "\" name=\"" + DESTINATION + "\" />" + RETURN);
	}
}
