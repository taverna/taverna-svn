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
package net.sf.taverna.t2.activities.localworker.xmlsplitter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.testutils.DummyProcessor;
import net.sf.taverna.t2.activities.testutils.LocationConstants;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.java.EchoList;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import org.embl.ebi.escience.scuflworkers.java.XMLInputSplitter;
import org.embl.ebi.escience.scuflworkers.java.XMLOutputSplitter;
import org.junit.Test;

public class XMLOutputSplitterLocalWorkerTranslatorTest implements LocationConstants {

	@Test
	public void testCanHandle() throws Exception {
		XMLOutputSplitterLocalWorkerTranslator translator = new XMLOutputSplitterLocalWorkerTranslator();
		LocalServiceProcessor processor = new LocalServiceProcessor(
				null, "XMLInputSplitter",
				new XMLOutputSplitter());
		assertTrue("should be able to handle XMLOutputSplitter",translator.canHandle(processor));
	}
	
	@Test
	public void testCantHandleInputSplitter() throws Exception {
		XMLOutputSplitterLocalWorkerTranslator translator = new XMLOutputSplitterLocalWorkerTranslator();
		LocalServiceProcessor processor = new LocalServiceProcessor(
				null, "XMLInputSplitter",
				new XMLInputSplitter());
		assertFalse("should not be able to handle XMLInputSplitter",translator.canHandle(processor));
	}
	
	@Test
	public void testLocalWorkerTranslatorCantHandle() throws Exception {
		XMLOutputSplitterLocalWorkerTranslator translator = new XMLOutputSplitterLocalWorkerTranslator();
		Processor processor = new DummyProcessor();
		assertFalse("should be not be able ot handle the dummy processor",translator.canHandle(processor));
		assertFalse("should be not be able ot handle null processor",translator.canHandle(null));
	}
	
	@Test
	public void testCantHandleOtherLocalWorkers() throws Exception {
		XMLOutputSplitterLocalWorkerTranslator translator = new XMLOutputSplitterLocalWorkerTranslator();
		LocalServiceProcessor processor = new LocalServiceProcessor(null, "EchoList", new EchoList());
		assertFalse("should not be able to handle the local service",translator.canHandle(processor));
	}
	
   
}
