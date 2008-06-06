package net.sf.taverna.t2.reference;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class ReferenceSetDaoTest {

	@Test
	public void doTest() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
		"referenceSetDaoTestContext1.xml");
		ReferenceSetService rss = (ReferenceSetService) context.getBean("referenceSetService");
		
		Set<ExternalReferenceSPI> references = new HashSet<ExternalReferenceSPI>();
		references.add((ExternalReferenceSPI) context.getBean("exampleUrlBean"));
		references.add((ExternalReferenceSPI) context.getBean("exampleFileBean"));
				
		ReferenceSet rs = rss.registerReferenceSet(references);
		System.out.println(rs);
		
	}
	
}
