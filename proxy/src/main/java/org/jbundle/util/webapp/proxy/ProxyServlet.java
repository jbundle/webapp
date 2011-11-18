/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.webapp.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.jbundle.util.webapp.redirect.*;

public class ProxyServlet extends RegexRedirectServlet {
	private static final long serialVersionUID = 1L;

	public static final String GET = "GET";	// TODO Find these in javax.servlet
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String DELETE = "DELETE";
	public static final String HEAD = "HEAD";
	public static final String OPTIONS = "OTPIONS";
	public static final String TRACE = "TRACE";
	
    public static final String PROXY_URL_PREFIX = PROPERTY_PREFIX + "urlprefix";
    
    private String proxyURLPrefix = null;

    /**
     * Returns the servlet info
     */ 
    public String getServletInfo()
    {
        return "This the proxy servlet";
    }
    /**
     * Init method.
     * @exception ServletException From inherited class.
     */
    public void init(ServletConfig config) throws ServletException
    {
    	proxyURLPrefix = config.getInitParameter(PROXY_URL_PREFIX);
    	if (proxyURLPrefix != null)
    		if (proxyURLPrefix.endsWith("/"))
    			proxyURLPrefix = proxyURLPrefix.substring(0, proxyURLPrefix.length() - 1);
        super.init(config);
    }
    /**
     * Destroy this Servlet and any active applications.
     * This is only called when all users are done using this Servlet.
     */
    public void destroy()
    {
        super.destroy();
    }
    /**
     * Process an HTML get or post.
     * @exception ServletException From inherited class.
     * @exception IOException From inherited class.
     */
    public void service(HttpServletRequest req, HttpServletResponse res) 
        throws ServletException, IOException
    {
    	if ((proxyURLPrefix == null) || (proxyURLPrefix.length() == 0))
    	{	// No proxy specified
        	super.service(req, res);
    		return;
    	}
		ServletOutputStream streamOut = res.getOutputStream();
    	try {
    		String proxyURLString = getProxyURLString(req);
    		HttpRequestBase httpRequest = getHttpRequest(req, proxyURLString);
    		addHeaders(req, httpRequest);
    		this.getDataFromClient(httpRequest, streamOut);
		} catch (Exception e) {
			displayErrorInHtml(streamOut, e);
		}
    }
    /**
     * Create the url string for the proxy server.
     * @param req
     * @return
     * @throws MalformedURLException
     */
    public String getProxyURLString(HttpServletRequest req) throws MalformedURLException
    {
    	String proxyUrlString = proxyURLPrefix;
    	if (req.getPathInfo() != null)
    		if (req.getPathInfo().length() > 0)
    			proxyUrlString += req.getPathInfo();
    	if (req.getQueryString() != null)
    		if (req.getQueryString().length() > 0)
    			proxyUrlString += "?" + req.getQueryString();
    	req.getContentType();
    	return proxyUrlString;
    }
    /**
     * Get the correct client type for this request.
     * @param req
     * @param urlString
     * @return
     */
	public HttpRequestBase getHttpRequest(HttpServletRequest req, String urlString)
	{	 // Prepare a request object
		HttpRequestBase httpget = null;
		String method = req.getMethod();
		if (GET.equalsIgnoreCase(method))
			httpget = new HttpGet(urlString);
		else if (POST.equalsIgnoreCase(method))
			httpget = new HttpPost(urlString);
		else if (PUT.equalsIgnoreCase(method))
			httpget = new HttpPut(urlString);
		else if (DELETE.equalsIgnoreCase(method))
			httpget = new HttpDelete(urlString);
		else if (HEAD.equalsIgnoreCase(method))
			httpget = new HttpHead(urlString);
		else if (OPTIONS.equalsIgnoreCase(method))
			httpget = new HttpOptions(urlString);
		else if (TRACE.equalsIgnoreCase(method))
			httpget = new HttpTrace(urlString);
		return httpget;
	}
	/**
	 * Move the header info to the proxy request.
	 * @param reqSource
	 * @param httpTarget
	 */
    public void addHeaders(HttpServletRequest reqSource, HttpRequestBase httpTarget)
    {
    	Enumeration<?> headerNames = reqSource.getHeaderNames();
    	while (headerNames.hasMoreElements())
    	{
    		String key = headerNames.nextElement().toString();
    		if (CONTENT_LENGTH.equalsIgnoreCase(key))
    			continue;	// Length will be different
    		Enumeration<?> headers = reqSource.getHeaders(key);
    		while (headers.hasMoreElements())
    		{
    			String value = (String)headers.nextElement();
    			if (HOST.equalsIgnoreCase(key))
    			{
    				value = proxyURLPrefix;
    				if (value.indexOf(":") != -1)
    				{
    					value = value.substring(value.indexOf(":") + 1);
    					while (value.startsWith("/"))
    					{
    						value = value.substring(1);
    					}
    				}
    				if (value.indexOf("/") != -1)
    					value = value.substring(0, value.indexOf("/"));
    			}
    			httpTarget.setHeader(new BasicHeader(key, value));
    		}
    	}
    }
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String HOST = "Host";
    /**
     * Display error.
     * @param streamOut
     * @param e 
     */
    public void displayErrorInHtml(OutputStream streamOut, Exception e)
    {
    	// TODO (Find this simple code somewhere ?in cocoon?)
		e.printStackTrace();
    }
    /**
     * Get the data from the proxy and send it to the client.
     * @param httpget
     * @param streamOut
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public void getDataFromClient(HttpRequestBase httpget, OutputStream streamOut) throws ClientProtocolException, IOException
    {
    	HttpClient httpclient = new DefaultHttpClient();
    	     	 
    	 // Execute the request
    	 HttpResponse response = httpclient.execute(httpget);
    	 
    	 // Get the response entity
    	 HttpEntity entity = response.getEntity();
    	 
    	 if (entity != null)
    	 {
	    	 InputStream instream = entity.getContent();
	    	    	 
		     ProxyServlet.transferURLStream(instream, streamOut);
	
	         // Closing the input stream will trigger connection release
	         instream.close();
	    	         	     
	         // There is probably a less resource intensive way to do this.
	         httpclient.getConnectionManager().shutdown();
    	 }
    }
    /**
     * Transfer the data stream from this URL to another stream.
     * @param strURL The URL to read.
     * @param strFilename If non-null, create this file and send the URL data here.
     * @param strFilename If null, return the stream as a string.
     * @param in If this is non-null, read from this input source.
     * @return The stream as a string if filename is null.
     */
    public static void transferURLStream(InputStream streamIn, OutputStream streamOut)
    {
    	try {
            byte[] cbuf = new byte[1000];
            int iLen = 0;
            while ((iLen = streamIn.read(cbuf, 0, cbuf.length)) > 0)
            {   // Write the entire file to the output buffer
            	streamOut.write(cbuf, 0, iLen);
            }
            streamIn.close();
            if (streamIn != null)
            	streamIn.close();
        } catch (MalformedURLException ex)  {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
