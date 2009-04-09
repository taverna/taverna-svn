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
package net.sf.taverna.t2.reference;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests construction and use of the ReferenceServiceImpl through spring backed
 * by the InMemory...Dao implementations
 * 
 * @author Tom Oinn
 */
public class InMemoryReferenceServiceTest {

	@SuppressWarnings("unused")
	private ReferenceContext dummyContext = new ReferenceContext() {
		public <T> List<? extends T> getEntities(Class<T> arg0) {
			return new ArrayList<T>();
		}
	};

	@Test
	public void testInit() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"inMemoryReferenceServiceTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");
		System.out.println("Created reference service implementation :"
				+ rs.getClass().getCanonicalName());
	}

	@Test
	public void getReferenceFromString() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"inMemoryReferenceServiceTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");
		String ref = "t2:ref//test?abcd1234";
		// // get the bit before and after the ?
		// String[] split = ref.split("\\?");
		// // get the bit before and after the final '/' ie. the local part and
		// the
		// // depth, there might not be a split1[1] since it might not be a list
		// String[] split2 = split[1].split("/");
		// // get the t2:abc:// and the namespace
		// String[] split3 = split[0].split("//");
		// // get the t2 bit and the reference type bit
		// String[] split4 = split3[0].split(":");
		T2Reference referenceFromString = rs.referenceFromString(ref);

		
		assertEquals(referenceFromString.getNamespacePart(), "test");

		assertEquals(referenceFromString.getLocalPart(), "abcd1234");

		assertEquals(referenceFromString.getReferenceType(), T2ReferenceType.ReferenceSet);

		// T2Reference fromReference = rs.fromReference(ref);
		//		
		// fromReference.getDepth();
		//		
		// fromReference.getLocalPart();
		//		
		// fromReference.getNamespacePart();
		//		
		// fromReference.containsErrors();

	}

	@Test
	public void getErrorFromString() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"inMemoryReferenceServiceTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");
		String ref = "t2:error//test?abcd1234/2";
		T2Reference referenceFromString = null;
		try {
			referenceFromString = rs.referenceFromString(ref);		
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		assertEquals(referenceFromString.getNamespacePart(), "test");

		assertEquals(referenceFromString.getLocalPart(), "abcd1234");

		assertEquals(referenceFromString.getReferenceType(), T2ReferenceType.ErrorDocument);
		
		assertEquals(referenceFromString.getDepth(), 2);
		
	}
	
	
	@Test
	public void getListFromString() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"inMemoryReferenceServiceTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");
		String ref = "t2:list//test?abcd1234/true/2";
		
		T2Reference parseRef = rs.referenceFromString(ref);

		
		assertEquals(parseRef.getNamespacePart(), "test");

		assertEquals(parseRef.getLocalPart(), "abcd1234");

		assertEquals(parseRef.getReferenceType(), T2ReferenceType.IdentifiedList);
		
		assertEquals(parseRef.getDepth(), 2);
		
		assertEquals(parseRef.containsErrors(), true);
		
	}

	private Map<String, String> parseRef(String ref) {
		String[] split = ref.split("\\?");
		// get the bit before and after the final '/' ie. the local part and the
		// depth, there might not be a split1[1] since it might not be a list
		String[] split2 = split[1].split("/");
		// get the t2:abc:// and the namespace
		String[] split3 = split[0].split("//");
		// get the t2 bit and the reference type bit
		String[] split4 = split3[0].split(":");

		Map<String, String> refPartsMap = new HashMap<String, String>();
		refPartsMap.put("type", split4[1]);
		refPartsMap.put("namespace", split3[1]);
		refPartsMap.put("localPart", split2[0]);

		if (refPartsMap.get("type").equals("list")) {
			refPartsMap.put("error", split2[1]);
			refPartsMap.put("depth", split2[2]);
		}
		if (refPartsMap.get("type").equals("error")) {
			refPartsMap.put("depth", split2[1]);
		}

		return refPartsMap;

	}

}
