////////////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2002
//
// Copyright in this library belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP, UK.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1
// of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation Inc, 59 Temple Place, Suite 330, Boston MA 02111-1307 USA.
//
//      Created By          :   Darren Marvin
//      Created Date        :   2003/04/16
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2003-05-23 12:36:00 $
//                              $Revision: 1.5 $
//
///////////////////////////////////////////////////////////////////////////////////////


package uk.ac.soton.itinnovation.taverna.enactor;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.broker.WSFlowReceipt;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBroker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBrokerFactory;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowCallback;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowMessage;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowReceipt;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.evictor.Evictor;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.frontend.WorkflowEnactor;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.frontend.WorkflowInstance;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.frontend.iWorkflowExecutionListener;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.frontend.iWorkflowExecutionSource;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.User;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.specification.WorkflowSpecParseException;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.specification.WorkflowValidationException;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaWorkflowSubmission;

// Utility Imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// IO Imports
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import java.lang.Exception;
import java.lang.String;
import java.lang.StringBuffer;
import java.lang.System;



/**
 This is the basic workflow frontend that is
 provided for export as a web service
 */
public class TavernaWorkflowEnactor extends WorkflowEnactor implements FlowCallback, iWorkflowExecutionSource {

    private static ArrayList registry = new ArrayList();
    private static List listeners = null;
	protected static Evictor evictor;
    protected Logger m_log = Logger.getLogger(getClass());
	

    public TavernaWorkflowEnactor() {
        //I know that throwing exception is not best but should
        //create soap fault.
        System.out.println("Using Taverna XScufl Support");
		try {            
            m_log = Logger.getLogger(getClass());
			//set up an Evictor if one does not already exist
			evictor = Evictor.getInstance();
			evictor.commenceEviction();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	public void shutdown() {
		evictor.ceaseEviction();
	}
	
    public synchronized String validateWorkflow(String workflowSpec) throws Exception {
        StringBuffer errorLog = new StringBuffer();
        boolean val = isValid(workflowSpec, errorLog);
        m_log.debug("validateWorkflow()");
        m_log.debug("isValid returned: " + val);
        return errorLog.toString();
    }

    public synchronized String submitWorkflow(String workflowSpec,
        String dataIn, String user)
        throws Exception {

        try {
            String nl = System.getProperty("line.separator");

            byte[] wfArray = workflowSpec.getBytes();
            BufferedInputStream wfbais = new BufferedInputStream(new ByteArrayInputStream(wfArray));

            byte[] indArray = dataIn.getBytes();
            BufferedInputStream indbais = new BufferedInputStream(new ByteArrayInputStream(indArray));

            StringWriter sWriter = new StringWriter();

            while (wfbais.available() > 0) {
                sWriter.write(wfbais.read());
            }
            String workflowDefn = sWriter.toString();

            sWriter = new StringWriter();
            while (indbais.available() > 0) {
                sWriter.write(indbais.read());
            }

            byte[] ub = user.getBytes();
            ByteArrayInputStream userStream = new ByteArrayInputStream(ub);
            User userObj = new User(userStream);

            //submit the flow
            String input = sWriter.toString();
            
			TavernaWorkflowSubmission submit = new TavernaWorkflowSubmission(workflowDefn, input, userObj.getID(), userObj.getContext());
			FlowBroker broker = FlowBrokerFactory.createFlowBroker("uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker");
			TavernaFlowReceipt receipt = (TavernaFlowReceipt) broker.submitFlow(submit);
			String workflowID = receipt.getID();
			
            //registry this as listener
            receipt.registerFlowCallback(this);
            WorkflowInstance workflow = new WorkflowInstance(workflowID, receipt, workflowSpec, dataIn, user);

            registry.add(workflow);

            if (m_log != null) {
                m_log.debug("submitWorkflow");
                m_log.debug("Workflow instance id: " + workflowID);
                m_log.debug("workflowSpec: " + nl + workflowSpec);
                m_log.debug("dataIn: " + nl + dataIn);
                m_log.debug("user: " + nl + user);
			
                // log number of workflows running
                m_log.debug("submitWorkflow");
                m_log.debug("Number of active workflows: " + registry.size());
            }

            return workflowID;

        } catch (Exception ex) {
            if (m_log != null) {
                m_log.warn(ex);
            }
            //ex.printStackTrace();
            throw ex;
        }
    }

    /*
     public synchronized String checkServicesAvailable(String workflowSpec) throws Exception {
     byte[] wfArray = workflowSpec.getBytes();
     ByteArrayInputStream wfbais = new ByteArrayInputStream(wfArray);
     // check that the wsdl for each activity in the workflowSpec
     // is discoverable and is readable.  But don't invoke the services.
     DummyRunner dummy = new DummyRunner(wfbais);
     return dummy.run();
     }
     */
    public synchronized String getStatus(String workflowInstanceID) throws Exception {
        //find the workflow
        String ret = null;
        Iterator iterator = registry.iterator();
        boolean found = false;

        while (iterator.hasNext() && !found) {
            WorkflowInstance wfl = (WorkflowInstance) iterator.next();

            if (wfl.getID().equals(workflowInstanceID)) {
                found = true;
                if (wfl.getFlowReceipt() != null) {
                    ret = wfl.getFlowReceipt().getStatusString();
                } else {
                    StringBuffer buf = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

                    buf.append("<Error>Not started yet ;(</Error>");
                    ret = buf.toString();
                }
            }
        }
        if (!found || ret == null) {
            throw new Exception("No status available for workflow instance with ID, " +
                    workflowInstanceID);
        }
        return ret;
    }

	public String getProgressReportXMLString(String workflowInstanceID) throws Exception {
		//find the workflow
        String ret = null;
        Iterator iterator = registry.iterator();
        boolean found = false;

        while (iterator.hasNext() && !found) {
            WorkflowInstance wfl = (WorkflowInstance) iterator.next();
            if (wfl.getID().equals(workflowInstanceID)) {
                found = true;
                if (wfl.getFlowReceipt() != null) 
                    ret = wfl.getFlowReceipt().getProgressReportXMLString();                
            }
        }
        if (!found || ret == null) {
            m_log.error("Unable to find workflow instance with workflow ID: "+ workflowInstanceID);
			//generate best attempt at xml
			StringBuffer buf = new StringBuffer("<workflowReport workflowID=\"");
			buf.append(workflowInstanceID);
			buf.append("\" workflowStatus=\"");
			buf.append("UNKNOWN\"");
			buf.append("><processorList/></workflowReport>");
			ret = buf.toString();
        }
        return ret;
	}

	public synchronized String getErrorMessage(String workflowInstanceID) throws Exception {
		//find the workflow
        String ret = null;
        Iterator iterator = registry.iterator();
        boolean found = false;

        while (iterator.hasNext() && !found) {
            WorkflowInstance wfl = (WorkflowInstance) iterator.next();

            if (wfl.getID().equals(workflowInstanceID)) {
                found = true;
                if (wfl.getFlowReceipt() != null) {
                    ret = wfl.getFlowReceipt().getErrorMessage();
                } else {
                    StringBuffer buf = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

                    buf.append("<Error>Not started yet ;(</Error>");
                    ret = buf.toString();
                }
            }
        }
        if (!found || ret == null) {
            throw new Exception("No error message available for workflow instance with ID, " +
                    workflowInstanceID);
        }
        return ret;	
	}

    public synchronized String getOutput(String workflowInstanceID) throws Exception {
        //find workflow
        //StringBuffer buf = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        StringBuffer buf = new StringBuffer("");
        Iterator iterator = registry.iterator();
        boolean found = false;

        while (iterator.hasNext() && !found) {
            WorkflowInstance wfl = (WorkflowInstance) iterator.next();

            if (wfl.getID().equals(workflowInstanceID)) {
                found = true;

								TavernaFlowReceipt flowReceipt = (TavernaFlowReceipt) wfl.getFlowReceipt();
                if(flowReceipt == null) {
									// what does this mean?
									m_log.warn("Flow receipt for workflow instance with id=" + wfl.getID() + " was NULL");
									return "";
								}
						
								// modification for compatibility with Talisman 1.4
								// It's desirable to return the empty string when the 
								// workflow instance hasn't completed yet
								// so that talisman can block until completion, if it wishes
								if(flowReceipt.getStatusString().equals("COMPLETE")) {
									return flowReceipt.getOutputString();
								}
								else {
									return "";
								}
               
            }
        }
        if (!found) {
            throw new Exception("No status available for workflow instance with ID, " +
                    workflowInstanceID);
        }
        return buf.toString();
    }

	public String getProvenance(String workflowInstanceID) throws Exception {
		String ret = null;
			Iterator iterator = registry.iterator();
			boolean found = false;
			while (iterator.hasNext() && !found) {
				WorkflowInstance wfl = (WorkflowInstance) iterator.next();

				if (wfl.getID().equals(workflowInstanceID)) {
					found = true;

					WSFlowReceipt flowReceipt = wfl.getFlowReceipt();
					if(flowReceipt == null) {
						// what does this mean?
						m_log.warn("Flow receipt for workflow instance with id=" + wfl.getID() + " was NULL");
						return "";
					}
					ret = flowReceipt.getProvenanceXMLString();
				}
			}
			if (!found || ret == null) {
				throw new Exception("No provenance available for workflow instance with ID, " +
						workflowInstanceID);
			}
			return ret;
		
	}

	public synchronized boolean releaseWorkflow(String workflowInstanceID) throws Exception {
		boolean released = false;
		Iterator iterator = registry.iterator();
		while(iterator.hasNext()) {
			WorkflowInstance wf = (WorkflowInstance) iterator.next();
			FlowReceipt rec = wf.getFlowReceipt();
			if(rec.getID().equals(workflowInstanceID)) {
				FlowBroker broker = FlowBrokerFactory.createFlowBroker("uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker");	
				broker.releaseFlow(rec);
				released = true;
			}
			iterator.remove();
		}
		return released;
	}

    public boolean stopWorkflow(String workflowInstanceID) {

       try{
			 //find the workflow in the registry
			 Iterator iterator = registry.iterator();
			 boolean found = false;
			 boolean success = false;
			 while(iterator.hasNext() && !found){
				 WorkflowInstance wfl = (WorkflowInstance) iterator.next();
				 if(wfl.getID().equals(workflowInstanceID)){
					FlowBroker broker = FlowBrokerFactory.createFlowBroker("uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker");
					broker.cancelFlow(workflowInstanceID);
					//if no exception then it will be successful
				}
			}
			return success;
         }
         catch(Exception ex){
			
         }         
        return false;
    }

    /**
     * Process message associated with submitted flow.
     *
     * @param FlowMessage describing the event.
     */
    public void notify(FlowMessage msg) {
        //can notify standalone client about finalised execution
        String flowID = msg.getFlowID();

        notifyListeners(flowID);
        
    }

    public synchronized void addListener(iWorkflowExecutionListener listener) {
        if (listeners == null)
            listeners = new ArrayList();
        listeners.add(listener);
    }

    public void notifyListeners(String id) {
        Iterator iterator = listeners.iterator();

        while (iterator.hasNext()) {
            iWorkflowExecutionListener listener = (iWorkflowExecutionListener) iterator.next();

            if (listener.getWorkflowInstanceID().equals(id))
                listener.notifyWorkflowExecutionFinalised();
        }
    }

    public synchronized boolean isValid(String workflow, StringBuffer errorLog) throws WorkflowValidationException, WorkflowSpecParseException, IOException {
        boolean success = true;
        String nl = System.getProperty("line.separator");
        StringBuffer sb = errorLog;

        ScuflModel model = new ScuflModel();
		try{
			XScuflParser.populate(workflow,model,null);
		}
		catch(Exception ex) {
			sb.append(ex.getMessage());	
			success = true;
		}
		return success;
    }
}

