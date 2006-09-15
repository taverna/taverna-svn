package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;

import org.jdom.Element;

/**
 * The blank component used when there isn't anything else,
 * contains actions to create the other components (which will
 * therefore be created empty by default).
 * @author Tom Oinn
 */
public class ZBlankComponent extends ZPane implements ZTreeNode {

	private List<Action> actions;
	
	public ZBlankComponent() {
		super();
		actions = new ArrayList<Action>();
		actions.add(createSplitPaneAction);
		createSplitPaneAction.putValue(Action.NAME,"Split");
		add(new JPanel(), BorderLayout.CENTER);
	}

	private Action createSplitPaneAction = new AbstractAction() {
		public void actionPerformed(ActionEvent arg0) {
			replaceWith(new ZSplitPane());
		}
	};
	
	public List<ZTreeNode> getZChildren() {
		return new ArrayList<ZTreeNode>();
	}

	public Element getElement() {
		// TODO Auto-generated method stub
		return null;
	}

	public void configure(Element e) {
		// TODO Auto-generated method stub
	}

	public List<Action> getActions() {
		return actions;
	}

	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent) {
		// Do nothing, this has no children
	}

}
