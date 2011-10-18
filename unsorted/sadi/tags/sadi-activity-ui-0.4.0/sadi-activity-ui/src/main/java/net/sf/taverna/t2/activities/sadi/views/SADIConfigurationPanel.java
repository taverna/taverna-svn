/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.sadi.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.io.IOException;

import javax.help.CSH;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean;
import net.sf.taverna.t2.activities.sadi.SADIUtils;
import net.sf.taverna.t2.activities.sadi.actions.SADIActivityConfigurationAction;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.apache.log4j.Logger;

import ca.wilkinsonlab.sadi.SADIException;
import ca.wilkinsonlab.sadi.client.Service;
import ca.wilkinsonlab.sadi.restrictiontree.RestrictionTree;
import ca.wilkinsonlab.sadi.restrictiontree.RestrictionTreeModel;

/**
 * 
 * 
 * @author David Withers
 * @author Luke McCarthy
 */
public class SADIConfigurationPanel extends
		ActivityConfigurationPanel<SADIActivity, SADIActivityConfigurationBean> {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(SADIConfigurationPanel.class);
	
	private SADIActivity activity;

	private SADIActivityConfigurationBean oldConfiguration;

	private JLabel titleLabel, titleIcon;

	private DialogTextArea titleMessage;

	private JPanel titlePanel, buttonPanel;

	private JButton actionOkButton, actionCancelButton;

	private JComponent sadiInputTree, sadiOutputTree;

	// package visibility for unit test...
	RestrictionTreeModel sadiInputTreeModel, sadiOutputTreeModel;

	private JTabbedPane tabbedPane;

	private JPanel inputPanel;

	private JPanel outputPanel;

	public SADIConfigurationPanel(SADIActivity activity) {
		this.activity = activity;
		CSH.setHelpIDString(this, this.getClass().getCanonicalName());
		initialise();
	}

	private void initialise() {
		oldConfiguration = activity.getConfiguration();

		// title
		titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBackground(Color.WHITE);
		SADIViewUtils.addDivider(titlePanel, SwingConstants.BOTTOM, true);

		titleLabel = new JLabel(SADIActivityConfigurationAction.CONFIGURE);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13.5f));
		titleIcon = new JLabel("");
		titleMessage = new DialogTextArea("Select the service inputs from the input class");
		titleMessage.setMargin(new Insets(5, 10, 10, 10));
		titleMessage.setFont(titleMessage.getFont().deriveFont(11f));
		titleMessage.setEditable(false);
		titleMessage.setFocusable(false);
		
		// input tree
		try {
			sadiInputTreeModel = new RestrictionTreeModel(activity.getService().getInputClass());
			sadiInputTreeModel.selectPaths(SADIUtils.convertPaths(oldConfiguration.getInputPortMap().values()));
			sadiInputTree = new RestrictionTree(sadiInputTreeModel);
		} catch (Exception e) {
			log.error("error loading input OWL class", e);
		}

		// output tree
		try {
			sadiOutputTreeModel = new RestrictionTreeModel(activity.getService().getOutputClass(), activity.getService().getInputClass());
			sadiOutputTreeModel.selectPaths(SADIUtils.convertPaths(oldConfiguration.getOutputPortMap().values()));
			sadiOutputTree = new RestrictionTree(sadiOutputTreeModel);
		} catch (Exception e) {
			log.error("error loading input OWL class", e);
		}

		inputPanel = new JPanel(new BorderLayout());
		outputPanel = new JPanel(new BorderLayout());

		// tabs
		tabbedPane = new JTabbedPane();
		tabbedPane.add("Inputs", new JScrollPane(inputPanel));
		tabbedPane.add("Outputs", new JScrollPane(outputPanel));
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (inputPanel.equals(tabbedPane.getSelectedComponent())) {
					titleMessage.setText("Select the service inputs from the input class");
				} else {
					titleMessage.setText("Select the service outputs from the output class");
				}
			}
		});
		
		// buttons
		actionOkButton = new JButton();
		actionOkButton.setFocusable(false);

		actionCancelButton = new JButton();
		actionCancelButton.setFocusable(false);

		layoutPanel();
	}
	
	private void layoutPanel() {
		setPreferredSize(new Dimension(450, 400));
		setLayout(new BorderLayout());

		// title
		titlePanel.setBorder(new CompoundBorder(titlePanel.getBorder(), new EmptyBorder(10, 10, 0, 10)));
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.add(titleLabel, BorderLayout.NORTH);
		titlePanel.add(titleIcon, BorderLayout.WEST);
		titlePanel.add(titleMessage, BorderLayout.CENTER);

		if (sadiInputTree != null) {
			JScrollPane treePanel = new JScrollPane(sadiInputTree);
			inputPanel.add(treePanel, BorderLayout.CENTER);
		} else {
			inputPanel.add(new JLabel("Error fetching activity input class"));
		}
		
		if (sadiOutputTree != null) {
			JScrollPane treePanel = new JScrollPane(sadiOutputTree);
			outputPanel.add(treePanel, BorderLayout.CENTER);
		} else {
			outputPanel.add(new JLabel("Error fetching activity output class"));
		}
		
		add(tabbedPane, BorderLayout.CENTER);

		// buttons
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		SADIViewUtils.addDivider(buttonPanel, SwingConstants.TOP, true);

		buttonPanel.add(actionCancelButton);
		buttonPanel.add(actionOkButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	@Override
	public SADIActivityConfigurationBean getConfiguration() {
		SADIActivityConfigurationBean newConfiguration = new SADIActivityConfigurationBean(oldConfiguration);
		Service service = activity.getService();
		newConfiguration.setInputPortMap(SADIUtils.buildPortMap(sadiInputTreeModel.getSelectedPaths(), SADIUtils.getInputClassLabel(service)));
		newConfiguration.setOutputPortMap(SADIUtils.buildPortMap(sadiOutputTreeModel.getSelectedPaths(), SADIUtils.getOutputClassLabel(service)));
		return newConfiguration;
	}

	@Override
	public boolean isConfigurationChanged() {
		return !getConfiguration().equals(oldConfiguration);
	}

	public void setOkAction(Action okAction) {
		actionOkButton.setAction(okAction);
	}

	public void setCancelAction(Action cancelAction) {
		actionCancelButton.setAction(cancelAction);
	}

	@Override
	public void noteConfiguration() {
		SADIActivityConfigurationBean newConfiguration = new SADIActivityConfigurationBean();
		oldConfiguration = newConfiguration;
	}

	@Override
	public void refreshConfiguration() {
		removeAll();
		initialise();
	}

	@Override
	public boolean checkValues() {
		// TODO Not yet done
		return true;
	}

	public static void main(String[] args) throws ActivityConfigurationException, SADIException, IOException {
//		final JFrame frame = new JFrame();
//		SADIActivity activity = new SADIActivity();
//		SADIActivityConfigurationBean configurationBean = new SADIActivityConfigurationBean();
//		configurationBean.setSparqlEndpoint("http://biordf.net/sparql");
//		configurationBean.setGraphName("http://sadiframework.org/registry/");
//		configurationBean.setServiceURI("http://sadiframework.org/examples/ermineJgo");
//		activity.configure(configurationBean);
//
//		final SADIConfigurationPanel config = new SADIConfigurationPanel(activity);
//		frame.add(config);
//		frame.pack();
//		frame.setVisible(true);

		final SADIActivity activity2 = new SADIActivity();
		SADIActivityConfigurationBean configurationBean2 = new SADIActivityConfigurationBean();
		configurationBean2.setSparqlEndpoint("http://biordf.net/sparql");
		configurationBean2.setGraphName("http://sadiframework.org/registry/");
		configurationBean2.setServiceURI("http://sadiframework.org/examples/linear");
		activity2.configure(configurationBean2);

		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
        		JFrame frame2 = new JFrame();
        		SADIConfigurationPanel config2 = new SADIConfigurationPanel(activity2);
                frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        		frame2.getContentPane().add(config2);
        		frame2.pack();
        		frame2.setVisible(true);
            }
        });
	}
}
