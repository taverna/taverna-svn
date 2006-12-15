/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: WorkbenchPerspectives.java,v $
 * Revision           $Revision: 1.13 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-05 11:46:23 $
 *               by   $Author: davidwithers $
 * Created on 10 Nov 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui.workbench;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import net.sf.taverna.perspectives.CustomPerspective;
import net.sf.taverna.perspectives.CustomPerspectiveFactory;
import net.sf.taverna.perspectives.PerspectiveRegistry;
import net.sf.taverna.perspectives.PerspectiveSPI;
import net.sf.taverna.raven.spi.RegistryListener;
import net.sf.taverna.raven.spi.SpiRegistry;
import net.sf.taverna.utils.MyGridConfiguration;
import net.sf.taverna.zaria.ZBasePane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.shared.ModelMap.ModelChangeListener;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

@SuppressWarnings("serial")
public class WorkbenchPerspectives {

	private static Logger logger = Logger
			.getLogger(WorkbenchPerspectives.class);

	private ButtonGroup perspectiveButtons = new ButtonGroup();

	private AbstractButton lastPerspectiveButton = null;

	private Action openPerspectiveAction = null;

	private Action deletePerspectiveAction = null;

	private CurrentPerspectiveListener modelChangeListener = null;	

	Set<CustomPerspective> customPerspectives = null;

	private ZBasePane basePane = null;

	private JToolBar toolBar = null;

	private Map<PerspectiveSPI, JToggleButton> perspectives = new HashMap<PerspectiveSPI, JToggleButton>();

	public ModelChangeListener getModelChangeListener() {
		if (modelChangeListener == null) {
			modelChangeListener = new CurrentPerspectiveListener();
		}
		return modelChangeListener;
	}

	public WorkbenchPerspectives(ZBasePane basePane, JToolBar toolBar) {		

		this.basePane = basePane;
		this.toolBar = toolBar;

		PerspectiveRegistry.getInstance().addRegistryListener(new RegistryListener() {

			public void spiRegistryUpdated(SpiRegistry registry) {
				refreshPerspectives();
			}
			
		});
	}	

	public JMenu getEditPerspectivesMenu() {
		JMenu editPerspectivesMenu = new JMenu("Edit perspectives");

		Action newPerspectiveAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				String name = JOptionPane.showInputDialog(basePane,
						"New perspective name");
				if (name != null) {
					newPerspective(name);
				}
			}
		};

		newPerspectiveAction.putValue(Action.NAME, "New ..");
		newPerspectiveAction.putValue(Action.SMALL_ICON,
				TavernaIcons.newIcon);
		editPerspectivesMenu.add(newPerspectiveAction);
		Action toggleEditAction = basePane.getToggleEditAction();
		toggleEditAction.putValue(Action.SMALL_ICON, TavernaIcons.editIcon);
		editPerspectivesMenu.add(toggleEditAction);

		editPerspectivesMenu.add(getOpenPerspectiveAction());
		editPerspectivesMenu.add(getSavePerspectiveAction());
		editPerspectivesMenu.add(getDeleteCurrentPerspectiveAction());

		return editPerspectivesMenu;
	}

	public void saveAll() throws FileNotFoundException, IOException {
		// update current perspective
		PerspectiveSPI current = (PerspectiveSPI) ModelMap.getInstance()
				.getNamedModel(ModelMap.CURRENT_PERSPECTIVE);
		if (current != null) {
			current.update(basePane.getElement());
		}

		CustomPerspectiveFactory.getInstance().saveAll(customPerspectives);		
		
		for (PerspectiveSPI perspective : perspectives.keySet()) {
			if (!(perspective instanceof CustomPerspective)) {
				savePerspective(perspective);
			}
		}
	}

	private void savePerspective(PerspectiveSPI perspective) {

		InputStreamReader isr = new InputStreamReader(perspective
				.getLayoutInputStream());
		SAXBuilder builder = new SAXBuilder(false);
		Document document;
		try {
			document = builder.build(isr);

			String filename = perspective.getClass().getName() + ".perspective";
			File file = new File(MyGridConfiguration.getUserDir("conf"),
					filename);

			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			outputter.output(document.getRootElement(), new FileOutputStream(
					file));

		} catch (JDOMException e) {
			logger.error("Error parsing perspective XML", e);
		} catch (IOException e) {
			logger.error("Error saving perspective XML", e);
		}

	}

	public void removeCustomPerspective(CustomPerspective perspective) {
		customPerspectives.remove(perspective);
		perspectives.remove(perspective);
	}

	public void initialisePerspectives() {

		List<PerspectiveSPI> perspectives = PerspectiveRegistry.getInstance()
				.getPerspectives();
		for (final PerspectiveSPI perspective : perspectives) {			
			updatePerspectiveSplitPanesWithSaved(perspective);
			addPerspective(perspective, false);
		}		

		try {
			customPerspectives = CustomPerspectiveFactory.getInstance()
					.getAll();
		} catch (IOException e) {
			logger.error("Error reading user perspectives", e);
		}
		if (customPerspectives != null && customPerspectives.size() > 0) {
			toolBar.addSeparator();			
			
			for (CustomPerspective perspective : customPerspectives) {
				addPerspective(perspective, false);
			}
		}

		for (Component c : toolBar.getComponents()) {
			if (c instanceof AbstractButton) {
				((AbstractButton) c).doClick();
				break;
			}
		}
	}

	/**
	 * Checks the saved copy of the perspective for the split pane ratios, and updates
	 * the current perspective with these values. This is so that the split pane ratios
	 * are restored to the users last session. Reopenning the whole layout file was found to
	 * be dangerous, as embedded components can disappear if there are errors initialising them.
	 */
	private void updatePerspectiveSplitPanesWithSaved(PerspectiveSPI perspective) {
		String filename = perspective.getClass().getName() + ".perspective";
		File file = new File(MyGridConfiguration.getUserDir("conf"), filename);
		if (file.exists()) {
			try {
				Document savedLayout = new SAXBuilder().build(file);
				Element perspectiveLayout = new SAXBuilder().build(perspective.getLayoutInputStream()).detachRootElement();				
				
				List<Element> savedSplitElements = getSplitChildElements(savedLayout.getRootElement());
				List<Element> perspectiveSplitElements = getSplitChildElements(perspectiveLayout);
				
				if (savedSplitElements.size()==perspectiveSplitElements.size()) {
					for (int i=0;i<savedSplitElements.size();i++) {
						Element savedElement = savedSplitElements.get(i);
						Element perspectiveElement = perspectiveSplitElements.get(i);						
						perspectiveElement.setAttribute("ratio",savedElement.getAttributeValue("ratio"));
					}					
					perspective.update(perspectiveLayout);
				}
				else {
					logger.warn("Number of split panes differ, default perspective must have changed. Restoring to default.");
				}
				
			} catch (JDOMException e) {
				logger.error("Error parsing saved layout xml '" + filename
						+ "'", e);
			} catch (IOException e) {
				logger.error("Error opening saved layout xml '" + filename
						+ "'", e);
			}
		}
	}
	
	private List<Element> getSplitChildElements(Element root) {
		List<Element> result = new ArrayList<Element>();
		getSplitChildElements(root,result);
		return result;
	}
	
	private void getSplitChildElements(Element root, List<Element> result) {
		for (Object el : root.getChildren()) {
			Element element = (Element)el;
			if (element.getName().equals("split")) result.add(element);
			getSplitChildElements(element,result);
		}
	}
	
	

	private void addPerspective(final PerspectiveSPI perspective,
			boolean makeActive) {
		// ensure icon image is always 16x16
		ImageIcon buttonIcon = null;
		if (perspective.getButtonIcon() != null) {
			Image buttonImage = perspective.getButtonIcon().getImage();
			buttonIcon = new ImageIcon(buttonImage.getScaledInstance(16, 16,
					Image.SCALE_SMOOTH));
		}

		final JToggleButton toolbarButton = new JToggleButton(perspective
				.getText(), buttonIcon);
		toolbarButton.setToolTipText(perspective.getText() + " perspective");
		Action action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (basePane.isEditable()) {
					JOptionPane
							.showMessageDialog(
									basePane,
									"Sorry, unable to change perspectives whilst in edit mode",
									"Cannot change perspective",
									JOptionPane.INFORMATION_MESSAGE);
					// make sure selected button is the previous one.
					if (lastPerspectiveButton != null)
						lastPerspectiveButton.setSelected(true);
				} else {
					ModelMap.getInstance().setModel(
							ModelMap.CURRENT_PERSPECTIVE, perspective);
					lastPerspectiveButton = toolbarButton;
				}
			}
		};
		action.putValue(Action.NAME, perspective.getText());
		action.putValue(Action.SMALL_ICON, buttonIcon);
		
		toolbarButton.setAction(action);
		toolBar.add(toolbarButton);
		perspectiveButtons.add(toolbarButton);
		perspectives.put(perspective, toolbarButton);
		if (makeActive) {
			toolbarButton.doClick();
		}
	}

	private void openLayout(InputStream layoutStream) {
		try {
			InputStreamReader isr = new InputStreamReader(layoutStream);
			SAXBuilder builder = new SAXBuilder(false);
			Document document = builder.build(isr);
			basePane.configure(document.detachRootElement());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error opening layout file", ex);
			JOptionPane.showMessageDialog(basePane,
					"Error opening layout file: " + ex.getMessage());
		}
	}

	private void newPerspective(String name) {
		Element layout = new Element("layout");
		layout.setAttribute("name", name);
		layout.addContent(new WorkbenchZBasePane().getElement());
		CustomPerspective p = new CustomPerspective(layout);
		customPerspectives.add(p);
		addPerspective(p, true);
	}

	private Action getSavePerspectiveAction() {
		Action result = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Save perspective");
				chooser.setFileFilter(new ExtensionFileFilter(
						new String[] { "xml" }));
				int retVal = chooser.showSaveDialog(basePane);
				if (retVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					if (file != null) {
						PrintWriter out;
						try {
							out = new PrintWriter(new FileWriter(file));
							Element element = basePane.getElement();
							XMLOutputter xo = new XMLOutputter(Format
									.getPrettyFormat());
							out.print(xo.outputString(element));
							out.flush();
							out.close();
						} catch (IOException ex) {
							logger.error("IOException saving layout", ex);
							JOptionPane.showMessageDialog(basePane,
									"Error saving layout file: "
											+ ex.getMessage());
						}
					}
				}
			}
		};
		result.putValue(Action.NAME, "Save current");
		result.putValue(Action.SMALL_ICON, TavernaIcons.saveIcon);
		return result;
	}

	private Action getOpenPerspectiveAction() {
		if (openPerspectiveAction == null) {
			openPerspectiveAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					chooser.setDialogTitle("Open Layout");
					chooser.setFileFilter(new ExtensionFileFilter(
							new String[] { "xml" }));
					int retVal = chooser.showOpenDialog(basePane);
					if (retVal == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						if (file != null) {
							try {
								openLayout(file.toURI().toURL().openStream());
							} catch (IOException ex) {
								logger.error("Error saving default layout", ex);
							}
						}
					}
				}
			};
			openPerspectiveAction.putValue(Action.NAME, "Load");
			openPerspectiveAction.putValue(Action.SMALL_ICON,
					TavernaIcons.openIcon);
		}
		return openPerspectiveAction;
	}

	private Action getDeleteCurrentPerspectiveAction() {
		if (deletePerspectiveAction == null) {
			deletePerspectiveAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					int ret = JOptionPane
							.showConfirmDialog(
									basePane,
									"Are you sure you wish to delete the current perspective",
									"Delete perspective?",
									JOptionPane.YES_NO_OPTION);
					if (ret == JOptionPane.YES_OPTION) {
						PerspectiveSPI p = (PerspectiveSPI) ModelMap
								.getInstance().getNamedModel(
										ModelMap.CURRENT_PERSPECTIVE);
						if (p != null) {
							ModelMap.getInstance().setModel(
									ModelMap.CURRENT_PERSPECTIVE, null);
							basePane.setEditable(false); // cancel edit mode
															// so perspective
															// can be changed
															// after deletion
							if (p instanceof CustomPerspective) {
								removeCustomPerspective((CustomPerspective) p);
							}
							try {
								CustomPerspectiveFactory.getInstance().saveAll(
										customPerspectives);
								refreshPerspectives();
							} catch (FileNotFoundException e1) {
								logger.error(
										"No file to save custom perspectives",
										e1);
							} catch (IOException e1) {
								logger
										.error(
												"Error writing custom perspectives to file",
												e1);
							}
						}
					}
				}

			};
			deletePerspectiveAction.putValue(Action.NAME, "Delete current");
			deletePerspectiveAction.putValue(Action.SMALL_ICON,
					TavernaIcons.deleteIcon);
		}
		return deletePerspectiveAction;
	}

	/**
	 * Recreates the toolbar buttons. Useful if a perspective has been
	 * removed.
	 * 
	 */
	private void refreshPerspectives() {
		toolBar.removeAll();
		toolBar.repaint();
		
		customPerspectives.clear();
		initialisePerspectives();
	}

	public void switchPerspective(PerspectiveSPI perspective) {
		// If we don't know it, and it's not a custom perspective
		// (where each instance is really unique),
		// we'll try to locate one of the existing buttons
		if (!perspectives.containsKey(perspective)
				&& !(perspective instanceof CustomPerspective)) {
			for (PerspectiveSPI buttonPerspective : perspectives.keySet()) {
				// FIXME: Should have some other identifier than getClass() ?
				// First (sub)class instance wins
				if (perspective.getClass().isInstance(buttonPerspective)) {
					// Do the known button instead
					perspective = buttonPerspective;
					break;
				}
			}
		}

		// Regardless of the above, we'll add it as a button
		// if it still does not exist in the toolbar.
		if (!perspectives.containsKey(perspective)) {
			addPerspective(perspective, true);
		}
		// (Button should now be in perspectives)

		// Make sure the button is selected
		perspectives.get(perspective).setSelected(true);

		if (perspective instanceof CustomPerspective) {
			// only allow custom perspectives to be editable.
			basePane.getToggleEditAction().setEnabled(true);
			getOpenPerspectiveAction().setEnabled(true);
			getDeleteCurrentPerspectiveAction().setEnabled(true);
		} else {
			basePane.getToggleEditAction().setEnabled(false);
			getOpenPerspectiveAction().setEnabled(false);
			getDeleteCurrentPerspectiveAction().setEnabled(false);
		}
		openLayout(perspective.getLayoutInputStream());
	}

	/**
	 * Change perspective when ModelMap.CURRENT_PERSPECTIVE has been modified.
	 * 
	 * @author Stian Soiland
	 * @author Stuart Owen
	 * 
	 */
	public class CurrentPerspectiveListener implements ModelChangeListener {

		public boolean canHandle(String modelName, Object model) {
			return modelName.equals(ModelMap.CURRENT_PERSPECTIVE)
					&& model instanceof PerspectiveSPI;
		}

		public void modelCreated(String modelName, Object model) {
			PerspectiveSPI perspective = (PerspectiveSPI) model;
			switchPerspective(perspective);
		}

		public void modelChanged(String modelName, Object oldModel,
				Object newModel) {
			((PerspectiveSPI) oldModel).update(basePane.getElement());

			PerspectiveSPI perspective = (PerspectiveSPI) newModel;
			switchPerspective(perspective);
		}

		public void modelDestroyed(String modelName, Object oldModel) {
			if (oldModel instanceof CustomPerspective) {
				removeCustomPerspective((CustomPerspective) oldModel);
			}
		}
	}

}
