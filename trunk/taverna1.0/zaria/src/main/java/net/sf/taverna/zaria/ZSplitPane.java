package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JSplitPane;

import org.jdom.Element;

/**
 * ZPane manifesting a split panel design where each
 * sub-panel is itself a ZPane
 * @author Tom Oinn
 */
public class ZSplitPane extends ZPane {

	private JSplitPane splitPane = new JSplitPane();
	
	private class SwitchOrientationAction extends AbstractAction {
		
		public SwitchOrientationAction() {
			super();
			if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
				putValue(Action.SHORT_DESCRIPTION,"Switch to horizontal split");
				putValue(Action.SMALL_ICON,ZIcons.iconFor("converttohorizontalsplit"));
			}
			else {
				putValue(Action.SHORT_DESCRIPTION,"Switch to vertical split");
				putValue(Action.SMALL_ICON,ZIcons.iconFor("converttoverticalsplit"));
			}
		}

		public void actionPerformed(ActionEvent arg0) {
			if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
				splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
				putValue(Action.SHORT_DESCRIPTION,"Switch to vertical split");
				putValue(Action.SMALL_ICON,ZIcons.iconFor("converttoverticalsplit"));
			}
			else {
				splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
				putValue(Action.SHORT_DESCRIPTION,"Switch to horizontal split");
				putValue(Action.SMALL_ICON,ZIcons.iconFor("converttohorizontalsplit"));
			}
		}
		
	}
	
	private List<Action> actions = new ArrayList<Action>();
	
	public ZSplitPane() {
		super();
		splitPane.setLeftComponent(new ZBlankComponent());
		splitPane.setRightComponent(new ZBlankComponent());
		splitPane.setDividerLocation(0.5d);
		splitPane.setResizeWeight(0.5d);
		actions.add(new SwitchOrientationAction());
		actions.add(new ReplaceWithBlankAction());
		add(splitPane, BorderLayout.CENTER);
	}

	public Element getElement() {
		Element splitElement = new Element("split");
		splitElement.setAttribute("orientation",splitPane.getOrientation()==JSplitPane.HORIZONTAL_SPLIT ? "horizontal" : "vertical");
		Element rightElement = new Element("right");
		splitElement.addContent(rightElement);
		Element leftElement = new Element("left");
		splitElement.addContent(leftElement);
		ZTreeNode rightNode = (ZTreeNode)splitPane.getRightComponent();
		ZTreeNode leftNode = (ZTreeNode)splitPane.getLeftComponent();
		rightElement.addContent(elementFor(rightNode));
		leftElement.addContent(elementFor(leftNode));
		return splitElement;
	}

	public void configure(Element e) {
		Element splitElement = e.getChild("split");
		if (splitElement!=null) {
			String orientation=splitElement.getAttributeValue("orientation");
			if (orientation!=null) {
				splitPane.setOrientation(orientation.equalsIgnoreCase("horizontal") ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT);
			}
			Element leftElement = splitElement.getChild("left");
			Element leftDefinition = leftElement.getChild("znode");
			ZTreeNode leftNode = (ZTreeNode)componentFor(leftDefinition);
			splitPane.setLeftComponent((JComponent)leftNode);
			
			Element rightElement = splitElement.getChild("right");
			Element rightDefinition = rightElement.getChild("znode");
			ZTreeNode rightNode = (ZTreeNode)componentFor(rightDefinition);
			splitPane.setRightComponent((JComponent)rightNode);
			
			leftNode.configure(leftDefinition);
			rightNode.configure(rightDefinition);
		}
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
		// Store the old divider location, we don't want this to change
		int location = splitPane.getDividerLocation();
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
		splitPane.setDividerLocation(location);
		revalidate();
	}

	
	
}
