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
 * Filename           $RCSfile: ServiceTreePopupHandler.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-14 10:43:24 $
 *               by   $Author: sowen70 $
 * Created on 21 Nov 2006
 *****************************************************************/
package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.jdom.Element;

/**
 * Popup menu when right-clicking on a result node, giving the option to add to the current Scufl Model.
 * 
 * @author Stuart Owen
 */
public class ServiceTreePopupHandler extends MouseAdapter {
	
	private static Logger logger = Logger.getLogger(ServiceTreePopupHandler.class);
	
	ServiceTree theTree = null;
	
	public ServiceTreePopupHandler(ServiceTree theTree) {
		this.theTree=theTree;
	}
		
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	/**
	 * Handle the mouse pressed event in case this is the platform specific
	 * trigger for a popup menu
	 */
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}	
	
	private final Processor createProcessorFromServiceModel(final ScuflModel model, BasicServiceModel serviceModel) {
		Processor newProcessor = null;
		Element wrapperElement = new Element("wrapper");						
		wrapperElement.addContent(serviceModel.getTavernaProcessorSpecAsElement().detach());
		String validName = serviceModel.getServiceName();
		validName=model.getValidProcessorName(validName);
		
		try {
			newProcessor = ProcessorHelper
			.loadProcessorFromXML(wrapperElement, model,
					validName);							
		} catch (Exception e1) {
			logger.error("Error creating processor",e1);
			JOptionPane.showMessageDialog(theTree, "An error occured creating the processor"
					+ e1.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
			
		} 
		return newProcessor;
	}
	
	private void doEvent(MouseEvent e) {
		TreePath path=theTree.getPathForLocation(e.getX(), e.getY());
		if (path!=null) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) (path.getLastPathComponent());
			final ScuflModel model = TavernaFetaGUI.getInstance().getScuflModel();
			if (node.getUserObject() instanceof BasicServiceModel) {								
				JPopupMenu menu=new JPopupMenu("Query result.");
				JMenuItem addToModelMenuItem=new JMenuItem("Add to model");
				if (model==null) addToModelMenuItem.setEnabled(false);	
				
				ActionListener addToModelListener=new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Processor newProcessor;
						BasicServiceModel serviceModel = (BasicServiceModel) node.getUserObject();
						newProcessor=createProcessorFromServiceModel(model, serviceModel);
						if (newProcessor!=null)
							model.addProcessor(newProcessor);
					}																		
				};
				addToModelMenuItem.addActionListener(addToModelListener);
				menu.add(addToModelMenuItem);
				
				if (model!=null && model.getProcessors().length>0) {
					JMenu processorList = new JMenu("Add as alternate to...");
					for (final Processor p : model.getProcessors()) {
						JMenuItem processorItem = new JMenuItem(p.getName(), ProcessorHelper
								.getPreferredIcon(p));
						processorList.add(processorItem);
						processorItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								try {																		
									BasicServiceModel serviceModel = (BasicServiceModel) node.getUserObject();
									Processor alternateProcessor = createProcessorFromServiceModel(model, serviceModel);
									if (alternateProcessor!=null) {
										AlternateProcessor alternate = new AlternateProcessor(alternateProcessor);
										p.addAlternate(alternate);
										if (p.getModel() != null) {
											boolean isOffline = p.getModel().isOffline();
											if (isOffline) {
												alternateProcessor.setOffline();
											} else {
												alternateProcessor.setOnline();
											}
										}
									}
								} catch (Exception ex) {
									logger.error("Error creating alternate processor",ex);
									JOptionPane.showMessageDialog(theTree, "Problem creating alternate : \n"
											+ ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
								}
							}														

						});
					}
					menu.add(processorList);
				}
				
				menu.show(theTree,e.getX(), e.getY());
			}
		}
	}	
}
