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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.reference.impl.EmptyReferenceContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests that we can register a large byte array as a reference in the database,
 * exercising the binary sql type
 * 
 * @author Tom Oinn
 */
public class RegisterLargeByteArrayTest {

	private ReferenceContext dummyContext = new EmptyReferenceContext();

	@Test
	public void testRegisterFromByteArrayList() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"registrationAndTraversalTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");

		List<byte[]> objectsToRegister = new ArrayList<byte[]>();

		byte[] largeBytes = getLargeByteArray();
		System.out.println(largeBytes.length);
		objectsToRegister.add(largeBytes);
		objectsToRegister.add(largeBytes);
		objectsToRegister.add(largeBytes);
		objectsToRegister.add(largeBytes);
		objectsToRegister.add(largeBytes);

		T2Reference ref = rs.register(objectsToRegister, 1, true, dummyContext);
		System.out.println(ref);

		Iterator<ContextualizedT2Reference> refIter = rs.traverseFrom(ref, 0);
		while (refIter.hasNext()) {
			System.out.println(refIter.next());
		}

	}
	
	@Test
	public void register1kByteArray() throws Exception {
		byte[] bytes = readBinaryData("1k.bin");
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"registrationAndTraversalTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");
		T2Reference reference = rs.register(bytes, 0, true, dummyContext);
		System.out.println("Registered 1k with rs " + rs);
		
		Object returnedObject=rs.renderIdentifier(reference, Object.class,dummyContext);
		byte [] newbytes=(byte[])returnedObject;
		assertEquals("There bytes should be of the same length",bytes.length,newbytes.length);
		assertNotSame("They shouldn't be the same actual object",bytes, newbytes);
		assertTrue("The bytes should have the same content",Arrays.equals(bytes,newbytes));
		
	}
	
	@Test
	public void register1mByteArray() throws Exception {
		byte[] bytes = readBinaryData("1m.bin");
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"registrationAndTraversalTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");
		T2Reference reference = rs.register(bytes, 0, true, dummyContext);
		
		Object returnedObject=rs.renderIdentifier(reference, Object.class,dummyContext);
		byte [] newbytes=(byte[])returnedObject;
		assertEquals("There bytes should be of the same length",bytes.length,newbytes.length);
		assertNotSame("They shouldn't be the same actual object",bytes, newbytes);
		assertTrue("The bytes should have the same content",Arrays.equals(bytes,newbytes));
		
	}
	
	private byte[] readBinaryData(String resourceName) throws Exception {
		InputStream instr = RegisterLargeByteArrayTest.class
				.getResourceAsStream("/data/" + resourceName);
		int size = instr.available();
		byte[] result = new byte[size];
		instr.read(result);
		return result;
	}

	private static byte[] getLargeByteArray() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10000; i++) {
			sb.append("abcdeabcdeabcdeabcdeabcdeabcdeabcdeabcdeabcdeabcde\n");
		}
		return sb.toString().getBytes();
	}

}
