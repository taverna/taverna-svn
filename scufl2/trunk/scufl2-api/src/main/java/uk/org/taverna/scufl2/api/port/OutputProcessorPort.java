package uk.org.taverna.scufl2.api.port;

import uk.org.taverna.scufl2.api.core.Processor;


/**
 * An <code>OutputProcessorPort</code> is a <Port> that outputs data from a {@link Processor}.
 */
public class OutputProcessorPort extends AbstractGranularDepthPort implements
SenderPort, ProcessorPort, OutputPort, GranularDepthPort {



	private Processor parent;

	/**
	 * Constructs an <code>OutputProcessorPort</code> with a random UUID as the name.
	 */
	public OutputProcessorPort() {
		super();
	}

	/**
	 * Constructs an <code>OutputProcessorPort</code> for the specified <code>Processor</code> with the
	 * specified name.
	 * <p>
	 * The <code>OutputPort</code> is added to the <code>Processor</code> (if the
	 * <code>Processor</code> is not <code>null</code>).
	 * 
	 * @param parent
	 *            the <code>Processor</code> to add this <code>Port</code> to. Can be
	 *            <code>null</code>
	 * @param name
	 *            the name of the <code>Port</code>. <strong>Must not</strong> be <code>null</code>
	 *            or an empty String.
	 */
	public OutputProcessorPort(Processor parent, String name) {
		super(name);
		setParent(parent);
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.scufl2.api.common.Child#getParent()
	 */
	@Override
	public Processor getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.scufl2.api.common.Child#setParent(uk.org.taverna.scufl2.api.common.WorkflowBean)
	 */
	@Override
	public void setParent(Processor parent) {
		if (this.parent != null && this.parent != parent) {
			this.parent.getOutputPorts().remove(this);
		}
		this.parent = parent;
		if (parent != null) {
			parent.getOutputPorts().add(this);
		}
	}

	@Override
	public String toString() {
		return getParent().getName() + "." + getName();
	}
}
