package uk.org.mygrid.logbook.ui.util;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.TavernaIcons;

public class Label {

	
	public Label(String URI){
		
		this.URI = URI;
		
	}
	
	boolean personal;
	
	String URI;
	
	String creator;
	
	Object [] workflowRuns;
	
	public String toString(){
		
		
		return URI;
		
	}
	
	public ImageIcon getIcon(){
		
		return TavernaIcons.runIcon;
	
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public boolean isPersonal() {
		return personal;
	}

	public void setPersonal(boolean personal) {
		this.personal = personal;
	}

	public Object[] getWorkflowRuns() {
		return workflowRuns;
	}

	public void setWorkflowRuns(Object[] workflowRuns) {
		this.workflowRuns = workflowRuns;
	}
	
	
}
