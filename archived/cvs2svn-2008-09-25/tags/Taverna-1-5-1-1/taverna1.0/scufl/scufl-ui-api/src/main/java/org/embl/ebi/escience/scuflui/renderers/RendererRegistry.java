package org.embl.ebi.escience.scuflui.renderers;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflui.spi.RendererSPI;
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
    
    public static synchronized RendererRegistry instance() {
        if (instance == null) {
            instance = new RendererRegistry();
        }
        return instance;
    }    

    /**
     * Create a new instance. Intialize it with no renderers. You will probably
     * want to call <code>RendererRegistry.instance()</code> instead.
     */
    private RendererRegistry() {
    	super(RendererSPI.class);
    }

    /**
     * Get the default renderers for a user object and mime type.
     *
     * @param dataThing the object to render
     * @return  a MimeTypeRendereSPI that can render this, or null if none is
     *      found
     */
    public RendererSPI getRenderer(DataThing dataThing) {
        logger.info("Finding renderer: " + dataThing);
        for (RendererSPI rend : findComponents()) {           
            if (rend.canHandle(this, dataThing)) {
                logger.info("Found: " + rend.getName());
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
    public List<RendererSPI> getRenderers(DataThing dataThing) {
        logger.info("Finding renderers: " + dataThing);
        List<RendererSPI> res = new ArrayList<RendererSPI>();
        for (RendererSPI rend : findComponents()) {            
            if (rend.canHandle(this, dataThing)) {
                logger.info("Found: " + rend.getName());
                res.add(rend);
            }
        }
        return res;
    }

}
