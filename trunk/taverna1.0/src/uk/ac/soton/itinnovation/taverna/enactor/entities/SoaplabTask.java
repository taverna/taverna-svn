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
//      Created Date        :   2003/4/9
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: dmarvin $
//                              $Date: 2003-06-09 16:49:00 $
//                              $Revision: 1.16 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import javax.xml.namespace.QName;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.SoaplabProcessor;
import org.w3c.dom.Element;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Input;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Output;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;

// Utility Imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// Network Imports
import java.net.URL;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import java.lang.Boolean;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Object;
import java.lang.String;



public class SoaplabTask extends ProcessorTask{
	private static Logger logger = Logger.getLogger(SoaplabTask.class);
	private static final int INVOCATION_TIMEOUT = 0;
	private String soaplabWSDL = null;
	private Input inputForLog = null;
	private Output outputForLog = null;
	private String report = null;
	private String detailedStatus = null;

	public SoaplabTask(String id,Processor proc,LogLevel l, String userID, String userCtx) {
		super(id,proc,l,userID,userCtx);		
	}
	
	protected java.util.Map execute(java.util.Map inputMap) throws TaskExecutionException {
		try{
			HashMap outMap = null;
						
			//grab the input map
			//Map inputMap = new HashMap();
			//GraphNode[] inputs = getParents();
			if(logLevel.getLevel()>=LogLevel.HIGH)
				inputForLog = new Input();
			
			Map soaplabInputMap = new HashMap();
			for (Iterator i = inputMap.keySet().iterator(); i.hasNext() ; ){
					Part p = (Part)(inputMap.get(i.next()));
					soaplabInputMap.put(p.getName(), p.getTypedValue());
			}
				
			
			// Invoke the web service...
			Call call = (Call) new Service().createCall();
			URL soaplabWSDLURL = ((SoaplabProcessor) proc).getEndpoint();
			soaplabWSDL = soaplabWSDLURL.toExternalForm();
			call.setTargetEndpointAddress(soaplabWSDLURL);
			call.setOperationName(new QName("runAndWaitFor"));
			//call.setReturnType(new QName("apachesoap:Map"));
			HashMap outputMap = new HashMap((Map)call.invoke(new Object[] { soaplabInputMap }));
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
				if(keys[k].equals("report")) {
					if(logLevel.getLevel()>=LogLevel.NORMAL) 							
						report = (String) values[k];
				}
				if(keys[k].equals("detailed_status")) {
					if(logLevel.getLevel()>=LogLevel.HIGH) 
						detailedStatus = (String) values[k];
				}
				k++;
			}
			
			outputForLog = new Output();

			//ideally would want to check the correctness of the output within an iteration step
			//have to convert the types of the output map into part types
			outMap = new HashMap(keys.length);
			for(int j=0;j<keys.length;j++) {					
						
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
								throw new TaskExecutionException("Task failed since could not handle return type");
							Part part = new Part(-1,keys[j],type,values[j]);
							outMap.put(keys[j],part);
																			
					}
			
			
			//success
			return outMap;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			logger.error("Error invoking soaplab service for task " +getID() ,ex);
			throw new TaskExecutionException("Task " + getID() + " failed due to problem invoking soaplab service");			
		}
	}

	public void cleanUpConcreteTask() {
		//nothing at mo, but should call destroy on job if job id available
		inputForLog = null;
		outputForLog = null;
		report = null;
		detailedStatus = null;
	}

	/**
	 * Retrieve provenance information for this task, concrete tasks should
	 * overide this method and provide this information as an XML JDOM element
	 */
	public org.jdom.Element getProvenance() {
		org.jdom.Element e = new org.jdom.Element("SOAPLabInvocation",PROVENANCE_NAMESPACE);
		if(logLevel.getLevel()>=LogLevel.LOW) {
			org.jdom.Element status = new org.jdom.Element("status",PROVENANCE_NAMESPACE);
			status.addContent(new org.jdom.Text(getStateString()));
			e.addContent(status);
			//add start and end time
			if(startTime!=null) {
				org.jdom.Element sT = new org.jdom.Element("startTime",PROVENANCE_NAMESPACE);
				sT.addContent(new org.jdom.Text(startTime.getString()));
				e.addContent(sT);
			}
			if(endTime!=null) {
				org.jdom.Element eT = new org.jdom.Element("endTime",PROVENANCE_NAMESPACE);
				eT.addContent(new org.jdom.Text(endTime.getString()));
				e.addContent(eT);
			}						
		}

		if(logLevel.getLevel()>=LogLevel.NORMAL) {			
			//add the wsdl service invoked
			if(soaplabWSDL!=null) {
				org.jdom.Element uri = new org.jdom.Element("WSDLURI",PROVENANCE_NAMESPACE);
				uri.addContent(new org.jdom.Text(soaplabWSDL));
				e.addContent(uri);
			}
			if(report!=null) {
				org.jdom.Element rep = new org.jdom.Element("soaplabReport",PROVENANCE_NAMESPACE);
				rep.addContent(new org.jdom.CDATA(report));
				e.addContent(rep);
			}			
		}

		if(logLevel.getLevel()>=LogLevel.HIGH) {
			//add the input and output data
			//required retrieving of it
			if(detailedStatus!=null) {
				org.jdom.Element stat = new org.jdom.Element("soaplabDetailedStatus",PROVENANCE_NAMESPACE);
				stat.addContent(new org.jdom.CDATA(detailedStatus));
				e.addContent(stat);
			}
			if(inputForLog!=null)
				e.addContent(inputForLog.toXMLElement());
			if(outputForLog!=null)
				e.addContent(outputForLog.toXMLElement());
		}		
		return e;
	}
}
