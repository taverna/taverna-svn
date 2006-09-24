package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdom.Element;

/**
 * ZPane implementation which holds its children within
 * a tabbed pane container. Can allow addition, removal,
 * ordering and renaming of tabs as well as conversion to
 * a split pane view if and only if there are exactly two
 * tabs present.
 * @author Tom Oinn
 */
public class ZTabbedPane extends ZPane {
	
	private JTabbedPane tabs;
	private List<Action> actions = 
		new ArrayList<Action>();
	private Action removeTabAction = new RemoveCurrentTabAction();
	private Action demoteTabAction = new DemoteTabAction();
	private Action promoteTabAction = new PromoteTabAction();
	private JTextField tabName = new JTextField();
	
	public ZTabbedPane() {
		super();
		tabs = new JTabbedPane();
		add(tabs, BorderLayout.CENTER);
		actions.add(new AddTabAction());
		actions.add(removeTabAction);
		removeTabAction.setEnabled(false);
		actions.add(demoteTabAction);
		promoteTabAction.setEnabled(false);
		actions.add(promoteTabAction);
		demoteTabAction.setEnabled(false);
		actions.add(new ReplaceWithBlankAction());
		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				checkValidity();
			}
		});
		tabName.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent arg0) {
				doName();
			}
			public void removeUpdate(DocumentEvent arg0) {
				doName();
			}
			public void changedUpdate(DocumentEvent arg0) {
				doName();
			}
			private void doName() {
				int i = tabs.getSelectedIndex();
				if (i>=0) {
					tabs.setTitleAt(i,tabName.getText());
				}
			}
		});
		checkValidity();
	}
	
	public List<ZTreeNode> getZChildren() {
		List<ZTreeNode> children = new ArrayList<ZTreeNode>();
		for (int i = 0; i < tabs.getComponentCount(); i++) {
			children.add((ZPane)tabs.getComponentAt(i));
		}
		return children;
	}
	
	public Element getElement() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void configure(Element e) {
		// TODO Auto-generated method stub
		
	}
	
	private void checkValidity() {
		int index = tabs.getSelectedIndex();
		if (index >= 0) {
			removeTabAction.setEnabled(true);
			tabName.setEnabled(true);
			if (tabName.getText().equals(tabs.getTitleAt(index))==false) {
				tabName.setText(tabs.getTitleAt(index));
			}
			if (index > 0) {
				demoteTabAction.setEnabled(true);
			}
			else {
				demoteTabAction.setEnabled(false);
			}
			if (index < tabs.getComponentCount() - 1) {
				promoteTabAction.setEnabled(true);
			}
			else {
				promoteTabAction.setEnabled(false);
			}
		}
		else {
			tabName.setEnabled(false);
			removeTabAction.setEnabled(false);
			promoteTabAction.setEnabled(false);
			demoteTabAction.setEnabled(false);
		}
		// Set up state for other tab controls
		// here, will do later when we actually
		// have the other controls...		
	}
	
	private class AddTabAction extends AbstractAction {
		
		public AddTabAction() {
			super();
			putValue(Action.NAME,"Add tab");
		}
		
		public void actionPerformed(ActionEvent arg0) {
			ZBlankComponent c = new ZBlankComponent();
			c.setEditable(editable);
			tabs.add("Tab "+tabs.getComponentCount(), c);
			tabs.setSelectedComponent(c);
			checkValidity();
		}
		
	};
	
	private class RemoveCurrentTabAction extends AbstractAction {
		
		public RemoveCurrentTabAction() {
			super();
			putValue(Action.NAME,"Remove tab");
		}
		
		public void actionPerformed(ActionEvent arg0) {
			int index = tabs.getSelectedIndex();
			tabs.remove(index);
			checkValidity();
		}
		
	}
	
	private class PromoteTabAction extends AbstractAction {
		
		public PromoteTabAction() {
			super();
			putValue(Action.NAME,"Promote");
		}
		
		public void actionPerformed(ActionEvent e) {
			int selectedIndex = tabs.getSelectedIndex();
			swapTabs(selectedIndex);
			tabs.setSelectedIndex(selectedIndex+1);
		}
		
	}
	
	private class DemoteTabAction extends AbstractAction {
		
		public DemoteTabAction() {
			super();
			putValue(Action.NAME,"Demote");
		}
		
		public void actionPerformed(ActionEvent e) {
			int selectedIndex = tabs.getSelectedIndex();
			swapTabs(selectedIndex-1);
			tabs.setSelectedIndex(selectedIndex-1);
		}
		
	}
	
	public List<Action> getActions() {
		return actions;
	}
	
	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent) {
		// Find the tab index, if any, of the old component
		int componentIndex = -1;
		for (int i = 0; i < tabs.getComponentCount() && componentIndex < 0; i++) {
			if (tabs.getComponentAt(i) == oldComponent) {
				componentIndex = i;
			}
		}
		if (componentIndex == -1) {
			// Give up, couldn't find the old component
			return;
		}
		newComponent.setEditable(editable);
		tabs.setComponentAt(componentIndex, (JComponent)newComponent);
	}
	
	/**
	 * Swap the tab at index i with that at index
	 * i+1
	 */
	private void swapTabs(int i) {
		Component c = tabs.getComponentAt(i);
		String text = tabs.getTitleAt(i);
		Icon icon = tabs.getIconAt(i);
		tabs.remove(i);
		tabs.add(c,text,i+1);
		if (icon != null) {
			tabs.setIconAt(i+1,icon);
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
	
	/**
	 * Get the tab name editor
	 */
	public List<JComponent> getToolbarComponents() {
		List<JComponent> components = new ArrayList<JComponent>();
		components.add(tabName);
		return components;
	}
	
}
