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
package net.sf.taverna.t2.activities.soaplab.query;

import static org.junit.Assert.*;

import java.util.Collection;

import net.sf.taverna.t2.activities.soaplab.servicedescriptions.SoaplabServiceProvider;
import net.sf.taverna.t2.activities.soaplab.servicedescriptions.SoaplabServiceProviderConfig;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;

import org.junit.Test;

import com.ibm.icu.impl.Assert;

public class SoaplabServiceProviderTest {

	private boolean foundResults = false;
	private boolean finished = false;
	
	@Test
	public void test() throws ConfigurationException {
		SoaplabServiceProvider provider = new SoaplabServiceProvider();
		SoaplabServiceProviderConfig conf = new SoaplabServiceProviderConfig("http://www.ebi.ac.uk/soaplab/services/");
		provider.configure(conf);
		
		FindServiceDescriptionsCallBack callback = new FindServiceDescriptionsCallBack() {
			public void fail(String message, Throwable ex) {
				Assert.fail(new Exception(message, ex));
			}

			public void finished() {
				finished = true;
			}
			public void partialResults(
					Collection<? extends ServiceDescription> serviceDescriptions) {
				foundResults = serviceDescriptions.size()>0;
			}

			public void status(String message) {
			}

			public void warning(String message) {
			}};
		provider.findServiceDescriptionsAsync(callback);
		assertTrue("Did not finish", finished);
		assertTrue("Did not find any results", foundResults);
		
	}
}
