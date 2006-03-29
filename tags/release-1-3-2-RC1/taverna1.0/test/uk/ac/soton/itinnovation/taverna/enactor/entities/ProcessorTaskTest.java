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
 * Filename           $RCSfile: ProcessorTaskTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-03-27 11:03:11 $
 *               by   $Author: sowen70 $
 * Created on 27-Mar-2006
 *****************************************************************/
package uk.ac.soton.itinnovation.taverna.enactor.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.embl.ebi.escience.scufl.enactor.WorkflowEventListener;
import org.embl.ebi.escience.scufl.enactor.event.CollectionConstructionEvent;
import org.embl.ebi.escience.scufl.enactor.event.IterationCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.UserChangedDataEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent;
import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;
import org.embl.ebi.escience.testhelpers.WorkflowFactory;

public class ProcessorTaskTest extends TestCase 
{
	
	
	public void testIterationsStoreCompletionEvents() throws Exception
	{				
		WorkflowLauncher launcher=new WorkflowLauncher(WorkflowFactory.getSimpleIteratingWorkflow());
		Map inputs=new HashMap();
		final List iterationCompletionEvents=new ArrayList();
		
		WorkflowEventListener listener=new WorkflowEventListener()
		{			
			
			public void processCompletedWithIteration(IterationCompletionEvent e) {
				iterationCompletionEvents.add(e);
			}

			public void collectionConstructed(CollectionConstructionEvent e) { 
				
			}

			public void dataChanged(UserChangedDataEvent e) { 
				
			}

			public void processCompleted(ProcessCompletionEvent e) {
				
			}

			public void processFailed(ProcessFailureEvent e) {
				
			}

			public void workflowCompleted(WorkflowCompletionEvent e) {
				
			}

			public void workflowCreated(WorkflowCreationEvent e) {
				
			}

			public void workflowFailed(WorkflowFailureEvent e) {
				
			}
			
		};
		launcher.execute(inputs, listener);
		assertEquals("There is no IterationCompletionEvent",1,iterationCompletionEvents.size());
		IterationCompletionEvent event=(IterationCompletionEvent)iterationCompletionEvents.get(0);
		
		assertEquals("There should be 4 associated completion events",4,event.getAssociatedCompletionEvents().size());
		
		
	}
	
	
}
