package org.biomoby.client.taverna.plugin;

import org.biomoby.shared.MobyException;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import junit.framework.TestCase;

public class XMLUtilitiesTest extends TestCase {

	public void testIsMultipleInvocationMessage() {
		try {
			assertFalse(XMLUtilities.isMultipleInvocationMessage(sim_xml));
		} catch (MobyException e) {
			fail(e.getLocalizedMessage());
		}
		
		try {
			assertFalse(XMLUtilities.isMultipleInvocationMessage(mixed_single_xml));
		} catch (MobyException e) {
			fail(e.getLocalizedMessage());
		}
		
		try {
			assertTrue(XMLUtilities.isMultipleInvocationMessage(mim_xml));
		} catch (MobyException e) {
			fail(e.getLocalizedMessage());
		}
		
		try {
			assertFalse(XMLUtilities.isMultipleInvocationMessage((String)null));
		} catch (MobyException e) {
		}
		try {
			assertFalse(XMLUtilities.isMultipleInvocationMessage(simples_xml));
		} catch (MobyException e) {
		}
		try {
			if (XMLUtilities.isMultipleInvocationMessage(invalid))
				fail();
		} catch (MobyException e) {
		}
	}

	public void testGetListOfSimples() {
		try {
			// an invalid message should not return anything
			String[] simples = XMLUtilities.getListOfSimples(invalid);
			if (simples.length > 0)
				fail("getListOfSimples should not return simples from an invalid XML message.");
		} catch (MobyException e) {
		}
		
		try {
			// a message containing a collection should not return anything
			String[] simples = XMLUtilities.getListOfSimples(sim_xml);
			if (simples.length > 0)
				fail("getListOfSimples should not return simples from an XML message that contains a collection.");
		} catch (MobyException e) {
		}
		
		try {
			// a message containing multiple invocations should not return anything
			String[] simples = XMLUtilities.getListOfSimples(mim_xml);
			if (simples.length > 0)
				fail("getListOfSimples should not return simples from an XML message that contains a MIM message.");
		} catch (MobyException e) {
		}
		
		try {
			// here 5 simples should be returned
			String[] simples = XMLUtilities.getListOfSimples(mixed_single_xml);
			assertNotNull(simples);
			if (simples.length <= 0)
				fail("getListOfSimples should return simples from an XML message that contains only simples.");
			assertEquals(simples.length, 5);
			int start = 1;
			for (int i = 0; i < simples.length; i++) {
				assertNotNull(simples[i]);
				for (int j = start; j < simples.length; j++) {
					assertNotNull(simples[j]);
					assertNotSame(simples[i], simples[j]);
				}
				start++;
			}
		} catch (MobyException e) {
			fail(e.getLocalizedMessage());
		}
		
		try {
			// here 2 simples should be returned
			String[] simples = XMLUtilities.getListOfSimples(simples_xml);
			assertNotNull(simples);
			if (simples.length <= 0)
				fail("getListOfSimples should return simples from an XML message that contains only simples.");
			assertEquals(simples.length, 2);
			assertNotSame(simples[0], simples[1]);
			assertNotNull(simples[0]);
			assertNotNull(simples[1]);
		} catch (MobyException e) {
			fail(e.getLocalizedMessage());
		}
	}

	public void testGetListOfCollections() {
		try {
			String[] collections = XMLUtilities.getListOfCollections(invalid);
			if (collections.length > 0)
				fail("Invalid bioMOBY message should not return any collections");
		} catch (MobyException e) {
		}
		try {
			String[] collections = XMLUtilities.getListOfCollections(mim_xml);
			if (collections.length > 0)
				fail("Multiple invocation bioMOBY messages should not return any collections");
		} catch (MobyException e) {
		}
		
		try {
			// one collection
			String[] collections = XMLUtilities.getListOfCollections(sim_xml);
			assertNotNull(collections);
			if (collections.length <= 0)
				fail("BioMOBY message should have returned a collection");
			assertEquals(collections.length, 1);
			assertNotNull(collections[0]);
		} catch (MobyException e) {
		}
		
		try {
			// one collection
			String[] collections = XMLUtilities.getListOfCollections(simples_xml);
			if (collections.length > 0)
				fail("Collections should not have been found in\n" + simples_xml);
		} catch (MobyException e) {
		}
		
		try {
			// one collection
			String[] collections = XMLUtilities.getListOfCollections(mixed_single_xml);
			assertNotNull(collections);
			if (collections.length <= 0)
				fail("BioMOBY message should have returned a collection");
			assertEquals(collections.length, 2);
			assertNotNull(collections[0]);
			assertNotNull(collections[1]);
			assertNotSame(collections[0], collections[1]);
		} catch (MobyException e) {
		}
	}

	public void testGetSimple() {
		try {
			// one simple
			XMLUtilities.getSimple("sim1",mim_xml);
			fail();
		} catch (MobyException e) {
		}
		try {
			// one simple
			XMLUtilities.getSimple("outputString", mim_xml);
			fail();
		} catch (MobyException e) {
		}
		try {
			// one simple
			XMLUtilities.getSimple("mySimple",mim_xml);
			fail();
		} catch (MobyException e) {
		}
		try {
			// one simple
			XMLUtilities.getSimple("myDNA",mim_xml);
			fail();
		} catch (MobyException e) {
		}
		try {
			// one simple
			XMLUtilities.getSimple("sim1",invalid);
			fail();
		} catch (MobyException e) {
		}
		try {
			String simple = XMLUtilities.getSimple("outputString3",mixed_single_xml);
			assertNotNull(simple);
			String simple2 = XMLUtilities.getSimple("outputString2",mixed_single_xml);
			assertNotNull(simple2);
			String simple3 = XMLUtilities.getSimple("outputString",mixed_single_xml);
			assertNotNull(simple3);
			assertNotSame(simple, simple2);
			assertNotSame(simple3, simple2);
			assertNotSame(simple, simple3);
		} catch (MobyException e) {
			fail();
		}
		try {
			// should fail because simple inside collection
			XMLUtilities.getSimple("mySimple",sim_xml);
			fail();	
		} catch (MobyException e) {
		}
		try {
			// should fail because simple inside collection
			XMLUtilities.getSimple("outputString",sim_xml);
			fail();	
		} catch (MobyException e) {
		}
		try {
			String simple = XMLUtilities.getSimple("Simple2",simples_xml);
			assertNotNull(simple);
			String simple2 = XMLUtilities.getSimple("Simple1",simples_xml);
			assertNotNull(simple2);
			assertNotSame(simple, simple2);			
		} catch (MobyException e) {
			fail();
		}		
	}

	public void testGetQueryID() {
		try {
			//FIXME should fail?
			XMLUtilities.getQueryID(invalid);
			//fail();	
		} catch (MobyException e) {
		}
		try {
			// should fail
			XMLUtilities.getQueryID(mim_xml);
			fail();	
		} catch (MobyException e) {
		}
		try {
			String id = XMLUtilities.getQueryID(mixed_single_xml);
			assertNotNull(id);
			assertEquals(id,"a10");
		} catch (MobyException e) {
			fail();
		}
		try {
			String id = XMLUtilities.getQueryID(sim_xml);
			assertNotNull(id);
			assertEquals(id,"a10");
		} catch (MobyException e) {
			fail();
		}
		try {
			String id = XMLUtilities.getQueryID(simples_xml);
			assertNotNull(id);
			assertEquals(id,"a10");
		} catch (MobyException e) {
			fail();
		}		
	}

	public void testSetQueryID() {
		try {
			// FIXME should fail?
			String xml = XMLUtilities.setQueryID(invalid, "a11");
			assertEquals("a11", XMLUtilities.getQueryID(xml));
			//fail();
		} catch (MobyException e) {
		}
		try {
			XMLUtilities.setQueryID(mim_xml, "a11");
			fail();
		} catch (MobyException e) {
		}
		try {
			String xml = XMLUtilities.setQueryID(mixed_single_xml, "a11");
			assertEquals("a11", XMLUtilities.getQueryID(xml));
		} catch (MobyException e) {
			fail();
		}
		try {
			String xml = XMLUtilities.setQueryID(simples_xml, "a11");
			assertEquals("a11", XMLUtilities.getQueryID(xml));
		} catch (MobyException e) {
			fail();
		}
		try {
			String xml = XMLUtilities.setQueryID(sim_xml, "a11");
			assertEquals("a11", XMLUtilities.getQueryID(xml));
		} catch (MobyException e) {
			fail();
		}
	}

	public void testGetWrappedSimple() {
		try {
			XMLUtilities.getWrappedSimple("sim1",invalid);
			fail();
		} catch (MobyException e) {
		}
		
		try {
			XMLUtilities.getWrappedSimple("sim1",mim_xml);
			fail();
		} catch (MobyException e) {
		}
		
		try {
			String simple = XMLUtilities.getWrappedSimple("outputString",mixed_single_xml);
			assertNotNull(simple);
			String simple2 = XMLUtilities.getWrappedSimple("outputString2",mixed_single_xml);
			assertNotNull(simple2);
			String simple3 = XMLUtilities.getWrappedSimple("outputString3",mixed_single_xml);
			assertNotNull(simple3);
			assertTrue(!simple.equalsIgnoreCase(simple2));
			assertTrue(!simple.equalsIgnoreCase(simple3));
			assertTrue(!simple3.equalsIgnoreCase(simple2));
		} catch (MobyException e) {
			fail();
		}
		
		try {
			XMLUtilities.getWrappedSimple("outputString",sim_xml);
			fail();
		} catch (MobyException e) {
		}
		try {
			String simple = XMLUtilities.getWrappedSimple("Simple1",simples_xml);
			assertNotNull(simple);
			String simple2 = XMLUtilities.getWrappedSimple("Simple2",simples_xml);
			assertNotNull(simple2);
			assertTrue(!simple.equalsIgnoreCase(simple2));
		} catch (MobyException e) {
			fail();
		}
				
	}

	public void testGetCollection() {
		try {
			XMLUtilities.getCollection("outputString",invalid);
			fail();
		} catch (MobyException e) {
		}
		
		try {
			XMLUtilities.getCollection("outputString",mim_xml);
			fail();
		} catch (MobyException e) {
		}
		try {
			XMLUtilities.getCollection("myCollection",mim_xml);
			fail();
		} catch (MobyException e) {
		}
		try {
			String collection = XMLUtilities.getCollection("outputString",sim_xml);
			assertNotNull(collection);
		} catch (MobyException e) {
			fail();
		}
		try {
			XMLUtilities.getCollection("Simple1",simples_xml);
			fail();
		} catch (MobyException e) {
		}
		try {
			String collection = XMLUtilities.getCollection("outputString",mixed_single_xml);
			assertNotNull(collection);
			String collection2 = XMLUtilities.getCollection("myCollection",mixed_single_xml);
			assertNotNull(collection2);
			assertTrue(!collection.equalsIgnoreCase( collection2));
		} catch (MobyException e) {
			fail();
		}
	}

	public void testGetWrappedCollection() {
			try {
				XMLUtilities.getWrappedCollection("outputString",invalid);
				fail();
			} catch (MobyException e) {
			}
			
			try {
				XMLUtilities.getWrappedCollection("outputString",mim_xml);
				fail();
			} catch (MobyException e) {
			}
			try {
				XMLUtilities.getWrappedCollection("myCollection",mim_xml);
				fail();
			} catch (MobyException e) {
			}
			try {
				String collection = XMLUtilities.getWrappedCollection("outputString",sim_xml);
				assertNotNull(collection);
				assertEquals(2, XMLUtilities.getSimplesFromCollection(collection).length);
			} catch (MobyException e) {
				fail();
			}
			try {
				XMLUtilities.getWrappedCollection("Simple1",simples_xml);
				fail();
			} catch (MobyException e) {
			}
			try {
				String collection = XMLUtilities.getWrappedCollection("outputString",mixed_single_xml);
				assertNotNull(collection);
				assertEquals(2, XMLUtilities.getSimplesFromCollection(collection).length);
				String collection2 = XMLUtilities.getWrappedCollection("myCollection",mixed_single_xml);
				assertNotNull(collection2);
				assertTrue(!collection.equalsIgnoreCase( collection2));
				assertEquals(1, XMLUtilities.getSimplesFromCollection(collection2).length);
			} catch (MobyException e) {
				fail();
			}
	}

	public void testGetSimplesFromCollection() {
			try {
				XMLUtilities.getSimplesFromCollection("outputString",invalid);
				fail();
			} catch (MobyException e) {
			}
			
			try {
				XMLUtilities.getSimplesFromCollection("outputString",mim_xml);
				fail();
			} catch (MobyException e) {
			}
			try {
				XMLUtilities.getSimplesFromCollection("myCollection",mim_xml);
				fail();
			} catch (MobyException e) {
			}
			try {
				assertEquals(2, XMLUtilities.getSimplesFromCollection("outputString", sim_xml).length);
				String[] simples = XMLUtilities.getSimplesFromCollection("outputString", sim_xml);
				assertNotNull(simples[0]);
				assertNotNull(simples[1]);
				assertTrue(!simples[0].equalsIgnoreCase(simples[1]));
			} catch (MobyException e) {
				fail();
			}
			try {
				XMLUtilities.getSimplesFromCollection("Simple1",simples_xml);
				fail();
			} catch (MobyException e) {
			}
			try {
				assertEquals(2, XMLUtilities.getSimplesFromCollection("outputString", mixed_single_xml).length);
				assertEquals(1, XMLUtilities.getSimplesFromCollection("myCollection", mixed_single_xml).length);
				String[] simples = XMLUtilities.getSimplesFromCollection("outputString", mixed_single_xml);
				String[] simples2 = XMLUtilities.getSimplesFromCollection("myCollection", mixed_single_xml);
				assertNotNull(simples[0]);
				assertNotNull(simples[1]);
				assertNotNull(simples2[0]);
				
				assertTrue(!simples[0].equalsIgnoreCase(simples[1]));
				assertTrue(!simples[0].equalsIgnoreCase(simples2[0]));
				assertTrue(!simples[1].equalsIgnoreCase(simples2[0]));
				
			} catch (MobyException e) {
				fail();
			}
	}

	public void testGetAllSimplesByArticleName() {
		try {
			String[] simples = XMLUtilities.getAllSimplesByArticleName("",sim_xml);
			assertNotNull(simples);
			assertEquals(0, simples.length);
		} catch (MobyException e) {
		}
		try {
			String[] simples = XMLUtilities.getAllSimplesByArticleName("outputString",sim_xml);
			assertNotNull(simples);
			assertEquals(simples.length, 2);
			assertNotNull(simples[0]);
			assertNotNull(simples[1]);
		} catch (MobyException e) {
			fail();
		}
		try {
			String[] simples = XMLUtilities.getAllSimplesByArticleName("Simple1",simples_xml);
			assertNotNull(simples);
			assertEquals(simples.length, 1);
			assertNotNull(simples[0]);
			String[] simples2 = XMLUtilities.getAllSimplesByArticleName("Simple2",simples_xml);
			assertNotNull(simples2);
			assertEquals(simples2.length, 1);
			assertNotNull(simples2[0]);
			assertTrue(!simples[0].equalsIgnoreCase(simples2[0]));
		} catch (MobyException e) {
			fail();
		}
		
		try {
			String[] simples = XMLUtilities.getAllSimplesByArticleName("outputString",mixed_single_xml);
			assertNotNull(simples);
			assertEquals(3, simples.length);
			assertNotNull(simples[0]);
			assertNotNull(simples[1]);
			assertNotNull(simples[2]);
			String[] simples2 = XMLUtilities.getAllSimplesByArticleName("outputString2",mixed_single_xml);
			assertNotNull(simples2);
			assertEquals(1, simples2.length);
			assertNotNull(simples2[0]);
			assertTrue(!simples[0].equalsIgnoreCase(simples[1]));
			assertTrue(!simples[0].equalsIgnoreCase(simples[2]));
			assertTrue(!simples[1].equalsIgnoreCase(simples[2]));
			assertTrue(!simples[0].equalsIgnoreCase(simples2[0]));
			assertTrue(!simples[1].equalsIgnoreCase(simples2[0]));
			assertTrue(!simples[2].equalsIgnoreCase(simples2[0]));
		} catch (MobyException e) {
			fail();
		}
		
		try {
			String[] simples = XMLUtilities.getAllSimplesByArticleName("myCollection",mixed_single_xml);
			assertNotNull(simples);
			assertEquals(1, simples.length);
			assertNotNull(simples[0]);
		} catch (MobyException e) {
			fail();
		}
		
		try {
			String[] simples = XMLUtilities.getAllSimplesByArticleName("sequence",mixed_single_xml);
			assertNotNull(simples);
			assertEquals(1, simples.length);
			assertNotNull(simples[0]);
		
		} catch (MobyException e) {
			fail();
		}
		try {
			XMLUtilities.getAllSimplesByArticleName("outputString",invalid);
			fail();
		} catch (MobyException e) {
		}
		try {
			XMLUtilities.getAllSimplesByArticleName("sim1",invalid);
			fail();
		} catch (MobyException e) {
		}
		
		try {
			String[] simples = XMLUtilities.getAllSimplesByArticleName("outputString",mim_xml);
			assertNotNull(simples);
			assertEquals(5, simples.length);
			assertNotNull(simples[0]);
			assertNotNull(simples[1]);
		
			assertTrue(!simples[0].equalsIgnoreCase(simples[1]));
		} catch (MobyException e) {
			fail();
		}
		try {
			String[] simples = XMLUtilities.getAllSimplesByArticleName("myDNA",mim_xml);
			assertNotNull(simples);
			assertEquals(1, simples.length);
			assertNotNull(simples[0]);
		} catch (MobyException e) {
			fail();
		}
		try {
			String[] simples = XMLUtilities.getAllSimplesByArticleName("mySimple",mim_xml);
			assertNotNull(simples);
			assertEquals(1, simples.length);
			assertNotNull(simples[0]);
		} catch (MobyException e) {
			fail();
		}
		try {
			String[] simples = XMLUtilities.getAllSimplesByArticleName("my_simple",mim_xml);
			assertNotNull(simples);
			assertEquals(0, simples.length);
		} catch (MobyException e) {
			fail();
		}
		try {
			String[] simples = XMLUtilities.getAllSimplesByArticleName("myCollection",mim_xml);
			assertNotNull(simples);
			assertEquals(1, simples.length);
			assertNotNull(simples[0]);
		} catch (MobyException e) {
			fail();
		}
		try {
			String[] simples = XMLUtilities.getAllSimplesByArticleName("my_collection",mim_xml);
			assertNotNull(simples);
			assertEquals(0, simples.length);
		} catch (MobyException e) {
			fail();
		}
	}

	public void testGetWrappedSimplesFromCollection() {
		try {
			String[] simples = XMLUtilities.getWrappedSimplesFromCollection("outputString",sim_xml);
			assertNotNull(simples);
			assertEquals(2, simples.length);
			assertNotNull(simples[0]);
			assertNotNull(simples[1]);
			assertEquals(1, XMLUtilities.getAllSimplesByArticleName("outputString", simples[0]).length);
			assertEquals(1, XMLUtilities.getAllSimplesByArticleName("outputString", simples[1]).length);
			assertTrue(!simples[0].equalsIgnoreCase(simples[1]));
		} catch (MobyException e) {
			fail();
		}
		try {
			XMLUtilities.getWrappedSimplesFromCollection("mySimple",sim_xml);
			fail();
		} catch (MobyException e) {
		}
		
		try {
			XMLUtilities.getWrappedSimplesFromCollection("Simple1",simples_xml);
			fail();
		} catch (MobyException e) {
		}
		
		try {
			String[] simples = XMLUtilities.getWrappedSimplesFromCollection("outputString",mixed_single_xml);
			assertNotNull(simples);
			assertEquals(2, simples.length);
			assertNotNull(simples[0]);
			assertNotNull(simples[1]);
			assertEquals(1, XMLUtilities.getAllSimplesByArticleName("outputString", simples[0]).length);
			assertEquals(1, XMLUtilities.getAllSimplesByArticleName("outputString", simples[1]).length);
			assertTrue(!simples[0].equalsIgnoreCase(simples[1]));
		} catch (MobyException e) {
			fail();
		}
		try {
			XMLUtilities.getWrappedSimplesFromCollection("outputString3",mixed_single_xml);
			fail();
		} catch (MobyException e) {
		}
		
		try {
			XMLUtilities.getWrappedSimplesFromCollection("outputString",invalid);
			fail();
		} catch (MobyException e) {
		}
		
		try {
			XMLUtilities.getWrappedSimplesFromCollection("outputString",mim_xml);
			// mim not supported
			fail();
		} catch (MobyException e) {
		}
	}

	public void testGetSingleInvokationsFromMultipleInvokations() {
		try {
			XMLUtilities.getSingleInvokationsFromMultipleInvokations(invalid);
			fail();
		} catch (MobyException e) {
		}
		try {
			String[] invocations = XMLUtilities.getSingleInvokationsFromMultipleInvokations(mim_xml);
			assertNotNull(invocations);
			assertEquals(7, invocations.length);
			for (int i = 0; i < invocations.length; i++) {
				assertNotNull(invocations[i]);
				for (int j = i+1; j < invocations.length; j++) {
					assertTrue(!invocations[i].equalsIgnoreCase(invocations[j]));
				}
			}
		} catch (MobyException e) {
			fail();
		}
		
		try {
			String[] invocations = XMLUtilities.getSingleInvokationsFromMultipleInvokations(mixed_single_xml);
			assertNotNull(invocations);
			assertEquals(1, invocations.length);
			for (int i = 0; i < invocations.length; i++) {
				assertNotNull(invocations[i]);
				for (int j = i+1; j < invocations.length; j++) {
					assertTrue(!invocations[i].equalsIgnoreCase(invocations[j]));
				}
			}
		} catch (MobyException e) {
			fail();
		}
		
		try {
			String[] invocations = XMLUtilities.getSingleInvokationsFromMultipleInvokations(sim_xml);
			assertNotNull(invocations);
			assertEquals(1, invocations.length);
			for (int i = 0; i < invocations.length; i++) {
				assertNotNull(invocations[i]);
				for (int j = i+1; j < invocations.length; j++) {
					assertTrue(!invocations[i].equalsIgnoreCase(invocations[j]));
				}
			}
		} catch (MobyException e) {
			fail();
		}
		
		try {
			String[] invocations = XMLUtilities.getSingleInvokationsFromMultipleInvokations(simples_xml);
			assertNotNull(invocations);
			assertEquals(1, invocations.length);
			for (int i = 0; i < invocations.length; i++) {
				assertNotNull(invocations[i]);
				for (int j = i+1; j < invocations.length; j++) {
					assertTrue(!invocations[i].equalsIgnoreCase(invocations[j]));
				}
			}
		} catch (MobyException e) {
			fail();
		}
	}

	public void testGetDOMDocument() {
		try {
			Document d = XMLUtilities.getDOMDocument(mim_xml);
			assertNotNull(d);
		} catch (MobyException e) {
			fail();
		}
		try {
			XMLUtilities.getDOMDocument("<foo>");
			fail();
		} catch (MobyException e) {
		}
	}

	public void testRenameCollection() {
	}

	public void testRenameSimple() {
	}

	public void testCreateMobyDataElementWrapper() {
	}

	public void testCreateMultipleInvokations() {

		try {
			String[] invocations = XMLUtilities.getSingleInvokationsFromMultipleInvokations(mim_xml);
			assertNotNull(invocations);
			String inv = XMLUtilities.createMultipleInvokations(invocations);
			assertEquals(new XMLOutputter(Format.getPrettyFormat()).outputString(XMLUtilities.getDOMDocument(mim_xml)), new XMLOutputter(Format.getPrettyFormat()).outputString(XMLUtilities.getDOMDocument(inv)));
		} catch (MobyException e) {
			fail();
		}
		try {
			String[] invocations = XMLUtilities.getSingleInvokationsFromMultipleInvokations(mixed_single_xml);
			assertNotNull(invocations);
			String inv = XMLUtilities.createMultipleInvokations(invocations);
			assertEquals(new XMLOutputter(Format.getPrettyFormat()).outputString(XMLUtilities.getDOMDocument(mixed_single_xml)), new XMLOutputter(Format.getPrettyFormat()).outputString(XMLUtilities.getDOMDocument(inv)));
		} catch (MobyException e) {
			fail();
		}
		
	}

	public void testIsWrapped() {
		try {
			boolean b = XMLUtilities.isWrapped(invalid);
			assertEquals(false, b);
			b = XMLUtilities.isWrapped(mim_xml);
			assertEquals(true, b);
			b = XMLUtilities.isWrapped(sim_xml);
			assertEquals(true, b);
			b = XMLUtilities.isWrapped(simples_xml);
			assertEquals(true, b);
			b = XMLUtilities.isWrapped(mixed_single_xml);
			assertEquals(true, b);
		} catch (MobyException e) {
			fail();
		}
	}

	public void testIsCollection() {
		try {
			boolean b = XMLUtilities.isCollection(invalid);
			assertEquals(false, b);
			b = XMLUtilities.isCollection(mim_xml);
			assertEquals(false, b);
			b = XMLUtilities.isCollection(sim_xml);
			assertEquals(true, b);
			b = XMLUtilities.isCollection(simples_xml);
			assertEquals(false, b);
			b = XMLUtilities.isCollection(mixed_single_xml);
			assertEquals(true, b);
		} catch (MobyException e) {
			fail();
		}
	}

	public void testIsEmpty() {
		boolean b = XMLUtilities.isEmpty("<MOBY><mobyContent><mobyData/></mobyContent></MOBY>");
		assertTrue(b);
		b = XMLUtilities.isEmpty("<MOBY><mobyContent><mobyData>foo</mobyData></mobyContent></MOBY>");
		assertTrue(b);
		b = XMLUtilities.isEmpty("<MOBY><mobyContent><mobyData><foo/></mobyData></mobyContent></MOBY>");
		assertTrue(b);
		b = XMLUtilities.isEmpty(invalid);
		assertTrue(b);
		b = XMLUtilities.isEmpty(mim_xml);
		assertTrue(!b);
		b = XMLUtilities.isEmpty(mixed_single_xml);
		assertTrue(!b);
		b = XMLUtilities.isEmpty(sim_xml);
		assertTrue(!b);
		b = XMLUtilities.isEmpty(simples_xml);
		assertTrue(!b);
	}

	public void testMergeCollections() {

	}

	public void TestIsThereData() {
		boolean b = XMLUtilities.isThereData("<MOBY><mobyContent><mobyData/></mobyContent></MOBY>");
		assertTrue(!b);
		b = XMLUtilities.isThereData("<MOBY><mobyContent><mobyData>foo</mobyData></mobyContent></MOBY>");
		assertTrue(!b);
		b = XMLUtilities.isThereData("<MOBY><mobyContent><mobyData><foo/></mobyData></mobyContent></MOBY>");
		assertTrue(!b);
		b = XMLUtilities.isThereData(invalid);
		assertTrue(!b);
		b = XMLUtilities.isThereData(mim_xml);
		assertTrue(b);
		b = XMLUtilities.isThereData(mixed_single_xml);
		assertTrue(b);
		b = XMLUtilities.isThereData(sim_xml);
		assertTrue(b);
		b = XMLUtilities.isThereData(simples_xml);
		assertTrue(b);
		
		
	}

	private String sim_xml =  
		  "<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">"
		+ "  <mobyContent>"
		+ "    <moby:mobyData moby:queryID=\"a10\">"
		+ "      <moby:Collection moby:articleName=\"outputString\">"
		+ "      <moby:Simple moby:articleName=\"\">"
		+ "        <String moby:id=\"ID\" namespace=\"NS\">aa</String>"
		+ "      </moby:Simple>"
		+ "      <moby:Simple moby:articleName=\"mySimple\">"
		+ "        <moby:String moby:id=\"ID2\" moby:namespace=\"NS2\">bb</moby:String>"
		+ "      </moby:Simple>"
		+ "      </moby:Collection >"
		+ "    </moby:mobyData>"
		+ "  </mobyContent>"
		+ "</moby:MOBY>";
	
	private String simples_xml =  
		  "<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">"
		+ "  <mobyContent>"
		+ "    <moby:mobyData moby:queryID=\"a10\">"
		+ "      <moby:Simple moby:articleName=\"Simple1\">"
		+ "        <Integer moby:id=\"ID\" namespace=\"NS\">1</Integer>"
		+ "      </moby:Simple>"
		+ "      <moby:Simple moby:articleName=\"Simple2\">"
		+ "        <moby:String moby:id=\"ID2\" moby:namespace=\"NS2\">bb</moby:String>"
		+ "      </moby:Simple>"
		+ "    </moby:mobyData>"
		+ "  </mobyContent>"
		+ "</moby:MOBY>";
	private String mixed_single_xml = 
		  "<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">"
		+ "  <moby:mobyContent>"
		+ "    <moby:mobyData moby:queryID=\"a10\">"
		+ "      <moby:Collection moby:articleName=\"outputString\">"
		+ "      <moby:Simple moby:articleName=\"\">"
		+ "        <moby:String moby:id=\"\" moby:namespace=\"\">aa</moby:String>"
		+ "      </moby:Simple>"
		+ "      <moby:Simple moby:articleName=\"\">"
		+ "        <moby:String moby:id=\"\" moby:namespace=\"\">bb</moby:String>"
		+ "      </moby:Simple>"
		+ "      </moby:Collection >"
		+ "      <moby:Simple moby:articleName=\"outputString\">"
		+ "        <moby:String moby:id=\"\" moby:namespace=\"\">b</moby:String>"
		+ "      </moby:Simple>"
		+ "      <moby:Simple moby:articleName=\"outputString2\">"
		+ "        <moby:String moby:id=\"\" moby:namespace=\"\">c</moby:String>"
		+ "      </moby:Simple>"
		+ "		      <moby:Collection articleName=\"myCollection\">"
		+ "		      <moby:Simple>"
		+ "		        <moby:DNASequence moby:id=\"AJ012310\" moby:namespace=\"\">"
		+ "			        <moby:String moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"SequenceString\"></moby:String>"
		+ "			        <moby:Integer moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"Length\">5</moby:Integer>"
		+ "			  </moby:DNASequence>		"
		+ "		      </moby:Simple>"
		+ "		      </moby:Collection>"
		+ "		      <moby:Simple articleName=\"mySimple\">"
		+ "		        <moby:DNASequence moby:id=\"AJ012310\" moby:namespace=\"\">"
		+ "			        <moby:String moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"SequenceString\"></moby:String>"
		+ "			        <moby:Integer moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"Length\">5</moby:Integer>"
		+ "			  </moby:DNASequence>		"
		+ "		      </moby:Simple>"
		+ "		      <moby:Simple articleName=\"sequence\">"
		+ "		        <moby:DNASequence moby:id=\"AJ012310\" moby:namespace=\"\">"
		+ "			        <moby:String moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"SequenceString\">actgcgcgc</moby:String>"
		+ "			        <moby:Integer moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"Length\">5</moby:Integer>"
		+ "			  </moby:DNASequence>		" 
		+ "		      </moby:Simple>"
		+ "      <moby:Simple moby:articleName=\"outputString3\">"
		+ "        <moby:String moby:id=\"\" moby:namespace=\"\">d</moby:String>"
		+ "      </moby:Simple>" 
		+ "    </moby:mobyData>" 
		+ "  </moby:mobyContent>"
		+ "</moby:MOBY>";

	private String invalid =  
		  "<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">"
		+ "    <moby:mobyData moby:queryID=\"a10\">"
		+ "      <moby:Collection moby:articleName=\"outputString\">"
		+ "      <moby:Simple moby:articleName=\"\">"
		+ "        <String moby:id=\"ID\" namespace=\"NS\">aa</String>"
		+ "      </moby:Simple>"
		+ "      <moby:Simple moby:articleName=\"\">"
		+ "        <moby:String moby:id=\"ID2\" moby:namespace=\"NS2\">bb</moby:String>"
		+ "      </moby:Simple>"
		+ "      </moby:Collection >"
		+ "      <moby:Simple moby:articleName=\"sim1\">"
		+ "        <moby:String moby:id=\"ID2\" moby:namespace=\"NS2\">bb</moby:String>"
		+ "      </moby:Simple>"
		+ "    </moby:mobyData>"
		+ "</moby:MOBY>";
	
	private String mim_xml = 
			  "<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">"
			+ "  <moby:mobyContent>"
			+ "    <moby:mobyData moby:queryID=\"a10\">"
			+ "      <moby:Collection moby:articleName=\"outputString\">"
			+ "      <moby:Simple moby:articleName=\"\">"
			+ "        <moby:String moby:id=\"\" moby:namespace=\"\">aa</moby:String>"
			+ "      </moby:Simple>"
			+ "      <moby:Simple moby:articleName=\"\">"
			+ "        <moby:String moby:id=\"\" moby:namespace=\"\">bb</moby:String>"
			+ "      </moby:Simple>"
			+ "      </moby:Collection >"
			+ "      <moby:Simple moby:articleName=\"sim1\">"
			+ "        <moby:String moby:id=\"\" moby:namespace=\"\">bb</moby:String>"
			+ "      </moby:Simple>"
			+ "    </moby:mobyData>"
			+ "    <moby:mobyData moby:queryID=\"a11\">"
			+ "      <moby:Simple moby:articleName=\"outputString\">"
			+ "        <moby:String moby:id=\"\" moby:namespace=\"\">b</moby:String>"
			+ "      </moby:Simple>"
			+ "    </moby:mobyData>"
			+ "    <moby:mobyData moby:queryID=\"a12\">"
			+ "      <moby:Simple moby:articleName=\"outputString\">"
			+ "        <moby:String moby:id=\"\" moby:namespace=\"\">c</moby:String>"
			+ "      </moby:Simple>"
			+ "    </moby:mobyData>"
			+ "<moby:mobyData moby:queryID=\"my_collection\">"
			+ "		      <moby:Collection articleName=\"myCollection\">"
			+ "		      <moby:Simple>"
			+ "		        <moby:DNASequence moby:id=\"AJ012310\" moby:namespace=\"\">"
			+ "			        <moby:String moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"SequenceString\"></moby:String>"
			+ "			        <moby:Integer moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"Length\">5</moby:Integer>"
			+ "			  </moby:DNASequence>		"
			+ "		      </moby:Simple>"
			+ "		      </moby:Collection>"
			+ "		    </moby:mobyData>"
			+ "		    <moby:mobyData moby:queryID=\"my_simple\">"
			+ "		      <moby:Simple articleName=\"mySimple\">"
			+ "		        <moby:DNASequence moby:id=\"AJ012310\" moby:namespace=\"\">"
			+ "			        <moby:String moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"SequenceString\"></moby:String>"
			+ "			        <moby:Integer moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"Length\">5</moby:Integer>"
			+ "			  </moby:DNASequence>		"
			+ "		      </moby:Simple>"
			+ "		    </moby:mobyData>"
			+ "		    <moby:mobyData moby:queryID=\"q2\">"
			+ "		      <moby:Simple articleName=\"myDNA\">"
			+ "		        <moby:DNASequence moby:id=\"AJ012310\" moby:namespace=\"\">"
			+ "			        <moby:String moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"SequenceString\"></moby:String>"
			+ "			        <moby:Integer moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"Length\">5</moby:Integer>"
			+ "			  </moby:DNASequence>		" + "		      </moby:Simple>"
			+ "		    </moby:mobyData>" + "    <moby:mobyData moby:queryID=\"a13\">"
			+ "      <moby:Simple moby:articleName=\"outputString\">"
			+ "        <moby:String moby:id=\"\" moby:namespace=\"\">d</moby:String>"
			+ "      </moby:Simple>" + "    </moby:mobyData>" + "  </moby:mobyContent>"
			+ "</moby:MOBY>";

}
