package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.jdom.Element;

/**
 * A base ZPane implementation, this is always the root
 * of the ZTreeNode heirarchy (or should be for sane uses).
 * We need an additional layer here as the swap component
 * method relies on having a parent, without the extra 'invisible'
 * parent here we couldn't swap out the user visible top
 * level UI component.
 * @author Tom Oinn
 */
public class ZBasePane extends ZPane {

	private ZTreeNode child = null;
	
	public ZBasePane() {
		super();
		child = new ZBlankComponent();
		add((Component)child, BorderLayout.CENTER);
	}

	public List<ZTreeNode> getZChildren() {
		List<ZTreeNode> children = new ArrayList<ZTreeNode>();
		children.add(child);
		return children;
	}

	public Element getElement() {
		// TODO Auto-generated method stub
		return null;
	}

	public void configure(Element e) {
		// TODO Auto-generated method stub
	}

	public List<Action> getActions() {
		return new ArrayList<Action>();
	}

	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent) {
		if (oldComponent == child) {
			remove((Component)child);
			child = newComponent;
			add((Component) newComponent, BorderLayout.CENTER);
			newComponent.setEditable(this.editable);
			revalidate();
		}
	}
	
	public void setEditable(boolean b) {
		super.setEditable(b);
		child.setEditable(b);
	}

}
