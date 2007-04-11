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
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-11 16:43:14 $
 *               by   $Author: sowen70 $
 * Created on 23 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.wings.SBoxLayout;
import org.wings.SButton;
import org.wings.SConstants;
import org.wings.SDimension;
import org.wings.SForm;
import org.wings.SOptionPane;
import org.wings.SScrollPane;
import org.wings.SSeparator;
import org.wings.STree;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.wsdl.SchemaParsingException;
import uk.org.mygrid.dataproxy.xml.ElementDefinition;

@SuppressWarnings("serial")
public class WSDLConfigurationPanel extends CentrePanel{
	
	private static Logger logger = Logger
			.getLogger(WSDLConfigurationPanel.class);
		
	private STree tree = new STree();
	private WSDLTreeModel model;
	private WSDLConfig config;

	public WSDLConfigurationPanel(WSDLConfig config) {
		this.config=config;
		setLayout(new SBoxLayout(SBoxLayout.HORIZONTAL));	
		SButton backButton = createBackButton();		
		SButton toggleButton = createToggleButton();		
		SButton commitButton = createCommitButton();
		
		SForm buttonPanel = new SForm(new SBoxLayout(SBoxLayout.VERTICAL));		
		buttonPanel.setVerticalAlignment(SConstants.TOP);		
		buttonPanel.add(backButton);
		buttonPanel.add(new SSeparator());
		buttonPanel.add(toggleButton);								
		buttonPanel.add(commitButton);
		backButton.setPreferredSize(SDimension.FULLWIDTH);
		toggleButton.setPreferredSize(SDimension.FULLWIDTH);
		commitButton.setPreferredSize(SDimension.FULLWIDTH);
				
		initialiseTree();		
		
		SScrollPane treeScrollPane = new SScrollPane(tree);
		treeScrollPane.setPreferredSize(new SDimension("90%","100%"));
		add(buttonPanel);
		add(treeScrollPane);
		
	}

	private void initialiseTree() {
		model = new WSDLTreeModel(config);
		try {
			model.populate();			
		} catch (SchemaParsingException e) {
			logger.error("Error parsing the wsdl:"+config.getAddress(),e);
			setError("Error parsing the wsdl:"+e.getMessage());
		}
		
		tree.setCellRenderer(new WSDLTreeCellRenderer());
		tree.setModel(model);		
		tree.setRootVisible(false);
		tree.getSelectionModel().setSelectionMode(STree.SINGLE_TREE_SELECTION);
	}

	private SButton createCommitButton() {
		SButton commitButton = new SButton("Commit");
		commitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ActionListener optionListener = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand().equals(SOptionPane.YES_ACTION)) {
							commit();
						}
					}					
				};
				SOptionPane.showYesNoDialog(WSDLConfigurationPanel.this, "Are you sure you want to commit your changes? "+config.getName()+"?\nYou will lose any configurations you have made to this WSDL but stored data will remain.", "Delete WSDL?",optionListener);				
			}			
		});
		return commitButton;
	}

	private SButton createToggleButton() {
		SButton toggleButton = new SButton("Toggle");
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
		backButton = new SButton("Back");		
		backButton.setShowAsFormComponent(true);		
		backButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				switchPanel(new WSDLListPanel());				
			}
			
		});
		return backButton;
	}	
	
	private void commit() {
		List<ElementDefinition> elementDefs = config.getElements();
		elementDefs.clear();
		elementDefs.addAll(model.getSelectedForProxy());
		try {
			ProxyConfigFactory.writeConfig();
		} catch (Exception e) {
			//TODO: report back error
			logger.error("Error writing config",e);
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
		}		
	}	
}
