package net.sf.taverna.t2.workflowmodel.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;

public class MergeImpl implements Merge {

	private List<MergeInputPortImpl> inputs = new ArrayList<MergeInputPortImpl>();

	private String name;

	private BasicEventForwardingOutputPort output;

	public MergeImpl(String mergeName) {
		super();
		this.name = mergeName;
		this.output = new MergeOutputPortImpl(this, "merged", 0, 0);
	}

	public String getLocalName() {
		return this.name;
	}

	/**
	 * Adds a new input port to the internal list of ports.
	 * 
	 * @param inputPort
	 *            the MergeInputPortImpl
	 */
	public void addInputPort(MergeInputPortImpl inputPort) {
		inputs.add(inputPort);
	}

	/**
	 * Removes an input port from the internal list of ports.
	 * 
	 * @param inputPort
	 */
	public void removeInputPort(MergeInputPortImpl inputPort) {
		inputs.remove(inputPort);
	}

	public List<? extends MergeInputPort> getInputPorts() {
		return inputs;
	}

	public EventForwardingOutputPort getOutputPort() {
		return this.output;
	}

	/**
	 * Return the index of the port with the specified name, or -1 if the port
	 * can't be found (this is a bad thing!)
	 * 
	 * @param portName
	 * @return
	 */
	private int inputPortNameToIndex(String portName) {
		int i = 0;
		for (InputPort ip : inputs) {
			if (ip.getName().equals(portName)) {
				return i;
			}
			i++;
		}
		return -1; // FIXME: as the javadoc states, this is a bad thing!
	}

	protected void receiveEvent(WorkflowDataToken token, String portName) {
		int portIndex = inputPortNameToIndex(portName);
		if (portIndex == -1) {
			throw new WorkflowStructureException(
					"Received event on a port that doesn't exist, huh?");
		}
		int[] currentIndex = token.getIndex();
		int[] newIndex = new int[currentIndex.length + 1];
		newIndex[0] = portIndex;
		for (int i = 0; i < currentIndex.length; i++) {
			newIndex[i + 1] = currentIndex[i];
		}
		output.sendEvent(new WorkflowDataToken(token.getOwningProcess(),
				newIndex, token.getData()));
	}

	/**
	 * There is only ever a single output from a merge node but the token
	 * processing entity interface defines a list, in this case it always
	 * contains exactly one item.
	 */
	public List<? extends EventForwardingOutputPort> getOutputPorts() {
		List<EventForwardingOutputPort> result = new ArrayList<EventForwardingOutputPort>();
		result.add(output);
		return result;
	}

}
