package net.sf.taverna.t2.annotation.spi;

import net.sf.taverna.t2.annotation.AnnotationSourceSPI;
import net.sf.taverna.t2.spi.SPIRegistry;

public class AnnotationSourceRegistry extends SPIRegistry<AnnotationSourceSPI>{

	public AnnotationSourceRegistry() {
		super(AnnotationSourceSPI.class);
	}

}
