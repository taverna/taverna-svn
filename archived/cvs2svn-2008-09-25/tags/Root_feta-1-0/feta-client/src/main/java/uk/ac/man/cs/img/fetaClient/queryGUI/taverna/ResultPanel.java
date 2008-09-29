/*
 *
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
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import uk.ac.man.cs.img.fetaClient.resource.FetaResources;
import uk.ac.man.cs.img.fetaClient.util.GUIUtil;

/**
 * @author alperp
 * 
 */
public class ResultPanel extends JPanel {

	private QueryHelper helper;

	private ServiceTreePanel treePanel;

	private DefaultTreeModel serviceTree;

	private ServiceFormPanel formPanel;

	private BasicServiceModel serviceModel;

	private ServiceModelAdaptor serviceModelAdaptor;

	private JLabel nameField, desciptionField;

	JButton annotateButton, queryButton;

	public ResultPanel(QueryHelper queryHelper) {
		super();
		serviceModel = new BasicServiceModel();
		serviceTree = new DefaultTreeModel(new DefaultMutableTreeNode(
				"Resulting Operations"));
		this.helper = queryHelper;
		helper.setTree(serviceTree);

		initialize();

	}

	private void initialize() {

		serviceModelAdaptor = new ServiceModelAdaptor(serviceModel);

		formPanel = new ServiceFormPanel(serviceModelAdaptor);
		serviceModel.addChangeListener(serviceModelAdaptor);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.setMaximumSize(new Dimension(500, 700));

		// Top Pane
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
		topPane.setOpaque(true);

		treePanel = new ServiceTreePanel(serviceTree, serviceModel);

		// JScrollPane scrollPane = new
		// JScrollPane(treePanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		JPanel myPane = new JPanel();
		myPane.setLayout(new BoxLayout(myPane, BoxLayout.X_AXIS));
		myPane.setOpaque(true);
		myPane.add(treePanel);

		topPane.add(myPane/** scrollPane* */
		);
		topPane.add(formPanel);

		// Bottom Pane
		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));

		annotateButton = new JButton("Launch Annotator");
		FetaResources.getPedroIcon();
		annotateButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		annotateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				GUIUtil.launchAnnotator(treePanel.getServiceModel()
						.getServiceDescriptionLocation());
			}
		});

		JButton queryButton = new JButton("List All Operations");
		queryButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				helper.queryForAll(true);
			}
		});

		bottomPane.add(queryButton);
		boolean annotator = false;
		try{
			annotator = FetaClientProperties.isAnnotator();
		}catch(Exception exp){
			exp.printStackTrace();			
		}
		if (annotator) {
			bottomPane.add(annotateButton);	
		}
		 

		this.add(topPane);
		this.add(bottomPane);

	}

	public void clearForm() {
		formPanel.setInterfaceLocationURL("");
		formPanel.setOperationName("");
		formPanel.setOrganisationName("");
		formPanel.setServiceDescriptionLocation("");
		formPanel.setServiceDescriptionText("");
		formPanel.setServiceName("");
		formPanel.setServiceName("");
		formPanel.setLocationURL("");
	}

	/**
	 * @return
	 */
	public QueryHelper getHelper() {
		return helper;
	}

}
