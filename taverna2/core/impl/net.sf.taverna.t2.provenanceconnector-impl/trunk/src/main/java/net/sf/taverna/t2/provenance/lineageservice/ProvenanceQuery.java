package net.sf.taverna.t2.provenance.lineageservice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.provenance.lineageservice.utils.Arc;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProcBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceProcessor;
import net.sf.taverna.t2.provenance.lineageservice.utils.Var;
import net.sf.taverna.t2.provenance.lineageservice.utils.VarBinding;

import org.jdom.Document;

public interface ProvenanceQuery {

	/**
	 * select Var records that satisfy constraints
	 */
	public abstract List<Var> getVars(Map<String, String> queryConstraints)
			throws SQLException;

	/**
	 * return the input variables for a given processor and a wfInstanceId
	 * @param pname
	 * @param wfInstanceId
	 * @return list of input variables
	 * @throws SQLException 
	 */
	public abstract List<Var> getInputVars(String pname, String wfInstanceId)
			throws SQLException;

	/**
	 * return the output variables for a given processor and a wfInstanceId
	 * @param pname
	 * @param wfInstanceId
	 * @return list of output variables
	 * @throws SQLException 
	 */
	public abstract List<Var> getOutputVars(String pname, String wfInstanceId)
			throws SQLException;

	/**
	 * selects all Arcs
	 * @param queryConstraints
	 * @return
	 * @throws SQLException
	 */
	public abstract List<Arc> getArcs(Map<String, String> queryConstraints)
			throws SQLException;

	/**
	 * all WF instances, in reverse chronological order 
	 * @return
	 * @throws SQLException
	 */
	public abstract List<String> getWFInstanceIDs() throws SQLException;

	/**
	 * all ProCBinding records that satisfy the input constraints
	 * @param constraints
	 * @return
	 * @throws SQLException
	 */
	public abstract List<ProcBinding> getProcBindings(
			Map<String, String> constraints) throws SQLException;

	/**
	 * 
	 * @param constraints a Map columnName -> value that defines the query constraints. Note: columnName must be fully qualified.
	 * This is not done well at the moment, i.e., PNameRef should be VarBinding.PNameRef to avoid ambiguities
	 * @return
	 * @throws SQLException
	 */
	public abstract List<VarBinding> getVarBindings(
			Map<String, String> constraints) throws SQLException;

	/**
	 * used in the toposort phase -- propagation of anl() values through the graph
	 * @return
	 * @throws SQLException
	 */
	public abstract Map<String, Integer> getProcessorsIncomingLinks(
			String wfInstanceRef) throws SQLException;

	public abstract List<Var> getSuccVars(String pName, String vName,
			String wfInstanceRef) throws SQLException;

	public abstract List<String> getSuccProcessors(String pName,
			String wfInstanceRef) throws SQLException;

	public abstract List<ProvenanceProcessor> getProcessors(
			Map<String, String> constraints) throws SQLException;

	/**
	 * simplest possible pinpoint query. Uses iteration info straight away
	 * @param wfInstance
	 * @param pname
	 * @param vname
	 * @param iteration
	 * @return
	 */
	public abstract LineageSQLQuery simpleLineageQuery(String wfInstance,
			String pname, String vname, String iteration);

	/**
	 * if var2Path is null this generates a trivial query for the current output var and current path 
	 * @param proc
	 * @param var2Path
	 * @param path
	 * @return
	 */
	public abstract LineageSQLQuery lineageQueryGen(String wfInstance,
			String proc, Map<Var, String> var2Path, Var outputVar, String path);

	public abstract LineageQueryResult runLineageQuery(LineageSQLQuery lq)
			throws SQLException;

	public abstract List<LineageQueryResult> runLineageQueries(
			List<LineageSQLQuery> lqList) throws SQLException;

	/**
	 * takes an ordered set of records for the same variable with iteration indexes and builds a collection out of it 
	 * @param lqr
	 * @return a jdom Document with the collection
	 */
	public abstract Document recordsToCollection(LineageQueryResult lqr);

	/**
	 * persists var v back to DB 
	 * @param v
	 * @throws SQLException 
	 */
	public abstract void updateVar(Var v) throws SQLException;

	public abstract void setConnection(Connection openConnection);

}