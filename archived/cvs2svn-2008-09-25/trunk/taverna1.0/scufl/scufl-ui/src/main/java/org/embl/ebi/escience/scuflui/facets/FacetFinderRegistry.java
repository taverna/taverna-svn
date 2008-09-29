package org.embl.ebi.escience.scuflui.facets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflui.spi.FacetFinderSPI;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;

/**
 * A registry that maintains a list of all facet finder service providers.
 * <p>
 * The <code>FacetFinderRegistry</code> should be accessed through the
 * instance obtained by calling <code>FacetFinderRegistry.instance()</code>.
 * This instance is initialised with the SPI implementations available through
 * the current class path.
 * 
 * @author Matthew Pocock
 * @author Stuart Owen
 */
public class FacetFinderRegistry extends TavernaSPIRegistry<FacetFinderSPI> {

	private static Logger logger = Logger.getLogger(FacetFinderRegistry.class);
	private static FacetFinderRegistry instance;

	public static synchronized FacetFinderRegistry instance() {
		if (instance == null) {
			instance = new FacetFinderRegistry();
		}
		return instance;
	}

	/**
	 * Create a new instance. Intialize it with no facet finders. You will
	 * probably want to call <code>FacetFinderRegistry.instance()</code>
	 * instead.
	 */
	private FacetFinderRegistry() {
		super(FacetFinderSPI.class);
	}


	/**
	 * Get all facet finders that can decompose a user object and mime type. If
	 * there are no facet finders, then this list is empty.
	 * 
	 * @param dataThing
	 *            the object to render
	 * @return a (possibly empty) List of FacetFinderSPI instances
	 */
	public List getFinders(DataThing dataThing) {
		logger.debug("Finding facetisers: " + dataThing);
		List res = new ArrayList();
		for (FacetFinderSPI finder : findComponents()) {
			logger.debug("\tfound: " + finder.getName());
			if (finder.canMakeFacets(dataThing)) {
				logger.debug("\taccepted: " + finder.getName());
				res.add(finder);
			}
		}

		return res;
	}

}
