package net.sf.taverna.t2.annotation;

public class DisputeEvent implements CurationEvent<DisputeEventDetails>{
	
	private DisputeEventDetails disputeEventDetails;
	private CurationEventType curationEventType;
	private Curateable targetEvent;

	public DisputeEvent(DisputeEventDetails disputeEventDetails, CurationEventType curationEventType, Curateable targetEvent) {
		this.disputeEventDetails = disputeEventDetails;
		this.curationEventType = curationEventType;
		this.targetEvent = targetEvent;
	}

	public DisputeEventDetails getDetail() {
		return disputeEventDetails;
	}

	public Curateable getTarget() {
		return targetEvent;
	}

	public CurationEventType getType() {
		return curationEventType;
	}

}
