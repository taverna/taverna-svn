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
public class ActivityTranslatorSPIRegistry extends SPIRegistry<ActivityTranslator> {

	/**
	 * Constructs the ActivityTranslatorSPIRegistry to be an SPIRegsistry based upon the ActivityTransator class 
	 */
	public ActivityTranslatorSPIRegistry() {
		super(ActivityTranslator.class);
	}

	

	
}
