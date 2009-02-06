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
package net.sf.taverna.t2.activities.beanshell.translator;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;

import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import org.junit.Test;

public class BeanshellActivityTranlslatorTest {

	@Test
	public void testScript() throws Exception {
		BeanshellProcessor processor = new BeanshellProcessor(null,
				"simplebeanshell", "", new String[] { "input1", "input2" },
				new String[] { "output" });
		processor.setScript("this is a script");
		BeanshellActivity activity = (BeanshellActivity)new BeanshellActivityTranslator().doTranslation(processor);
		assertEquals("the getScript result was not what was expected","this is a script",activity.getConfiguration().getScript());
	}
}
