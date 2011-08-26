package net.sf.taverna.t2.annotation.spi;

import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.spi.SPIRegistry;

public class AnnotationBeanRegistry extends SPIRegistry<AnnotationBeanSPI>{

	public AnnotationBeanRegistry() {
		super(AnnotationBeanSPI.class);
	}

}
