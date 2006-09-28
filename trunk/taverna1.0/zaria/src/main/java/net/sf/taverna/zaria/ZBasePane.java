package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.spi.SpiRegistry;

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
	
	/**
	 * Construct a new ZBasePane, inserting a default
	 * ZBlankComponent as the solitary child
	 */
	public ZBasePane() {
		super();
		child = new ZBlankComponent();
		add((Component)child, BorderLayout.CENTER);
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
		// TODO Auto-generated method stub
		return null;
	}

	public void configure(Element e) {
		// TODO Auto-generated method stub
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
	public JMenuItem getMenuItem(Class theClass) {
		return new JMenuItem(theClass.getName());
	}
	
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
	
	
	
	
}
