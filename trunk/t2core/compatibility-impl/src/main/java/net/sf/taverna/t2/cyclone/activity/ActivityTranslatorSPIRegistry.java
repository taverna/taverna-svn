package net.sf.taverna.t2.cyclone.activity;

import net.sf.taverna.t2.spi.SPIRegistry;

//need to suppress these warning as in the constructor ActivityTranslator<?>.class is not possible
//as far as I can tell.
/**
 * SPI Registry based upon ActivityTranslators
 * 
 * @see SPIRegistry
 * 
 * @author Stuart Owen
 * 
 */
@SuppressWarnings("unchecked")
public class ActivityTranslatorSPIRegistry extends
		SPIRegistry<ActivityTranslator> {

	private static ActivityTranslatorSPIRegistry instance = null;

	public static synchronized ActivityTranslatorSPIRegistry getInstance() {
		if (instance == null) {
			instance = new ActivityTranslatorSPIRegistry();
		}
		return instance;
	}

	/**
	 * Protected constructor, use {@link #getInstance()} instead.
	 * <p>
	 * Construct the ActivityTranslatorSPIRegistry to be an SPIRegistry based
	 * upon the ActivityTransator SPI interface.
	 */
	protected ActivityTranslatorSPIRegistry() {
		super(ActivityTranslator.class);
	}

}
