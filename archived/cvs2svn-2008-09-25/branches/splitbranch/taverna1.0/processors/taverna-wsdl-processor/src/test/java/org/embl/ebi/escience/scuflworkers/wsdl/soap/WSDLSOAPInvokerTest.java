/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: WSDLSOAPInvokerTest.java,v $
 * Revision           $Revision: 1.1.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-07 13:57:18 $
 *               by   $Author: sowen70 $
 * Created on 04-May-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;

public class WSDLSOAPInvokerTest extends TestCase {

	private static Logger logger = Logger.getLogger(WSDLSOAPInvokerTest.class);

	public void testPrimitive() throws Exception {
		
		WSDLBasedProcessor processor = null;
		try {
			processor = new WSDLBasedProcessor(null, "procName", "http://soap.genome.jp/KEGG.wsdl",
					"get_pathways_by_genes");
		} catch (ProcessorCreationException e) {
			logger.error("Unable to connect to serivce in testComplexDocStyle, skipping test");
			return; // don't fail because the service is unavailable
		}

		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(processor);

		String[] inputs = { "eco:b0077", "eco:b0078" };

		Map inputMap = new HashMap();
		inputMap.put("genes_id_list", new DataThing(inputs));

		Map outputMap = invoker.invoke(inputMap);

		assertEquals("should be 2 elements to output", 2, outputMap.size());

		DataThing outputThing = (DataThing) outputMap.get("return");

		assertNotNull("no return value with name 'return' found", outputThing);

		assertEquals("result should be an ArrayList", ArrayList.class, outputThing.getDataObject().getClass());

	}

	public void testComplexDocStyle() throws Exception {
		
		WSDLBasedProcessor processor = null;
		try {
			processor = new WSDLBasedProcessor(null, "procName",
					"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils_lite.wsdl", "run_eInfo");

		} catch (ProcessorCreationException e) {
			logger.error("Unable to connect to serivce in testComplexDocStyle, skipping test");
			return; // don't fail because the service is unavailable
		}

		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(processor);

		String input = "<eInfoRequest><db>pubmed</db></eInfoRequest>";

		Map inputMap = new HashMap();
		inputMap.put("parameters", new DataThing(input));

		Map outputMap = invoker.invoke(inputMap);

		assertEquals("should be 2 elements to output", 2, outputMap.size());

		DataThing outputThing = (DataThing) outputMap.get("parameters");

		assertNotNull("no return with name 'parameters' found", outputThing);

		assertEquals("result should be a string", String.class, outputThing.getDataObject().getClass());

		String xml = (String) outputThing.getDataObject();
		assertTrue("unexpected start to result", xml.startsWith("<eInfoResult"));
		assertTrue("unexpected end to result", xml.endsWith("/eInfoResult>"));

	}

	public void testComplexMultiRef() throws Exception {
		
		WSDLBasedProcessor processor = null;
		try {
			processor = new WSDLBasedProcessor(null, "procName", "http://genex.hgu.mrc.ac.uk/axis/services/ma?wsdl",
					"whatGeneInStage");

		} catch (ProcessorCreationException e) {
			logger.error("Unable to connect to serivce in testComplexMultiRef, skipping test");
			return; // don't fail because the service is unavailable
		}

		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(processor);

		Map inputs = new HashMap();
		inputs.put("in0", new DataThing("13"));
		inputs.put("in1", new DataThing("14"));
		inputs.put("in2", new DataThing("1"));

		Map outputs = invoker.invoke(inputs);

		assertEquals("outputs should have 2 entries", 2, outputs.size());

		DataThing thing = (DataThing) outputs.get("whatGeneInStageReturn");

		assertNotNull("output should contain an entry with key 'whatGeneInStageReturn'", thing);

		assertEquals("output type should be of type String", String.class, thing.getDataObject().getClass());

		assertTrue("invalid start to xml", thing.getDataObject().toString().startsWith("<whatGeneInStageReturn>"));
		assertTrue("invalid end to xml", thing.getDataObject().toString().endsWith("</whatGeneInStageReturn>"));
	}
	
	public void testMultirefWithOutputNamespaced() throws Exception
	{
		WSDLBasedProcessor processor = null;
		try {
			processor = new WSDLBasedProcessor(null, "procName", "http://www.broad.mit.edu/webservices/genecruiser/services/Annotation?wsdl",
					"getDatabasesWithDetails");

		} catch (ProcessorCreationException e) {
			logger.error("Unable to connect to serivce in testMultirefWithOutputNamespaced, skipping test");
			return; // don't fail because the service is unavailable
		}
		
		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(processor);
		
		Map output = invoker.invoke(new HashMap());
		
		assertNotNull("no result returned",output.get("getDatabasesWithDetailsReturn"));
	}	

	// The following services were found at http://www.xmethods.org/
	// and can be tested via that site.

	public void testSOAPEncoded() throws Exception {
		
		WSDLBasedProcessor processor = null;
		try {
			processor = new WSDLBasedProcessor(null, "procName",
					"http://www.claudehussenet.com/ws/services/Anagram.wsdl", "getRandomizeAnagram");
		} catch (ProcessorCreationException e) {
			logger.error("Unable to connect to serivce in testSOAPEncoded, skipping test");
			return; // don't fail because the service is unavailable
		}

		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(processor);
		Map output = invoker.invoke(new HashMap());
		assertEquals("should be 2 elements", 2, output.size());
		DataThing outputThing = (DataThing) output.get("Result");
		assertNotNull("there should be a result of name 'Result'", outputThing);
		assertEquals("output data should be ArrayList", ArrayList.class, outputThing.getDataObject().getClass());
	}
	
	public void testDocumentNamespace() throws Exception {
		
		WSDLBasedProcessor processor = null;
		try {
			processor = new WSDLBasedProcessor(null, "procName",
					"http://www.oorsprong.org/websamples.countryinfo/CountryInfoService.wso?WSDL", "CapitalCity");
		} catch (ProcessorCreationException e) {
			logger.error("Unable to connect to serivce in testDocumentNamespace, skipping test");
			return; // don't fail because the service is unavailable
		}
		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(processor);
		Map inputMap = new HashMap();
		inputMap.put("parameters", new DataThing("<parameters><sCountryISOCode>FR</sCountryISOCode></parameters>"));

		invoker.invoke(inputMap);
	}
	
	//can't always assume the return will be nested in a tag named the same as the output message part.
	public void testEncodedDifferentOutputName() throws Exception
	{
		WSDLBasedProcessor processor = null;
		try {
			processor = new WSDLBasedProcessor(null, "procName",
					"http://biowulf.bu.edu/zlab/promoser/promoser.wsdl", "help");
		} catch (ProcessorCreationException e) {
			logger.error("Unable to connect to serivce in testEncodedDifferentOutputName, skipping test");
			return; // don't fail because the service is unavailable
		}
		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(processor);
		Map output=invoker.invoke(new HashMap());
		
		assertNotNull("missing output",output.get("helpResponseSoapMsg"));
		
		DataThing thing = (DataThing)output.get("helpResponseSoapMsg");
		
		assertTrue("unexpected output contents",thing.getDataObject().toString().startsWith("Usage:"));
	}

}
