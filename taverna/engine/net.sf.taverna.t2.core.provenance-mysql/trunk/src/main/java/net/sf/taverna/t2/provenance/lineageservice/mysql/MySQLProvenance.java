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
package net.sf.taverna.t2.provenance.lineageservice.mysql;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

import net.sf.taverna.t2.provenance.lineageservice.EventProcessor;
import net.sf.taverna.t2.provenance.lineageservice.Provenance;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * implements the WS interface -- only exposed method is {@link #acceptRawProvenanceEvent(String, String)}
 * @author paolo
 *
 */
public class MySQLProvenance implements Provenance, SharedVocabulary
{

//	static boolean saveEvent = true;
	static String saveEvents = null;

	static Logger logger = Logger.getLogger(MySQLProvenance.class);

	private ProvenanceWriter     pw = null;
	private ProvenanceQuery      pq = null;
	private EventProcessor       ep = null;
	private String location;
	

	public MySQLProvenance(String location, boolean isClearDB) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		this.location = location;
		setPw(new MySQLProvenanceWriter(this.location));  // singleton
//		setPw(new DummyProvenanceWriter());  // singleton
		setPq(new MySQLProvenanceQuery(this.location));   // singleton
		
		setEp(new EventProcessor(getPw(),getPq())); // singleton	
		
		
		// clear the DB prior to collecting new provenance  
		if (isClearDB) {
			System.out.println("clearing DB");
			pw.clearDBStatic();
			pw.clearDBDynamic();
		} else {
			System.out.println("clearDB is FALSE: not clearing");
		}
		
	}

	public String getString() { return "Taverna provenance service"; }

	
	/**
	 * maps each incoming event to an insert query into the provenance store
	 * @param eventType
	 * @param content
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public void acceptRawProvenanceEvent(String eventType, String content) throws SQLException, IOException {

//		System.out.println("received raw event of type " + eventType);
		//System.out.println("content \n"+content);

		processEvent(content, eventType, null);
	}

	

	public void acceptRawProvenanceEvent(String eventType, String content,
			Object context) throws SQLException, IOException {

		processEvent(content, eventType, context);
		
	}


	/**
	 * parse d and generate SQL insert calls into the provenance DB
	 * @param d DOM for the event
	 * @param eventType see {@link SharedVocabulary}
	 * @throws SQLException 
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	private void processEvent(String content, String eventType, Object context) throws SQLException, IOException {

//		saveEvent for debugging / testing
		if (saveEvents != null && saveEvents.equals("all")) {

			getEp().saveEvent(content, eventType);
//			System.out.println("event saved");
			
		} else if (saveEvents != null && saveEvents.equals("iteration")) {
			if (eventType.equals("iteration"))
				
				getEp().saveEvent(content, eventType);
//				System.out.println("event saved");
				
		}
		
		if (eventType.equals(SharedVocabulary.WORKFLOW_EVENT_TYPE)) {
			// process the workflow structure
						
			System.out.println("Provenance: ************  processing event of type "+SharedVocabulary.WORKFLOW_EVENT_TYPE);
			String workflowID = getEp().processWorkflowStructure(content);
			
			// add propagation of anl code here
			if (workflowID != null) getEp().propagateANL(workflowID);   // operates on the DB
			
		}  else  {
			
//			parse the event into DOM
			SAXBuilder  b = new SAXBuilder();
			Document d;

			try {
				d = b.build (new StringReader(content));
			
			getEp().processProcessEvent(d, context);
			
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

	}


	
	public void clearDB() throws SQLException {
		((MySQLProvenanceWriter)pw).clearDBStatic();
		((MySQLProvenanceWriter)pw).clearDBDynamic();
	}


	public void createDB() throws SQLException {
		// TODO Auto-generated method stub

	}


	public void setLocation(String location) {
		this.location = location;		
	}

	public void setPw(ProvenanceWriter pw) {
		this.pw = pw;
	}

	public ProvenanceWriter getPw() {
		return pw;
	}

	public void setPq(ProvenanceQuery pq) {
		this.pq = pq;
	}

	public ProvenanceQuery getPq() {
		return pq;
	}

	public void setEp(EventProcessor ep) {
		this.ep = ep;
	}

	public EventProcessor getEp() {
		return ep;
	}

	/**
	 * @return the saveEvents
	 */
	public  String getSaveEvents() {
		return saveEvents;
	}

	/**
	 * @param saveEvents the saveEvents to set
	 */
	public void setSaveEvents(String saveEvents) {
		MySQLProvenance.saveEvents = saveEvents;
	}



}  // end class
