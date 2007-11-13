package net.sf.taverna.t2.cloudone.gui.entity.view;

import org.apache.log4j.Logger;

public class EditableImpl implements Editable {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EditableImpl.class);
	
	public EditableImpl(EditableImpl parent) {
		this.parent = parent;
	}
	
	private EditableImpl parent;
	
	boolean editable;

	private Editable editableChild;

	public EditableImpl getEditableParent() {
		return parent;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) throws EditableException {
		if (editable == this.editable) {
			return;
		}
		if (editable) {
			setEditable();
		} else {
			setNotEditable();
		}
	}

	private void setEditable() throws EditableException {
		EditableImpl editableParent = getEditableParent();
		editableParent.setEditable(true);
		if (editableParent.getEditableChild() != this) {
			editableParent.setEditableChild(this);
		}
		editable = true;
	}

	private void setNotEditable() {
		editable = false;
	}

	protected Editable getEditableChild() {
		return editableChild;
	}

	protected void setEditableChild(Editable child) throws EditableException {
		if (editableChild != null) {
			editableChild.setEditable(false);
		}
		editableChild = child;
	}
	
	
}
