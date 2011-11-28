package org.jbundle.util.webapp.files;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;

import org.apache.catalina.Globals;
import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.ProxyDirContext;
import org.jbundle.util.webapp.osgi.WebappServlet;

/**
 * Make the DefaultServlet work with OSGi.
 * @author don
 *
 */
public class DefaultServlet extends org.apache.catalina.servlets.DefaultServlet
    implements WebappServlet
{
    private static final long serialVersionUID = 1L;
    
    protected ProxyDirContext proxyDirContext = null;
    
    Dictionary<String, String> properties = null;

    /**
     * The local file system document base. Note: getPath (incorrectly) adds the total path,
     * so you will need to put your files in the correct subdirectory. If if docbase is /space/files 
     * xyz.com/files/index.html -> /space/files/files/index.html.
     */
    
    public DefaultServlet()
    {
        super();
    }
    @Override
    public void init(Object bundleContext, String servicePid, Dictionary<String, String> properties) {
        this.setProperties(properties);
    }
    @Override
    public void free() {
    }
    /**
     * Set servlet the properties.
     */
    @Override
    public boolean setProperties(Dictionary<String, String> properties) {
        this.properties = properties;
        if (this.properties != null)
        {
            if (properties.get("debug") != null)
                debug = Integer.parseInt(properties.get("debug"));

            if (properties.get("input") != null)
                input = Integer.parseInt(properties.get("input"));

            if (properties.get("output") != null)
                output = Integer.parseInt(properties.get("output"));

            listings = Boolean.parseBoolean(properties.get("listings"));

            if (properties.get("readonly") != null)
                readOnly = Boolean.parseBoolean(properties.get("readonly"));

            if (properties.get("sendfileSize") != null)
                sendfileSize = 
                    Integer.parseInt(properties.get("sendfileSize")) * 1024;

            fileEncoding = properties.get("fileEncoding");

            globalXsltFile = properties.get("globalXsltFile");
            contextXsltFile = properties.get("contextXsltFile");
            localXsltFile = properties.get("localXsltFile");
            readmeFile = properties.get("readmeFile");

            if (properties.get("useAcceptRanges") != null)
                useAcceptRanges = Boolean.parseBoolean(properties.get("useAcceptRanges"));

            // Sanity check on the specified buffer sizes
            if (input < 256)
                input = 256;
            if (output < 256)
                output = 256;

            if (debug > 0) {
                log("DefaultServlet.init:  input buffer size=" + input +
                    ", output buffer size=" + output);
            }

            return this.setDocBase(this.properties.get(BASE_PATH));
        }
        else
        {
            return this.setDocBase(null);
        }
    }
    @Override
    public Dictionary<String, String> getProperties() {
        return this.properties;
    }
    @Override
    public boolean restartRequired() {
        return true;
    }

    @Override
    public void init() throws ServletException
    {
        if (proxyDirContext != null)
            if (this.getServletConfig() != null)
                if (this.getServletConfig().getServletContext() != null)
                    this.getServletConfig().getServletContext().setAttribute(Globals.RESOURCES_ATTR, proxyDirContext);
        super.init();
    }
    
    /**
     * Set the local file path to serve files from.
     * @param basePath
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean setDocBase(String basePath)
    {
        proxyDirContext = null;
        if (basePath == null)
            return false;
        Hashtable env = new Hashtable();
                    
        File file = new File(basePath);
        if (!file.exists())
            return false;
        if (!file.isDirectory())
            return false;
      
        env.put(ProxyDirContext.CONTEXT, file.getPath());
        FileDirContext fileContext = new FileDirContext(env);
        fileContext.setDocBase(file.getPath());
        proxyDirContext = new ProxyDirContext(env, fileContext);
          /* Can't figure this one out
        InitialContext cx = null;
          try {
              cx.rebind(RESOURCES_JNDI_NAME, obj);
          } catch (NamingException e) {
              e.printStackTrace();
          }
          */
        // Load the proxy dir context.
        return true;
    }
    protected static final String RESOURCES_JNDI_NAME = "java:/comp/Resources";

}
