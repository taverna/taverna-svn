package net.sf.taverna.t2.annotation.spi;

import net.sf.taverna.t2.annotation.CurationEventBeanSPI;
import net.sf.taverna.t2.spi.SPIRegistry;

public class CurationEventBeanRegistry extends SPIRegistry<CurationEventBeanSPI>{

	public CurationEventBeanRegistry() {
		super(CurationEventBeanSPI.class);
	}

}
