package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Component;
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
import net.sf.taverna.raven.spi.SpiRegistry;

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
	
	private Action toggleScroll = new ToggleScrollPaneAction();
	private Action selectSPI = new SelectSPIAction();
	private Action selectInstance = new SelectInstanceAction();
	
	public ZRavenComponent() {
		super();
		actions.add(selectSPI);
		actions.add(selectInstance);
		actions.add(toggleScroll);
		actions.add(new ReplaceWithBlankAction());
		setLayout(new BorderLayout());
		contentArea.setLayout(new BorderLayout());
		add(contentArea, BorderLayout.CENTER);
		// TODO Auto-generated constructor stub
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
		contentArea.removeAll();
		if (hasScrollPane) {
			contentArea.add(new JScrollPane(theComponent), BorderLayout.CENTER);
		}
		else {
			contentArea.add(theComponent, BorderLayout.CENTER);
		}
		this.contents = theComponent;
		toggleScroll.setEnabled(true);
		revalidate();
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
						setComponent(getRoot().getComponent(theClass));
						try {
							artifact = getRoot().getRepository().artifactForClass(theClass);
						} catch (ArtifactNotFoundException e) {
							// Should never happen as these things can only be loaded
							// from within a raven classloader and so should by definition
							// have an artifact associated with them. You never know though.
							e.printStackTrace();
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
				contentArea.add(new JScrollPane(contents), 
						BorderLayout.CENTER);
			}
			else {
				contentArea.add(contents, BorderLayout.CENTER);
			}
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
