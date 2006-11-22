package org.embl.ebi.escience.scufl.view;

import java.util.List;

public class DotViewSettings {
  
	boolean showBoring;
	private boolean alignment;
	private boolean expandWorkflow;
	private List<String> fillColours;
	private int portDisplay;
	private boolean typeLabelDisplay;
	
	public void setShowBoring(boolean showBoring) {
		this.showBoring = showBoring;
	}
	public boolean getShowBoring() {
		return showBoring;
	}
	
	public void setAlignment(boolean alignment) {
		this.alignment = alignment;
	}
	public boolean getAlignment() {
		return alignment;
	}
	
	public boolean getExpandWorkflow() {
		return expandWorkflow;
	}
	public void setExpandWorkflow(boolean expandWorkflow) {
		this.expandWorkflow = expandWorkflow;
	}
	
	public boolean getTypeLabelDisplay() {
		return typeLabelDisplay;
	}
	public void setTypeLabelDisplay(boolean typeLabelDisplay) {
		this.typeLabelDisplay = typeLabelDisplay;
	}
	
	
	public void setFillColours(List<String> colours) {
		this.fillColours = colours;
	}
	public List<String> getFillColours() {
		return fillColours;
	}
	
	public int getPortDisplay() {
		return portDisplay;
	}
	public void setPortDisplay(int portDisplay) {
		this.portDisplay = portDisplay;
	}

	
	
	
}
