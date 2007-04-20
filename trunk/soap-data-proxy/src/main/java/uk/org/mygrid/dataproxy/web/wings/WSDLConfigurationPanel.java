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
 * Filename           $RCSfile: WSDLConfigurationPanel.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-20 11:33:59 $
 *               by   $Author: sowen70 $
 * Created on 23 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.wings.SBorderLayout;
import org.wings.SBoxLayout;
import org.wings.SButton;
import org.wings.SConstants;
import org.wings.SDimension;
import org.wings.SImageIcon;
import org.wings.SLabel;
import org.wings.SOptionPane;
import org.wings.SPanel;
import org.wings.SScrollPane;
import org.wings.STree;
import org.wings.border.STitledBorder;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.wsdl.WSDLParsingException;
import uk.org.mygrid.dataproxy.xml.ElementDefinition;

@SuppressWarnings("serial")
public class WSDLConfigurationPanel extends CentrePanel{
	
	private static Logger logger = Logger
			.getLogger(WSDLConfigurationPanel.class);
		
	private STree tree = new STree();
	private WSDLTreeModel model;
	private WSDLConfig config;
	private boolean changesMade=false;

	public WSDLConfigurationPanel(WSDLConfig config) {
		this.config=config;
		setLayout(new SBorderLayout());
		
		SButton backButton = createBackButton();
		SPanel toolbar = new SPanel(new SBoxLayout(SBoxLayout.VERTICAL));
		toolbar.setHorizontalAlignment(SConstants.LEFT_ALIGN);
		toolbar.add(backButton);
		SPanel spacer = new SPanel();
		spacer.setName("topspacer");
		spacer.setPreferredSize(new SDimension(null,"20px"));
		toolbar.add(spacer);
		add(toolbar,SBorderLayout.NORTH);
		
		SPanel mainPanel = new SPanel(new SBorderLayout());
						
		SButton toggleButton = createToggleButton();		
		SButton commitButton = createCommitButton();
		
		SButton expandButton = createExpandButton();
		SButton collapseButon = createCollapseButton();
								
		SPanel mainToolBar = new SPanel(new SBoxLayout(SBoxLayout.VERTICAL));
		mainToolBar.setVerticalAlignment(SConstants.TOP);
		mainToolBar.add(toggleButton);	
		mainToolBar.add(commitButton);
		mainToolBar.add(new SLabel(" "));
		mainToolBar.add(expandButton);
		mainToolBar.add(collapseButon);
		
		toggleButton.setPreferredSize(SDimension.FULLWIDTH);
		commitButton.setPreferredSize(SDimension.FULLWIDTH);
				
		initialiseTree();
		
		SScrollPane treeScrollPane = new SScrollPane(tree);
		treeScrollPane.setName("treescrollpane");
		treeScrollPane.setVerticalAlignment(SConstants.TOP_ALIGN);
		treeScrollPane.setMode(SScrollPane.MODE_COMPLETE);
		treeScrollPane.setPreferredSize(new SDimension("95%",null));
		
		mainPanel.setBorder(new STitledBorder("Toggle service response elements for referencing"));
		mainPanel.setName("typetreepanel");
		mainPanel.add(mainToolBar,SBorderLayout.WEST);
		mainPanel.add(treeScrollPane,SBorderLayout.CENTER);						
		mainPanel.setPreferredSize(new SDimension("95%","95%"));		
		mainPanel.setHorizontalAlignment(SConstants.CENTER_ALIGN);
		mainPanel.setVerticalAlignment(SConstants.TOP_ALIGN);
		
		add(mainPanel,SBorderLayout.CENTER);		
	}

	private void initialiseTree() {
		model = new WSDLTreeModel(config);
		try {
			model.populate();			
		} catch (WSDLParsingException e) {
			logger.error("Error parsing the wsdl:"+config.getAddress(),e);
			reportError("Error parsing the wsdl:"+e.getMessage());
		}
		
		tree.setCellRenderer(new WSDLTreeCellRenderer());
		tree.setModel(model);		
		tree.setRootVisible(false);
		tree.getSelectionModel().setSelectionMode(STree.SINGLE_TREE_SELECTION);
		
		//expand and collapse so that when opening an operation all its inner nodes are expanded.
		expandTree();
		collapseTree();
		
		tree.setShowAsFormComponent(false);
		tree.setNodeIndentDepth(20);
	}

	private SButton createCommitButton() {
		SButton commitButton = new SButton(new SImageIcon(Icons.getIcon("save")));
		commitButton.setToolTipText("Save changes");
		commitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ActionListener optionListener = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand().equals(SOptionPane.YES_ACTION)) {
							commit();
						}
					}					
				};
				SOptionPane.showYesNoDialog(WSDLConfigurationPanel.this, "Are you sure you want to commit your changes to "+config.getName()+"?\nYou will overwrite the previous configuration, but stored data will remain.", "Delete WSDL?",optionListener);				
			}			
		});
		return commitButton;
	}

	private SButton createToggleButton() {
		SButton toggleButton = new SButton(new SImageIcon(Icons.getIcon("toggle")));
		toggleButton.setToolTipText("Toggle selected element");
		toggleButton.setShowAsFormComponent(true);
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleSelectedTreeItem();				
			}			
		});
		return toggleButton;
	}

	private SButton createBackButton() {
		SButton backButton;
		backButton = new SButton(new SImageIcon(Icons.getIcon("back")));		
		backButton.setShowAsFormComponent(true);		
		backButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (changesMade) {
					ActionListener optionListener = new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (e.getActionCommand().equals(SOptionPane.YES_ACTION)) {
								switchPanel(new WSDLListPanel());
							}
						}					
					};
					SOptionPane.showYesNoDialog(WSDLConfigurationPanel.this, "You have made uncommitted changes, are you sure wish to go back?", "Lose changes?",optionListener);
				}
				else {
					switchPanel(new WSDLListPanel());
				}
			}
			
		});
		return backButton;
	}	
	
	private SButton createExpandButton() {
		SButton expandButton = new SButton(new SImageIcon(Icons.getIcon("expand")));
		expandButton.setToolTipText("Expand all");
		expandButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				expandTree();
			}			
		});
		return expandButton;
	}
	
	private void expandTree() {
		for (int i=0; i< tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}
	
	private void collapseTree() {
		for (int i=0; i< tree.getRowCount(); i++) {
			tree.collapseRow(i);
		}
	}
	
	private SButton createCollapseButton() {
		SButton collapseButton = new SButton(new SImageIcon(Icons.getIcon("collapse")));
		collapseButton.setToolTipText("Collapse all");
		collapseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				collapseTree();
			}			
		});
		return collapseButton;
	}
	
	private void commit() {
		List<ElementDefinition> elementDefs = config.getElements();
		elementDefs.clear();
		elementDefs.addAll(model.getSelectedForProxy());
		try {
			ProxyConfigFactory.writeConfig();
			reportStatus("Changes successfully committed");
			changesMade=false;
		} catch (Exception e) {
			logger.error("Error writing config",e);
			reportError("An error occurred rewriting the config after committing changes:"+e.getMessage());
		}
	}
	
	private void toggleSelectedTreeItem() {
		TreePath selectedPath = tree.getSelectionPath();
		Object selectedObj = selectedPath.getLastPathComponent();		
		if (selectedObj instanceof TypeNode) {
			TypeNode node = (TypeNode)selectedObj;
			model.toggleNode(node);
			tree.collapseRow(0);
			tree.expandRow(0);
			tree.setSelectionPath(selectedPath);
			reportStatus("Uncommitted changes made");
			changesMade=true;
		}		
		
	}	
}
