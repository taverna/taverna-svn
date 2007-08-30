package net.sf.taverna.t2.workflowmodel.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sf.taverna.t2.annotation.impl.AbstractMutableAnnotatedThing;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationTypeMismatchException;

public class DataflowImpl extends AbstractMutableAnnotatedThing implements
		Dataflow {

	private List<ProcessorImpl> processors;
	private String name;
	private static int nameIndex = 0;
	private List<DataflowInputPortImpl> inputs;
	private List<DataflowOutputPortImpl> outputs;

	protected DataflowImpl() {
		this.name = "dataflow" + (nameIndex++);
		this.processors = new ArrayList<ProcessorImpl>();
		this.inputs = new ArrayList<DataflowInputPortImpl>();
		this.outputs = new ArrayList<DataflowOutputPortImpl>();
	}

	public synchronized List<? extends DataflowInputPort> getInputPorts() {
		return Collections.unmodifiableList(inputs);
	}

	/**
	 * For each processor input, merge input and workflow output get the
	 * incoming link and, if non null, add to a list and return the entire list.
	 */
	public synchronized List<? extends Datalink> getLinks() {
		List<Datalink> result = new ArrayList<Datalink>();
		// All processors have a set of input ports each of which has at most
		// one incoming data link
		for (ProcessorImpl p : processors) {
			for (ProcessorInputPort pip : p.getInputPorts()) {
				Datalink dl = pip.getIncomingLink();
				if (dl != null) {
					result.add(dl);
				}
			}
		}
		// Workflow outputs have zero or one incoming data link to their
		// internal input port
		for (DataflowOutputPort dop : getOutputPorts()) {
			Datalink dl = dop.getInternalInputPort().getIncomingLink();
			if (dl != null) {
				result.add(dl);
			}
		}
		// TODO - when Merge is implemented we'll need to handle merge inputs
		// here as well
		return result;
	}

	public synchronized List<? extends Processor> getProcessors() {
		return Collections.unmodifiableList(this.processors);
	}

	public synchronized List<? extends DataflowOutputPort> getOutputPorts() {
		return Collections.unmodifiableList(this.outputs);
	}

	public String getLocalName() {
		return this.name;
	}

	public synchronized DataflowValidationReport checkValidity() {
		// First things first - nullify the resolved depths in all datalinks
		for (Datalink dl : getLinks()) {
			if (dl instanceof DatalinkImpl) {
				DatalinkImpl dli = (DatalinkImpl) dl;
				dli.setResolvedDepth(-1);
			}
		}
		// Now copy type information from workflow inputs
		for (DataflowInputPort dip : getInputPorts()) {
			for (Datalink dl : dip.getInternalOutputPort().getOutgoingLinks()) {
				if (dl instanceof DatalinkImpl) {
					DatalinkImpl dli = (DatalinkImpl) dl;
					dli.setResolvedDepth(dip.getDepth());
				}
			}
		}
		// Now iteratively attempt to resolve everything else.

		// Firstly take a copy of the processor list, we'll processors from this
		// list as they become either failed or resolved
		List<Processor> unresolved = new ArrayList<Processor>(getProcessors());

		// Keep a list of processors that have failed, initially empty
		List<Processor> failed = new ArrayList<Processor>();

		/**
		 * Is the dataflow valid? The flow is valid if and only if both
		 * unresolved and failed lists are empty a the end. This doesn't
		 * guarantee that the workflow will run, in particular it doesn't
		 * actually check for issues such as unresolved output edges.
		 */

		// Flag to indicate whether we've finished yet, set to true if no
		// changes are made in an iteration
		boolean finished = false;

		while (!finished) {
			// We're finished unless something happens later
			finished = true;
			// Keep a list of processors to remove from the unresolved list
			// because they've been resolved properly
			List<Processor> removeValidated = new ArrayList<Processor>();
			// Keep another list of those that have failed
			List<Processor> removeFailed = new ArrayList<Processor>();

			for (Processor p : unresolved) {
				try {
					// true = checked and valid, false = can't check, the
					// exception means the processor was checked but was invalid
					// for some reason
					boolean processorValid = p.doTypeCheck();
					if (processorValid) {
						removeValidated.add(p);
					}
				} catch (IterationTypeMismatchException e) {
					removeFailed.add(p);
				}
			}

			/**
			 * Remove validated and failed items from the pending lists. If
			 * anything was removed because it validated okay then we're not
			 * finished yet and should reset the boolean finished flag
			 */
			for (Processor p : removeValidated) {
				unresolved.remove(p);
				finished = false;
			}
			for (Processor p : removeFailed) {
				unresolved.remove(p);
				failed.add(p);
			}

		}

		// At this point we know whether the processors within the workflow
		// validated. If all the processors validated then we're probably okay,
		// but there are a few other problems to check for. Firstly we need to
		// check whether all the dataflow outputs are connected; any unconnected
		// output is by definition a validation failure.
		List<DataflowOutputPort> unresolvedOutputs = new ArrayList<DataflowOutputPort>();
		for (DataflowOutputPortImpl dopi : outputs) {
			Datalink dl = dopi.getInternalInputPort().getIncomingLink();
			// Unset any type information on the output port, we'll set it again
			// later if there's a suitably populated link going into it
			dopi.setDepths(-1, -1);
			if (dl == null) {
				// not linked, this is by definition an unsatisfied link!
				unresolvedOutputs.add(dopi);
			} else if (dl.getResolvedDepth() == -1) {
				// linked but the edge hasn't had its depth resolved, i.e. it
				// links from an unresolved entity
				unresolvedOutputs.add(dopi);
			} else {
				// linked and edge depth is defined, we can therefore populate
				// the granular and real depth of the dataflow output port. Note
				// that this is the only way these values can be populated, you
				// don't define them when creating the ports as they are
				// entirely based on the type check stage.
				int granularDepth = dl.getSource().getGranularDepth();
				int resolvedDepth = dl.getResolvedDepth();
				dopi.setDepths(resolvedDepth, granularDepth);
			}
		}

		boolean dataflowValid = true;
		if (unresolvedOutputs.isEmpty() == false || failed.isEmpty() == false
				|| unresolved.isEmpty() == false) {
			dataflowValid = false;
		}

		// Build and return a new validation report containing the overal state
		// along with lists of failed and unsatisfied processors and unsatisfied
		// output ports
		return new DataflowValidationReportImpl(dataflowValid, failed,
				unresolved, unresolvedOutputs);
	}

}
