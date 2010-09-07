package uk.org.taverna.scufl2.api.core;


import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import uk.org.taverna.scufl2.api.common.Child;
import uk.org.taverna.scufl2.api.common.WorkflowBean;
import uk.org.taverna.scufl2.api.port.ReceiverPort;
import uk.org.taverna.scufl2.api.port.SenderPort;
import uk.org.taverna.scufl2.api.reference.Reference;


/**
 * @author Alan R Williams
 *
 */
@XmlRootElement
@XmlType(propOrder = {"senderPortReference", "receiverPortReference"})
public class DataLink implements WorkflowBean, Child<Workflow> {

	private ReceiverPort receiverPort;
	
	private SenderPort senderPort;
	
	private Workflow parent;
	
	@XmlTransient
	public Workflow getParent() {
		return parent;
	}

	public void setParent(Workflow parent) {
		if (this.parent != null && this.parent != parent) {
			this.parent.getDatalinks().remove(this);
		}
		this.parent = parent;
		if (parent != null) {
			parent.getDatalinks().add(this);
		}
	}

	/**
	 * @param senderPort
	 * @param receiverPort
	 */
	public DataLink(SenderPort senderPort, ReceiverPort receiverPort) {
		this.senderPort = senderPort;
		this.receiverPort = receiverPort;
	}
	
	public DataLink() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((receiverPort == null) ? 0 : receiverPort.hashCode());
		result = prime * result
				+ ((senderPort == null) ? 0 : senderPort.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataLink other = (DataLink) obj;
		if (receiverPort == null) {
			if (other.receiverPort != null)
				return false;
		} else if (!receiverPort.equals(other.receiverPort))
			return false;
		if (senderPort == null) {
			if (other.senderPort != null)
				return false;
		} else if (!senderPort.equals(other.senderPort))
			return false;
		return true;
	}


	/**
	 * @param senderPort
	 */
	public void setSenderPort(SenderPort senderPort) {
		this.senderPort = senderPort;
	}

	/**
	 * @param receiverPort
	 */
	public void setReceiverPort(ReceiverPort receiverPort) {
		this.receiverPort = receiverPort;
	}

	public Reference<ReceiverPort> getReceiverPortReference() {
		return Reference.createReference(receiverPort);
	}

	public void setReceiverPortReference(Reference<ReceiverPort> receiverPortReference) {
		receiverPort = receiverPortReference.resolve();
	}
	
	public Reference<SenderPort> getSenderPortReference() {
		return Reference.createReference(senderPort);
	}

	public void setSenderPortReference(Reference<SenderPort> senderPortReference) {
		senderPort = senderPortReference.resolve();
	}
	
	/**
	 * @return
	 */
	@XmlTransient
	public ReceiverPort getReceiverPort() {
		return receiverPort;
	}

	/**
	 * @return
	 */
	@XmlTransient
	public SenderPort getSenderPort() {
		return senderPort;
	}
	
	@Override
	public String toString() {
		return getSenderPort() + "=>" + getReceiverPort();
	}

}
