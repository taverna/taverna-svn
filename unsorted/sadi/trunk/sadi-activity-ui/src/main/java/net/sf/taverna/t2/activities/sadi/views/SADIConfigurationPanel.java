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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.help.CSH;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.activities.sadi.RestrictionNode;
import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean;
import net.sf.taverna.t2.activities.sadi.actions.SADIActivityConfigurationAction;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import ca.wilkinsonlab.sadi.common.SADIException;

/**
 * 
 * 
 * @author David Withers
 */
public class SADIConfigurationPanel extends
		ActivityConfigurationPanel<SADIActivity, SADIActivityConfigurationBean> {

	private static final long serialVersionUID = 1L;

	private SADIActivity activity;

	private SADIActivityConfigurationBean oldConfiguration, newConfiguration;

	private JLabel titleLabel, titleIcon;

	private DialogTextArea titleMessage;

	private JPanel titlePanel, buttonPanel;

	private JButton actionOkButton, actionCancelButton;

	private SADITreeNode sadiInputTree, sadiOutputTree;

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
		newConfiguration = new SADIActivityConfigurationBean(oldConfiguration);

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
			RestrictionNode restrictionTree = activity.getInputRestrictionTree();
			sadiInputTree = new SADITreeNode();
			convertTree(restrictionTree, true, sadiInputTree);
			selectComponents(sadiInputTree, newConfiguration.getInputRestrictionPaths());
			addActions(sadiInputTree, true);
		} catch (SADIException e) {
			sadiInputTree = null;
		} catch (IOException e) {
			sadiInputTree = null;
		}

		// output tree
		try {
			RestrictionNode restrictionTree = activity.getOutputRestrictionTree();
			sadiOutputTree = new SADITreeNode();
			convertTree(restrictionTree, true, sadiOutputTree);
			selectComponents(sadiOutputTree, newConfiguration.getOutputRestrictionPaths());
			addActions(sadiOutputTree, false);
		} catch (SADIException e) {
			sadiOutputTree = null;
		} catch (IOException e) {
			sadiOutputTree = null;
		}

		inputPanel = new JPanel(new GridBagLayout());
		outputPanel = new JPanel(new GridBagLayout());

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

	/**
	 * 
	 */
	private void layoutPanel() {
		setPreferredSize(new Dimension(450, 400));
		setLayout(new BorderLayout());

		// title
		titlePanel.setBorder(new CompoundBorder(titlePanel.getBorder(), new EmptyBorder(10, 10, 0,
				10)));
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.add(titleLabel, BorderLayout.NORTH);
		titlePanel.add(titleIcon, BorderLayout.WEST);
		titlePanel.add(titleMessage, BorderLayout.CENTER);
		
		// input/output trees
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 1;
		c.weightx = 1;

		if (sadiInputTree != null) {
			JPanel treePanel = new JPanel(new GridBagLayout());
			layoutComponents(treePanel, c, sadiInputTree, 0);
			c.insets = new Insets(5, 5, 5, 5);
			c.weighty = 1;
			inputPanel.add(treePanel, c);
		} else {
			inputPanel.add(new JLabel("Error fetching activity input class"), c);
		}

		c.weighty = 0;
		
		if (sadiOutputTree != null) {
			JPanel treePanel = new JPanel(new GridBagLayout());
			layoutComponents(treePanel, c, sadiOutputTree, 0);
			c.insets = new Insets(5, 5, 5, 5);
			c.weighty = 1.0;
			outputPanel.add(treePanel, c);
		} else {
			outputPanel.add(new JLabel("Error fetching activity output class"), c);
		}
		
		add(tabbedPane, BorderLayout.CENTER);

		// buttons
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		SADIViewUtils.addDivider(buttonPanel, SwingConstants.TOP, true);

		buttonPanel.add(actionCancelButton);
		buttonPanel.add(actionOkButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void selectComponents(SADITreeNode sadiTree, List<List<String>> inputRestrictionPaths) {
		for (List<String> path : inputRestrictionPaths) {
			selectComponent(sadiTree, path);
		}		
	}
	
	private boolean selectComponent(SADITreeNode sadiTree, List<String> path) {
		if (path.size() == 1) {
			sadiTree.getButton().setSelected(true);
			return true;
		} else {
			for (SADITreeNode child : sadiTree.getChildren()) {
				RestrictionNode restrictedProperty = (RestrictionNode) child.getRestrictedProperty();
				if (restrictedProperty.toString().equals(path.get(1))) {
					if (selectComponent(child, path.subList(1, path.size()))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void convertTree(RestrictionNode node, boolean checkBox, SADITreeNode sadiNode) {
		AbstractButton button = createButton(!node.isExclusive(), node.toString());
		SADITreeNode newNode = new SADITreeNode();
		if (node.isRoot()) {
			sadiNode.setButton(button);
			sadiNode.setRestrictedProperty(node);
			newNode = sadiNode;
		} else {
			newNode = new SADITreeNode(node, button);
			sadiNode.add(newNode);				
		}
		for (RestrictionNode child : node.getChildren()) {
			convertTree(child, true, newNode);
		}
	}
	
	private void addActions(final SADITreeNode node, final boolean inputs) {
		final AbstractButton button = node.getButton();
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (button.isSelected()) {
					for (AbstractButton abstractButton : getParentButtons(node)) {
						abstractButton.setSelected(false);
					}
					for (AbstractButton abstractButton : getChildButtons(node)) {
						abstractButton.setSelected(false);
					}
					if (button instanceof JRadioButton) {
						for (AbstractButton abstractButton : getSiblingButtons(node)) {
							if (abstractButton instanceof JRadioButton) {
								abstractButton.setSelected(false);
							}
						}
					} else {
						for (AbstractButton abstractButton : getUnselectedSiblingButtons(node)) {
							if (!(abstractButton instanceof JRadioButton)) {
								abstractButton.setSelected(true);
							}
						}
					}
					for (AbstractButton abstractButton : getInvalidButtons(node)) {
						abstractButton.setSelected(false);
					}
					if (inputs) {
						newConfiguration.addInputRestrictionPath(getRestrictionPath(node));
					} else {
						newConfiguration.addOutputRestrictionPath(getRestrictionPath(node));
					}
				} else {
					if (inputs) {
						newConfiguration.removeInputRestrictionPath(getRestrictionPath(node));
					} else {
						newConfiguration.removeOutputRestrictionPath(getRestrictionPath(node));
					}
				}
			}
		});
		for (SADITreeNode child : node.getChildren()) {
			addActions(child, inputs);
		}
	}
	
	/**
	 * @param node
	 * @return
	 */
	protected List<String> getRestrictionPath(SADITreeNode node) {
		List<String> restrictionPath = new ArrayList<String>();
		for (SADITreeNode pathNode : node.getPath()) {
			RestrictionNode restrictedProperty = pathNode.getRestrictedProperty();
			if (restrictedProperty != null) {
				restrictionPath.add(restrictedProperty.toString());
			}
		}
		return restrictionPath;
	}

	private Set<AbstractButton> getInvalidButtons(SADITreeNode node) {
		Set<AbstractButton> buttons = new HashSet<AbstractButton>();
		for (TreeNode parentNode : node.getPath()) {
			SADITreeNode parent = ((SADITreeNode) parentNode);
			if (parent.getButton() instanceof JRadioButton) {
				buttons.addAll(getSiblingTreeButtons(parent));
			}
		}
		
		return buttons;
	}
	
	private Set<AbstractButton> getChildButtons(SADITreeNode node) {
		Set<AbstractButton> buttons = new HashSet<AbstractButton>();
		for (SADITreeNode child : node.getChildren()) {
			buttons.add(child.getButton());
			buttons.addAll(getChildButtons(child));
		}
		return buttons;
	}
	
	private Set<AbstractButton> getParentButtons(SADITreeNode node) {
		Set<AbstractButton> buttons = new HashSet<AbstractButton>();
		SADITreeNode parent = (SADITreeNode) node.getParent();
		if (parent != null) {
			buttons.add(parent.getButton());
			buttons.addAll(getParentButtons(parent));
		}
		return buttons;
	}
	
	private Set<AbstractButton> getSiblingButtons(SADITreeNode node) {
		Set<AbstractButton> buttons = new HashSet<AbstractButton>();
		SADITreeNode parent = (SADITreeNode) node.getParent();
		if (parent != null) {
			for (SADITreeNode child : parent.getChildren()) {
				if (child != node) {
					buttons.add(child.getButton());
				}
			}
		}
		return buttons;
	}
	
	private Set<AbstractButton> getUnselectedSiblingButtons(SADITreeNode node) {
		Set<AbstractButton> buttons = new HashSet<AbstractButton>();
		SADITreeNode parent = (SADITreeNode) node.getParent();
		if (parent != null) {
			for (SADITreeNode child : parent.getChildren()) {
				if (child != node) {
					if (!isNodeSelected(child)) {
						buttons.add(child.getButton());
					}
				}
			}
		}
		return buttons;
	}
	
	private Set<AbstractButton> getSiblingTreeButtons(SADITreeNode node) {
		Set<AbstractButton> buttons = new HashSet<AbstractButton>();
		SADITreeNode parent = (SADITreeNode) node.getParent();
		if (parent != null) {
			for (SADITreeNode child : parent.getChildren()) {
				if (child != node) {
					if (child.getButton() instanceof JRadioButton) {
						buttons.add(child.getButton());
						buttons.addAll(getChildButtons(child));
					}
				}
			}
		}
		return buttons;
	}
	
	private boolean isNodeSelected(SADITreeNode node) {
		boolean selected = false;
		if (node.getButton().isSelected()) {
			selected = true;
		} else {
			for (SADITreeNode child : node.getChildren()) {
				if (isNodeSelected(child)) {
					selected = true;
					break;
				}
			}
		}
		return selected;
	}
	
	private void layoutComponents(Container container, GridBagConstraints c, SADITreeNode node, int level) {
		AbstractButton button = node.getButton();
		c.insets = new Insets(0, level * 20, 0, 0);
		container.add(button, c);
		level++;
		for (SADITreeNode child : node.getChildren()) {
			layoutComponents(container, c, child, level);
		}
	}
	
	private AbstractButton createButton(boolean checkBox, String name) {
		AbstractButton button;
		if (checkBox) {
			button = new JCheckBox(name);
		} else {
			button = new JRadioButton(name);
		}
		return button;
	}

	@Override
	public SADIActivityConfigurationBean getConfiguration() {
		return newConfiguration;
	}

	@Override
	public boolean isConfigurationChanged() {
		return !newConfiguration.equals(oldConfiguration);
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
		final JFrame frame = new JFrame();
		SADIActivity activity = new SADIActivity();
		SADIActivityConfigurationBean configurationBean = new SADIActivityConfigurationBean();
		configurationBean.setSparqlEndpoint("http://biordf.net/sparql");
		configurationBean.setGraphName("http://sadiframework.org/registry/");
		configurationBean.setServiceURI("http://sadiframework.org/examples/ermineJgo");
		activity.configure(configurationBean);

		final SADIConfigurationPanel config = new SADIConfigurationPanel(activity);
		frame.add(config);
		frame.pack();
		frame.setVisible(true);

		final JFrame frame2 = new JFrame();
		SADIActivity activity2 = new SADIActivity();
		SADIActivityConfigurationBean configurationBean2 = new SADIActivityConfigurationBean();
		configurationBean2.setSparqlEndpoint("http://biordf.net/sparql");
		configurationBean2.setGraphName("http://sadiframework.org/registry/");
		configurationBean2.setServiceURI("http://sadiframework.org/examples/linear");
		activity2.configure(configurationBean2);

		final SADIConfigurationPanel config2 = new SADIConfigurationPanel(activity2);
		frame2.add(config2);
		frame2.pack();
		frame2.setVisible(true);

	}
}
