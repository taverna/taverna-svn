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
package net.sf.taverna.t2.activities.stringconstant.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.testutils.DummyProcessor;
import net.sf.taverna.t2.workflowmodel.AbstractPort;

import org.embl.ebi.escience.scuflworkers.stringconstant.StringConstantProcessor;
import org.junit.Test;

/**
 * Tests the translation of a T1 StringConstantProcessor to a T2 StringConstantActivity
 * @author Stuart Owen
 *
 */
public class StringConstantActivityTranslatorTest {

	@Test
	public void testTranslation() throws Exception {
		StringConstantProcessor processor = new StringConstantProcessor(null,
				"simplestringconstant", "the_string_value");
		StringConstantActivity activity = (StringConstantActivity)new StringConstantActivityTranslator().doTranslation(processor);
		assertEquals("there should be no inputs",0,activity.getInputPorts().size());
		assertEquals("there should be 1 output",1,activity.getOutputPorts().size());
		assertEquals("the output port name should be value","value",((AbstractPort)activity.getOutputPorts().toArray()[0]).getName());
		assertEquals("the value should be 'the_string_value'","the_string_value",activity.getStringValue());
	}
	
	@Test
	public void testCanHandleTrue() throws Exception {
		StringConstantProcessor processor = new StringConstantProcessor(null,
				"simplestringconstant", "the_string_value");
		assertTrue(new StringConstantActivityTranslator().canHandle(processor));
	}
	
	@Test
	public void testCanHandleFalse() throws Exception {
		assertFalse(new StringConstantActivityTranslator().canHandle(new DummyProcessor()));
	}
}
