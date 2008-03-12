package net.sf.taverna.t2.provenance;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.jdom.Element;

public class ProcessorProvenanceItem implements ProvenanceItem {

	private List<? extends Activity<?>> activities;
	private ActivityProvenanceItem activityProvenanceItem;
	private String processorID;
	
	public ProcessorProvenanceItem(List<? extends Activity<?>> activities,String processorID) {
		super();
		this.activities = activities;
		this.processorID = processorID;
	}


	public Element getAsXML(DataFacade dataFacade) {
		Element result = new Element("processor");
		result.setAttribute("id",processorID);
		Element activitiesElement = new Element("potentialActivites");
		result.addContent(activitiesElement);
		
		if (activities!=null) {
			for (Activity<?> a : activities) {
			activitiesElement.addContent(new ActivityProvenanceItem(a).getAsXML(dataFacade));
			}
		}
		if (activityProvenanceItem!=null) {
			Element activityUsed = new Element("activityUsed");
			activityUsed.addContent(activityProvenanceItem.getAsXML(dataFacade));
			result.addContent(activityUsed);
		}
		return result;
	}


	public void setActivityProvenanceItem(
			ActivityProvenanceItem activityProvenanceItem) {
		this.activityProvenanceItem = activityProvenanceItem;
	}

}
