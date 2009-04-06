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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.sf.taverna.t2.provenance.connector.DerbyProvenanceConnector;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProcBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.Var;
import net.sf.taverna.t2.provenance.lineageservice.utils.VarBinding;

import org.apache.log4j.Logger;

/**
 * Uses your database of choice to write workflow provenance
 * 
 * @author Paolo Missier
 * @author Ian Dunlop
 * 
 */
public class DerbyProvenanceWriter implements ProvenanceWriter {

	private static Logger logger = Logger
			.getLogger(DerbyProvenanceWriter.class);

	private String location;
	static boolean useDB = true;

	private final DerbyProvenanceConnector derbyProvenanceConnector;

	/**
	 * Part of the Derby DB based provenance storage system for Taverna. Works with the
	 * {@link DerbyProvenanceQuery} to store and retrieve the events that happen
	 * during a workflow run
	 * @param derbyProvenanceConnector 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public DerbyProvenanceWriter(DerbyProvenanceConnector derbyProvenanceConnector) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
				this.derbyProvenanceConnector = derbyProvenanceConnector;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * add each Var as a row into the VAR DB table<br/>
	 * <strong>note: no static var type available as part of the
	 * dataflow...</strong>
	 * 
	 * @param vars
	 * @param wfId
	 * @throws SQLException
	 */
	public void addVariables(List<Var> vars, String wfId) throws SQLException {

		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		String q;
		for (Var v : vars) {

			int isInput = v.isInput() ? 1 : 0;

			q = "INSERT INTO Var (varname, pNameRef, inputOrOutput, nestingLevel, wfInstanceRef) VALUES(\'"
					+ v.getVName()
					+ "\',\'"
					+ v.getPName()
					+ "\',"
					+ isInput
					+ ","
					+ (v.getTypeNestingLevel() >= 0 ? v.getTypeNestingLevel()
							: 0) + ",\'" + wfId + "\')";

			try {

				stmt.executeUpdate(q);

			} catch (Exception e) {
				logger.info("Ignore this insert and continue: " + e);
				// ignore this insert and continue
				continue;
			}

		}
	}

	/**
	 * inserts one row into the ARC DB table -- OBSOLETE, see instead
	 * 
	 * @param sourceVar
	 * @param sinkVar
	 * @param wfId
	 */
	public void addArc(Var sourceVar, Var sinkVar, String wfId)
			throws SQLException {

		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		String q = "INSERT INTO wfInstanceRef (wfInstanceRef, sourcePNameRef, SourceVarNameRef, sinkPNameRef,sinkVarNameRef) VALUES(\'"
				+ wfId
				+ "\',\'"
				+ sourceVar.getPName()
				+ "\',\'"
				+ sourceVar.getVName()
				+ "\',\'"
				+ sinkVar.getPName()
				+ "\',\'"
				+ sinkVar.getVName() + "\')";

		stmt.executeUpdate(q);
	}

	public void addArc(String sourceVarName, String sourceProcName,
			String sinkVarName, String sinkProcName, String wfId)
			throws SQLException {

		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		String q = "INSERT INTO Arc (wfInstanceRef, sourcePNameRef, sourceVarNameRef, sinkPNameRef, sinkVarNameRef) VALUES(\'"
				+ wfId
				+ "\',\'"
				+ sourceProcName
				+ "\',\'"
				+ sourceVarName
				+ "\',\'" + sinkProcName + "\',\'" + sinkVarName + "\')";

		stmt.executeUpdate(q);

	}

	public void addWFId(String wfId) throws SQLException {

		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		String q = "INSERT INTO Workflow (wfName) VALUES (\'" + wfId + "\')";

		stmt.executeUpdate(q);
	}

	public void addWFId(String wfId, String parentWFname) throws SQLException {
		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		String q = "INSERT INTO Workflow (wfname, parentWFname) VALUES (\'"
				+ wfId + "\'," + "\'" + parentWFname + "\')";
		stmt.executeUpdate(q);

	}

	public void addWFInstanceId(String wfId, String wfInstanceId)
			throws SQLException {
		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		String q = "INSERT INTO WfInstance (instanceID, wfnameRef) VALUES (\'"
				+ wfInstanceId + "\'" + ", \'" + wfId + "\')";

		stmt.executeUpdate(q);

	}

	/**
	 * insert new processor into the provenance DB
	 * 
	 * @param name
	 * @throws SQLException
	 */
	public void addProcessor(String name, String wfID) throws SQLException {

		addProcessor(name, null, wfID);

	}

	public void addProcessor(String name, String type, String wfNameRef)
			throws SQLException {
		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		String q = "INSERT INTO Processor (pname, type, wfInstanceRef) VALUES (\'"
				+ name + "\'," + "\'" + type + "\'," + "\'" + wfNameRef + "\')";

		stmt.executeUpdate(q);

	}

	public void addProcessorBinding(ProcBinding pb) throws SQLException {

		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		String q = "INSERT INTO ProcBinding (pnameRef, execIDRef, iteration, actName) VALUES(\'"
				+ pb.getPNameRef()
				+ "\',\'"
				+ pb.getExecIDRef()
				+ "\',\'"
				+ pb.getIterationVector() + "\',\'" + pb.getActName() + "\')";

		stmt.executeUpdate(q);

	}

	public String addCollection(String processorId, String collId,
			String parentCollectionId, String iteration, String portName,
			String dataflowId) throws SQLException {

		String newParentCollectionId = null;

		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		if (parentCollectionId == null) {
			// this is a top-level list
			parentCollectionId = "TOP";
		}

		newParentCollectionId = collId;

		String q = "INSERT INTO Collection (PNameRef, wfInstanceRef, varNameRef, iteration, parentCollIdRef, collId) VALUES(\'"
				+ processorId
				+ "\',\'"
				+ dataflowId
				+ "\',\'"
				+ portName
				+ "\',\'"
				+ iteration
				+ "\',\'"
				+ parentCollectionId
				+ "\',\'"
				+ collId + "\')";

		stmt.executeUpdate(q);

		return newParentCollectionId;

	}

	public void addVarBinding(VarBinding vb) throws SQLException {

		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		String q = "INSERT INTO VarBinding (pnameRef, wfInstanceRef, varNameRef, valueType, value, ref, collIdRef, iteration,positionInColl) VALUES(\'"
				+ vb.getPNameRef()
				+ "\',\'"
				+ vb.getWfInstanceRef()
				+ "\',\'"
				+ vb.getVarNameRef()
				+ "\',\'"
				+ vb.getValueType()
				+ "\',\'"
				+ vb.getValue()
				+ "\',\'"
				+ vb.getRef()
				+ "\',\'"
				+ vb.getCollIDRef()
				+ "\',\'"
				+ vb.getIteration()
				+ "\',"
				+ vb.getPositionInColl() + ")";

		stmt.executeUpdate(q);

	}

	/**
	 *Where should the database be placed, not always necessary depending on
	 * the db solution you are using
	 * 
	 * @return
	 */
	public String getLocation() {
		return location;
	}

	public void clearDBDynamic() throws SQLException {
		String q = null;

		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		q = "DELETE FROM WfInstance";
		stmt.executeUpdate(q);

		q = "DELETE FROM ProcBinding";
		stmt.executeUpdate(q);

		q = "DELETE FROM VarBinding";
		stmt.executeUpdate(q);

		q = "DELETE FROM Collection";
		stmt.executeUpdate(q);

		logger.info(" **** DB cleared DYNAMIC ****");

	}

	public void clearDBStatic() throws SQLException {
		String q = null;

		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		q = "DELETE FROM Workflow";
		stmt.executeUpdate(q);

		q = "DELETE FROM Processor";
		stmt.executeUpdate(q);

		q = "DELETE FROM Arc";
		stmt.executeUpdate(q);

		q = "DELETE FROM Var";
		stmt.executeUpdate(q);

		logger.info(" **** DB cleared STATIC ****");

	}

	public void clearDBStatic(String wfID) throws SQLException {
		String q = null;

		Statement stmt = derbyProvenanceConnector.getConnection().createStatement();

		q = "DELETE FROM Workflow WHERE wfname = \'" + wfID + "\'";
		stmt.executeUpdate(q);

		q = "DELETE FROM Processor WHERE wfInstanceRef = \'" + wfID + "\'";
		stmt.executeUpdate(q);

		q = "DELETE FROM Arc WHERE wfInstanceRef = \'" + wfID + "\'";
		stmt.executeUpdate(q);

		q = "DELETE FROM Var WHERE wfInstanceRef = \'" + wfID + "\'";
		stmt.executeUpdate(q);

		logger.info(" **** DB cleared STATIC for wfID " + wfID + " ****");

	}

	public Connection openConnection() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public void addVarBinding(VarBinding vb, Object context)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
