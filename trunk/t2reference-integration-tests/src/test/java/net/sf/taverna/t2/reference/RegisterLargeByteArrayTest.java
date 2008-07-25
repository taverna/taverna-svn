package net.sf.taverna.t2.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests that we can register a large byte array as a reference in the database,
 * exercising the binary sql type
 * 
 * @author Tom Oinn
 */
public class RegisterLargeByteArrayTest {

	private ReferenceContext dummyContext = new ReferenceContext() {
		public <T> List<? extends T> getEntities(Class<T> arg0) {
			return new ArrayList<T>();
		}
	};

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
		
		Object returnedObject=rs.renderIdentifier(reference, Object.class,dummyContext);
		byte [] newbytes=(byte[])returnedObject;
		assertEquals("There bytes should be of the same length",bytes.length,newbytes.length);
		assertNotSame("They shouldn't be the same actual object",bytes, newbytes);
		assertEquals("The bytes should have the same content",bytes,newbytes);
		
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
		assertEquals("The bytes should have the same content",bytes,newbytes);
		
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
