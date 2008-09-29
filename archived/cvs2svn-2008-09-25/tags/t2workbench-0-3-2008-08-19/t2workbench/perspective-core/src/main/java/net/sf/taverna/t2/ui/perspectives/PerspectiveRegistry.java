package net.sf.taverna.t2.ui.perspectives;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

/**
 * SPI registry responsible for finding PerspectiveSPI's which define a UI
 * perspective
 * 
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 * 
 */
public class PerspectiveRegistry extends SPIRegistry<PerspectiveSPI> {

	private static PerspectiveRegistry instance = null;

	public static synchronized PerspectiveRegistry getInstance() {
		if (instance == null) {
			instance = new PerspectiveRegistry();
		}
		return instance;
	}

	protected PerspectiveSorter perspectiveSorter = new PerspectiveSorter();

	protected PerspectiveRegistry() {
		super(PerspectiveSPI.class);
	}

	/**
	 * Return a list of the discovered {@link PerspectiveSPI}s, sorted by
	 * increasing {@link PerspectiveSPI#positionHint()}s.
	 * 
	 * @return {@link List} of the discovered {@link PerspectiveSPI}s
	 */
	public List<PerspectiveSPI> getPerspectives() {
		List<PerspectiveSPI> result = getInstances();
		Collections.sort(result, perspectiveSorter);
		return result;
	}

	protected class PerspectiveSorter implements Comparator<PerspectiveSPI> {
		public int compare(PerspectiveSPI o1, PerspectiveSPI o2) {
			return new Integer(o1.positionHint()).compareTo(new Integer(o2
					.positionHint()));
		}
	}
}
