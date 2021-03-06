/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.run;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.workbench.ShutdownSPI;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;

/**
 * Performs clean up of the Reference Manager's database.
 *  
 * @author Alex Nenadic
 *
 */
public class ReferenceDatabaseCleanUpShutdownHook implements ShutdownSPI{

	public int positionHint() {
		// Run after RemoveDataflowRunsShutdownHook but before Ref. Manager shutdown
		return 199;
	}
	
	public boolean shutdown() {
		boolean shutdown = true;
		final ArrayList<DataflowRun> wfRunList = DataflowRunsComponent.getInstance().getPreviousWFRuns();

		if (!wfRunList.isEmpty()) {
			final ReferenceDatabaseCleanUpShutdownDialog dialog = new ReferenceDatabaseCleanUpShutdownDialog();
			dialog.setInitialQueueSize(wfRunList.size());
				
			Thread dataDeletionThread = new Thread(){
				public void run(){
					for (int i=wfRunList.size()-1; i>=0; i--){
						if (!wfRunList.get(i).isProvenanceEnabledForRun()){ // provenance was not enabled for the wf run
							if (wfRunList.get(i).isDataSavedInDatabase()){ // was data for the wf run stored in database
								// Delete all the referenced data for the run
								DataflowRunsComponent.getInstance().getReferenceService().deleteReferencesForWorkflowRun(wfRunList.get(i).getRunId());
							}
						}
						else{ // provenance was enabled for the wf run
							if (!wfRunList.get(i).isDataSavedInDatabase()){ // data was stored in memory
								//Delete the run from provenance database
								String connectorType = DataManagementConfiguration
								.getInstance().getConnectorType();
								ProvenanceAccess provenanceAccess = new ProvenanceAccess(
										connectorType);
								provenanceAccess.removeRun(wfRunList.get(i).getRunId());
							}
						}
						wfRunList.remove(i);
					}
				}
			};
			dataDeletionThread.start();

			Timer timer = new Timer(500, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.setCurrentQueueSize(wfRunList.size());
					if (wfRunList.isEmpty()) {
						dialog.setVisible(false);
					}
				}
			});
			timer.start();

			dialog.setVisible(true);			
			timer.stop();
			shutdown = dialog.confirmShutdown();
			dialog.dispose();
		} 
		return shutdown;
		
	}	
}
