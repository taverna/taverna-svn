/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.workbench.views.results;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;
import uk.org.taverna.platform.report.Invocation;
import uk.org.taverna.platform.report.StatusReport;
import uk.org.taverna.platform.report.WorkflowReport;
import uk.org.taverna.scufl2.api.port.Port;

/**
 * View showing the results of iterations from a workflow or processor report.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ReportView extends JPanel implements Updatable {

	private final RendererRegistry rendererRegistry;

	private final List<SaveAllResultsSPI> saveActions;

	private final List<SaveIndividualResultSPI> saveIndividualActions;

	private int invocationCount = 0;

	private CardLayout cardLayout = new CardLayout();

	private Map<Invocation, InvocationView> invocationComponents = new HashMap<>();

	private InvocationTreeModel invocationTreeModel;

	private StatusReport<?, ?> report;

	private Invocation selectedInvocation;

	private JPanel invocationPanel;

	private JButton saveButton;

	private Port selectedPort;

	public ReportView(StatusReport<?, ?> report, RendererRegistry rendererRegistry,
			List<SaveAllResultsSPI> saveActions, List<SaveIndividualResultSPI> saveIndividualActions) {
		super(new BorderLayout());
		this.report = report;
		this.rendererRegistry = rendererRegistry;
		this.saveActions = saveActions;
		this.saveIndividualActions = saveIndividualActions;
		init();
	}

	private void init() {
		removeAll();

		SortedSet<Invocation> invocations = report.getInvocations();
		invocationCount = invocations.size();

		if (invocationCount == 0) {
			JLabel noDataMessage = new JLabel("No data available", JLabel.CENTER);
			Font font = noDataMessage.getFont();
			if (font != null) {
				font = font.deriveFont(Math.round((font.getSize() * 1.5))).deriveFont(Font.BOLD);
				noDataMessage.setFont(font);
			}
			add(noDataMessage, BorderLayout.CENTER);
		} else {
			JPanel saveButtonsPanel = new JPanel(new BorderLayout());
			if (report instanceof WorkflowReport) {
				saveButton = new JButton(new SaveAllAction("Save all values", this));
			} else {
				saveButton = new JButton(new SaveAllAction("Save invocation values", this));
			}
			saveButtonsPanel.add(saveButton, BorderLayout.EAST);
			add(saveButtonsPanel, BorderLayout.NORTH);

			invocationPanel = new JPanel();
			invocationPanel.setLayout(cardLayout);
			invocationPanel.add(new JPanel(), "BLANK");

			if (invocationCount == 1) {
				add(invocationPanel, BorderLayout.CENTER);
				showInvocation(invocations.first());
			} else {
				invocationTreeModel = new InvocationTreeModel(report);
				JTree invocationTree = new JTree(invocationTreeModel);
				invocationTree.setExpandsSelectedPaths(true);
				invocationTree.setRootVisible(false);
				invocationTree.setShowsRootHandles(true);
				invocationTree.getSelectionModel().setSelectionMode(
						TreeSelectionModel.SINGLE_TREE_SELECTION);
				invocationTree.addTreeSelectionListener(new TreeSelectionListener() {
					@Override
					public void valueChanged(TreeSelectionEvent e) {
						Object selectedComponent = e.getPath().getLastPathComponent();
						if (selectedComponent instanceof InvocationTreeNode) {
							InvocationTreeNode selectedNode = (InvocationTreeNode) selectedComponent;
							if (selectedNode.isLeaf()) {
								showInvocation(selectedNode.getInvocation());
							} else {
								showInvocation(null);
							}
						}
					}
				});
				invocationTree.setCellRenderer(new DefaultTreeCellRenderer() {
					public Component getTreeCellRendererComponent(JTree tree, Object value,
							boolean selected, boolean expanded, boolean leaf, int row,
							boolean hasFocus) {
						Component renderer =  super.getTreeCellRendererComponent(tree, value, selected,
									expanded, leaf, row, hasFocus);
						if (renderer instanceof JLabel) {
							JLabel label = (JLabel) renderer;
							label.setIcon(null);
						}
						return renderer;
					}
				});

				JScrollPane jScrollPane = new JScrollPane(invocationTree,
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				jScrollPane.setMinimumSize(new Dimension(150, 0));

				JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
				splitPane.setLeftComponent(jScrollPane);
				splitPane.setRightComponent(invocationPanel);

				add(splitPane, BorderLayout.CENTER);
				invocationTree.setSelectionPath(new TreePath(invocationTreeModel.getFirstInvocationNode().getPath()));
			}
		}
	}

	public void selectPort(Port port) {
		InvocationView invocationView = invocationComponents.get(selectedInvocation);
		if (invocationView != null) {
			invocationView.selectPortTab(port);
		}
	}

	public void update() {
		SortedSet<Invocation> invocations = report.getInvocations();
		if (invocationCount < 2) {
			if (invocations.size() != invocationCount) {
				init();
			} else if (invocationCount == 1) {
				if (selectedInvocation != null) {
					invocationComponents.get(selectedInvocation).update();
				}
			}
		} else {
			if (invocations.size() != invocationCount) {
				// update invocations tree
				invocationCount = invocations.size();
				invocationTreeModel.update();
				// int selectedIndex = invocationList.getSelectedIndex();
				// invocationList.setListData(invocations.toArray(new Invocation[invocationCount]));
				// invocationList.setSelectedIndex(selectedIndex);
			}
			if (selectedInvocation != null) {
				invocationComponents.get(selectedInvocation).update();
			}
		}
	}

	private void showInvocation(Invocation invocation) {
		if (selectedInvocation != null) {
			InvocationView invocationView = invocationComponents.get(selectedInvocation);
			selectedPort = invocationView.getSelectedPort();
		}
		if (invocation != null) {
			if (!invocationComponents.containsKey(invocation)) {
				InvocationView invocationView = new InvocationView(invocation, rendererRegistry,
						saveIndividualActions);
				invocationComponents.put(invocation, invocationView);
				invocationPanel.add(invocationView, invocation.getId());
			}
			if (selectedPort != null) {
				invocationComponents.get(invocation).selectPortTab(selectedPort);
			}
			cardLayout.show(invocationPanel, invocation.getId());
			saveButton.setEnabled(true);
		} else {
			saveButton.setEnabled(false);
			cardLayout.show(invocationPanel, "BLANK");
		}
		selectedInvocation = invocation;
	}

	private class SaveAllAction extends AbstractAction {

		public SaveAllAction(String name, ReportView resultViewComponent) {
			super(name);
			putValue(SMALL_ICON, WorkbenchIcons.saveAllIcon);
		}

		public void actionPerformed(ActionEvent e) {

			String title = "Data saver";

			final JDialog dialog = new HelpEnabledDialog(MainWindow.getMainWindow(), title, true);
			dialog.setResizable(false);
			dialog.setLocationRelativeTo(MainWindow.getMainWindow());
			JPanel panel = new JPanel(new BorderLayout());
			DialogTextArea explanation = new DialogTextArea();
			explanation.setText("Select the input and output ports to save the associated data");
			explanation.setColumns(40);
			explanation.setEditable(false);
			explanation.setOpaque(false);
			explanation.setBorder(new EmptyBorder(5, 20, 5, 20));
			explanation.setFocusable(false);
			explanation.setFont(new JLabel().getFont()); // make the font the same as for other
															// components in the dialog
			panel.add(explanation, BorderLayout.NORTH);
			final Map<String, JCheckBox> inputChecks = new HashMap<String, JCheckBox>();
			final Map<String, JCheckBox> outputChecks = new HashMap<String, JCheckBox>();
			final Map<JCheckBox, Path> checkReferences = new HashMap<JCheckBox, Path>();
			final Map<String, Path> chosenReferences = new HashMap<String, Path>();
			final Set<Action> actionSet = new HashSet<Action>();

			ItemListener listener = new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					JCheckBox source = (JCheckBox) e.getItemSelectable();
					if (inputChecks.containsValue(source)) {
						if (source.isSelected()) {
							if (outputChecks.containsKey(source.getText())) {
								outputChecks.get(source.getText()).setSelected(false);
							}
						}
					}
					if (outputChecks.containsValue(source)) {
						if (source.isSelected()) {
							if (inputChecks.containsKey(source.getText())) {
								inputChecks.get(source.getText()).setSelected(false);
							}
						}
					}
					chosenReferences.clear();
					for (JCheckBox checkBox : checkReferences.keySet()) {
						if (checkBox.isSelected()) {
							chosenReferences.put(checkBox.getText(), checkReferences.get(checkBox));
						}
					}
				}

			};

			SortedMap<String, Path> inputPorts = selectedInvocation.getInputs();
			SortedMap<String, Path> outputPorts = selectedInvocation.getOutputs();

			JPanel portsPanel = new JPanel();
			portsPanel.setLayout(new GridBagLayout());
			if (!inputPorts.isEmpty()) {
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.fill = GridBagConstraints.NONE;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.insets = new Insets(5, 10, 5, 10);
				portsPanel.add(new JLabel("Workflow inputs:"), gbc);

				TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<String, JCheckBox>();
				for (Entry<String, Path> entry : inputPorts.entrySet()) {
					String portName = entry.getKey();
					Path value = entry.getValue();
					if (value != null) {
						JCheckBox checkBox = new JCheckBox(portName);
						checkBox.setSelected(!outputPorts.containsKey(portName));
						checkBox.addItemListener(listener);
						inputChecks.put(portName, checkBox);
						sortedBoxes.put(portName, checkBox);
						checkReferences.put(checkBox, value);
					}
				}
				gbc.insets = new Insets(0, 10, 0, 10);
				for (String portName : sortedBoxes.keySet()) {
					gbc.gridy++;
					portsPanel.add(sortedBoxes.get(portName), gbc);
				}
				gbc.gridy++;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.insets = new Insets(5, 10, 5, 10);
				portsPanel.add(new JLabel(""), gbc); // empty space
			}
			if (!outputPorts.isEmpty()) {
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.fill = GridBagConstraints.NONE;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.insets = new Insets(5, 10, 5, 10);
				portsPanel.add(new JLabel("Workflow outputs:"), gbc);
				TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<String, JCheckBox>();
				for (Entry<String, Path> entry : outputPorts.entrySet()) {
					String portName = entry.getKey();
					Path value = entry.getValue();
					if (value != null) {
						JCheckBox checkBox = new JCheckBox(portName);
						checkBox.setSelected(true);

						checkReferences.put(checkBox, value);
						checkBox.addItemListener(listener);
						outputChecks.put(portName, checkBox);
						sortedBoxes.put(portName, checkBox);
					}
				}
				gbc.insets = new Insets(0, 10, 0, 10);
				for (String portName : sortedBoxes.keySet()) {
					gbc.gridy++;
					portsPanel.add(sortedBoxes.get(portName), gbc);
				}
				gbc.gridy++;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.insets = new Insets(5, 10, 5, 10);
				portsPanel.add(new JLabel(""), gbc); // empty space
			}
			panel.add(portsPanel, BorderLayout.CENTER);
			chosenReferences.clear();
			for (JCheckBox checkBox : checkReferences.keySet()) {
				if (checkBox.isSelected()) {
					chosenReferences.put(checkBox.getText(), checkReferences.get(checkBox));
				}
			}

			JPanel buttonsBar = new JPanel();
			buttonsBar.setLayout(new FlowLayout());
			// Get all existing 'Save result' actions
			for (SaveAllResultsSPI spi : saveActions) {
				AbstractAction action = spi.getAction();
				actionSet.add(action);
				JButton saveButton = new JButton((AbstractAction) action);
				if (action instanceof SaveAllResultsSPI) {
					((SaveAllResultsSPI) action).setChosenReferences(chosenReferences);
					((SaveAllResultsSPI) action).setParent(dialog);
				}
				buttonsBar.add(saveButton);
			}
			JButton cancelButton = new JButton("Cancel", WorkbenchIcons.closeIcon);
			cancelButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
				}

			});
			buttonsBar.add(cancelButton);
			panel.add(buttonsBar, BorderLayout.SOUTH);
			panel.revalidate();
			dialog.add(panel);
			dialog.pack();
			dialog.setVisible(true);
		}

	}

}
