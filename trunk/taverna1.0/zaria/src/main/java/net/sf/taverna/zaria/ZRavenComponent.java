package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.spi.SpiRegistry;
import net.sf.taverna.zaria.ZBasePane.NamedRavenComponentSpecifier;

import org.jdom.Element;

public class ZRavenComponent extends ZPane {

	private String spiName = null;
	private Artifact artifact = null;
	private String className = null;
	private boolean hasScrollPane = true;
	private JPanel contentArea = new JPanel();
	private JComponent contents = null;
	private List<Action> actions = new ArrayList<Action>();
	private SpiRegistry registry = null;
	private String sharedName = "";
	
	private Action toggleScroll = new ToggleScrollPaneAction();
	private Action selectSPI = new SelectSPIAction();
	private Action selectInstance = new SelectInstanceAction();
	private Action nameComponent = new NameComponentAction();
	private Action selectNamedInstance = new SelectNamedInstanceAction();
	
	
	public ZRavenComponent() {
		super();
		actions.add(selectSPI);
		actions.add(selectInstance);
		actions.add(toggleScroll);
		actions.add(nameComponent);
		actions.add(selectNamedInstance);
		nameComponent.setEnabled(false);
		actions.add(new ReplaceWithBlankAction());
		setLayout(new BorderLayout());
		contentArea.setLayout(new BorderLayout());
		add(contentArea, BorderLayout.CENTER);
	}
	
	public Element getElement() {
		if (sharedName.equals("")) {
			Element e = new Element("component");
			if (artifact != null && className != null) {
				Element artifactElement = new Element("raven");
				Element g = new Element("group");
				g.setText(artifact.getGroupId());
				Element a = new Element("artifact");
				a.setText(artifact.getArtifactId());
				Element v = new Element("version");
				v.setText(artifact.getVersion());
				artifactElement.addContent(g);
				artifactElement.addContent(a);
				artifactElement.addContent(v);
				
				Element classNameElement = new Element("classname");
				classNameElement.setText(className);
				
				Element spiNameElement = new Element("interface");
				spiNameElement.setText(spiName);
				
				e.addContent(artifactElement);
				e.addContent(classNameElement);
				e.addContent(spiNameElement);
			}
			return e;
		}
		else {
			Element e = new Element("namedcomponent");
			Element nameElement = new Element("name");
			nameElement.setText(sharedName);
			e.addContent(nameElement);
			return e;
		}
	}

	public void unsetSharedName() {
		this.sharedName = "";
		nameComponent.setEnabled(true);
	}
	
	public void setSharedName(String name) {
		this.sharedName = name;
		// Either fetch a component from the base pane with this name
		// or we're creating a new one.
		JComponent jc = getRoot().getNamedComponent(name);
		if (jc != null) {
			if (jc.getParent()!=null) {
				JComponent parent = (JComponent) jc.getParent();
				// Remove from any existing parent just in case
				jc.getParent().remove(jc);
				parent.revalidate();
				parent.repaint();
				
			}
			setComponent(jc);
		}
		else {
			// Create a new shared component entry in the basepane
			NamedRavenComponentSpecifier nrcs = 
				getRoot().new NamedRavenComponentSpecifier(artifact, className, name);
			getRoot().namedComponentDefinitions.put(name, nrcs);
			if (this.contents != null) {
				getRoot().namedComponents.put(name, this.contents);
			}
		}
		nameComponent.setEnabled(false);
	}
	
	public void configure(Element confElement) {
		Element e = confElement.getChild("component");
		if (e!=null) {
			Element spiNameElement = e.getChild("interface");
			if (spiNameElement != null) {
				this.setSPI(spiNameElement.getTextTrim());
			}
			Element artifactElement = e.getChild("artifact");
			if (artifactElement != null) {
				artifact = new BasicArtifact(
						artifactElement.getChild("group").getTextTrim(),
						artifactElement.getChild("artifact").getTextTrim(),
						artifactElement.getChild("version").getTextTrim());
			}
			Element classNameElement = e.getChild("classname");
			if (classNameElement != null) {
				className = classNameElement.getTextTrim();
			}
			if (className != null) {
				ClassLoader acl;
				try {
					acl = getRoot().getRepository().getLoader(artifact, null);
					Class theClass = acl.loadClass(className);
					setComponent(getRoot().getComponent(theClass));
				} catch (ArtifactNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ArtifactStateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		else {
			e = confElement.getChild("namedcomponent");
			if (e!=null) {
				Element componentNameElement = e.getChild("name");
				String componentName = componentNameElement.getTextTrim();
				JComponent jc = getRoot().getNamedComponent(componentName);
				if (jc.getParent()!=null) {
					jc.getParent().remove(jc);
				}
				setComponent(jc);
			}
		}
	}
	
	public List<Action> getActions() {
		return actions;
	}

	public void setSPI(String newSPIName) {
		if (spiName == null || newSPIName.equals(spiName)==false) {
			this.spiName = newSPIName;
			registry = getRoot().getRegistryFor(spiName);
			selectInstance.setEnabled(true);
		}
	}
	
	/**
	 * Set the current component, whether within a scrollpane or
	 * not based on the boolean hasScrollPane property
	 * @param theComponent
	 */
	private synchronized void setComponent(JComponent theComponent) {
		if (this.contents != null) {
			getRoot().deregisterComponent(this.contents);
		}
		contentArea.removeAll();
		if (hasScrollPane) {
			contentArea.add(new JScrollPane(theComponent), BorderLayout.CENTER);
		}
		else {
			contentArea.add(theComponent, BorderLayout.CENTER);
		}
		this.contents = theComponent;
		getRoot().registerComponent(this.contents);
		toggleScroll.setEnabled(true);
		getRoot().revalidate();
	}
	
	/**
	 * Get the current component
	 */
	public JComponent getComponent() {
		return this.contents;
	}
	
	/**
	 * Pick the component from a list of known named base scoped components
	 */
	public class SelectNamedInstanceAction extends AbstractAction {

		public SelectNamedInstanceAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION,"Select named component");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("selectnamed"));
			//putValue(Action.NAME, "fromName");
		}
		
		public void actionPerformed(ActionEvent arg0) {
			JPopupMenu menu = new JPopupMenu("Named Components");
			for (final String name : getRoot().namedComponentDefinitions.keySet()) {
				NamedRavenComponentSpecifier nrcs = 
					getRoot().namedComponentDefinitions.get(name);
				JMenuItem prototype;
				try {
					prototype = getRoot().getMenuItem(nrcs.getComponentClass());
					JMenuItem item = new JMenuItem(name, prototype.getIcon());
					item.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
								setSharedName(name);
						}
					});
					menu.add(item);
				} catch (ArtifactNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ArtifactStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Component sourceComponent = (Component) arg0.getSource();
			menu.show(sourceComponent, 0, sourceComponent.getHeight());
		}
			
		
	}
	
	/**
	 * Assign a new name to the current (unnamed) component
	 */
	public class NameComponentAction extends AbstractAction {

		public NameComponentAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION,"Assign name to component");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("setname"));
			//putValue(Action.NAME,"addName");
		}
		
		public void actionPerformed(ActionEvent arg0) {
			ImageIcon icon = null;
			String[] options = getRoot().getKnownSPINames();
			String newName = (String)JOptionPane.showInputDialog(
					getFrame(),
					"Assign name to component",
					"Name",
					JOptionPane.QUESTION_MESSAGE,
					icon,
					null,
					"new name");
			if (newName!=null) {
				setSharedName(newName);
			}
		}
			
		
	}
	
	/**
	 * Action to select an instance of the current SPI
	 */
	public class SelectInstanceAction extends AbstractAction {
		
		public SelectInstanceAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION, "Select component");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("selectraven"));
			setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent e) {
			JPopupMenu menu = new JPopupMenu("Available Components");
			for (final Class theClass : registry.getClasses()) {
				JMenuItem item = getRoot().getMenuItem(theClass);
				item.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						try {
							className = theClass.getName();
							setComponent(getRoot().getComponent(theClass));
							artifact = getRoot().getRepository().artifactForClass(theClass);
							unsetSharedName();
							nameComponent.setEnabled(true);
						} catch (ArtifactNotFoundException e) {
							// Should never happen as these things can only be loaded
							// from within a raven classloader and so should by definition
							// have an artifact associated with them. You never know though.
							e.printStackTrace();
						} catch (NoClassDefFoundError ncdfe) {
							JOptionPane.showMessageDialog(null,
									"Transitive dependency failure - this is normally\n"+
									"caused by an invalid POM file with missing dependencies.",
									"Error!",
									JOptionPane.ERROR_MESSAGE);
						}
						
					}
				});
				menu.add(item);
			}
			Component sourceComponent = (Component) e.getSource();
			menu.show(sourceComponent, 0, sourceComponent.getHeight());
		}
	}
	
	/**
	 * Action to select the current SPI from which components
	 * can be selected
	 */
	public class SelectSPIAction extends AbstractAction {

		public SelectSPIAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION,"Select SPI");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("selectspi"));
		}
		
		public void actionPerformed(ActionEvent arg0) {
			ImageIcon icon = null;
			String[] options = getRoot().getKnownSPINames();
			String newSPI = (String)JOptionPane.showInputDialog(
					getFrame(),
					"Select Service Provider Interface (SPI)",
					"SPI Chooser",
					JOptionPane.QUESTION_MESSAGE,
					icon,
					options,
					spiName);
			if (newSPI!=null) {
				setSPI(newSPI);
			}
		}
			
	}
	
	/**
	 * Action to toggle whether the contents are shown within
	 * a scroll pane or directly within the panel
	 * @author Tom
	 */
	public class ToggleScrollPaneAction extends AbstractAction {

		public ToggleScrollPaneAction() {
			super();
			setEnabled(false);
			updateState();
		}
		
		private void updateState() {
			if (hasScrollPane) {
				// Set state for button to remove pane
				putValue(Action.SHORT_DESCRIPTION,"Disable scroll pane");
				putValue(Action.SMALL_ICON, ZIcons.iconFor("disablescroll"));
			}
			else {
				// Set state for button to enable pane
				putValue(Action.SHORT_DESCRIPTION,"Enable scroll pane");
				putValue(Action.SMALL_ICON, ZIcons.iconFor("enablescroll"));
			}
			if (contents == null) {
				setEnabled(false);
			}
			else {
				setEnabled(true);
			}
		}
		
		public void actionPerformed(ActionEvent arg0) {
			contentArea.removeAll();
			hasScrollPane = !hasScrollPane;
			if (hasScrollPane) {
				JScrollPane sp = new JScrollPane(contents,
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				sp.setPreferredSize(new Dimension(0,0));
				contentArea.add(sp,	BorderLayout.CENTER);
			}
			else {
				contentArea.add(contents, BorderLayout.CENTER);
			}
			repaint();
			revalidate();
			updateState();
		}
		
	}
	
	
	/**
	 * Component has no children so the swap method is never used
	 */
	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent) {
		// Do nothing, will never be called
	}

	/**
	 * Component has no children, it's always a leaf
	 */
	public List<ZTreeNode> getZChildren() {
		return new ArrayList<ZTreeNode>();
	}
	
}
