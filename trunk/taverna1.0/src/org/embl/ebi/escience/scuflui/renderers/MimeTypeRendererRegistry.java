package org.embl.ebi.escience.scuflui.renderers;

import org.apache.commons.discovery.tools.Service;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;

// Utility Imports
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.embl.ebi.escience.scuflui.renderers.MimeTypeRendererSPI;

/**
 * A registry that maintains a list of all renderer service providers.
 * <p>
 * The <code>MimeTypeRendererRegistry</code> should be accessed through the
 * instance obtained by calling <code>MimeTypeRendererRegistry.instance()</code>.
 * This instance is initialised with the SPI implementations available through
 * the current class path.
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
            instance.loadInstances(MimeTypeRendererRegistry.class.getClassLoader());
        }

        return instance;
    }

    private List renderers;

    /**
     * Create a new instance. Intialize it with no renderers. You will probably
     * want to call <code>MimeTypeRendererRegistry.instance()</code> instead.
     */
    public MimeTypeRendererRegistry()
    {
        renderers = new ArrayList();
    }

    /**
     * Load all MimeTypeRendererSPI implementations that are registered in the
     * given ClassLoader.
     *
     * @param classLoader  a ClassLoader which will be searched
     */
    public void loadInstances(ClassLoader classLoader)
    {
        LOG.info("Loading all renderers");
        SPInterface spiIF = new SPInterface(MimeTypeRendererSPI.class);
        ClassLoaders loaders = new ClassLoaders();
        loaders.put(classLoader);
        Enumeration spe = Service.providers(spiIF, loaders);
        while (spe.hasMoreElements()) {
            MimeTypeRendererSPI spi = (MimeTypeRendererSPI) spe.nextElement();
            LOG.info("\t" + spi.getName());
            renderers.add(spi);
        }
        LOG.info("Done");
    }

    /**
     * Get the default renderers for a user object and mime type.
     *
     * @param dataThing the object to render
     * @return  a MimeTypeRendereSPI that can render this, or null if none is
     *      found
     */
    public MimeTypeRendererSPI getRenderer(DataThing dataThing)
    {
        LOG.info("Finding renderer: " + dataThing);
        for(Iterator i = renderers.iterator(); i.hasNext(); )
        {
            MimeTypeRendererSPI rend = (MimeTypeRendererSPI) i.next();
            if(rend.canHandle(this, dataThing))
            {
                LOG.info("\tFound: " + rend.getName());
                return rend;
            }
        }

        return null;
    }

    /**
     * Get all renderers that can render a user object and mime type.
     * If there are no renderers, then this list is empty.
     *
     * @param dataThing the object to render
     * @return a (possibly empty) List of MimeTypeRendererSPI instances
     */
    public List getRenderers(DataThing dataThing)
    {
        LOG.info("Finding renderers: " + dataThing);
        List res = new ArrayList();
        for(Iterator i = renderers.iterator(); i.hasNext();) {
            MimeTypeRendererSPI rend = (MimeTypeRendererSPI) i.next();
            if(rend.canHandle(this, dataThing)) {
                LOG.info("\tFound: " + rend.getName());
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
