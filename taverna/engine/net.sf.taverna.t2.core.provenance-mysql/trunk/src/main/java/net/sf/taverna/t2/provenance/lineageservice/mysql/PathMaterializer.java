/**
 * 
 */
package net.sf.taverna.t2.provenance.lineageservice.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceProcessor;
import net.sf.taverna.t2.provenance.lineageservice.utils.Port;

/**
 * given a graph structure in the DB, generates all pairs (p1,p2) such that there is a path from processor p1 to processor p2
 * in the graph, and stores each pair in the provenance DB.
 * @author paolo
 *
 */
public class PathMaterializer {

	private ProvenanceWriter     pw = null;
	private ProvenanceQuery      pq = null;
	private String location;

	public PathMaterializer(String location) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		pq = new MySQLProvenanceQuery();
		pw = new MySQLProvenanceWriter();
		
		setLocation(location);
	}


	/**
	 * 
	 * @throws SQLException
	 */
	public Map<String, Set<String>> materializePathPairs(String dataflowRef) throws SQLException {

		// propagate through 1 level of processors, ignore nesting. A subworkflow here is
		// simply a processor
		Map<String, Set<String>> canBeReachedFrom = materializePathPairsWithinSubflow(dataflowRef);

		// now fetch all children workflows and recurse
		List<String> children = pq.getChildrenOfWorkflow(dataflowRef);

		for (String childWFName: children) {			
			Map<String, Set<String>> canBeReachedFrom1 = materializePathPairs(childWFName);
			
			// merge maps: assumes unique processor IDs??
			canBeReachedFrom.entrySet().addAll(canBeReachedFrom1.entrySet());
		}
		
		return canBeReachedFrom;
	}


	/**
	 * @param dataflowRef the static wfNameRef of the dataflow whose processors we need to sort 
	 * @throws SQLException
	 * @return a list of processors that are immediately contained within wfInstanceRef. This is used by caller to recurse on 
	 * sub-workflows
	 */
	public Map<String, Set<String>> materializePathPairsWithinSubflow(String dataflowRef) throws SQLException {

		 
		// (p,p') in canBeReachedFrom iff p' can be reached from p 
		Map<String, Set<String>> canBeReachedFrom = new HashMap<String, Set<String>>();

		List<String> Q = new ArrayList<String>();

		// fetch processors along with the count of their predecessors
		Map<String, Integer> processorsLinks = pq.getProcessorsIncomingLinks(dataflowRef);

		// fetch processors that are dataflows -- these will be excluded from the loop
		List<ProvenanceProcessor> dataflows = 
			pq.getProcessorsShallow("net.sf.taverna.t2.activities.dataflow.DataflowActivity", dataflowRef);
		List<String> dataflowNames = new ArrayList<String>();
		
		for (ProvenanceProcessor proc:dataflows) { dataflowNames.add(proc.getPname()); } 
			
		// initialize queue with roots of graph
		for (Map.Entry<String,Integer> entry: processorsLinks.entrySet()) {
			if (entry.getValue().intValue()==0)  Q.add(entry.getKey());

			// init reacheability with empty sets
			Set<String>  reachableSet = new HashSet<String>();
			canBeReachedFrom.put(entry.getKey(), reachableSet);
		}

		Set<String> visited = new HashSet<String>();
		
		while (!Q.isEmpty()) {

			String p = Q.remove(0);
			visited.add(p);

			// collect successors to p

			// skip dataflowName -- this is not a valid successor
			if (dataflowNames.contains(p))  continue;
			
			List<String> successors = pq.getSuccProcessors(p, dataflowRef, null);  // null wfInstanceID: CHECK

			for (String p1: successors) {

				Set<String>  reachableSet = canBeReachedFrom.get(p1);  // set of p' that p can reach

				reachableSet.add(p);  // p1 can reach p

				// p1 can also reach all nodes that p can reach
				reachableSet.addAll(canBeReachedFrom.get(p));

				if (!visited.contains(p1) && !Q.contains(p1)) Q.add(p1);
			}
		}
		return canBeReachedFrom;
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

	public void setLocation(String location) {
		this.location = location;		
	}
}
