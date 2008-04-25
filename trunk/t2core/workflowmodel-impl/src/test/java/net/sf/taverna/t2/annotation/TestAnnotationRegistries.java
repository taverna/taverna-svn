package net.sf.taverna.t2.annotation;

import net.sf.taverna.t2.annotation.spi.AnnotationBeanRegistry;
import net.sf.taverna.t2.annotation.spi.AnnotationSourceRegistry;
import net.sf.taverna.t2.annotation.spi.CurationEventBeanRegistry;

import org.junit.Test;

public class TestAnnotationRegistries {
	
	@Test
	public void testAnnotationBeanSPI() {
		
		AnnotationBeanRegistry registry = new AnnotationBeanRegistry();
		for (AnnotationBeanSPI bean:registry.getInstances()){
			System.out.println(bean.getClass().getCanonicalName());
		}
		
	}
	
	@Test
	public void testAnnotationSourceSPICurationEventBeanSPI() {
		
		AnnotationSourceRegistry registry = new AnnotationSourceRegistry();
		for (AnnotationSourceSPI source:registry.getInstances()) {
			System.out.println(source.getClass().getCanonicalName());
		}
		
	}
	
	@Test
	public void testCurationEventBeanSPI() {
		
		CurationEventBeanRegistry registry = new CurationEventBeanRegistry();
		for (CurationEventBeanSPI bean:registry.getInstances()) {
			System.out.println(bean.getClass().getCanonicalName());
		}
		
	}

}
