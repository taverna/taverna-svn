package net.sf.taverna.t2.cloudone.gui.entity.view;

public interface Editable {
	public boolean isEditable();

	public void setEditable(boolean editable) throws EditableException;

	public Editable getEditableParent();

}
