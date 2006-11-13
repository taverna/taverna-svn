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
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-11-13 10:50:10 $
 *               by   $Author: sowen70 $
 * Created on 10 Nov 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui.workbench;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import net.sf.taverna.perspectives.CustomPerspective;
import net.sf.taverna.perspectives.CustomPerspectiveFactory;
import net.sf.taverna.perspectives.PerspectiveRegistry;
import net.sf.taverna.perspectives.PerspectiveSPI;
import net.sf.taverna.zaria.ZBasePane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

@SuppressWarnings("serial")
public class WorkbenchPerspectives {
	
	private static Logger logger = Logger
			.getLogger(WorkbenchPerspectives.class);
	
	private ButtonGroup perspectiveButtons = new ButtonGroup();
	private AbstractButton lastPerspectiveButton = null;
	private Action openPerspectiveAction=null;
	private Action deletePerspectiveAction=null;
	private JMenu perspectivesMenu = null;		
	Set<CustomPerspective> customPerspectives = null;
	private ZBasePane basePane = null;
	private JToolBar toolBar=null;
	
	public WorkbenchPerspectives(ZBasePane basePane, JToolBar toolBar) {
		perspectivesMenu=new JMenu("Perspectives");
		this.basePane=basePane;
		this.toolBar=toolBar;
		
	}

	public JMenu getPerspectivesMenu() {
		return perspectivesMenu;
	}
	
	public void saveAll() throws FileNotFoundException, IOException {
		CustomPerspectiveFactory.getInstance().saveAll(customPerspectives);
	}
	
	public void removeCustomPerspective(CustomPerspective perspective) {
		customPerspectives.remove(perspective);
	}
		
	public void initialisePerspectives() {		
		Action newPerspectiveAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				String name=JOptionPane.showInputDialog(basePane,"New perspective name");
				if (name!=null) {
					newPerspective(name);
				}
			}			
		};
		
		newPerspectiveAction.putValue(Action.NAME, "New ..");
		newPerspectiveAction.putValue(Action.SMALL_ICON,TavernaIcons.newInputIcon);
		perspectivesMenu.add(newPerspectiveAction);
		Action toggleEditAction = basePane.getToggleEditAction();
		toggleEditAction.putValue(Action.SMALL_ICON, TavernaIcons.editIcon);
		perspectivesMenu.add(toggleEditAction);		
		
		perspectivesMenu.add(getOpenPerspectiveAction());
		perspectivesMenu.add(getSavePerspectiveAction());
		perspectivesMenu.add(getDeleteCurrentPerspectiveAction());
		perspectivesMenu.addSeparator();
				
		List<PerspectiveSPI> perspectives=PerspectiveRegistry.getInstance().getPerspectives();
		for (final PerspectiveSPI perspective : perspectives) {			
			addPerspective(perspective,false);			
		}		
		
		toolBar.addSeparator();
		perspectivesMenu.addSeparator();
		
		try {
			customPerspectives = CustomPerspectiveFactory.getInstance().getAll();
		} catch (IOException e) {
			logger.error("Error reading user perspectives",e);				
		}
		if (customPerspectives!=null && customPerspectives.size()>0) {			
			for (CustomPerspective perspective : customPerspectives) {
				addPerspective(perspective,false);		
			}
		}
		for (Component c : toolBar.getComponents()) {
			if (c instanceof AbstractButton) { 
				((AbstractButton)c).doClick();
				break;
			}
		}
	}
	
	
	
	private void addPerspective(final PerspectiveSPI perspective, boolean makeActive) {
		final JToggleButton toolbarButton = new JToggleButton(perspective.getText(),perspective.getButtonIcon());
		toolbarButton.setToolTipText(perspective.getText()+" perspective");		
		Action action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (basePane.isEditable()) {					
					JOptionPane.showMessageDialog(basePane, "Sorry, unable to change perspectives whilst in edit mode", "Cannot change perspective",JOptionPane.INFORMATION_MESSAGE);
					//make sure selected button is the previous one.
					if (lastPerspectiveButton!=null) lastPerspectiveButton.setSelected(true);
				}
				else {
					ModelMap.getInstance().setModel(ModelMap.CURRENT_PERSPECTIVE, perspective);
					toolbarButton.setSelected(true); //select the button incase action was invoked via the menu
					lastPerspectiveButton=toolbarButton;
				}
			}				
		};			
		
		action.putValue(Action.NAME, perspective.getText());
		action.putValue(Action.SMALL_ICON,perspective.getButtonIcon());
		
		perspectivesMenu.add(action);						
		toolbarButton.setAction(action);
		toolBar.add(toolbarButton);
		perspectiveButtons.add(toolbarButton);
		if (makeActive) toolbarButton.doClick();
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
					"Error opening layout file: "
							+ ex.getMessage());
		}
	}
	
	private void newPerspective(String name) {
		Element layout=new Element("layout");
		layout.setAttribute("name",name);
		layout.addContent(new WorkbenchZBasePane().getElement());
		CustomPerspective p = new CustomPerspective(layout);
		customPerspectives.add(p);
		addPerspective(p,true);			
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
		if (openPerspectiveAction==null) {
			openPerspectiveAction=new AbstractAction() {
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
							}  catch(IOException ex) {
								logger.error("Error saving default layout",ex);
							}
						}
					}
				}
			};
			openPerspectiveAction.putValue(Action.NAME, "Load");
			openPerspectiveAction.putValue(Action.SMALL_ICON, TavernaIcons.openIcon);
		}
		return openPerspectiveAction;
	}
	
	private Action getDeleteCurrentPerspectiveAction() {
		if (deletePerspectiveAction==null) {
			deletePerspectiveAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					int ret=JOptionPane.showConfirmDialog(basePane, "Are you sure you wish to delete the current perspective","Delete perspective?",JOptionPane.YES_NO_OPTION);
					if (ret == JOptionPane.YES_OPTION) {
						PerspectiveSPI p = (PerspectiveSPI)ModelMap.getInstance().getNamedModel(ModelMap.CURRENT_PERSPECTIVE);
						if (p!=null) {
							ModelMap.getInstance().setModel(ModelMap.CURRENT_PERSPECTIVE, null);
							basePane.setEditable(false); //cancel edit mode so perspective can be changed after deletion
							customPerspectives.remove(p);						
							try {
								CustomPerspectiveFactory.getInstance().saveAll(customPerspectives);
								refreshPerspectives();
							} catch (FileNotFoundException e1) {
								logger.error("No file to save custom perspectives",e1);
							} catch (IOException e1) {
								logger.error("Error writing custom perspectives to file",e1);
							}
						}
					}
				}
				
			};
			deletePerspectiveAction.putValue(Action.NAME, "Delete current");
			deletePerspectiveAction.putValue(Action.SMALL_ICON,TavernaIcons.deleteIcon);		
		}
		return deletePerspectiveAction;
	}
	
	/**
	 * Recreates the menu and toolbar buttons. Useful if a perspective has been removed.
	 *
	 */
	private void refreshPerspectives() {
		toolBar.removeAll();
		toolBar.repaint();
		
		perspectivesMenu.removeAll();
		customPerspectives.clear();
		initialisePerspectives();
	}
	
	public void switchPerspective(PerspectiveSPI perspective) {
		if (perspective instanceof CustomPerspective) { //only allow custom perspectives to be editable.
			basePane.getToggleEditAction().setEnabled(true);
			openPerspectiveAction.setEnabled(true);
			deletePerspectiveAction.setEnabled(true);
		}
		else {
			basePane.getToggleEditAction().setEnabled(false);
			openPerspectiveAction.setEnabled(false);
			deletePerspectiveAction.setEnabled(false);
		}
		openLayout(perspective.getLayoutInputStream());	
	}
	
}
