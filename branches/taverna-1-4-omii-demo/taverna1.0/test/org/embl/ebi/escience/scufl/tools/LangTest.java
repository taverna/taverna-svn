package org.embl.ebi.escience.scufl.tools;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junit.framework.TestCase;

public class LangTest extends TestCase {
	public void testAsObject() {
		assertEquals(new Boolean(true), Lang.asObject(true));
		assertEquals(new Byte("20"), Lang.asObject(Byte.parseByte("20")));
		assertEquals(new Character('x'), Lang.asObject('x'));
		assertEquals(new Double(1.337), Lang.asObject(1.337));
		assertEquals(new Float(13.37), Lang.asObject((float) 13.37));
		assertEquals(new Integer(1337), Lang.asObject(1337));
		assertEquals(new Long(Long.MIN_VALUE), Lang.asObject(Long.MIN_VALUE));
		assertEquals(new Short(Short.MAX_VALUE), Lang.asObject(Short.MAX_VALUE));		
	}
	public void testAsObjectListInt() {
		int[] ints = {1,2,3};
		List intList = Lang.asObjectList(ints);
		assertEquals(new Integer(ints[0]), intList.get(0));
		assertEquals(new Integer(ints[2]), intList.get(2));
		assertEquals(ints.length, intList.size());
	}
	public void testAsObjectListDouble() {
		double[] doubles = {1.23, 1.5, 1.5};
		List doubleList = Lang.asObjectList(doubles);
		assertEquals(new Double(doubles[0]), doubleList.get(0));
		assertEquals(new Double(doubles[2]), doubleList.get(2));
		assertEquals(doubles.length, doubleList.size());
	}	
	public void testAsObjectListStrings() {
		String[] strings = {"Hello", "there", "mister"};
		List stringList = Lang.asObjectList(strings);
		assertEquals(strings[0], stringList.get(0));
		assertEquals(strings[2], stringList.get(2));
		assertEquals(strings.length, stringList.size());
	}
	public void testAsObjectListComposite() {
		String[] strings = { "Hello", "there", "mister" };
		int[] ints = { 1, 2, 3 };
		List list = new ArrayList();
		list.add("extra");
		Object[] composite = { strings, ints, list };
		List compositeList = Lang.asObjectList(composite);
		assertTrue(compositeList.get(0) instanceof List);		
		assertTrue(compositeList.get(1) instanceof List);		
		assertEquals(composite.length, compositeList.size());
		assertEquals("there", ((List) compositeList.get(0)).get(1));
		assertEquals(new Integer(3), ((List) compositeList.get(1)).get(2));		
		assertEquals("extra", ((List) compositeList.get(2)).get(0));		
		assertNotSame("Should be a copy", list, compositeList.get(2));		
	}
	public void testAsObjectListList() {
		String[] strings = { "Hello", "there", "mister" };
		List list = new ArrayList();
		list.add(strings);				
		List objectList = Lang.asObjectList(list);
		assertNotSame(objectList, list);
		assertTrue(objectList.get(0) instanceof List);		
		assertEquals(((List) objectList.get(0)).get(1), "there");				
	}
	
	public void testMap() {
		List list = new ArrayList();
		list.add("fish");
		list.add("chips");
		class Class {
			public String upper(String s) {
				return s.toUpperCase();
			}
		}
		Class myObject = new Class();
		List newList = Lang.map("upper", list, myObject);
		assertEquals("fish", list.get(0)); // should be unchanged  
		assertEquals("chips", list.get(1));
		assertEquals("FISH", newList.get(0)); // mapped!
		assertEquals("CHIPS", newList.get(1));
	}
	
	public void testGetMethod() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		class Class {
			int value = 0;
			public void method1() {value=1;}
			public void method2() {value=2;}
			public void method3() {value=3;}
		}
		Class c = new Class();
		Method method = Lang.getMethod("method2", c);
		assertEquals(method.getName(), "method2");
		method.invoke(c, new Object[0]);
		assertEquals(c.value, 2);		
	}

}
