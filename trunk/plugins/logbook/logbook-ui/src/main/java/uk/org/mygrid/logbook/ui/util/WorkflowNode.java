package uk.org.mygrid.logbook.ui.util;

import java.util.Date;

import javax.swing.ImageIcon;

public interface WorkflowNode extends Comparable<WorkflowNode> {
	
	public abstract String getLsid();

	public abstract String getLsidSuffix();

	public abstract String getDisplayDate();

	public abstract String getAuthor();

	public abstract String getDescription();

	public abstract Date getDate();

	public abstract ImageIcon getIcon();
	
	public abstract void setIcon(ImageIcon icon);
}