////////////////////////////////////////////////////////////////////////////////
//
// � University of Southampton IT Innovation Centre, 2002
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
//      Created Date        :   2003/4/9
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: dmarvin $
//                              $Date: 2003-04-18 20:41:40 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;

import org.apache.axis.client.*;

import org.apache.log4j.*;
import javax.xml.namespace.QName;

import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Input;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Output;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Task;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;

import org.embl.ebi.escience.scufl.*;

public class SoaplabTask extends ProcessorTask{
	private static Logger logger = Logger.getLogger(SoaplabTask.class);
	private static final int INVOCATION_TIMEOUT = 0;

	public SoaplabTask(String id,Processor proc) {
		super(id,proc);
	}
	
	public uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage doTask() {
		try{
			//grab the input map
			Map inputMap = new HashMap();
			GraphNode[] inputs = getParents();
			
			for(int i=0;i<inputs.length;i++) {
				PortTask pT = (PortTask) inputs[i];				
				//for now going to wait for all data inputs to be available, this must change for the special requirements for taverna.
				//actually want to set data in jobs as it becomes available, so don't block, check if data available every so often
				Part p = pT.getData();			
				Element e = (Element) p.getValue();
				inputMap.put(p.getName(),e.getFirstChild().getNodeValue());
					
			}
			// Invoke the web service...
			Call call = (Call) new Service().createCall();
			call.setTargetEndpointAddress(((SoaplabProcessor) proc).getEndpoint());
			call.setOperationName(new QName("waitFor"));
			call.setReturnType(new QName("apachesoap:Map"));
			HashMap outputMap = new HashMap((Map)call.invoke(new Object[] { inputMap }));
			//could also get some log info from service for the provenance using the describe method on the service

		
			//convert map content to array to speed things up a bit
			Set set = outputMap.keySet();
			String[] keys = new String[set.size()];
			Object[] values = new Object[set.size()];
			Iterator iterator = set.iterator();
			int k = 0;
			while(iterator.hasNext()) {
				keys[k] = (String) iterator.next();
				values[k] = outputMap.get(keys[k]);				
				k++;
			}
			
			GraphNode[] outputs = getChildren();
			boolean foundAllOutput = true;
			for(int i=0;i<outputs.length;i++) {				
				boolean foundOutputItem = false;
				//look for portTests with the correct portname
				PortTask pT = (PortTask) outputs[i];
				String pTName = pT.getScuflPort().getName();
				for(int j=0;j<keys.length;j++) {					
					if(pTName.equals(keys[j])) {
						String type = null;
						if(values[j] instanceof Boolean)
							type = "boolean";
						else if(values[j] instanceof String)
							type = "string";
						else if(values[j] instanceof Float)
							type = "float";
						else if(values[j] instanceof Integer)
							type = "int";
						else if(values[j] instanceof java.math.BigInteger)
							type = "integer";
						else if(values[j] instanceof Double)
							type = "double";
						else if(values[j] instanceof byte[])
							type = "byte[]";
						else if(values[j] instanceof String[])
							type = "string[]";
						else if(values[j] instanceof byte[][])
							type = "byte[][]";
						else if(values[j] instanceof Element)
							type = "org.w3c.dom.Element";
						else
							return new TaskStateMessage(getParentFlow().getID(),getID(),TaskStateMessage.FAILED,"Task failed since could not handle return type");
						pT.setData(new Part(-1,keys[j],type,values[j]));
						
						foundOutputItem = true;
					}					
				}
				if(!foundOutputItem)
					foundAllOutput = false;
			}
			if(!foundAllOutput) {
				return new TaskStateMessage(getParentFlow().getID(),getID(),TaskStateMessage.FAILED,"Task failed since could not obtain all required output from soaplab service");
			}
			
			//success
			return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.COMPLETE, "Task finished successfully");
		}
		catch(Exception ex) {
			logger.error("Error invoking soaplab service for task " +getID() ,ex);
			return new TaskStateMessage(getParentFlow().getID(),getID(),TaskStateMessage.FAILED,"Task " + getID() + " failed due to problem invoking soaplab service");
		}
	}

	public void cleanUpConcreteTask() {
		//nothing at mo, but should call destroy on job if job id available
	}
}