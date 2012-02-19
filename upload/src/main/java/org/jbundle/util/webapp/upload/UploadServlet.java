/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.upload;

/**
 * @(#)DBServlet.java	0.00 12-Feb-97 Don Corley
 *
 * Copyright © 2012 tourapp.com. All Rights Reserved.
 *		don@tourgeek.com
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oreilly.servlet.MultipartRequest;

/** 
 * Upload Servlet.
 * This Servlet is used to upload a file to a server.
 * @author  Don Corley
 * @version 1.0.0
 */
public class UploadServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	public static final boolean DEBUG = true;

	public static final String RETURN = "\n";
	public static final String gstrBlank = "";

	public static final String TITLE = "Upload files";

	public static final String FILENAME = "filename";

	public static final String DESTINATION = "destination";

	public static final int MAX_SIZE = 100 * 1024 * 1024;  // 100 Meg

	/**
	  * Creates new Servlet.
	  */
	public UploadServlet()
	{
		super();
	}
	/**
	 * Returns the servlet info.
	 */ 
	public String getServletInfo()
	{
		return "This the Servlet receives files over http";
	}
	/**
	 * Init method.
	 * @exception	ServletException From inherited class.
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
	}
	/**
	 * Destroy this applet.
	 */
	public void destroy()
	{
		super.destroy();
	}
	/**
	 * Process an HTML get.
	 * @exception	ServletException From inherited class.
	 * @exception	IOException From inherited class.
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res) 
		throws ServletException, IOException
	{
		this.doProcess(req, res);
	}
	/**
	 * Process an HTML post.
	 * @exception	ServletException From inherited class.
	 * @exception	IOException From inherited class.
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res) 
		throws ServletException, IOException
	{
		this.doProcess(req, res);
	}
	/**
	 * Process an HTML get or post.
	 * @exception	ServletException From inherited class.
	 * @exception	IOException From inherited class.
	 */
	public void doProcess(HttpServletRequest req, HttpServletResponse res) 
		throws ServletException, IOException
	{
		PrintStream out = System.out;
		Properties properties = new Properties();
		String strTargetDirectory = this.getDestDirectory();
		
		String strReceiveMessage = gstrBlank;
		try	{
			MultipartRequest multi = new MultipartRequest(req, strTargetDirectory, MAX_SIZE);

			this.parseProperties(properties, multi);
			if (DEBUG)
				out.println();
			if (DEBUG)
				out.println("Files:");
			Enumeration<?> files = multi.getFileNames();
			while (files.hasMoreElements())
			{
				String name = (String)files.nextElement();
				String filename = multi.getFilesystemName(name);
				String type = multi.getContentType(name);
				File f = multi.getFile(name);
				if (DEBUG)
				{
					out.println("name: " + name);
					out.println("filename: " + filename);
					out.println("type: " + type);
					if (f != null) {
						out.println("f.toString(): " + f.toString());
						out.println("f.getName(): " + f.getName());
						out.println("f.exists(): " + f.exists());
						out.println("f.length(): " + f.length());
						out.println();
					}
				}

				if (f != null)
					strReceiveMessage += this.successfulFileUpload(f, properties);
			}
		} catch (IOException ex)	{
			String strError = ex.getMessage();
			if (strError.toLowerCase().indexOf("isn't multipart/form-data") != -1)
				strReceiveMessage = gstrBlank;	// Didn't specify a file (probably first time through)
			else
				strReceiveMessage = "<p>Previous upload not successful. Error: " + ex.getMessage() + "</p>" + RETURN;
		}
		this.sendForm(req, res, strReceiveMessage, properties);
	}
	/*
	 * Get the title for this servlet (Override to change).
	 * @return The screen title.
	 */
	public String getTitle()
	{
		return TITLE;
	}
	/**
	 * Process an HTML get or post.
	 * @exception	ServletException From inherited class.
	 * @exception	IOException From inherited class.
	 */
	public void sendForm(HttpServletRequest req, HttpServletResponse res, String strReceiveMessage, Properties properties)
		throws ServletException, IOException
	{
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
		out.write("<html>" + RETURN +
						"<head>" + RETURN +
						"<title>" + this.getTitle() + "</title>" + RETURN +
						"</head>" + RETURN +
						"<body>" + RETURN +
						"<center><b>" + this.getTitle() + "</b></center><p />" + RETURN);

		String strServletPath = null;
		try	{
			strServletPath = req.getRequestURI();	//Path();
		} catch (Exception ex)
		{
			strServletPath = null;
		}
		if (strServletPath == null)
			strServletPath = "servlet/" + UploadServlet.class.getName();	// Never
//strServletPath = "http://localhost/jbackup/servlet/com.tourapp.jbackup.Servlet";

		out.write(strReceiveMessage);		// Status of previous upload
		out.write("<p /><form action=\"" + strServletPath + "\"");
		out.write(" method=\"post\"" + RETURN);
		out.write(" enctype=\"multipart/form-data\">" + RETURN);
// TARGET=_top
//		out.write(" ACTION="http://www45.visto.com/?uid=219979&service=fileaccess&method=upload&nextpage=fa%3Dafter_upload.html&errorpage=fa_upload@.html&overwritepage=fa=upload_replace_dialog.html">
//		out.write("What is your name? <INPUT TYPE=TEXT NAME=submitter> <BR>" + RETURN);
		String strFormHTML = this.getFormHTML();
		if ((strFormHTML != null) && (strFormHTML.length() > 0))
			out.write(strFormHTML);
		out.write("Which file to upload? <input type=\"file\" name=\"file\" />" + RETURN);
		this.writeAfterHTML(out, req, properties);
		out.write("<input type=\"submit\" value=\"upload\" />" + RETURN);
		out.write("</form></p>" + RETURN);
		
		out.write("</body>" + RETURN +
						"</html>" + RETURN);
	}
	/*
	 * The file was uploaded successfully, return an HTML string to display.
	 * NOTE: This is supplied to provide a convenient place to override this servlet and
	 * do some processing or supply a different (or no) return string.
	 * Usually, you want to call super to get the standard string.
	 * @param properties All the form parameters.
	 * @param file The file that was uploaded.
	 * @returns HTML String to display.
	 */
	public String successfulFileUpload(File file, Properties properties)
	{
		return "<p>Previous upload successful. Path: " + file.getPath() + " Filename: " + file.getName() + " received containing " + file.length() + " bytes</p>" + RETURN;
	}
	/*
	 * Here you place extra input parameters to be passed with the uploaded file.
	 * If you need extra paramters, override this method.
	 * out.write("What is your name? <INPUT TYPE=TEXT NAME=submitter> <BR>" + RETURN);
	 */
	public String getFormHTML()
	{
		String strFormHTML = "";
		return strFormHTML;
	}
	/*
	 * Get the temporary directory name.
	 */
	public String getDestDirectory()
	{
		String strTargetDirectory = this.getInitParameter(DESTINATION);		// Passed with WAR file
		if ((strTargetDirectory == null) || (strTargetDirectory.length() == 0))
			strTargetDirectory = System.getProperty("java.io.tmpdir");		// Temporary directory
		if ((strTargetDirectory == null) || (strTargetDirectory.length() == 0))
			strTargetDirectory = ".";										// Last choice - this directory
		return strTargetDirectory;
	}
	/*
	 * Parse the properties.
	 * Override this to do extra stuff.
	 */
	public void parseProperties(Properties properties, MultipartRequest multi)
	{
		Enumeration<?> params = multi.getParameterNames();
		while (params.hasMoreElements()) {
			String name = (String)params.nextElement();
			String value = multi.getParameter(name);
			if (value != null)
				properties.setProperty(name, value);
			if (DEBUG)
				System.out.println(name + " = " + value);
		}
	}
	/*
	 * Write HTML after the form (Override this to do something).
	 */
	public void writeAfterHTML(PrintWriter out, HttpServletRequest req, Properties properties)
	{
	}
}
