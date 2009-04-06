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

package net.sf.taverna.t2.provenance.lineageservice.derby;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

import net.sf.taverna.t2.provenance.connector.DerbyProvenanceConnector;
import net.sf.taverna.t2.provenance.lineageservice.EventProcessor;
import net.sf.taverna.t2.provenance.lineageservice.Provenance;
import net.sf.taverna.t2.provenance.lineageservice.types.ProvenanceEventType;
import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Uses Apache Derby to store provenance data
 * 
 * @author Paolo Missier
 * @author Ian Dunlop
 * 
 */
public class DerbyProvenance implements Provenance, SharedVocabulary {
	
	private static Logger logger = Logger.getLogger(DerbyProvenance.class);

	private static final String createDB = "CREATE DATABASE IF NOT EXISTS T2Provenance;";

	private static final String createTableArc = "CREATE TABLE  `T2Provenance`.`Arc` ("
			+ "`sourceVarNameRef` varchar(100) NOT NULL COMMENT 'ref. to var name for source of arc',"
			+ " `sinkVarNameRef` varchar(100) NOT NULL COMMENT 'ref. to var name for sink of arc',"
			+ " `sourcePNameRef` varchar(100) NOT NULL,"
			+ " `sinkPNameRef` varchar(100) NOT NULL,"
			+ " `wfInstanceRef` varchar(100) NOT NULL,"
			+ " PRIMARY KEY  USING BTREE (`sourceVarNameRef`,`sinkVarNameRef`,`sourcePNameRef`,`sinkPNameRef`,`wfInstanceRef`));";

	private static final String createTableCollection = "CREATE TABLE  `T2Provenance`.`Collection` ("
			+ " `collID` varchar(100) NOT NULL COMMENT 'ID of a list (collection). not sure yet what this looks like... ',"
			+ " `parentCollIDRef` varchar(100) NOT NULL default 'TOP' COMMENT 'used for list nesting.\ndefault is dummy list TOP since this attr. is key',"
			+ " `wfInstanceRef` varchar(100) NOT NULL,"
			+ " `PNameRef` varchar(100) NOT NULL,"
			+ " `varNameRef` varchar(100) NOT NULL,"
			+ " `iteration` char(10) NOT NULL default '',"
			+ " PRIMARY KEY  USING BTREE (`collID`,`wfInstanceRef`,`PNameRef`,`varNameRef`,`parentCollIDRef`,`iteration`));";

	private static final String createTableProcBinding = "CREATE TABLE  `T2Provenance`.`ProcBinding` ("
			+ "`pnameRef` varchar(100) NOT NULL COMMENT 'ref to static processor name',"
			+ "`execIDRef` varchar(100) NOT NULL COMMENT 'ref. to ID of wf execution',"
			+ "`actName` varchar(100) NOT NULL COMMENT 'name of activity bound to this processor',"
			+ "`iteration` char(10) NOT NULL default '',"
			+ "PRIMARY KEY  USING BTREE (`pnameRef`,`execIDRef`,`iteration`));";

	private static final String createTableProcessor = "CREATE TABLE  `T2Provenance`.`Processor` ("
			+ " `pname` varchar(100) NOT NULL,"
			+ " `wfInstanceRef` varchar(100) NOT NULL COMMENT 'ref to WfInstance.wfInstanceID',"
			+ " `type` varchar(100) default NULL COMMENT 'processor type',"
			+ " PRIMARY KEY  (`pname`,`wfInstanceRef`));";

	private static final String createTableVar = "CREATE TABLE  `T2Provenance`.`Var` ("
			+ " `varName` varchar(100) NOT NULL,"
			+ " `type` varchar(20) default NULL COMMENT 'variable type',"
			+ " `inputOrOutput` tinyint(1) NOT NULL COMMENT '1 = input, 0 = output',"
			+ " `pnameRef` varchar(100) NOT NULL COMMENT 'reference to the processor',"
			+ " `wfInstanceRef` varchar(100) NOT NULL,"
			+ " `nestingLevel` int(10) unsigned default '0',"
			+ " `actualNestingLevel` int(10) unsigned default '0',"
			+ " `anlSet` tinyint(1) default NULL,"
			+ "PRIMARY KEY  USING BTREE (`varName`,`inputOrOutput`,`pnameRef`,`wfInstanceRef`));";

	private static final String createTableVarBinding = "CREATE TABLE  `T2Provenance`.`VarBinding` ("
			+ " `varNameRef` varchar(100) NOT NULL COMMENT 'ref to var name',"
			+ " `wfInstanceRef` varchar(100) NOT NULL COMMENT 'ref to execution ID',"
			+ " `value` varchar(100) default NULL COMMENT 'ref to value. Either a string value or a string ref (URI) to a value',"
			+ " `collIDRef` varchar(100) NOT NULL default 'TOP',"
			+ " `positionInColl` int(10) unsigned NOT NULL default '1' COMMENT 'position within collection. default is 1',"
			+ " `PNameRef` varchar(100) NOT NULL,"
			+ " `valueType` varchar(50) default NULL,"
			+ " `ref` varchar(100) default NULL,"
			+ " `iteration` char(10) NOT NULL default '',"
			+ "PRIMARY KEY  USING BTREE (`varNameRef`,`wfInstanceRef`,`PNameRef`,`positionInColl`,`iteration`,`collIDRef`),"
			+ " KEY `collectionFK` (`wfInstanceRef`,`PNameRef`,`varNameRef`,`collIDRef`));";

	private static final String createTableWFInstance = "CREATE TABLE  `T2Provenance`.`WfInstance` ("
			+ " `instanceID` varchar(100) NOT NULL COMMENT 'T2-generated ID for one execution',"
			+ " `wfnameRef` varchar(100) NOT NULL COMMENT 'ref to name of the workflow being executed',"
			+ " `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT 'when execution has occurred',"
			+ " PRIMARY KEY  (`instanceID`));";

	private static final String createTableWorkflow = "CREATE TABLE  `T2Provenance`.`Workflow` ("
			+ " `wfname` varchar(100) NOT NULL," + "PRIMARY KEY  (`wfname`));";

	static boolean saveEvent = true;
	static boolean clearDB = false; // facilitates testing

	private DerbyProvenanceWriter pw = null;
	private EventProcessor ep = null;
	private DerbyProvenanceQuery pq;

	private String location;

	private final DerbyProvenanceConnector derbyProvenanceConnector;

	public DerbyProvenance(String location, DerbyProvenanceConnector derbyProvenanceConnector) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		this.location = location;
		this.derbyProvenanceConnector = derbyProvenanceConnector;
		setPw(new DerbyProvenanceWriter(derbyProvenanceConnector));
		setPq(new DerbyProvenanceQuery(derbyProvenanceConnector));
		getPw().setLocation(location);
		getPq().setLocation(location);

		setEp(new EventProcessor(getPw(), getPq())); // singleton

	}

	public String getString() {
		return "Derby Provenance Service";
	}

	/**
	 * not used --
	 * 
	 * @see #acceptRawProvenanceEvent(String, String)
	 * @param eventType
	 * @param content
	 */
	public void acceptProvenanceEvent(ProvenanceEventType eventType,
			String content) {
		; // TODO
	}

	/**
	 * maps each incoming event to an insert query into the provenance store
	 * 
	 * @param eventType
	 * @param content
	 * @throws SQLException
	 * @throws IOException
	 */
	public void acceptRawProvenanceEvent(String eventType, String content)
			throws SQLException, IOException {

		processEvent(content, eventType);

	}

	/**
	 * parse d and generate SQL insert calls into the provenance DB
	 * 
	 * @param d
	 *            DOM for the event
	 * @param eventType
	 *            see {@link SharedVocabulary}
	 * @throws SQLException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void processEvent(String content, String eventType)
			throws SQLException, IOException {

		if (eventType.equals(SharedVocabulary.WORKFLOW_EVENT_TYPE)) {
			// process the workflow structure

			String workflowID = getEp().processWorkflowStructure(content);

			// add propagation of anl code here
			if (workflowID != null)
				getEp().propagateANL(workflowID); // operates on the DB

		} else if (eventType.equals("EOW")) { // SharedVocabulary.END_WORKFLOW_EVENT_TYPE))
												// {
			// use this event to do housekeeping on the input/output varbindings

			getEp().fillInputVarBindings(); // indep. of current event
			getEp().fillOutputVarBindings();

			getEp().patchTopLevelnputs();
			// load up any annotations associated with this workflow TODO

		} else {

			// parse the event into DOM
			SAXBuilder b = new SAXBuilder();
			Document d;

			try {
				d = b.build(new StringReader(content));

				getEp().processProcessEvent(d);

			} catch (JDOMException e) {
				logger.warn("Process event problem: " + e);
			} catch (IOException e) {
				logger.warn("Process event problem: " + e);
			}

		}

	}

	public void clearDB() throws SQLException {
		getPw().clearDBStatic();
		getPw().clearDBDynamic();
	}

	public void setLocation(String location) {
		this.location = location;
		getPw().setLocation(location);
		getPq().setLocation(location);

	}

	public void setPq(DerbyProvenanceQuery pq) {
		this.pq = pq;
	}

	public DerbyProvenanceQuery getPq() {
		return pq;
	}

	public void setPw(DerbyProvenanceWriter pw) {
		this.pw = pw;
	}

	public DerbyProvenanceWriter getPw() {
		return pw;
	}

	public void setEp(EventProcessor ep) {
		this.ep = ep;
	}

	public EventProcessor getEp() {
		return ep;
	}

	public String getSaveEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSaveEvents(String saveEvents) {
		// TODO Auto-generated method stub
		
	}

} // end class
