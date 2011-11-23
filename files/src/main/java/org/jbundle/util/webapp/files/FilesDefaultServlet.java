package org.jbundle.util.webapp.files;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;

import org.apache.catalina.Globals;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.ProxyDirContext;
import org.jbundle.util.webapp.osgi.WebappServlet;

/**
 * Make the DefaultServlet work with OSGi.
 * @author don
 *
 */
public class FilesDefaultServlet extends DefaultServlet
    implements WebappServlet
{
    private static final long serialVersionUID = 1L;
    
    protected ProxyDirContext proxyDirContext = null;
    
    Dictionary<String, String> properties = null;

    public static final String BASE_PATH = "docBase";
    
    public FilesDefaultServlet()
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
    @Override
    public boolean setProperties(Dictionary<String, String> properties) {
        this.properties = properties;
        if (this.properties != null)
            return this.setDocBase(this.properties.get(BASE_PATH));
        else
            return this.setDocBase(null);
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
     * 
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
