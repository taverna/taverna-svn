package org.embl.ebi.escience.scuflui.facets;

import org.apache.log4j.Logger;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.tools.Service;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.embl.ebi.escience.baclava.DataThing;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * A registry that maintains a list of all facet finder service providers.
 * <p>
 * The <code>FacetFinderRegistry</code> should be accessed through the
 * instance obtained by calling <code>FacetFinderRegistry.instance()</code>.
 * This instance is initialised with the SPI implementations available through
 * the current class path.
 *
 * @author Matthew Pocock
 */
public class FacetFinderRegistry
{
    private static Logger LOG = Logger.getLogger(FacetFinderRegistry.class);
    private static FacetFinderRegistry instance;

    public static synchronized FacetFinderRegistry instance()
    {
        if (instance == null) {
            instance = new FacetFinderRegistry();
            instance.loadInstances(FacetFinderRegistry.class.getClassLoader());
        }

        return instance;
    }

    private List facetFinders;

    /**
     * Create a new instance. Intialize it with no facet finders. You will
     * probably want to call <code>FacetFinderRegistry.instance()</code>
     * instead.
     */
    public FacetFinderRegistry()
    {
        facetFinders = new ArrayList();
    }

    /**
     * Load all FacetFinderSPI implementations that are registered in the
     * given ClassLoader.
     *
     * @param classLoader  a ClassLoader which will be searched
     */
    public void loadInstances(ClassLoader classLoader)
    {
        LOG.info("Loading all facet finders");
        SPInterface spiIF = new SPInterface(FacetFinderSPI.class);
        ClassLoaders loaders = new ClassLoaders();
        loaders.put(classLoader);
        Enumeration spe = Service.providers(spiIF, loaders);
        while (spe.hasMoreElements()) {
            FacetFinderSPI spi = (FacetFinderSPI) spe.nextElement();
            LOG.info("\t" + spi.getName());
            facetFinders.add(spi);
        }
        LOG.info("Done");
    }

    /**
     * Get all facet finders that can decompose a user object and mime type.
     * If there are no facet finders, then this list is empty.
     *
     * @param dataThing  the object to render
     * @return a (possibly empty) List of FacetFinderSPI instances
     */
    public List getFinders(DataThing dataThing)
    {
        LOG.info("Finding facetisers: " + dataThing);
        List res = new ArrayList();
        for (Iterator i = facetFinders.iterator(); i.hasNext();) {
            FacetFinderSPI finder = (FacetFinderSPI) i.next();
            LOG.info("\tfound: " + finder.getName());
            if (finder.canMakeFacets(dataThing)) {
                LOG.info("\taccepted: " + finder.getName());
                res.add(finder);
            }
        }

        return res;
    }

    public void addFinder(FacetFinderSPI finder)
    {
        facetFinders.add(finder);
    }

    public void removeFinder(FacetFinderSPI finder)
    {
        facetFinders.remove(finder);
    }

    public int size()
    {
        return facetFinders.size();
    }

    public Iterator iterator()
    {
        return facetFinders.iterator();
    }

    public FacetFinderSPI get(int i)
    {
        return (FacetFinderSPI) facetFinders.get(i);
    }
}