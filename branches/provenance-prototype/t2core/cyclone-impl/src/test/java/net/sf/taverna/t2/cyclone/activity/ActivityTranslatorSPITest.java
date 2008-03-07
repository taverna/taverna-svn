package net.sf.taverna.t2.cyclone.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.activities.beanshell.translator.BeanshellActivityTranslator;
import net.sf.taverna.t2.activities.biomart.translator.BiomartActivityTranslator;
import net.sf.taverna.t2.activities.soaplab.translator.SoaplabActivityTranslator;
import net.sf.taverna.t2.activities.stringconstant.translator.StringConstantActivityTranslator;

import org.junit.Test;

public class ActivityTranslatorSPITest {

	@SuppressWarnings("unchecked")
	@Test
	public void testFindTranslators() {
		ActivityTranslatorSPIRegistry reg = new ActivityTranslatorSPIRegistry();
		List<ActivityTranslator> instances = reg.getInstances();
		
		assertEquals("There should be 4 instances - beanshell, soaplab, biomart and stringconstant",4,instances.size());
		
		List<Class> expectedTypes = new ArrayList<Class>();
		expectedTypes.addAll(Arrays.asList(BeanshellActivityTranslator.class,BiomartActivityTranslator.class,SoaplabActivityTranslator.class,StringConstantActivityTranslator.class));
		for (Object obj : instances) {
			if (expectedTypes.contains(obj.getClass())) {
				expectedTypes.remove(obj.getClass());
			}
			else {
				fail("Unexpected instance found of type:"+obj.getClass());
			}
		}
		
		assertEquals("Not all expected instance types were found",0,expectedTypes.size());
	}
}
