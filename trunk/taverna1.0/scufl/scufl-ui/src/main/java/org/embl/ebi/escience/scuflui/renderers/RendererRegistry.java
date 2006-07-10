package org.embl.ebi.escience.scuflui.renderers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;

/**
 * A registry that maintains a list of all renderer service providers.
 * <p>
 * The <code>RendererRegistry</code> should be accessed through the
 * instance obtained by calling <code>RendererRegistry.instance()</code>.
 * This instance is initialised with the SPI implementations available through
 * the current class path.
 *
 * @author Matthew Pocock
 * @author Stuart Owen
 */
public class RendererRegistry extends TavernaSPIRegistry<RendererSPI>{
	
    private static Logger logger = Logger.getLogger(RendererRegistry.class);
    private static RendererRegistry instance;
    private List<RendererSPI> renderers;

    public static synchronized RendererRegistry instance()
    {
        if(instance == null)
        {
            instance = new RendererRegistry();
            instance.loadInstances(RendererRegistry.class.getClassLoader());
        }

        return instance;
    }    

    /**
     * Create a new instance. Intialize it with no renderers. You will probably
     * want to call <code>RendererRegistry.instance()</code> instead.
     */
    private RendererRegistry()
    {
    	super(RendererSPI.class);
        renderers = new ArrayList<RendererSPI>();
    }

    /**
     * Load all RendererSPI implementations that are registered in the
     * given ClassLoader.
     *
     * @param classLoader  a ClassLoader which will be searched
     */
    public void loadInstances(ClassLoader classLoader)
    {
        logger.info("Loading all renderers");
        renderers=findComponents(classLoader);
    }

    /**
     * Get the default renderers for a user object and mime type.
     *
     * @param dataThing the object to render
     * @return  a MimeTypeRendereSPI that can render this, or null if none is
     *      found
     */
    public RendererSPI getRenderer(DataThing dataThing)
    {
        logger.info("Finding renderer: " + dataThing);
        for(RendererSPI rend : renderers)
        {           
            if(rend.canHandle(this, dataThing))
            {
                logger.info("\tFound: " + rend.getName());
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
     * @return a (possibly empty) List of RendererSPI instances
     */
    public List getRenderers(DataThing dataThing)
    {
        logger.info("Finding renderers: " + dataThing);
        List res = new ArrayList();
        for(RendererSPI rend : renderers) {            
            if(rend.canHandle(this, dataThing)) {
                logger.info("\tFound: " + rend.getName());
                res.add(rend);
            }
        }

        return res;
    }

    public void addRenderer(RendererSPI renderer)
    {
        renderers.add(renderer);
    }

    public void removeRenderer(RendererSPI renderer)
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

    public RendererSPI get(int i)
    {
        return renderers.get(i);
    }
}
