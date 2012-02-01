package uk.org.taverna.scufl2.api.profiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.AbstractNamed;
import uk.org.taverna.scufl2.api.common.Child;
import uk.org.taverna.scufl2.api.common.Visitor;
import uk.org.taverna.scufl2.api.common.WorkflowBean;
import uk.org.taverna.scufl2.api.core.Processor;

/**
 * A <code>ProcessorBinding</code> specifies that when enacting a
 * {@link uk.org.taverna.scufl2.api.core.Workflow Workflow}, if this particular <code>ProcessorBinding</code> is
 * used, then the boundActivity will be used to implement the boundProcessor.
 * <p>
 * The <code>ProcessorBinding</code> specifies the sets of input and output port bindings for the ports of the
 * {@link Processor}. Note that there may not need to be a binding for every <code>Processor</code> port, nor for every
 * {@link Activity} port. However, the ports must be of the bound <code>Processor</code> and <code>Activity</code>.
 * <p>
 * It has not been decided if the binding must be unique for a given <code>Processor</code> or <code>Activity</code> port
 * within a <code>ProcessorBinding</code>.
 * 
 * @author Alan R Williams
 */
public class ProcessorBinding extends AbstractNamed implements Child<Profile> {

	private Processor boundProcessor;
	private Activity boundActivity;

	private Set<ProcessorInputPortBinding> inputPortBindings = new HashSet<ProcessorInputPortBinding>();
	private Set<ProcessorOutputPortBinding> outputPortBindings = new HashSet<ProcessorOutputPortBinding>();

	private Integer activityPosition;
	private Profile parent;

	@Override
	public boolean accept(Visitor visitor) {
		if (visitor.visitEnter(this)) {
			List<Iterable<? extends WorkflowBean>> children = new ArrayList<Iterable<? extends WorkflowBean>>();
			if (getInputPortBindings() != null) {
				children.add(getInputPortBindings());
			}
			if (getOutputPortBindings() != null) {
				children.add(getOutputPortBindings());				
			}
			outer: for (Iterable<? extends WorkflowBean> it : children) {
				for (WorkflowBean bean : it) {
					if (!bean.accept(visitor)) {
						break outer;
					}
				}
			}
		}
		return visitor.visitLeave(this);
	}

	/**
	 * Returns the relative position of the bound <code>Activity</code> within the <code>Processor</code> (for the purpose of
	 * Failover).
	 * <p>
	 * <code>Activity</code>s will be ordered by this position. Gaps will be ignored, overlapping <code>Activity</code>
	 * positions will have an undetermined order.
	 * 
	 * @return the relative position of the bound <code>Activity</code> within the <code>Processor</code>
	 */
	public final Integer getActivityPosition() {
		return activityPosition;
	}

	/**
	 * Returns the <code>Activity</code> that will be used to enact the <code>Processor</code> if this ProcessorBinding is
	 * used.
	 * 
	 * @return the <code>Activity</code> that will be used to enact the <code>Processor</code>
	 */
	public Activity getBoundActivity() {
		return boundActivity;
	}

	/**
	 * Returns the <code>Processor</code> for which a possible means of enactment is specified.
	 * 
	 * @return the <code>Processor</code> for which a possible means of enactment is specified
	 */
	public Processor getBoundProcessor() {
		return boundProcessor;
	}

	/**
	 * Returns the bindings for individual input ports of the bound <code>Processor</code>.
	 * 
	 * @return the bindings for individual input ports of the bound <code>Processor</code>
	 */
	public Set<ProcessorInputPortBinding> getInputPortBindings() {
		return inputPortBindings;
	}

	/**
	 * Returns the bindings for individual output ports of the bound <code>Processor</code>.
	 * 
	 * @return the bindings for individual output ports of the bound <code>Processor</code>
	 */
	public Set<ProcessorOutputPortBinding> getOutputPortBindings() {
		return outputPortBindings;
	}

	@Override
	public Profile getParent() {
		return parent;
	}

	/**
	 * Sets the relative position of the bound <code>Activity</code> within the processor (for the purpose of
	 * Failover).
	 * <p>
	 * <code>Activity</code>s will be ordered by this position. Gaps will be ignored, overlapping <code>Activity</code>
	 * positions will have an undetermined order.
	 * 
	 * @param activityPosition the relative position of the bound <code>Activity</code> within the <code>Processor</code>
	 */
	public void setActivityPosition(Integer activityPosition) {
		this.activityPosition = activityPosition;
	}

	/**
	 * Sets the Activity that will be used to enact the <code>Processor</code> if this ProcessorBinding is
	 * used.
	 * 
	 * @param boundActivity the Activity that will be used to enact the <code>Processor</code>
	 */
	public void setBoundActivity(Activity boundActivity) {
		this.boundActivity = boundActivity;
	}

	/**
	 * Sets the <code>Processor</code> for which a possible means of enactment is specified.
	 * 
	 * @param boundProcessor the <code>Processor</code> for which a possible means of enactment is specified
	 */
	public void setBoundProcessor(Processor boundProcessor) {
		this.boundProcessor = boundProcessor;
	}

	/**
	 * Sets the bindings for individual input ports of the bound <code>Processor</code>.
	 * 
	 * @param inputPortBindings the bindings for individual input ports of the bound <code>Processor</code>
	 */
	public void setInputPortBindings(Set<ProcessorInputPortBinding> inputPortBindings) {
		this.inputPortBindings = inputPortBindings;
	}

	/**
	 * Sets the bindings for individual output ports of the bound <code>Processor</code>.
	 * 
	 * @param outputPortBindings the bindings for individual output ports of the bound <code>Processor</code>
	 */
	public void setOutputPortBindings(Set<ProcessorOutputPortBinding> outputPortBindings) {
		this.outputPortBindings = outputPortBindings;
	}

	@Override
	public void setParent(Profile parent) {
		if (this.parent != null && this.parent != parent) {
			this.parent.getProcessorBindings().remove(this);
		}
		this.parent = parent;
		if (parent != null) {
			parent.getProcessorBindings().add(this);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + getBoundProcessor() + " " + getBoundActivity();
	}

}
