package org.embl.ebi.escience.scuflui.renderers;

import org.apache.commons.discovery.tools.Service;
import org.apache.log4j.Logger;

// Utility Imports
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.embl.ebi.escience.scuflui.renderers.MimeTypeRendererSPI;
import java.lang.Object;
import java.lang.String;



/**
 * A registry that maintains a list of all renderer service providers.
 * <p>
 * The <code>MimeTypeRendererRegistry</code> is a singleton class. An instance
 * can be obtained by calling <code>MimeTypeRendererRegistry.instance()</code>.
 * This instance can then be used
 *
 * @author Matthew Pocock
 */
public class MimeTypeRendererRegistry {
    private static Logger LOG = Logger.getLogger(MimeTypeRendererRegistry.class);
    private static MimeTypeRendererRegistry instance;

    public static synchronized MimeTypeRendererRegistry instance()
    {
        if(instance == null)
        {
            instance = new MimeTypeRendererRegistry();
        }

        return instance;
    }

    private List renderers;

    /**
     * Create a new instance. Intialize it.
     */
    private MimeTypeRendererRegistry()
    {
        LOG.info("Loading all renderers");
        renderers = new ArrayList();
        Enumeration spe = Service.providers(MimeTypeRendererSPI.class);
        while(spe.hasMoreElements()) {
            MimeTypeRendererSPI spi = (MimeTypeRendererSPI) spe.nextElement();
            LOG.info("\t" + spi.getName());
            renderers.add(spi);
        }
        LOG.info("Done");
    }

    /**
     * Get the default renderers for a user object and mime type.
     *
     * @param userObject    the Object to render
     * @param mimetype      the mime type it has
     * @return  a MimeTypeRendereSPI that can render this, or null if none is
     *      found
     */
    public MimeTypeRendererSPI getRenderer(Object userObject, String mimetype)
    {
        for(Iterator i = renderers.iterator(); i.hasNext(); )
        {
            MimeTypeRendererSPI rend = (MimeTypeRendererSPI) i.next();
            if(rend.canHandle(userObject, mimetype))
            {
                return rend;
            }
        }

        return null;
    }

    /**
     * Get all renderers that can render a user object and mime type.
     * If there are no renderers, then this list is empty.
     *
     * @param userObject    the Object to render
     * @param mimetype      the mime type it has
     * @return a (possibly empty) List of MimeTypeRendererSPI instances
     */
    public List getRenderers(Object userObject, String mimetype)
    {
        List res = new ArrayList();
        for(Iterator i = renderers.iterator(); i.hasNext();) {
            MimeTypeRendererSPI rend = (MimeTypeRendererSPI) i.next();
            if(rend.canHandle(userObject, mimetype)) {
                res.add(rend);
            }
        }

        return res;
    }

    public void addRenderer(MimeTypeRendererSPI renderer)
    {
        renderers.add(renderer);
    }

    public void removeRenderer(MimeTypeRendererSPI renderer)
    {
        renderers.remove(renderer);
    }

    public int size()
    {
        return renderers.size();
    }

    public Iterator iterator()
    {
        return renderers.iterator();
    }

    public MimeTypeRendererSPI get(int i)
    {
        return (MimeTypeRendererSPI) renderers.get(i);
    }
}
