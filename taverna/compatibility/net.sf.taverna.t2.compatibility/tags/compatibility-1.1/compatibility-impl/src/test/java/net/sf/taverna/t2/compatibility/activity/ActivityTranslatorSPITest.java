/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.compatibility.activity;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.activities.apiconsumer.translator.ApiConsumerActivityTranslator;
import net.sf.taverna.t2.activities.beanshell.translator.BeanshellActivityTranslator;
import net.sf.taverna.t2.activities.biomart.translator.BiomartActivityTranslator;
import net.sf.taverna.t2.activities.soaplab.translator.SoaplabActivityTranslator;
import net.sf.taverna.t2.activities.stringconstant.translator.StringConstantActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslatorSPIRegistry;

import org.junit.Test;

public class ActivityTranslatorSPITest {

	@SuppressWarnings("unchecked")
	@Test
	public void testFindTranslators() {
		ActivityTranslatorSPIRegistry reg = ActivityTranslatorSPIRegistry.getInstance();
		List<ActivityTranslator> instances = reg.getInstances();
		
		assertEquals("There should be 5 instances - apiconsumer, beanshell, soaplab, biomart and stringconstant",5,instances.size());
		
		List<Class> expectedTypes = new ArrayList<Class>();
		expectedTypes.addAll(Arrays.asList(ApiConsumerActivityTranslator.class,BeanshellActivityTranslator.class,BiomartActivityTranslator.class,SoaplabActivityTranslator.class,StringConstantActivityTranslator.class));
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
	
	@Test
	public void testGetInstance() {
		ActivityTranslatorSPIRegistry reg = ActivityTranslatorSPIRegistry.getInstance();
		ActivityTranslatorSPIRegistry reg2 = ActivityTranslatorSPIRegistry.getInstance();
		assertNotNull(reg);
		assertSame(reg,reg2);
	}
}
