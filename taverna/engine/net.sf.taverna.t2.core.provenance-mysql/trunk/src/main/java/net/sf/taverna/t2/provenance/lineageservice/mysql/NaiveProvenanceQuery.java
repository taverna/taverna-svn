/**
 * 
 */
package net.sf.taverna.t2.provenance.lineageservice.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author paolo
 *
 */
public class NaiveProvenanceQuery {

	MySQLProvenanceQuery pq = null;

	public NaiveProvenanceQuery(String location) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		pq = new MySQLProvenanceQuery(location);  // manages connections
	}


	/**
	 * answers a lineage quey by performing a traversal of the nodes in path. This involves executing one query for each 
	 * DD record to be retrieved 
	 * @param targetVar
	 * @param targetProc
	 * @param targetIteration
	 * @param path
	 * @throws SQLException 
	 */
	public List<DDRecord> recursiveNaiveQuery(String targetVar, String targetProc, String targetIteration, List<String> path) throws SQLException  {

		String pTo = targetProc;
		String vTo = targetVar;
		String iteration = targetIteration;

		// fetch latest WFInstance ID, to use as part of the key
		List<String> IDs = pq.getWFInstanceIDs();
		
		String wfInstanceID = IDs.get(0);
		
		List<DDRecord> recordsQueue = new ArrayList<DDRecord>();

		// execute initial xform step
		List<DDRecord> DDrecords = pq.queryDD(pTo, vTo, null, iteration, wfInstanceID);

		recordsQueue.addAll(DDrecords);

		List<DDRecord> newRecords = DDrecords;

		while (!recordsQueue.isEmpty())  {

			DDRecord r = 	recordsQueue.remove(0);

			if (r.getPFrom().equals(r.getPTo())) {  // we just did a xform step

				newRecords = DDrecords;

				DDrecords.clear();
				DDrecords = pq.queryDD(r.getPFrom(), r.getVFrom(), r.getValFrom(), null, wfInstanceID);
				recordsQueue.addAll(DDrecords);

			}  else {  // we did an xfer

				if (path.contains(r.getPFrom())) {
					
					newRecords = DDrecords;

					DDrecords.clear();
					DDrecords = pq.queryDD(r.getPFrom(), r.getVFrom(), r.getValFrom(), null, wfInstanceID);
					recordsQueue.addAll(DDrecords);

				}
			}
		}
		return newRecords;
	}


	/**
	 * computeLineage using the naive approach -- based on table DD +Arcs
	 * DD is populated from the events log using DataDependenciesBuilder
	 * @throws SQLException 
	 */
	public void computeLineageNaive(
			String wfInstance,   // context
			String proc,   // qualified with its processor name
			String var,   // target var
			String value, // which value -- paths are not used in the naive approach
			boolean isInput  // true if this is an input value to a processor
	) throws SQLException  
	{

		List<DDRecord> stack = new ArrayList<DDRecord>();

		DDRecord start = new DDRecord();
		start.setPTo(proc);
		start.setVTo(var);
		start.setValTo(value);
		start.setInput(isInput);

		stack.add(start);

		while (!stack.isEmpty()) {

			DDRecord current = stack.remove(0);

			System.out.println("processing record: "+current.toString()+" isInput: "+current.isInput());

			if (current.isInput)  {
				// perform a xfer step, using FROM vars

				// query 2 -- corresponds to a xfer step
				Set<DDRecord> xferResults = pq.queryArcsForDD(
						current.getPTo(), current.getVFrom(), current.getValFrom(), wfInstance);

				if (xferResults != null) {			

					for (DDRecord r1:xferResults) {					
						System.out.println("xfer result: "+r1.toString());						
						r1.setInput(false);
					}
					stack.addAll(xferResults);
				}

			} else {
				// perform a xform step

				// query 1 on DD: by value -- corresponds to a xform step usng TO vars
				List<DDRecord> xformResults  = pq.queryDD(current.getPTo(), current.getVTo(), current.getValTo(), null, wfInstance);

				for (DDRecord r:xformResults) {
					System.out.println("xform result: "+r.toString());
					r.setInput(true);
				}
				stack.addAll(xformResults);
			}
		}
	}


	
	/**
	 * gnerates a multi-join query that encodes an entire path traversal over DD 
	 * @param path
	 * @return an SQL query that computes provenance along a path as a single multi-join query
	 */
	public String generateNaiveQueryTwoTables(String targetVar, String targetProc, String targetIteration, List<String> path) {

		StringBuffer selectClause = new StringBuffer();
		StringBuffer fromClause = new StringBuffer();
		StringBuffer whereClause = new StringBuffer();
		StringBuffer joinClause = new StringBuffer();

		String straughtJoinClause = " STRAIGHT_JOIN ";
		
		int tableCounter = 1;

		String currentTableVar = "D"+tableCounter;

		fromClause.append("FROM xformD "+currentTableVar);

		// assume that targetVar is an output variable
		whereClause.append("WHERE "+currentTableVar+".p='"+targetProc+"' and "+currentTableVar+".vTo='"+targetVar+"'");
		if (targetIteration != null) {
			whereClause.append(" and "+currentTableVar+".iteration='"+targetIteration+"' ");
		}

		String nextTableVar = null;

		path.remove(0);  // path excludes the initial processor node  CHECK

		for (String node:path) { 

			tableCounter++;
			nextTableVar = "D"+tableCounter;

			// xfer
			joinClause.append("\nJOIN xferD "+nextTableVar+" ON "+currentTableVar+".p= "+nextTableVar+".pTo"+
					" AND "+currentTableVar+".vFrom = "+nextTableVar+".vTo"+
					" AND "+currentTableVar+".valFrom = "+nextTableVar+".valTo");

			currentTableVar = nextTableVar;

			whereClause.append("\nand "+nextTableVar+".pFrom = '"+node+"' ");

			tableCounter++;
			nextTableVar = "D"+tableCounter;

			// xform
			joinClause.append("\nJOIN xformD "+nextTableVar+" ON "+currentTableVar+".pFrom = "+nextTableVar+".p"+
					" AND "+currentTableVar+".vFrom = "+nextTableVar+".vTo"+
					" AND "+currentTableVar+".valFrom = "+nextTableVar+".valTo");

			currentTableVar = nextTableVar;

		}

		if (nextTableVar == null) nextTableVar = currentTableVar;

		selectClause.append(straughtJoinClause+nextTableVar+".*");

		String q = "SELECT "+selectClause+"\n "+fromClause+" "+joinClause+"\n "+whereClause+" ";

		return q;
	}



	/**
	 * gnerates a multi-join query that encodes an entire path traversal over DD 
	 * @param path
	 * @return an SQL query that computes provenance along a path as a single multi-join query
	 */
	public String generateNaiveQuery(String targetVar, String targetProc, String targetIteration, List<String> path) {

		StringBuffer selectClause = new StringBuffer();
		StringBuffer fromClause = new StringBuffer();
		StringBuffer whereClause = new StringBuffer();
		StringBuffer joinClause = new StringBuffer();

		String straightJoinClause = " STRAIGHT_JOIN ";

		int tableCounter = 1;

		String currentTableVar = "D"+tableCounter;

		fromClause.append("FROM DD "+currentTableVar);

		// assume that targetVar is an output variable
		whereClause.append("WHERE "+currentTableVar+".pTo='"+targetProc+"' and "+currentTableVar+".vTo='"+targetVar+"'");
		if (targetIteration != null) {
			whereClause.append(" and "+currentTableVar+".iteration='"+targetIteration+"' ");
		}

		String nextTableVar = null;

		path.remove(0);  // path excludes the initial processor node  CHECK

		for (String node:path) { 

			tableCounter++;
			nextTableVar = "D"+tableCounter;

			// xfer
			joinClause.append("\nJOIN DD "+nextTableVar+" ON "+currentTableVar+".pFrom = "+nextTableVar+".pTo"+
					" AND "+currentTableVar+".vFrom = "+nextTableVar+".vTo"+
					" AND "+currentTableVar+".valFrom = "+nextTableVar+".valTo");

			currentTableVar = nextTableVar;

			whereClause.append("\nand "+nextTableVar+".pFrom = '"+node+"' ");

			tableCounter++;
			nextTableVar = "D"+tableCounter;

			// xform
			joinClause.append("\nJOIN DD "+nextTableVar+" ON "+currentTableVar+".pFrom = "+nextTableVar+".pTo"+
					" AND "+currentTableVar+".vFrom = "+nextTableVar+".vTo"+
					" AND "+currentTableVar+".valFrom = "+nextTableVar+".valTo");

			currentTableVar = nextTableVar;

		}

		if (nextTableVar == null) nextTableVar = currentTableVar;

		selectClause.append(straightJoinClause+nextTableVar+".*");

		String q = "SELECT "+selectClause+"\n "+fromClause+" "+joinClause+"\n "+whereClause+" ";

		return q;
	}


	/**
	 * @return the pq
	 */
	public MySQLProvenanceQuery getPq() {
		return pq;
	}


	/**
	 * @param pq the pq to set
	 */
	public void setPq(MySQLProvenanceQuery pq) {
		this.pq = pq;
	}

}
