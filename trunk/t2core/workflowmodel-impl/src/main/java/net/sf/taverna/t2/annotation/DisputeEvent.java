package net.sf.taverna.t2.annotation;

public class DisputeEvent implements CurationEvent<DisputeEventDetails>{
	
	private DisputeEventDetails disputeEventDetails;
	private CurationEventType curationEventType;
	private Curateable targetEvent;
	
	public DisputeEvent() {
		
	}
	
	
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


	public void setDisputeEventDetails(DisputeEventDetails disputeEventDetails) {
//		if (disputeEventDetails != null) {
//			throw new RuntimeException("Dispute event details have already been set");
//		}
		this.disputeEventDetails = disputeEventDetails;
	}


	public void setCurationEventType(CurationEventType curationEventType) {
//		if (curationEventType != null) {
//			throw new RuntimeException("Curation event details have already been set");
//		}
		this.curationEventType = curationEventType;
	}


	public void setTargetEvent(Curateable targetEvent) {
//		if (targetEvent!= null) {
//			throw new RuntimeException("Target event details have already been set");
//		}
		this.targetEvent = targetEvent;
	}

}
