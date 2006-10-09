package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.spi.SpiRegistry;
import net.sf.taverna.zaria.progress.InfiniteProgressPanel;

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
public abstract class ZBasePane extends ZPane {

	private ZTreeNode child = null;
	private Repository repository = null;
	private Map<String,SpiRegistry> registries =
		new HashMap<String,SpiRegistry>();
	private String[] knownSpiNames = new String[0];
	private InfiniteProgressPanel glassPane = 
		new InfiniteProgressPanel();
	private Component oldGlassPane = null;
	private Action toggleEditAction;
	
	/**
	 * Construct a new ZBasePane, inserting a default
	 * ZBlankComponent as the solitary child
	 */
	public ZBasePane() {
		super();
		child = new ZBlankComponent();
		add((Component)child, BorderLayout.CENTER);
		toggleEditAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				boolean edit = ZBasePane.this.editable;
				ZBasePane.this.setEditable(!edit);				
			}
		};
		setEditActionState();
	}
	
	/**
	 * Get an Action object which can toggle the editable state
	 * of the ZBasePane
	 */
	public Action getToggleEditAction() {
		return toggleEditAction;
	}

	/**
	 * Configure the edit action based on the current editable
	 * state.
	 */
	private void setEditActionState() {
		if (editable) {
			toggleEditAction.putValue(Action.NAME, "Stop editing");
		}
		else {
			toggleEditAction.putValue(Action.NAME, "Edit");
		}
	}
	
	/**
	 * Single element list consiting only of the child item
	 */
	public List<ZTreeNode> getZChildren() {
		List<ZTreeNode> children = new ArrayList<ZTreeNode>();
		children.add(child);
		return children;
	}

	public Element getElement() {
		return elementFor(child);
	}

	public void configure(Element e) {
		ZTreeNode node = componentFor(e);
		swap(child, node);
		node.configure(e);
	}

	/**
	 * No actions, the ZBasePane is effectively invisible
	 */
	public List<Action> getActions() {
		return new ArrayList<Action>();
	}

	/**
	 * Only a single child so always swap it out and replace with
	 * the new component
	 */
	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent) {
		if (oldComponent == child || child == null) {
			if (child != null) {
				remove((Component)child);
			}
			child = newComponent;
			add((Component) newComponent, BorderLayout.CENTER);
			newComponent.setEditable(this.editable);
			revalidate();
		}
	}
	
	/**
	 * Call setEditable on the single child
	 */
	public void setEditable(boolean b) {
		super.setEditable(b);
		if (child != null) {
			child.setEditable(b);
		}
		setEditActionState();
		repaint();
		revalidate();
	}
	
	/**
	 * Get the Raven repository associated with this ZBasePane
	 */
	public Repository getRepository() {
		return this.repository;
	}
	
	/**
	 * Set the Raven Repository object used to discover SPI
	 * implementations for the ZRavenComponent instances within
	 * the layout
	 * @param r
	 */
	public void setRepository(Repository r) {
		this.repository = r;
	}

	/**
	 * Get known SPIs to be used with the ZRavenComponent
	 */
	public String[] getKnownSPINames() {
		return knownSpiNames;
	}
	
	/**
	 * Set the array of known SPIs
	 */
	public void setKnownSPINames(String[] spis) {
		this.knownSpiNames = spis;
	}
	
	/**
	 * Create or return a cached reference to an SpiRegistry for
	 * the specified SPI name
	 */
	public synchronized SpiRegistry getRegistryFor(String spiName) {
		if (registries.containsKey(spiName)) {
			return registries.get(spiName);
		}
		else {
			SpiRegistry sr = new SpiRegistry(repository, spiName, null);
			registries.put(spiName, sr);
			return sr;
		}
	}
	
	/**
	 * Given a Class object from an SPI produce an appropriate
	 * JMenuItem. By default this method doesn't do much, it
	 * just returns a textual menu item with the classname
	 * in but it's expected that this will be extended by any
	 * implementing class to produce something sensible. I don't
	 * want to make this method any more specialized to avoid
	 * dependencies on the potential range of SPI interfaces.
	 */
	public abstract JMenuItem getMenuItem(Class theClass);
	
	/**
	 * Given a Class object from an SPI construct a
	 * JComponent. Typically the SPI itself will point to
	 * a factory pattern although it may be directly linked
	 * to the implementation classes (which is why we're using
	 * the SpiRegistry rather than the more elegant
	 * InstanceRegistry). This method will always be extended,
	 * the only reason to avoid making this entire class
	 * abstract is for testing purposes (not a very good reason
	 * really)
	 */
	public abstract JComponent getComponent(Class theClass);
	
	/**
	 * Called when a new component is added to a ZRavenComponent
	 * pane, can be overridden to perform implementation specific
	 * initialization of the component
	 */
	protected void registerComponent(JComponent comp) {
		// Do nothing by default
	}
	
	/**
	 * Called when a component is removed from a ZRavenComponent pane,
	 * only called if the component is not named (and therefore will
	 * not be shared by other panes)
	 */
	protected void deregisterComponent(JComponent comp) {
		// Do nothing by default
	}
	
	/**
	 * Enumerate all visible ZRavenComponent panes within the current
	 * layout
	 */
	public List<ZRavenComponent> getRavenComponents() {
		List<ZRavenComponent> result = 
			new ArrayList<ZRavenComponent>();
		enumerateRavenComponents(result, this);
		return result;
	}
	private void enumerateRavenComponents(List<ZRavenComponent> results, ZTreeNode node) {
		if (node instanceof ZRavenComponent) {
			results.add((ZRavenComponent)node);
		}
		else {
			for (ZTreeNode child : node.getZChildren()) {
				enumerateRavenComponents(results, child);
			}
		}
	}
	
	/**
	 * Lock the parent frame, showing an infinite progress display
	 * message
	 */
	public void lockFrame() {
		JFrame jf = getFrame();
		if (jf != null) {
			Component c = jf.getGlassPane();
			if (c != glassPane) {
				oldGlassPane = c;
				jf.setGlassPane(glassPane);
			}
			glassPane.setText("Locked...");
			glassPane.start();
		}
	}
	
	/**
	 * Unlock the parent frame
	 */
	public void unlockFrame() {
		JFrame jf = getFrame();
		if (jf != null) {
			glassPane.stop();
			if (oldGlassPane != null) {
				jf.setGlassPane(oldGlassPane);
			}
		}
	}
	
}
