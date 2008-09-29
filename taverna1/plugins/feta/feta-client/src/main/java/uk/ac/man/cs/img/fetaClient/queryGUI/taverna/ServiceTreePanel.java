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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

/**
 * @author alperp
 * 
 * 
 * 
 */
@SuppressWarnings("serial")
public class ServiceTreePanel extends JPanel {

	private TreeModel serviceTreeModel;

	private BasicServiceModel serviceModel;

	private ServiceTree tree;

	private JScrollPane listScroll;

	/**
	 * 
	 */
	public ServiceTreePanel(TreeModel treeModel, BasicServiceModel model) {
		super();
		setLayout(new BorderLayout());

		setTreeModel(treeModel);
		setServiceModel(model);
		// initialize();

		tree = new ServiceTree(serviceTreeModel);

		JScrollPane listScroll = new JScrollPane(tree,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS/** JScrollPane.HORIZONTAL_SCROLLBAR_NEVER* */
		);
		listScroll.setBorder(BorderFactory.createEtchedBorder());

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
						.getLastSelectedPathComponent();
				if (node == null)
					return;

				if (node.isLeaf()) {
					Object obj = node.getUserObject();
					if (obj instanceof BasicServiceModel)
						serviceModel.copyFrom((BasicServiceModel) node
								.getUserObject());
				} else {
				}

			}

		});

		this.add(listScroll);
		this.setOpaque(true);
	}

	/**
	 * @param treeModel
	 */
	private void setTreeModel(TreeModel treeModel) {
		serviceTreeModel = treeModel;

	}

	/**
	 * @return
	 */
	public BasicServiceModel getServiceModel() {
		return serviceModel;
	}

	/**
	 * @param model
	 */
	public void setServiceModel(BasicServiceModel model) {
		serviceModel = model;
	}

}
