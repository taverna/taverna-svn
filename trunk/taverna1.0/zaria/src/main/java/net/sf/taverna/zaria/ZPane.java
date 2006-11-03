package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JToolBar;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;

import org.jdom.Element;

/**
 * Abstract superclass of all Zaria node components,
 * extends JComponent and adds basic tree traversal
 * functionality.
 * @author Tom Oinn
 */
public abstract class ZPane extends JComponent implements ZTreeNode {

	protected boolean editable = false;
	protected JToolBar toolBar = new JToolBar();
	
	protected ZPane() {
		super();
		toolBar.setFloatable(false);
		toolBar.setRollover(false);
		toolBar.setBorderPainted(false);
		setLayout(new BorderLayout());
	}
	
	@SuppressWarnings("serial")
	protected class ReplaceWithBlankAction extends AbstractAction {
		public ReplaceWithBlankAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION,"Clear");
			putValue(Action.SMALL_ICON,ZIcons.iconFor("delete"));
		}
		public void actionPerformed(ActionEvent arg0) {
			replaceWith(new ZBlankComponent());
		}
	}
	
	/**
	 * Traverse up the component heirarchy until we find an
	 * instance of ZBasePane
	 */
	public ZBasePane getRoot() {
		if (this instanceof ZBasePane) {
			return (ZBasePane)this;
		}
		else {
			Component c = this;
			while (c != null) {
				c = c.getParent();
				if (c instanceof ZBasePane) {
					return (ZBasePane)c;
				}
			}
			return null;
		}
	}
	
	/**
	 * Traverse up to the JFrame this component is contained within,
	 * used for showing dialogues and locking the frame using the
	 * glass pane.
	 */
	public JFrame getFrame() {
		Component c = this;
		while (c != null) {
			c = c.getParent();
			if (c instanceof JFrame) {
				return (JFrame)c;
			}
		}
		return null;
	}
	
	/**
	 * Traverse up the swing container heirarchy looking for the
	 * first parent implementing ZTreeNode, or null if we fall off 
	 * the top of the container heirarchy.
	 */
	public ZTreeNode getZParent() {
		Component c = this;
		while (c != null) {
			c = c.getParent();
			if (c instanceof ZTreeNode) {
				return (ZTreeNode)c;
			}
		}
		return null;
	}

	public int getZChildCount() {
		return getZChildren().size();
	}

	public boolean isZRoot() {
		return (getZParent() == null);
	}

	public boolean isZLeaf() {
		return (getZChildren().isEmpty());
	}
	
	public List<Component> getToolbarComponents() {
		return new ArrayList<Component>();
	}
	
	/**
	 * If setting editable from false to true
	 * generates the toolbar from the getActions
	 * method of the subclass and displays it
	 * otherwise hides the toolbar. The toolbar
	 * is only shown if there are actions present,
	 * I'm not sure this is actually a sensible
	 * behaviour.
	 */
	public void setEditable(boolean b) {
		if (b!=editable) {
			editable = b;
			if (editable) {
				toolBar.removeAll();
				boolean hasContent = false;
				// Add arbitrary JComponents
				for (Component j : getToolbarComponents()) {
					toolBar.add(j);
					hasContent = true;
				}
				// Pack to force action buttons to the right hand side
				// of the toolbar, other components are inserted on the
				// left by default.
				toolBar.add(Box.createHorizontalGlue());
				// Add actions
				for (Action a : getActions()) {
					toolBar.add(a);
					hasContent = true;
				}
				if (hasContent) {
					add(toolBar,BorderLayout.NORTH);
				}
			}
			else {
				remove(toolBar);
			}
		}
		revalidate();		
	}
	
	/**
	 * Replace this component with the specified new one
	 */
	protected void replaceWith(ZTreeNode newComponent) {
		if (this.isZRoot()) {
			// Do nothing, we're the root
			// component and can't be swapped out
			return;
		}
		else {
			ZTreeNode parent = getZParent();
			parent.swap(this, newComponent);
			((Component)parent).repaint();
		}
	}
	
	/**
	 * Returns the base element for the ZPane but does not configure it,
	 * we don't configure here as we want the configure() method to have
	 * access to the ZBasePane so it can get at the Repository etc.
	 * @param e
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static ZTreeNode componentFor(Element e) {
		String className = e.getAttributeValue("classname");		
		try {
			Class<ZTreeNode> theClass = 
				(Class<ZTreeNode>)ZPane.class.getClassLoader().loadClass(className);
			return theClass.newInstance();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Return the base element + configuration for the given ZPane
	 * subclass
	 * @param z
	 * @return
	 */
	static Element elementFor(ZTreeNode z) {
		Element zPaneElement = new Element("znode");
		zPaneElement.setAttribute("classname",z.getClass().getName());
		zPaneElement.addContent(z.getElement());
		return zPaneElement;
	}
	
	/**
	 * Determines whether an artifact exists in the profile, if one exists.
	 * 
	 * @param artifact
	 * @return true if a profile exists and the artifact exists in it, other false
	 */
	protected boolean artifactExistsInProfile(Artifact artifact) {
		boolean result=false;
		Profile profile=ProfileFactory.getInstance().getProfile();
		if (profile!=null) {
			result=profile.getArtifacts().contains(artifact);
		}
		return result;
	}

}
