/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.xsl;

/**
 * @(#)DBServlet.java 0.00 12-Feb-97 Don Corley
 *
 * Copyright © 2012 tourgeek.com. All Rights Reserved.
 *      don@tourgeek.com
 */

import java.io.FileReader;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jbundle.util.webapp.base.BaseOsgiServlet;


/**
 * XSLServlet
 * params:
 * source = Path to source document.
 * stylesheet = Path to stylesheet document.
 * 
 * This is the xsl servlet.
 */
public class XSLServlet extends BaseOsgiServlet
{
	private static final long serialVersionUID = 1L;

	public XSLServlet()
	{
		super();
	}
	/**
     * returns the servlet info
     */ 
    public String getServletInfo()
    {
        return "This the xsl servlet";
    }
    /**
     * init method.
     * @exception ServletException From inherited class.
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
    }
    /**
     * Destroy this Servlet and any active applications.
     * This is only called when all users are done using this Servlet.
     */
    @Override
    public void destroy()
    {
        super.destroy();
    }
    /**
     *  process an HTML get or post.
     * @exception ServletException From inherited class.
     * @exception IOException From inherited class.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) 
        throws ServletException, IOException
    {
    	String sourceFileName = req.getParameter("source");
    	String styleSheetFileName = req.getParameter("stylesheet");
    	FileReader sourceFileReader = new FileReader(sourceFileName);
    	FileReader stylesheetFileReader = new FileReader(styleSheetFileName);
    	
    	ServletOutputStream outStream = res.getOutputStream();
    	
        try {
            StreamSource source = new StreamSource(sourceFileReader);

            Result result = new StreamResult(outStream);

            TransformerFactory tFact = TransformerFactory.newInstance();
            StreamSource streamTransformer = new StreamSource(stylesheetFileReader);
            
            Transformer transformer = tFact.newTransformer(streamTransformer);

            transformer.transform(source, result);
        } catch (TransformerConfigurationException ex)    {
            ex.printStackTrace();
        } catch (TransformerException ex) {
            ex.printStackTrace();
        }                    
    	
    	super.service(req, res);
    }
}
