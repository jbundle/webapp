package org.jbundle.util.webapp.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Dictionary;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbundle.util.osgi.finder.ClassServiceUtility;


/**
 * Base OSGi Servlet.
 * Note: Even though this is called OsgiServlet, is must be able to run in a non-osgi environment,
 * so don't have any osgi imports.
 * Note: This is designed to override the JnlpDownloadServlet. I just a little 
 * apprehensive about the licensing if I wrap the (sun) code in an OSGi wrapper. 
 * @author don
 *
 */
public abstract class BaseOsgiServlet extends HttpServlet /*JnlpDownloadServlet*/ {
	private static final long serialVersionUID = 1L;
    
    private Object context = null;
    String servicePid = null;
    Dictionary<String, String> properties = null;
    
    public static final String CONTEXT_PATH = "org.jbundle.util.osgi.contextpath";  // In Config Service

    /**
     * Constructor.
     * @param context
     */
    public BaseOsgiServlet() {
    	super();
    }
    
    /**
     * Constructor.
     * @param context
     */
    public BaseOsgiServlet(Object context, String servicePid, Dictionary<String, String> properties) {
    	this();
    	init(context, servicePid, properties);
    }
    
    /**
     * Constructor.
     * @param context
     */
    public void init(Object context, String servicePid, Dictionary<String, String> properties) {
    	this.context = context;
    	this.servicePid = servicePid;
    	this.properties = properties;
    }
     
    /**
     * Free my resources.
     */
    public void free()
    {
    }
    /**
     * Convenience method.
     * Note: You will have to cast the class or override this in your actual OSGi servlet.
     */
    public Object getBundleContext()
    {
        return context;
    }
    /**
     * Get this param from the request or from the servlet's properties.
     * @param request
     * @param param
     * @param defaultValue
     * @return
     */
    public String getRequestParam(HttpServletRequest request, String param, String defaultValue)
    {
        String value = request.getParameter(servicePid + '.' + param);
        if ((value == null) && (properties != null))
            value = properties.get(servicePid + '.' + param);
        if (value == null)
            value = request.getParameter(param);
        if ((value == null) && (properties != null))
            value = properties.get(param);
        if (value == null)
                value = defaultValue;
        return value;
    }
    /**
     * Send this is resource to the response stream.
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public boolean getResourceFile(HttpServletRequest request, HttpServletResponse response, boolean useOSGiLookup) throws IOException
    {
        URL url = null;
        if (!useOSGiLookup)
        {
            String path = request.getPathInfo();
            if (path == null)
                return false;
            if (!path.startsWith("/"))
                path = "/" + path;  // Must start from root
        	url = this.getClass().getResource(path);
        }
        else
        {
        	String contextPath = this.getServletContext().getContextPath();
            String path = request.getRequestURI();
            if ((contextPath != null) && (contextPath.length() > 0))
            	if (path.startsWith(contextPath))
            		path = path.substring(contextPath.length());
            if (path.startsWith("/"))
            	path = path.substring(1);	// Can't start from root
            
            try {
                url = ClassServiceUtility.getClassService().getResourceURL(path, null, this.getClass().getClassLoader());
            } catch (RuntimeException e) {
                e.printStackTrace();    // ???
            }        	
        }
        if (url == null)
            return false;   // Not found
        InputStream inStream = null;
        try {
            inStream = url.openStream();
        } catch (Exception e) {
            return false;   // Not found
        }

        // Todo may want to add cache info (using bundle lastModified).
        OutputStream writer = response.getOutputStream();
        copyStream(inStream, writer, true); // Ignore errors, as browsers do weird things
        writer.close();
        return true;
    }

	static final int BUFFER = 2048;
    public static void copyStream(InputStream inStream, OutputStream outStream, boolean ignoreErrors)
    {
    	byte[] data = new byte[BUFFER];
        int count;
        try {
			while((count = inStream.read(data, 0, BUFFER)) != -1)
			{
				outStream.write(data, 0, count);
			}
		} catch (IOException e) {
		    if (!ignoreErrors)
		        e.printStackTrace();
		}
    }
}