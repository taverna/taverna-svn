package net.sf.taverna.t2.invocation;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * A single data token passed between processors in a workflow. This is distinct
 * from the Job in that it contains a single (unnamed) data reference whereas
 * the Job holds a map of arbitrarily many named data references in a bundle.
 * 
 * @author Tom Oinn
 * 
 */
public class WorkflowDataToken extends Event<WorkflowDataToken> {

	private EntityIdentifier dataRef;
	
	/**
	 * Construct a new data token with the specified owning process, conceptual
	 * index array and data reference
	 * 
	 * @param owningProcess
	 * @param index
	 * @param dataRef
	 */
	public WorkflowDataToken(String owningProcess, int[] index, EntityIdentifier dataRef, InvocationContext context) {
		super(owningProcess, index, context);
		this.dataRef = dataRef;
	}

	@Override
	public WorkflowDataToken popOwningProcess()
			throws ProcessIdentifierException {
		return new WorkflowDataToken(popOwner(), index, dataRef, context);
	}

	@Override
	public WorkflowDataToken pushOwningProcess(String localProcessName)
			throws ProcessIdentifierException {
		return new WorkflowDataToken(pushOwner(localProcessName), index, dataRef, context);
	}

	
	/**
	 * Return the ID of the data this event represents
	 * 
	 * @return
	 */
	public EntityIdentifier getData() {
		return this.dataRef;
	}
	
	/**
	 * Show the owner, index array and data map in textual form for debugging
	 * and any other purpose. Jobs appear in the form :
	 * 
	 * <pre>
	 * Job(Process1)[2,0]{Input2=dataID4,Input1=dataID3}
	 * </pre>
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Token(" + owner + ")[");
		for (int i = 0; i < index.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(index[i] + "");
		}
		sb.append("]{");
		sb.append(dataRef.toString());
		sb.append("}");
		return sb.toString();
	}

	
}
