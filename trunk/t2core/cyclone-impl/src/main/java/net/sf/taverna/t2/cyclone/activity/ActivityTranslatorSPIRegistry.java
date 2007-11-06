package net.sf.taverna.t2.cyclone.activity;

import net.sf.taverna.t2.spi.SPIRegistry;

//need to suppress these warning as in the constructor ActivityTranslator<?>.class is not possible
//as far as I can tell.

@SuppressWarnings("unchecked") 
public class ActivityTranslatorSPIRegistry extends SPIRegistry<ActivityTranslator> {

	public ActivityTranslatorSPIRegistry() {
		super(ActivityTranslator.class);
	}

	

	
}
