package net.sf.taverna.t2.provenance.lineageservice;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.sf.taverna.t2.provenance.lineageservice.utils.ProcBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.Var;
import net.sf.taverna.t2.provenance.lineageservice.utils.VarBinding;

public interface ProvenanceWriter {


	public abstract Connection openConnection() 
	  throws InstantiationException, IllegalAccessException, ClassNotFoundException;

		/**
	 * add each Var as a row into the VAR DB table<br/>
	 * <strong>note: no static var type available as part of the dataflow...</strong>
	 * @param vars
	 * @param wfId
	 * @throws SQLException 
	 */
	public abstract void addVariables(List<Var> vars, String wfId)
			throws SQLException;

	/**
	 * inserts one row into the ARC DB table  -- OBSOLETE, see instead
	 * @param sourceVar
	 * @param sinkVar
	 * @param wfId
	 */
	public abstract void addArc(Var sourceVar, Var sinkVar, String wfId) throws SQLException;

	
	public abstract void addArc(String sourceVarName, String sourceProcName,
			String sinkVarName, String sinkProcName, String wfId)
			throws SQLException;

	public abstract void addWFId(String wfId) throws SQLException;

	public abstract void addWFInstanceId(String wfId, String wfInstanceId)
			throws SQLException;

	/**
	 * insert new processor into the provenance DB
	 * @param name
	 * @throws SQLException 
	 */
	public abstract void addProcessor(String name, String wfID)
			throws SQLException;

	public abstract void addProcessorBinding(ProcBinding pb)
			throws SQLException;

	public abstract String addCollection(String processorId, String collId,
			String parentCollectionId, String iteration, String portName,
			String dataflowId) throws SQLException;

	public abstract void addVarBinding(VarBinding vb) throws SQLException;

	/**
	 * deletes entire DB contents -- for testing purposes 
	 * @throws SQLException 
	 */
	public abstract void clearDB() throws SQLException;

}