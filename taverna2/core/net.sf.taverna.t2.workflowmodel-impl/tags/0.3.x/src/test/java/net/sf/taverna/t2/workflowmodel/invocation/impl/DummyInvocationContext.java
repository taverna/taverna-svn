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
package net.sf.taverna.t2.workflowmodel.invocation.impl;

import java.util.List;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceGenerator;

import org.junit.Ignore;
import org.springframework.context.ApplicationContext;

@Ignore
/**
 * Implementation of InvocationContext which pulls a ReferenceService from the
 * inMemoryReferenceServiceContext.xml context definition.
 * 
 * @author Tom Oinn
 */
public class DummyInvocationContext implements InvocationContext {

	private static ApplicationContext context = null;
	
	public DummyInvocationContext() {
		getReferenceService(); //force the context to be created early - otherwise tests seem to randomly fail depending upon the order they are run.
	}

	public synchronized ReferenceService getReferenceService() {
		if (context == null) {
			context = new RavenAwareClassPathXmlApplicationContext(
					"inMemoryReferenceServiceContext.xml");
			this.getReferenceService();
		}
		ReferenceService rs = (ReferenceService) context
				.getBean("referenceService");
		
		return rs;
	}

	public synchronized static T2Reference nextReference() {
		if (context == null) {
			context = new RavenAwareClassPathXmlApplicationContext(
					"inMemoryReferenceServiceContext.xml");
		}
		return ((T2ReferenceGenerator) context.getBean("referenceGenerator"))
				.nextReferenceSetReference();
	}

	public synchronized static T2Reference nextListReference(int depth) {
		if (context == null) {
			context = new RavenAwareClassPathXmlApplicationContext(
					"inMemoryReferenceServiceContext.xml");
		}
		return ((T2ReferenceGenerator) context.getBean("referenceGenerator"))
				.nextListReference(false, depth);
	}
	
	public <T> List<? extends T> getEntities(Class<T> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ProvenanceConnector getProvenanceConnector() {
		// TODO Auto-generated method stub
		return null;
	}

}
