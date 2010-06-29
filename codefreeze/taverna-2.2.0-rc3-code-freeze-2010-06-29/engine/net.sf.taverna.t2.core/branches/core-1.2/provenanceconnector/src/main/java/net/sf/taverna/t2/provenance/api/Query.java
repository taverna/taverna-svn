/**
 * 
 */
package net.sf.taverna.t2.provenance.api;

import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceProcessor;
import net.sf.taverna.t2.provenance.lineageservice.utils.QueryPort;

/**
 * @author Paolo Missier</br>
 * 
 * Bean encapsulating one provenance query, consisting of the following elements:
 * <ul>
 * <li>static scope: the (single) name of the workflow whose run(s) are queried
 * <li>dynamic scope: a list of workflow run IDs.
 * <li>a list of &lt;select> variables, encoded as List&lt;{@link QueryPort}>
 * <li>a list of &lt;target> processors, encoded as List&lt;{@link ProvenanceProcessor}> 
 * </ul>
 */
public class Query {

	String workflowName;
	List<QueryPort> targetPorts;
	List<String> runIDList;
	List<ProvenanceProcessor> selectedProcessors;


	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append("\n **** QUERY SCOPE: ****\n").
		append("\tworkflow name: ").append(getWorkflowName()).

		append("\n\truns: ");
		for (String r:getRunIDList()) {
			sb.append("\n\t"+r);
		}

		sb.append("\n**** TARGET PORTS: ****\n");
		for (QueryPort v:getTargetPorts()) {
			sb.append("\n\t"+v.toString());
		}

		sb.append("\n\n**** SELECTED PROCESSORS: **** ");
		for (ProvenanceProcessor pp:getSelectedProcessors()) {
			sb.append("\n\t"+pp.toString());
		}

		return sb.toString();
	}

	/**
	 * @return the targetVars
	 */
	public List<QueryPort> getTargetPorts() {
		return targetPorts;
	}
	/**
	 * @param targetVars the targetVars to set
	 */
	public void setTargetPorts(List<QueryPort> targetVars) {
		this.targetPorts = targetVars;
	}
	/**
	 * @return the selectedProcessors
	 */
	public List<ProvenanceProcessor>  getSelectedProcessors() {
		return selectedProcessors;
	}
	/**
	 * @param selectedProcessors the selectedProcessors to set
	 */
	public void setSelectedProcessors(List<ProvenanceProcessor>  selectedProcessors) {
		this.selectedProcessors = selectedProcessors;
	}
	/**
	 * @return the runIDList
	 */
	public List<String> getRunIDList() {
		return runIDList;
	}
	/**
	 * @param runIDList the runIDList to set
	 */
	public void setRunIDList(List<String> runIDList) {
		this.runIDList = runIDList;
	}

	/**
	 * @return the workflowName
	 */
	public String getWorkflowName() {
		return workflowName;
	}

	/**
	 * @param workflowName the workflowName to set
	 */
	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}
}
