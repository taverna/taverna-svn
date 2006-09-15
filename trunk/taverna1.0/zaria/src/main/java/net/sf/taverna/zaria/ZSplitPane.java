package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JSplitPane;

import org.jdom.Element;

/**
 * ZPane manifesting a split panel design where each
 * sub-panel is itself a ZPane
 * @author Tom Oinn
 */
public class ZSplitPane extends ZPane {

	private JSplitPane splitPane = new JSplitPane();
	
	/**
	 * Set horizontal split orientation
	 */
	private Action setHorizontalOrientationAction = new AbstractAction() {
		public void actionPerformed(ActionEvent arg0) {
			splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			setHorizontalOrientationAction.setEnabled(false);
			setVerticalOrientationAction.setEnabled(true);
		}
	};
	
	/**
	 * Set vertical split orientation
	 */
	private Action setVerticalOrientationAction = new AbstractAction() {
		
		public void actionPerformed(ActionEvent arg0) {
			splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			setHorizontalOrientationAction.setEnabled(true);
			setVerticalOrientationAction.setEnabled(false);
		}
	};
	
	private List<Action> actions = new ArrayList<Action>();
	
	public ZSplitPane() {
		super();
		actions.add(setHorizontalOrientationAction);
		setHorizontalOrientationAction.putValue(Action.NAME,"Horizontal");
		actions.add(setVerticalOrientationAction);
		setVerticalOrientationAction.putValue(Action.NAME,"Vertical");
		actions.add(new ReplaceWithBlankAction());
		// Set up action validity
		setVerticalOrientationAction.actionPerformed(null);
		splitPane.setLeftComponent(new ZBlankComponent());
		splitPane.setRightComponent(new ZBlankComponent());
		add(splitPane, BorderLayout.CENTER);
	}

	public Element getElement() {
		// TODO Auto-generated method stub
		return null;
	}

	public void configure(Element e) {
		// TODO Auto-generated method stub

	}

	/**
	 * Call superclass method to show or hide toolbar
	 * and recursively call on all child elements.
	 */
	public void setEditable(boolean editable) {
		super.setEditable(editable);
		for (ZTreeNode child : getZChildren()) {
			child.setEditable(editable);
		}
	}

	public List<Action> getActions() {
		return this.actions;
	}
	
	private ZTreeNode getLeftComponent() {
		return (ZTreeNode)splitPane.getLeftComponent();
	}
	
	private ZTreeNode getRightComponent() {
		return (ZTreeNode)splitPane.getRightComponent();
	}

	public List<ZTreeNode> getZChildren() {
		List<ZTreeNode> children = new ArrayList<ZTreeNode>();
		children.add(getLeftComponent());
		children.add(getRightComponent());
		return children;
	}

	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent) {
		if (getRightComponent().equals(oldComponent)) {
			// Swap the right component
			splitPane.remove((Component)oldComponent);
			splitPane.setRightComponent((Component)newComponent);
		}
		else if (getLeftComponent().equals(oldComponent)) {
			// Swap the left component
			splitPane.remove((Component)oldComponent);
			splitPane.setLeftComponent((Component)newComponent);
		}
		newComponent.setEditable(this.editable);
		revalidate();
	}

	
	
}
