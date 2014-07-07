/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
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
package net.sf.taverna.t2.activities.sadi.servicedescriptions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for {@link SADIServiceProvider}.
 *
 * @author David Withers
 */
public class SADIServiceProviderIT {
	
	private static final Logger logger = Logger.getLogger(SADIServiceProviderIT.class);

	private final class FindServiceDescriptionsCallBackImplementation implements
			FindServiceDescriptionsCallBack {
		
		public boolean warningCalled, statusCalled, partialResultsCalled, finishedCalled, failCalled;

		public void warning(String message) {
			warningCalled = true;
		}

		public void status(String message) {
			statusCalled = true;
		}

		@SuppressWarnings("rawtypes")
		public void partialResults(Collection<? extends ServiceDescription> serviceDescriptions) {
			partialResultsCalled = true;
			for (ServiceDescription service: serviceDescriptions)
				logger.debug(String.format("found service %s", service.getName()));
		}

		public void finished() {
			finishedCalled = true;
		}

		public void fail(String message, Throwable ex) {
			failCalled = true;
		}
	}

	private SADIServiceProvider sadiServiceProvider;
	
	private FindServiceDescriptionsCallBackImplementation callback;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sadiServiceProvider = new SADIServiceProvider();
		sadiServiceProvider.configure(sadiServiceProvider.getDefaultConfigurations().get(0));
		callback = new FindServiceDescriptionsCallBackImplementation();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceProvider#findServiceDescriptionsAsync(net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack)}.
	 * @throws ConfigurationException 
	 */
	@Test
	public void testFindServiceDescriptionsAsync() throws ConfigurationException {
		sadiServiceProvider.findServiceDescriptionsAsync(callback);
		assertTrue(callback.statusCalled);
		assertTrue(callback.partialResultsCalled);
		assertTrue(callback.finishedCalled);
		assertFalse(callback.warningCalled);
		assertFalse(callback.failCalled);

		sadiServiceProvider.configure(new SADIServiceProviderConfig());
		callback = new FindServiceDescriptionsCallBackImplementation();
		sadiServiceProvider.findServiceDescriptionsAsync(callback);
		assertTrue(callback.statusCalled);
		assertFalse(callback.partialResultsCalled);
		assertFalse(callback.finishedCalled);
		assertFalse(callback.warningCalled);
		assertTrue(callback.failCalled);
	}

}
