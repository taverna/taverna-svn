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
 * Filename           $RCSfile: MartServiceQueryConfigUIFactory05.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-23 17:06:39 $
 *               by   $Author: davidwithers $
 * Created on 04-Apr-2006
 *****************************************************************/
package org.biomart.martservice.config.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.biomart.martservice.DatasetLink;
import org.biomart.martservice.MartDataset;
import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartService;
import org.biomart.martservice.MartServiceException;
import org.biomart.martservice.config.QueryConfigController;
import org.biomart.martservice.config.QueryConfigUtils;
import org.biomart.martservice.config.event.QueryComponentAdapter;
import org.biomart.martservice.config.event.QueryComponentEvent;
import org.biomart.martservice.query.Attribute;
import org.biomart.martservice.query.Dataset;
import org.biomart.martservice.query.Filter;
import org.biomart.martservice.query.Query;
import org.biomart.martservice.query.QueryListener;
import org.ensembl.mart.lib.config.AttributeCollection;
import org.ensembl.mart.lib.config.AttributeDescription;
import org.ensembl.mart.lib.config.AttributeGroup;
import org.ensembl.mart.lib.config.AttributeList;
import org.ensembl.mart.lib.config.AttributePage;
import org.ensembl.mart.lib.config.BaseNamedConfigurationObject;
import org.ensembl.mart.lib.config.DatasetConfig;
import org.ensembl.mart.lib.config.FilterCollection;
import org.ensembl.mart.lib.config.FilterDescription;
import org.ensembl.mart.lib.config.FilterGroup;
import org.ensembl.mart.lib.config.FilterPage;
import org.ensembl.mart.lib.config.Option;
import org.ensembl.mart.lib.config.PushAction;

/**
 * Implementation of the <code>QueryConfigUIFactory</code> interface that
 * creates a UI which looks like the Biomart web interface.
 * 
 * @author David Withers
 */
public class MartServiceQueryConfigUIFactory05 implements QueryConfigUIFactory {
	private String version;

	private Color borderColor = new Color(202, 207, 213);

	private Color backgroundColor = Color.WHITE;

	private Color componentBackgroundColor = Color.WHITE;

	private MartService martService;

	private QueryConfigController controller;

	private MartDataset martDataset;

	private DatasetConfig datasetConfig;

	private Map filterNameToComponentMap = new HashMap();

	private Map attributeNameToComponentMap = new HashMap();

	private Map filterToDisplayName = new HashMap();

	private Map attributeToDisplayName = new HashMap();

	private Map attributePageNameToComponent = new HashMap();

	private Map attributePageNameToButton = new HashMap();

	private boolean settingAttributeState = false;

	private List componentRegister = new ArrayList();

	public MartServiceQueryConfigUIFactory05(MartService martService,
			QueryConfigController controller, MartDataset martDataset)
			throws MartServiceException {
		this.martService = martService;
		this.controller = controller;
		this.martDataset = martDataset;
		version = "0.5";
	}

	/**
	 * Returns the martDataset.
	 * 
	 * @return the martDataset.
	 */
	public MartDataset getMartDataset() {
		return martDataset;
	}

	/**
	 * Returns the query configuration for the dataset.
	 * 
	 * @return the query configuration for the dataset
	 * @throws MartServiceException
	 *             if the MartService returns an error or is unavailable
	 */
	public DatasetConfig getDatasetConfig() throws MartServiceException {
		if (datasetConfig == null) {
			datasetConfig = martService.getDatasetConfig(martDataset);
		}
		return datasetConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getDatasetConfigUI(org.ensembl.mart.lib.config.DatasetConfig)
	 */
	public Component getDatasetConfigUI() throws MartServiceException {
		JPanel panel = new JPanel(new BorderLayout());

		final SummaryPanel summaryPanel = new SummaryPanel();

		final JButton countButton = new JButton("Count");
		countButton.setBackground(Color.WHITE);
		countButton.setForeground(Color.BLACK);
		countButton.setFont(countButton.getFont().deriveFont(Font.BOLD));
		countButton.setBorder(null);
		// countButton.setOpaque(false);
		// countButton.setRolloverEnabled(true);
		countButton.setBorder(new EmptyBorder(5, 5, 5, 5));
		countButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				countButton.setEnabled(false);
				countButton.setText("Counting...");
				new Thread("BiomartDatasetCount") {
					public void run() {
						try {
							MartQuery martQuery = controller.getMartQuery();
							Query query = martQuery.getQuery();
							String datasetName = martQuery.getMartDataset()
									.getName();

							JLabel label1 = summaryPanel
									.getDataset1CountLabel();
							setSummaryCount(query, datasetName, label1);

							Set linkedDatasets = martQuery.getLinkedDatasets();
							if (linkedDatasets.size() == 1) {
								String linkedDatasetName = (String) linkedDatasets
										.iterator().next();
								JLabel label2 = summaryPanel
										.getDataset2CountLabel();
								setSummaryCount(query, linkedDatasetName,
										label2);
							}

						} catch (MartServiceException e) {
						}
						countButton.setText("Count");
						countButton.setEnabled(true);
					}
				}.start();
			}

		});

		JPanel buttonPanel = new JPanel(new MinimalLayout05());
		buttonPanel.setBackground(Color.BLACK);
		buttonPanel.setBorder(new EmptyBorder(5, 25, 5, 5));
		buttonPanel.add(countButton);
		panel.add(buttonPanel, BorderLayout.NORTH);

		JLabel label = new JLabel("biomart version 0.5");
		label.setBackground(Color.BLACK);
		label.setForeground(Color.WHITE);
		label.setOpaque(true);
		label.setBorder(new EmptyBorder(5, 25, 5, 5));
		panel.add(label, BorderLayout.SOUTH);

		JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				true);
		splitPanel.setBackground(backgroundColor);
		splitPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
		splitPanel.setDividerLocation(250);

		panel.add(splitPanel, BorderLayout.CENTER);

		JScrollPane scrollPane1 = new JScrollPane(summaryPanel);
		scrollPane1.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane1.setBorder(new LineBorder(borderColor, 3));

		splitPanel.setLeftComponent(scrollPane1);

		final JComponent inputPanel = createVerticalBox(backgroundColor);
		inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JScrollPane scrollPane = new JScrollPane(inputPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setBorder(new LineBorder(borderColor, 3));

		splitPanel.setRightComponent(scrollPane);

		final JComponent datasetPanel = new DatasetPanel();
		inputPanel.add(datasetPanel);

		final Component linkComponent = new DatasetLinkComponent(inputPanel,
				summaryPanel);
		componentRegister.add(linkComponent);

		final JComponent attributePanel = createVerticalBox(backgroundColor);
		attributePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		final JComponent filterPanel = createVerticalBox(backgroundColor);
		filterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		summaryPanel.getDataset1Button().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						inputPanel.removeAll();
						inputPanel.add(datasetPanel);
						inputPanel.revalidate();
						inputPanel.repaint();
					}
				});

		summaryPanel.getDataset2Button().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						inputPanel.removeAll();
						inputPanel.add(linkComponent);
						inputPanel.revalidate();
						inputPanel.repaint();
					}
				});

		generateConfiguration(this, summaryPanel, inputPanel, attributePanel,
				filterPanel);

		summaryPanel.getFilters1Button().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						inputPanel.removeAll();
						inputPanel.add(filterPanel);
						inputPanel.revalidate();
						inputPanel.repaint();
					}
				});

		summaryPanel.getAttributes1Button().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						inputPanel.removeAll();
						inputPanel.add(attributePanel);
						inputPanel.revalidate();
						inputPanel.repaint();
					}
				});

		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributePagesUI(org.ensembl.mart.lib.config.AttributePage[])
	 */
	public Component getAttributePagesUI(AttributePage[] attributePages,
			Object data) throws MartServiceException {
		final JComponent box = createVerticalBox(backgroundColor);

		if (attributePages.length > 1) {
			ButtonGroup buttonGroup = new ButtonGroup();
			final Map componentMap = new HashMap();

			final JComponent buttonBox = new JPanel(new GridLayout(0, 2));
			buttonBox.setBorder(new CompoundBorder(new LineBorder(borderColor,
					1), new EmptyBorder(10, 10, 10, 10)));
			buttonBox.setBackground(backgroundColor);
			box.add(buttonBox);

			final JComponent pagePanel = new JPanel(new BorderLayout());
			pagePanel.setBackground(backgroundColor);
			box.add(pagePanel);

			ItemListener listener = new ItemListener() {
				Component lastSelectedComponent;

				JRadioButton lastSelectedButton;

				public void itemStateChanged(ItemEvent e) {
					JRadioButton button = (JRadioButton) e.getItem();
					if (button != null) {
						Component selectedComponent = (Component) componentMap
								.get(button.getActionCommand());
						if (e.getStateChange() == ItemEvent.SELECTED) {
							boolean switchPage = true;
							if (lastSelectedComponent != null) {
								Map selected = new HashMap();
								// find all attributes on the last page that
								// were selected
								List oldChildren = getAttributeComponents(lastSelectedComponent);
								for (Iterator iter = oldChildren.iterator(); iter
										.hasNext();) {
									AttributeComponent attributeComponent = (AttributeComponent) iter
											.next();
									if (attributeComponent.isSelected()) {
										selected.put(attributeComponent
												.getQualifiedName(),
												attributeComponent);
									}
								}
								// remove attibutes that are already selected on
								// the new page
								List newChildren = getAttributeComponents(selectedComponent);
								for (Iterator iter = newChildren.iterator(); iter
										.hasNext();) {
									AttributeComponent attributeComponent = (AttributeComponent) iter
											.next();
									if (attributeComponent.isSelected()) {
										selected.remove(attributeComponent
												.getQualifiedName());
									}
								}
								Collection stillSelected = selected.values();
								if (stillSelected.size() > 0) {
									List attributeNames = new ArrayList();
									for (Iterator iter = stillSelected
											.iterator(); iter.hasNext();) {
										AttributeComponent component = (AttributeComponent) iter
												.next();
										attributeNames.add(component
												.getButton().getText());
									}
									List message = new ArrayList();
									message
											.add("The "
													+ button.getText()
													+ " page does not contain the following attributes:");
									JList jList = new JList(attributeNames
											.toArray());
									jList.setBorder(LineBorder
											.createGrayLineBorder());
									message.add(jList);
									message
											.add("These attributes will be removed. Do you wish to continue?");
									switchPage = JOptionPane.showConfirmDialog(
											buttonBox, message.toArray(),
											"Confirm page change",
											JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
								}
								if (switchPage) {
									// deselect any attributes on the old page
									// that are not on the new page
									for (Iterator iter = stillSelected
											.iterator(); iter.hasNext();) {
										AttributeComponent attributeComponent = (AttributeComponent) iter
												.next();
										attributeComponent.setSelected(false);
									}
								}
							}
							if (switchPage) {
								pagePanel.add(selectedComponent,
										BorderLayout.NORTH);
							} else {
								lastSelectedButton.setSelected(true);
							}
							box.revalidate();
							box.repaint();
						} else if (e.getStateChange() == ItemEvent.DESELECTED) {
							pagePanel.removeAll();
							lastSelectedComponent = selectedComponent;
							lastSelectedButton = button;
						}
					}
				}

			};

			for (int i = 0; i < attributePages.length; i++) {
				if (QueryConfigUtils.display(attributePages[i])) {
					Component component = getAttributePageUI(attributePages[i],
							data);
					JRadioButton button = new JRadioButton(attributePages[i]
							.getDisplayName());
					String description = attributePages[i].getDescription();
					if (description != null) {
						button.setToolTipText(description);
					}
					button.setBackground(backgroundColor);
					button.setFont(button.getFont().deriveFont(Font.BOLD));
					button
							.setActionCommand(attributePages[i]
									.getInternalName());
					button.addItemListener(listener);
					buttonGroup.add(button);
					buttonBox.add(button);
					componentMap.put(attributePages[i].getInternalName(),
							component);
					attributePageNameToComponent.put(attributePages[i]
							.getInternalName(), component);
					attributePageNameToButton.put(attributePages[i]
							.getInternalName(), button);
				}
			}

		} else if (attributePages.length == 1) {
			box.add(getAttributePageUI(attributePages[0], data));
		} else {
			box.add(new JLabel("No attributes available"));
		}

		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setBackground(backgroundColor);
		northPanel.add(box, BorderLayout.NORTH);

		return northPanel;

		// return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributePageUI(org.ensembl.mart.lib.config.AttributePage)
	 */
	public Component getAttributePageUI(AttributePage attributePage, Object data)
			throws MartServiceException {
		JComponent box = createVerticalBox(backgroundColor);

		AttributeGroup[] attributeGroups = (AttributeGroup[]) attributePage
				.getAttributeGroups().toArray(new AttributeGroup[0]);
		box.add(getAttributeGroupsUI(attributeGroups, data));

		return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeGroupsUI(org.ensembl.mart.lib.config.AttributeGroup[])
	 */
	public Component getAttributeGroupsUI(AttributeGroup[] attributeGroups,
			Object data) throws MartServiceException {
		JComponent box = createVerticalBox(backgroundColor);

		for (int i = 0; i < attributeGroups.length; i++) {
			if (QueryConfigUtils.display(attributeGroups[i])) {
				box.add(Box.createVerticalStrut(2));
				box.add(getAttributeGroupUI(attributeGroups[i], data));
			}
		}

		return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeGroupUI(org.ensembl.mart.lib.config.AttributeGroup)
	 */
	public Component getAttributeGroupUI(AttributeGroup attributeGroup,
			Object data) throws MartServiceException {
		JLabel title = new JLabel(attributeGroup.getDisplayName());
		title.setFont(title.getFont().deriveFont(Font.PLAIN));

		String description = attributeGroup.getDescription();
		if (description != null) {
			title.setToolTipText(description);
		}
		ExpandableBox box = new ExpandableBox(title, componentBackgroundColor,
				borderColor);

		AttributeCollection[] attributeCollections = attributeGroup
				.getAttributeCollections();
		box.add(getAttributeCollectionsUI(attributeCollections, data));

		box.setExpanded(false);

		return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeCollectionsUI(org.ensembl.mart.lib.config.AttributeCollection[])
	 */
	public Component getAttributeCollectionsUI(
			AttributeCollection[] attributeCollections, Object data)
			throws MartServiceException {
		JComponent box = createVerticalBox(componentBackgroundColor);

		for (int i = 0; i < attributeCollections.length; i++) {
			if (QueryConfigUtils.display(attributeCollections[i])) {
				box.add(Box.createVerticalStrut(10));
				box
						.add(getAttributeCollectionUI(attributeCollections[i],
								data));
			}
		}

		return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeCollectionUI(org.ensembl.mart.lib.config.AttributeCollection)
	 */
	public Component getAttributeCollectionUI(
			AttributeCollection attributeCollection, Object data)
			throws MartServiceException {
		JComponent box = null;

		AttributeDescription[] attributeDescriptions = (AttributeDescription[]) attributeCollection
				.getAttributeDescriptions()
				.toArray(new AttributeDescription[0]);

		AttributeList[] attributeLists = (AttributeList[]) attributeCollection
				.getAttributeLists().toArray(new AttributeList[0]);

		JLabel sequenceLabel = null;
		if ("seq_scope_type".equals(attributeCollection.getInternalName())) {
			sequenceLabel = new JLabel(MartServiceIcons
					.getIcon("gene_schematic"));
			box = createBox(sequenceLabel, false);
		} else if (attributeDescriptions.length > 1
				|| attributeLists.length > 1) {
			// more than one attribute so create a box with the collection name
			// as a header
			JLabel title = new JLabel(attributeCollection.getDisplayName());
			title.setFont(title.getFont().deriveFont(Font.BOLD));
			String description = attributeCollection.getDescription();
			if (description != null) {
				title.setToolTipText(description);
			}
			box = createBox(title, false);
		} else {
			box = createBox(null, false);
		}

		int maxSelect = attributeCollection.getMaxSelect();
		if (maxSelect == 1) {
			if (attributeDescriptions.length > 0) {
				box.add(getAttributeDescriptionsUI(attributeDescriptions,
						new Object[] { SINGLE_SELECTION, sequenceLabel }));
			} else {
				box.add(getAttributeListsUI(attributeLists, new Object[] {
						SINGLE_SELECTION, sequenceLabel }));
			}
		} else {
			if (attributeDescriptions.length > 0) {
				box.add(getAttributeDescriptionsUI(attributeDescriptions,
						new Object[] { MULTIPLE_SELECTION, sequenceLabel }));
			} else {
				box.add(getAttributeListsUI(attributeLists, new Object[] {
						MULTIPLE_SELECTION, sequenceLabel }));
			}
		}

		return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeDescriptionsUI(org.ensembl.mart.lib.config.AttributeDescription[],
	 *      int)
	 */
	public Component getAttributeDescriptionsUI(
			AttributeDescription[] attributeDescriptions, Object data)
			throws MartServiceException {
		Object[] dataArray = (Object[]) data;
		JComponent box = new JPanel(new GridLayout(1, 2));
		box.setBackground(componentBackgroundColor);
		JComponent box1 = createVerticalBox(componentBackgroundColor);
		JComponent box2 = createVerticalBox(componentBackgroundColor);
		box.add(box1);
		box.add(box2);

		// button group used if the attribute collection is SINGLE_SELECTION
		ButtonGroup buttonGroup = new ButtonGroup();
		JRadioButton off = new JRadioButton("OFF");
		buttonGroup.add(off);

		JComponent currentBox = box1;

		for (int i = 0; i < attributeDescriptions.length; i++) {
			if (QueryConfigUtils.display(attributeDescriptions[i])) {
				Component component = getAttributeDescriptionUI(
						attributeDescriptions[i],
						dataArray[0] == SINGLE_SELECTION ? new Object[] { off,
								dataArray[1] } : new Object[] { null,
								dataArray[1] });
				if (component != null) {
					currentBox.add(component);
					if (dataArray[0] == SINGLE_SELECTION
							&& component instanceof AttributeComponent) {
						AttributeComponent attributeComponent = (AttributeComponent) component;
						buttonGroup.add(attributeComponent.getButton());
					}
					if (QueryConfigUtils.isFilterReference(
							attributeDescriptions[i], version)) {
						FilterDescription filterDescription = QueryConfigUtils
								.getReferencedFilterDescription(
										attributeDescriptions[i], version);
						Component filterComponent = getFilterDescriptionUI(
								filterDescription, data);
						if (filterComponent instanceof QueryComponent
								&& component instanceof AttributeComponent) {
							AttributeComponent attributeComponent = (AttributeComponent) component;
							((QueryComponent) filterComponent)
									.setSelectorButton(attributeComponent
											.getButton());
						}
						componentRegister.add(filterComponent);
						box2.add(filterComponent);
						currentBox = box2;
					}
					if (currentBox == box1) {
						currentBox = box2;
					} else {
						currentBox = box1;
					}
				}
			}
		}
		currentBox.add(Box.createVerticalGlue());
		return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeDescriptionUI(org.ensembl.mart.lib.config.AttributeDescription,
	 *      int)
	 */
	public Component getAttributeDescriptionUI(
			AttributeDescription attributeDescription, Object data)
			throws MartServiceException {
		Object[] dataArray = (Object[]) data;
		MartDataset dataset = martDataset;
		AttributeDescription displayedAttribute;
		if (QueryConfigUtils.isReference(attributeDescription, version)) {
			dataset = QueryConfigUtils.getReferencedDataset(martService,
					martDataset, attributeDescription, version);
			if (dataset == null) {
				return null;
			}
			if (QueryConfigUtils.isFilterReference(attributeDescription,
					version)) {
				FilterDescription filter = QueryConfigUtils
						.getReferencedFilterDescription(martService, dataset,
								attributeDescription, version);
				if (filter == null) {
					return null;
				}
				displayedAttribute = attributeDescription;
				displayedAttribute.setDisplayName(filter.getDisplayName());
				filterToDisplayName.put(filter.getInternalName(), filter
						.getDisplayName());
			} else {
				displayedAttribute = QueryConfigUtils
						.getReferencedAttributeDescription(martService,
								dataset, attributeDescription, version);
				if (displayedAttribute == null) {
					// if the reference can't be resolved the the attribute just
					// doesn't get displayed
					return null;
				}
			}
		} else {
			displayedAttribute = attributeDescription;
		}

		final AttributeComponent component = new AttributeComponent(
				displayedAttribute, martDataset, dataArray[0]);
		component.setPointerDataset(attributeDescription
				.getAttribute("pointerDataset"));
		if (!QueryConfigUtils.isFilterReference(attributeDescription, version)) {
			if (!attributeNameToComponentMap.containsKey(component
					.getQualifiedName())) {
				attributeNameToComponentMap.put(component.getQualifiedName(),
						new ArrayList());
			}
			((List) attributeNameToComponentMap.get(component
					.getQualifiedName())).add(component);
			componentRegister.add(component);
			// nasty hard coded rules that aren't in the configs
			// component.addQueryComponentListener(new QueryComponentAdapter() {
			// public void attributeAdded(QueryComponentEvent event) {
			// String name = component.getName();
			// String dataset = component.getDataset().getName();
			// if (name.equals("coding_gene_flank")
			// || name.equals("coding_transcript_flank")
			// || name.equals("transcript_flank")
			// || name.equals("gene_flank")) {
			// QueryComponent filterComponent = (QueryComponent)
			// filterNameToComponentMap
			// .get("upstream_flank");
			// if (filterComponent != null) {
			// filterComponent.setSelected(true);
			// }
			// filterComponent = (QueryComponent) filterNameToComponentMap
			// .get("downstream_flank");
			// if (filterComponent != null) {
			// filterComponent.setSelected(true);
			// }
			// }
			// }
			//
			// });
		}
		if (dataArray[1] instanceof JLabel) {
			final JLabel sequenceLabel = (JLabel) dataArray[1];
			component.addQueryComponentListener(new QueryComponentAdapter() {
				public void attributeAdded(QueryComponentEvent event) {
					String name = component.getName();
					if ("3utr".equals(name)) {
						sequenceLabel.setIcon(MartServiceIcons
								.getIcon("gene_schematic_3utr"));
					} else if ("5utr".equals(name)) {
						sequenceLabel.setIcon(MartServiceIcons
								.getIcon("gene_schematic_5utr"));
					} else if ("cdna".equals(name)) {
						sequenceLabel.setIcon(MartServiceIcons
								.getIcon("gene_schematic_cdna"));
					} else if ("coding_gene_flank".equals(name)) {
						sequenceLabel.setIcon(MartServiceIcons
								.getIcon("gene_schematic_coding_gene_flank"));
					} else if ("coding_transcript_flank".equals(name)) {
						sequenceLabel
								.setIcon(MartServiceIcons
										.getIcon("gene_schematic_coding_transcript_flank"));
					} else if ("coding".equals(name)) {
						sequenceLabel.setIcon(MartServiceIcons
								.getIcon("gene_schematic_coding"));
					} else if ("gene_exon_intron".equals(name)) {
						sequenceLabel.setIcon(MartServiceIcons
								.getIcon("gene_schematic_gene_exon_intron"));
					} else if ("gene_exon".equals(name)) {
						sequenceLabel.setIcon(MartServiceIcons
								.getIcon("gene_schematic_gene_exon"));
					} else if ("gene_flank".equals(name)) {
						sequenceLabel.setIcon(MartServiceIcons
								.getIcon("gene_schematic_gene_flank"));
					} else if ("peptide".equals(name)) {
						sequenceLabel.setIcon(MartServiceIcons
								.getIcon("gene_schematic_peptide"));
					} else if ("transcript_exon_intron".equals(name)) {
						sequenceLabel
								.setIcon(MartServiceIcons
										.getIcon("gene_schematic_transcript_exon_intron"));
					} else if ("transcript_exon".equals(name)) {
						sequenceLabel.setIcon(MartServiceIcons
								.getIcon("gene_schematic_transcript_exon"));
					} else if ("transcript_flank".equals(name)) {
						sequenceLabel.setIcon(MartServiceIcons
								.getIcon("gene_schematic_transcript_flank"));
					}
				}

			});
		}
		return component;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeListsUI(org.ensembl.mart.lib.config.AttributeList[],
	 *      int)
	 */
	public Component getAttributeListsUI(AttributeList[] attributeLists,
			Object data) throws MartServiceException {
		Object[] dataArray = (Object[]) data;
		JComponent box = new JPanel(new GridLayout(1, 2));
		box.setBackground(componentBackgroundColor);
		JComponent box1 = createVerticalBox(componentBackgroundColor);
		JComponent box2 = createVerticalBox(componentBackgroundColor);
		box.add(box1);
		box.add(box2);

		// button group used if the attribute collection is SINGLE_SELECTION
		ButtonGroup buttonGroup = new ButtonGroup();
		JRadioButton off = new JRadioButton("OFF");
		buttonGroup.add(off);

		JComponent currentBox = box1;

		for (int i = 0; i < attributeLists.length; i++) {
			if (QueryConfigUtils.display(attributeLists[i])) {
				Component component = getAttributeListUI(attributeLists[i],
						dataArray[0] == SINGLE_SELECTION ? new Object[] { off,
								dataArray[1] } : new Object[] { null,
								dataArray[1] });
				if (component != null) {
					currentBox.add(component);
					if (dataArray[0] == SINGLE_SELECTION
							&& component instanceof AttributeComponent) {
						AttributeComponent attributeComponent = (AttributeComponent) component;
						buttonGroup.add(attributeComponent.getButton());
					}
					if (currentBox == box1) {
						currentBox = box2;
					} else {
						currentBox = box1;
					}
				}
			}
		}
		currentBox.add(Box.createVerticalGlue());
		return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeListUI(org.ensembl.mart.lib.config.AttributeList,
	 *      int)
	 */
	public Component getAttributeListUI(AttributeList attributeList, Object data)
			throws MartServiceException {
		Object[] dataArray = (Object[]) data;

		AttributeComponent component = new AttributeComponent(attributeList,
				martDataset, dataArray[0]);

		if (!attributeNameToComponentMap.containsKey(component
				.getQualifiedName())) {
			attributeNameToComponentMap.put(component.getQualifiedName(),
					new ArrayList());
		}
		((List) attributeNameToComponentMap.get(component.getQualifiedName()))
				.add(component);
		componentRegister.add(component);
		/*
		 * if (dataArray[1] instanceof JLabel) { final JLabel sequenceLabel =
		 * (JLabel) dataArray[1]; component.addQueryComponentListener(new
		 * QueryComponentAdapter() { public void
		 * attributeAdded(QueryComponentEvent event) { String name =
		 * component.getName(); if ("3utr".equals(name)) {
		 * sequenceLabel.setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_3utr")); } else if ("5utr".equals(name)) {
		 * sequenceLabel.setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_5utr")); } else if ("cdna".equals(name)) {
		 * sequenceLabel.setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_cdna")); } else if
		 * ("coding_gene_flank".equals(name)) {
		 * sequenceLabel.setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_coding_gene_flank")); } else if
		 * ("coding_transcript_flank".equals(name)) { sequenceLabel
		 * .setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_coding_transcript_flank")); } else if
		 * ("coding".equals(name)) { sequenceLabel.setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_coding")); } else if
		 * ("gene_exon_intron".equals(name)) {
		 * sequenceLabel.setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_gene_exon_intron")); } else if
		 * ("gene_exon".equals(name)) { sequenceLabel.setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_gene_exon")); } else if
		 * ("gene_flank".equals(name)) { sequenceLabel.setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_gene_flank")); } else if
		 * ("peptide".equals(name)) { sequenceLabel.setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_peptide")); } else if
		 * ("transcript_exon_intron".equals(name)) { sequenceLabel
		 * .setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_transcript_exon_intron")); } else if
		 * ("transcript_exon".equals(name)) {
		 * sequenceLabel.setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_transcript_exon")); } else if
		 * ("transcript_flank".equals(name)) {
		 * sequenceLabel.setIcon(MartServiceIcons
		 * .getIcon("gene_schematic_transcript_flank")); } }
		 * 
		 * }); }
		 */
		return component;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterPagesUI(org.ensembl.mart.lib.config.FilterPage[])
	 */
	public Component getFilterPagesUI(FilterPage[] filterPages, Object data)
			throws MartServiceException {
		final JComponent box = createVerticalBox(backgroundColor);

		for (int i = 0; i < filterPages.length; i++) {
			if (QueryConfigUtils.display(filterPages[i])) {
				box.add(getFilterPageUI(filterPages[i], data));
			}
		}

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(backgroundColor);
		panel.add(box, BorderLayout.NORTH);

		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterPageUI(org.ensembl.mart.lib.config.FilterPage)
	 */
	public Component getFilterPageUI(FilterPage filterPage, Object data)
			throws MartServiceException {
		JComponent box = createVerticalBox(backgroundColor);

		FilterGroup[] filterGroups = (FilterGroup[]) filterPage
				.getFilterGroups().toArray(new FilterGroup[0]);
		box.add(getFilterGroupsUI(filterGroups, data));

		return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterGroupsUI(org.ensembl.mart.lib.config.FilterGroup[])
	 */
	public Component getFilterGroupsUI(FilterGroup[] filterGroups, Object data)
			throws MartServiceException {
		JComponent box = createVerticalBox(backgroundColor);

		for (int i = 0; i < filterGroups.length; i++) {
			if (QueryConfigUtils.display(filterGroups[i])) {
				box.add(Box.createVerticalStrut(2));
				box.add(getFilterGroupUI(filterGroups[i], data));
			}
		}

		return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterGroupUI(org.ensembl.mart.lib.config.FilterGroup)
	 */
	public Component getFilterGroupUI(FilterGroup filterGroup, Object data)
			throws MartServiceException {
		JLabel title = new JLabel(filterGroup.getDisplayName());
		title.setFont(title.getFont().deriveFont(Font.PLAIN));
		String description = filterGroup.getDescription();
		if (description != null) {
			title.setToolTipText(description);
		}
		ExpandableBox box = new ExpandableBox(title, componentBackgroundColor,
				borderColor);

		FilterCollection[] filterCollections = filterGroup
				.getFilterCollections();
		box.add(getFilterCollectionsUI(filterCollections, data));

		box.setExpanded(false);

		return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterCollectionsUI(org.ensembl.mart.lib.config.FilterCollection[])
	 */
	public Component getFilterCollectionsUI(
			FilterCollection[] filterCollections, Object data)
			throws MartServiceException {
		JComponent box = createVerticalBox(componentBackgroundColor);

		for (int i = 0; i < filterCollections.length; i++) {
			if (QueryConfigUtils.display(filterCollections[i])) {
				Component component = getFilterCollectionUI(
						filterCollections[i], data);
				if (component != null) {
					box.add(Box.createVerticalStrut(10));
					box.add(component);
				}
			}
		}

		return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterCollectionUI(org.ensembl.mart.lib.config.FilterCollection)
	 */
	public Component getFilterCollectionUI(FilterCollection filterCollection,
			Object data) throws MartServiceException {
		JComponent box = null;

		String displayName = filterCollection.getDisplayName();
		if (displayName == null) {
			displayName = filterCollection.getInternalName();
		}
		AbstractButton selectorButton = new JCheckBox(QueryConfigUtils
				.splitSentence(displayName));
		selectorButton.setFont(selectorButton.getFont().deriveFont(Font.PLAIN));
		selectorButton.setBackground(componentBackgroundColor);
		String description = filterCollection.getDescription();
		if (description != null) {
			selectorButton.setToolTipText(description);
		}

		FilterDescription[] filterDescriptions = (FilterDescription[]) filterCollection
				.getFilterDescriptions().toArray(new FilterDescription[0]);

		if (filterDescriptions.length == 1) {
			if (QueryConfigUtils.display(filterDescriptions[0])) {
				Component filterComponent = getFilterDescriptionUI(
						filterDescriptions[0], data);
				filterToDisplayName.put(
						filterDescriptions[0].getInternalName(), displayName);
				if (QueryConfigUtils
						.isReference(filterDescriptions[0], version)) {
					MartDataset dataset = QueryConfigUtils
							.getReferencedDataset(martService, martDataset,
									filterDescriptions[0], version);
					FilterDescription referencedFilter = QueryConfigUtils
							.getReferencedFilterDescription(martService,
									dataset, filterDescriptions[0], version);
					filterToDisplayName.put(referencedFilter.getInternalName(),
							displayName);
				}

				box = createBox(null, false);
				JComponent grid = new JPanel(new GridLayout(1, 2));
				grid.setBackground(componentBackgroundColor);
				JPanel buttonPanel = new JPanel(new MinimalLayout05());
				buttonPanel.setBackground(componentBackgroundColor);
				buttonPanel.add(selectorButton);
				grid.add(buttonPanel);
				if (filterComponent instanceof QueryComponent) {
					((QueryComponent) filterComponent)
							.setSelectorButton(selectorButton);
				}
				grid.add(filterComponent);
				box.add(grid);
			}
		} else {
			Component component = getFilterDescriptionsUI(filterDescriptions,
					selectorButton);
			if (component != null) {
				box = createBox(selectorButton, false);
				box.add(component);
			}
		}

		return box;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterDescriptionsUI(org.ensembl.mart.lib.config.FilterDescription[],
	 *      int)
	 */
	public Component getFilterDescriptionsUI(
			FilterDescription[] filterDescriptions, Object data)
			throws MartServiceException {
		List components = new ArrayList();
		for (int i = 0; i < filterDescriptions.length; i++) {
			if (QueryConfigUtils.display(filterDescriptions[i])) {
				Component component = getFilterDescriptionUI(
						filterDescriptions[i], data);
				if (component instanceof QueryComponent
						&& data instanceof AbstractButton) {
					((QueryComponent) component)
							.setSelectorButton((AbstractButton) data);
				}

				String displayName = filterDescriptions[i].getDisplayName();
				if (displayName == null) {
					System.out.println("Cant find a display name for filter '"
							+ filterDescriptions[i].getInternalName() + "'");
					displayName = filterDescriptions[i].getInternalName();
				}
				filterToDisplayName.put(
						filterDescriptions[i].getInternalName(), displayName);
				JLabel displayLabel = new JLabel(QueryConfigUtils
						.splitSentence(displayName));
				displayLabel.setFont(displayLabel.getFont().deriveFont(
						Font.PLAIN));
				String description = filterDescriptions[i].getDescription();
				if (description != null) {
					displayLabel.setToolTipText(description);
				}
				displayLabel.setBackground(componentBackgroundColor);
				displayLabel.setBorder(new EmptyBorder(0, 22, 0, 0));

				components.add(displayLabel);
				components.add(component);
			}
		}

		if (components.size() > 0) {
			JComponent box = new JPanel(
					new GridLayout(components.size() / 2, 2));
			box.setBackground(componentBackgroundColor);
			for (Iterator iter = components.iterator(); iter.hasNext();) {
				box.add((Component) iter.next());
			}
			return box;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterDescriptionUI(org.ensembl.mart.lib.config.FilterDescription,
	 *      int)
	 */
	public Component getFilterDescriptionUI(
			FilterDescription filterDescription, Object data)
			throws MartServiceException {
		QueryComponent component;

		String pointerDataset = filterDescription
				.getAttribute("pointerDataset");
		MartDataset dataset = martDataset;
		FilterDescription displayedFilter;
		if (QueryConfigUtils.isReference(filterDescription, version)) {
			dataset = QueryConfigUtils.getReferencedDataset(martService,
					martDataset, filterDescription, version);
			displayedFilter = QueryConfigUtils.getReferencedFilterDescription(
					martService, dataset, filterDescription, version);
			filterDescription.setDisplayName(displayedFilter.getDisplayName());
		} else {
			displayedFilter = filterDescription;
		}
		assert dataset != null;

		String type = displayedFilter.getType();
		if (type == null) {
			type = "text";// default filter type
		}

		if (type.equals("boolean") || type.equals("boolean_num")) {
			component = new BooleanFilterComponent(displayedFilter, martDataset);
			component.setPointerDataset(pointerDataset);
			componentRegister.add(component);
		} else if (type.endsWith("list") || type.endsWith("basic_filter")
				|| QueryConfigUtils.isList(displayedFilter)) {
			if (type.equals("boolean_list")
					|| QueryConfigUtils.isBooleanList(displayedFilter)) {
				Option[] options = displayedFilter.getOptions();
				List filters = new ArrayList();
				for (int i = 0; i < options.length; i++) {
					FilterDescription booleanFilterDescription = new FilterDescription(
							options[i]);
					QueryComponent queryComponent = new BooleanFilterComponent(
							booleanFilterDescription, martDataset);
					queryComponent.setPointerDataset(pointerDataset);
					filters.add(queryComponent);
					componentRegister.add(queryComponent);
				}
				component = new BooleanListFilterComponent(displayedFilter,
						martDataset, filters);
				component.setPointerDataset(pointerDataset);
			} else if (type.equals("id_list")
					|| QueryConfigUtils.isIdList(displayedFilter)) {
				Option[] options = displayedFilter.getOptions();
				List filters = new ArrayList();
				for (int i = 0; i < options.length; i++) {
					FilterDescription idFilterDescription = new FilterDescription(
							options[i]);
					idFilterDescription.setType("id_list");
					QueryComponent queryComponent = new TextFilterComponent(
							idFilterDescription, martDataset);
					queryComponent.setPointerDataset(pointerDataset);
					filters.add(queryComponent);
					componentRegister.add(queryComponent);
				}
				component = new IdListFilterComponent(displayedFilter,
						martDataset, filters);
				component.setPointerDataset(pointerDataset);
			} else if (QueryConfigUtils.isNestedList(displayedFilter)) {
				TextFilterComponent filterComponent = new TextFilterComponent(
						displayedFilter, martDataset);
				filterComponent.setPointerDataset(pointerDataset);
				filterComponent.add(QueryConfigUtils.getOptionButton(
						displayedFilter, filterComponent));
				component = filterComponent;
				componentRegister.add(component);
			} else {
				ListFilterComponent filterComponent = new ListFilterComponent(
						getDatasetConfig().getDataset(), displayedFilter,
						martDataset, filterNameToComponentMap);
				filterComponent.setPointerDataset(pointerDataset);
				// map the component to a local dataset name as the 'ref' of a
				// pushaction may be a local reference
				filterNameToComponentMap.put(getDatasetConfig().getDataset()
						+ "." + displayedFilter.getInternalName(),
						filterComponent);
				// if the filter is a reference then also map the component to
				// its referenced dataset name as the 'ref' of a pushaction may
				// be a non-local reference
				if (QueryConfigUtils.isReference(filterDescription, version)) {
					filterNameToComponentMap.put(filterDescription
							.getInternalName(), filterComponent);
				}
				component = filterComponent;
				componentRegister.add(component);
			}
		} else {
			component = new TextFilterComponent(displayedFilter, martDataset);
			component.setPointerDataset(pointerDataset);
			componentRegister.add(component);
			// mapping for hard coded rules
			filterNameToComponentMap.put(filterDescription.getInternalName(),
					component);
		}

		return component;
	}

	private void registerComponents() {
		for (Iterator iter = componentRegister.iterator(); iter.hasNext();) {
			QueryComponent component = (QueryComponent) iter.next();
			controller.register(component);
		}
	}

	private void deregisterComponents() {
		for (Iterator iter = componentRegister.iterator(); iter.hasNext();) {
			QueryComponent component = (QueryComponent) iter.next();
			controller.deregister(component);
		}
	}

	/**
	 * 
	 * @param inputPanel
	 * @param attributePanel
	 * @param filterPanel
	 */
	private void generateConfiguration(
			final MartServiceQueryConfigUIFactory05 factory,
			final SummaryPanel summaryPanel, final JComponent inputPanel,
			final JComponent attributePanel, final JComponent filterPanel) {
		final JProgressBar filterProgressBar = new JProgressBar();
		filterProgressBar.setIndeterminate(true);
		filterProgressBar.setStringPainted(true);
		filterProgressBar.setString("Fetching filter configuration");
		filterPanel.add(filterProgressBar);

		final JProgressBar attributeProgressBar = new JProgressBar();
		attributeProgressBar.setIndeterminate(true);
		attributeProgressBar.setStringPainted(true);
		attributeProgressBar.setString("Fetching attribute configuration");
		attributePanel.add(attributeProgressBar);

		new Thread("DatasetConfigUI") {
			public void run() {
				try {

					FilterPage[] filterPages = factory.getDatasetConfig()
							.getFilterPages();
					AttributePage[] attributePages = factory.getDatasetConfig()
							.getAttributePages();

					final Component filterPagesComponent = factory
							.getFilterPagesUI(filterPages, null);
					final Component attributePagesComponent = factory
							.getAttributePagesUI(attributePages, null);

					filterPanel.remove(filterProgressBar);
					filterPanel.add(filterPagesComponent);

					attributePanel.remove(attributeProgressBar);
					attributePanel.add(attributePagesComponent);

					factory.registerComponents();

					factory.selectAttributePage(attributePages);

				} catch (MartServiceException e) {
					e.printStackTrace();
					JTextArea textArea = new JTextArea();
					textArea
							.append("Error while fetching dataset configuration\n\n");
					textArea.append(e.getMessage());
					inputPanel.removeAll();
					inputPanel.add(textArea);
				} catch (Exception e) {
					e.printStackTrace();
					JTextArea textArea = new JTextArea();
					textArea
							.append("Error while generating the Query Editor\n\n");
					textArea.append(e.toString());
					inputPanel.removeAll();
					inputPanel.add(textArea);
				} finally {
					inputPanel.revalidate();
					inputPanel.repaint();
					summaryPanel.updateDatasets();
				}
			}
		}.start();
	}

	private List getAttributeComponents(Component component) {
		List attributeComponents = new ArrayList();
		if (component instanceof AttributeComponent) {
			attributeComponents.add(component);
		} else if (component instanceof ExpandableBox) {
			Component[] children = ((ExpandableBox) component)
					.getComponents();
			for (int i = 0; i < children.length; i++) {
				attributeComponents.addAll(getAttributeComponents(children[i]));
			}
		} else if (component instanceof Container) {
			Component[] children = ((Container) component).getComponents();
			for (int i = 0; i < children.length; i++) {
				attributeComponents.addAll(getAttributeComponents(children[i]));
			}
		}
		return attributeComponents;
	}

	private List getSelectedAttributeComponents(Component component) {
		List attributeComponents = new ArrayList();
		if (component instanceof AttributeComponent) {
			if (((AttributeComponent) component).isSelected()) {
				attributeComponents.add(component);
			}
		} else if (component instanceof ExpandableBox) {
			Component[] children = ((ExpandableBox) component)
					.getComponents();
			for (int i = 0; i < children.length; i++) {
				attributeComponents
						.addAll(getSelectedAttributeComponents(children[i]));
			}
		} else if (component instanceof Container) {
			Component[] children = ((Container) component).getComponents();
			for (int i = 0; i < children.length; i++) {
				attributeComponents
						.addAll(getSelectedAttributeComponents(children[i]));
			}
		}
		return attributeComponents;
	}

	private void selectAttributePage(AttributePage[] attributePages) {
		int selectedAttributes = -1;
		JRadioButton selectedButton = null;
		Component selectedComponent = null;

		for (int i = 0; i < attributePages.length; i++) {
			if (QueryConfigUtils.display(attributePages[i])) {
				Component component = (Component) attributePageNameToComponent
						.get(attributePages[i].getInternalName());
				JRadioButton button = (JRadioButton) attributePageNameToButton
						.get(attributePages[i].getInternalName());
				if (component != null && button != null) {
					int attributeCount = getSelectedAttributeComponents(
							component).size();
					if (attributeCount > selectedAttributes) {
						selectedAttributes = attributeCount;
						selectedButton = button;
						selectedComponent = component;
					}
				}
			}
			if (selectedButton != null && selectedComponent != null) {
				selectedButton.setSelected(true);
			}
		}
	}

	private JComponent createHorizontalBox(Color background) {
		// using a JPanel instead of a Box as a workaround for bug 4907674
		JPanel box = new JPanel();
		box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
		box.setBackground(background);
		return box;
	}

	private JComponent createVerticalBox(Color background) {
		// using a JPanel instead of a Box as a workaround for bug 4907674
		JPanel box = new JPanel();
		box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
		box.setBackground(background);
		return box;
	}

	private JComponent createBox(Component titleComponent, boolean fullBorder) {
		JComponent box = createVerticalBox(componentBackgroundColor);
		box.add(Box.createHorizontalStrut(400));
		if (fullBorder) {
			box.setBorder(new CompoundBorder(new LineBorder(borderColor, 1),
					new EmptyBorder(10, 10, 10, 10)));
		} else {
			box.setBorder(new CompoundBorder(new SideBorder(SwingConstants.TOP,
					borderColor), new EmptyBorder(5, 10, 0, 10)));
		}
		if (titleComponent != null) {
			JComponent labelBox = createHorizontalBox(componentBackgroundColor);
			labelBox.add(titleComponent);
			labelBox.add(Box.createHorizontalGlue());
			box.add(labelBox);
		}
		return box;
	}

	private void setSummaryCount(Query query, String datasetName, JLabel label)
			throws MartServiceException {
		if ("".equals(label.getText())) {
			String count = null;
			String total = null;
			Query countQuery = new Query(query);
			countQuery.removeAllDatasets();
			countQuery.addDataset(new Dataset(datasetName));
			countQuery.setCount(1);

			List[] results = martService.executeQuery(countQuery);
			if (results.length == 1 && results[0].size() >= 1) {
				total = (String) results[0].get(0);
				// test for biomart's 'let add a random blank line'
				// thing
				if ("".equals(total) && results[0].size() > 1) {
					total = (String) results[0].get(1);
				}
				try {
					Integer.parseInt(total);
				} catch (NumberFormatException e) {
					total = "?";
				}
			}
			Dataset dataset = query.getDataset(datasetName);
			if (dataset != null && dataset.getFilters().size() > 0) {
				Dataset countDataset = new Dataset(dataset);
				countQuery.removeAllDatasets();
				countQuery.addDataset(countDataset);
				results = martService.executeQuery(countQuery);
				if (results.length == 1 && results[0].size() >= 1) {
					count = (String) results[0].get(0);
					// test for biomart's 'let add a random blank
					// line' thing
					if ("".equals(count) && results[0].size() > 1) {
						count = (String) results[0].get(1);
					}
					try {
						Integer.parseInt(count);
					} catch (NumberFormatException e) {
						count = "";
					}
				}
			} else {
				count = total;
			}

			if (count != null && total != null) {
				if (count.equals("")) {
					label.setText("0 / " + total + " Genes");
				} else {
					label.setText(count + " / " + total + " Genes");
				}
			}

		}
	}

	class TextFilterComponent extends QueryComponent {
		private static final long serialVersionUID = 1L;

		private JTextField textField;

		public TextFilterComponent(FilterDescription filterDescription,
				MartDataset dataset) {
			setConfigObject(filterDescription);
			setDataset(dataset);
			setName(filterDescription.getInternalName());
			setLayout(new MinimalLayout05(MinimalLayout05.HORIZONTAL));
			setBackground(componentBackgroundColor);

			textField = new JTextField();
			textField.setBackground(componentBackgroundColor);
			textField.setPreferredSize(new Dimension(200, textField
					.getPreferredSize().height + 4));

			textField.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
				}

				public void insertUpdate(DocumentEvent e) {
					fireFilterChanged(new QueryComponentEvent(this, getName(),
							getDataset(), textField.getText()));
				}

				public void removeUpdate(DocumentEvent e) {
					fireFilterChanged(new QueryComponentEvent(this, getName(),
							getDataset(), textField.getText()));
				}
			});

			add(textField);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryComponent#getType()
		 */
		public int getType() {
			return FILTER;
		}

		public void setValue(String value) {
			textField.setText(value);
		}

		public String getValue() {
			return textField.getText();
		}

	}

	class ListFilterComponent extends QueryComponent {
		private static final long serialVersionUID = 1L;

		private DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();

		private JComboBox comboBox;

		private JTextField textField;

		private Map optionMap = new HashMap();

		private List optionList = new ArrayList();

		private String dataset;

		private Map componentMap;

		private String type;

		public ListFilterComponent(String refDataset,
				FilterDescription filterDescription, MartDataset dataset,
				Map componentMap) {
			this.dataset = refDataset;
			this.componentMap = componentMap;
			setConfigObject(filterDescription);
			setDataset(dataset);
			setName(filterDescription.getInternalName());
			setLayout(new BorderLayout());
			setBackground(componentBackgroundColor);

			Option[] options = filterDescription.getOptions();
			// if there are no options but there is a default value then use the
			// default value as an option
			if (options.length == 0) {
				String defaultValue = filterDescription.getDefaultValue();
				if (defaultValue != null) {
					Option newOption = new Option();
					newOption.setInternalName(defaultValue);
					newOption.setDisplayName(defaultValue);
					newOption.setValue(defaultValue);
					newOption.setSelectable("true");
					options = new Option[] { newOption };
				}
			}

			textField = new JTextField();
			textField.setBackground(componentBackgroundColor);
			textField.setPreferredSize(new Dimension(200, textField
					.getPreferredSize().height + 4));

			textField.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					fireFilterChanged(new QueryComponentEvent(this, getName(),
							getDataset(), textField.getText()));
				}

				public void insertUpdate(DocumentEvent e) {
					fireFilterChanged(new QueryComponentEvent(this, getName(),
							getDataset(), textField.getText()));
				}

				public void removeUpdate(DocumentEvent e) {
					fireFilterChanged(new QueryComponentEvent(this, getName(),
							getDataset(), textField.getText()));
				}
			});

			comboBox = new JComboBox();
			comboBox.setFont(comboBox.getFont().deriveFont(Font.PLAIN));
			comboBox.setBackground(componentBackgroundColor);
			comboBox.setModel(comboBoxModel);

			if (options.length == 0) {
				add(textField, BorderLayout.WEST);
				type = "text";
			} else {
				add(comboBox, BorderLayout.WEST);
				type = "list";
			}

			setOptions(options);
			// comboBox.setSelectedIndex(-1);

			comboBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						Option option = (Option) optionList.get(comboBox
								.getSelectedIndex());
						optionSelected(option);
						ListFilterComponent.super.setValue(option.getValue());
						fireFilterChanged(new QueryComponentEvent(this,
								getName(), getDataset(), option.getValue()));
					} else if (e.getStateChange() == ItemEvent.DESELECTED) {
						Option option = (Option) optionMap.get(getValue());
						if (option != null) {
							optionDeselected(option);
						}
						fireFilterChanged(new QueryComponentEvent(this,
								getName(), getDataset(), ""));
					}
				}

			});

		}

		public void setOptions(Option[] options) {
			clearOptions();
			for (int i = 0; i < options.length; i++) {
				optionMap.put(options[i].getValue(), options[i]);
				optionList.add(options[i]);
				String displayName = options[i].getDisplayName();
				if (displayName != null) {
					comboBoxModel.addElement(QueryConfigUtils
							.truncateName(displayName));
				}
			}
			if (options.length > 0) {
				if ("text".equals(type)) {
					removeAll();
					add(comboBox, BorderLayout.WEST);
					type = "list";
					revalidate();
					repaint();
				}
				setValue(options[0].getValue());
			} else {
				if ("list".equals(type)) {
					removeAll();
					add(textField, BorderLayout.WEST);
					type = "text";
					revalidate();
					repaint();
				}
			}
		}

		private void clearOptions() {
			comboBox.setSelectedIndex(-1);
			comboBoxModel.removeAllElements();
			optionMap.clear();
			optionList.clear();
		}

		public void setValue(String value) {
			if ("list".equals(type)) {
				if (value == null) {
					if (getValue() != null) {
						optionDeselected((Option) optionMap.get(getValue()));
						comboBox.setSelectedIndex(-1);
					}
				} else {
					if (getValue() != null) {
						Option option = (Option) optionMap.get(getValue());
						if (option != null) {
							optionDeselected(option);
						}
					}
					Option option = (Option) optionMap.get(getValue());
					if (option != null) {
						optionSelected((Option) optionMap.get(value));
					}
					int index = optionList.indexOf(optionMap.get(value));
					comboBox.setSelectedIndex(index);
				}
			} else if ("text".equals(type)) {
				textField.setText(value);
			}
			super.setValue(value);
		}

		private void optionSelected(Option option) {
			if (option == null) {
				System.out.println("null option for " + getName());
			} else {
				PushAction[] pushActions = option.getPushActions();
				for (int i = 0; i < pushActions.length; i++) {
					QueryComponent queryComponent = getReferencedComponent(pushActions[i]);
					if (queryComponent instanceof ListFilterComponent) {
						ListFilterComponent filterComponent = (ListFilterComponent) queryComponent;
						if (filterComponent != null) {
							filterComponent.setOptions(pushActions[i]
									.getOptions());
						}
					}
				}
			}
		}

		private void optionDeselected(Option option) {
			PushAction[] pushActions = option.getPushActions();
			for (int i = 0; i < pushActions.length; i++) {
				QueryComponent queryComponent = getReferencedComponent(pushActions[i]);
				if (queryComponent instanceof ListFilterComponent) {
					ListFilterComponent filterComponent = (ListFilterComponent) queryComponent;
					if (filterComponent != null) {
						filterComponent.clearOptions();
					}
				}
			}
		}

		private QueryComponent getReferencedComponent(PushAction pushAction) {
			String ref = pushAction.getRef();
			if (ref.indexOf('.') == -1) {
				return (QueryComponent) componentMap.get(dataset + "." + ref);
			} else {
				return (QueryComponent) componentMap.get(ref);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryComponent#getType()
		 */
		public int getType() {
			return FILTER;
		}
	}

	class IdListFilterComponent extends QueryComponent {
		private static final long serialVersionUID = 1L;

		private DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();

		private JComboBox comboBox;

		private JTextArea textArea;

		private Map componentMap = new HashMap();

		private List filterList = new ArrayList();

		private int currentIndex;

		private boolean valueChanging;

		public IdListFilterComponent(FilterDescription description,
				MartDataset dataset, List filterComponentList) {
			setLayout(new MinimalLayout05(MinimalLayout05.VERTICAL));
			setBackground(componentBackgroundColor);

			comboBox = new JComboBox();
			comboBox.setFont(comboBox.getFont().deriveFont(Font.PLAIN));
			comboBox.setBackground(componentBackgroundColor);
			comboBox.setModel(comboBoxModel);

			for (Iterator iter = filterComponentList.iterator(); iter.hasNext();) {
				TextFilterComponent filterComponent = (TextFilterComponent) iter
						.next();
				BaseNamedConfigurationObject filterDescription = filterComponent
						.getConfigObject();
				componentMap.put(filterDescription.getInternalName(),
						filterComponent);
				filterList.add(filterDescription.getInternalName());
				comboBoxModel.addElement(filterDescription.getDisplayName());
				filterToDisplayName.put(filterDescription.getInternalName(),
						filterDescription.getDisplayName());
				filterComponent
						.addQueryComponentListener(new QueryComponentAdapter() {
							public void filterAdded(QueryComponentEvent event) {
								if (!valueChanging) {
									valueChanging = true;
									comboBox.setSelectedIndex(filterList
											.indexOf(event.getName()));
									selectorButton.setSelected(true);
									valueChanging = false;
								}
							}

							public void filterRemoved(QueryComponentEvent event) {
								if (!valueChanging) {
									valueChanging = true;
									selectorButton.setSelected(false);
									valueChanging = false;
								}
							}

							public void filterChanged(QueryComponentEvent event) {
								if (!valueChanging) {
									valueChanging = true;
									textArea
											.setText(QueryConfigUtils
													.csvToValuePerLine(event
															.getValue()));
									valueChanging = false;
								}
							}
						});
				filterComponent.setSelectorButton(new JCheckBox());
			}

			comboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					int selectedIndex = comboBox.getSelectedIndex();
					if (selectorButton.isSelected()) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							if (!valueChanging) {
								valueChanging = true;
								QueryComponent queryComponent = (QueryComponent) componentMap
										.get(filterList.get(selectedIndex));
								queryComponent.setValue(QueryConfigUtils
										.valuePerLineToCsv(textArea.getText()));
								queryComponent.setSelected(true);
								valueChanging = false;
							}
						} else if (e.getStateChange() == ItemEvent.DESELECTED) {
							if (!valueChanging) {
								valueChanging = true;
								((QueryComponent) componentMap.get(filterList
										.get(currentIndex))).setSelected(false);
								valueChanging = false;
							}
						}
					}
					currentIndex = selectedIndex;
				}
			});

			textArea = new JTextArea();
			textArea.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
				}

				public void insertUpdate(DocumentEvent e) {
					updateValue();
				}

				public void removeUpdate(DocumentEvent e) {
					updateValue();
				}

				private void updateValue() {
					if (!valueChanging) {
						valueChanging = true;
						int selectedIndex = comboBox.getSelectedIndex();
						String value = QueryConfigUtils
								.valuePerLineToCsv(textArea.getText());
						((QueryComponent) componentMap.get(filterList
								.get(selectedIndex))).setValue(value);
						valueChanging = false;
					}
				}
			});

			final JFileChooser chooser = new JFileChooser();
			JButton chooserButton = new JButton("Browse...");
			chooserButton.setFont(chooserButton.getFont()
					.deriveFont(Font.PLAIN));
			chooserButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int returnVal = chooser
							.showOpenDialog(IdListFilterComponent.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						if (file != null && file.exists() && file.canRead()
								&& !file.isDirectory()) {
							StringBuffer buffer = new StringBuffer();
							BufferedReader in = null;
							try {
								in = new BufferedReader(new FileReader(file));
								String line = in.readLine();
								while (line != null) {
									buffer.append(line);
									buffer.append(QueryConfigUtils.LINE_END);
									line = in.readLine();
								}
							} catch (IOException e1) {
								// no action
							} finally {
								if (in != null) {
									try {
										in.close();
									} catch (IOException e1) {
										// give up
									}
								}
							}
							textArea.setText(buffer.toString());
						}
					}
				}
			});

			JPanel buttonPanel = new JPanel(new BorderLayout());
			buttonPanel.setBackground(componentBackgroundColor);
			buttonPanel.add(chooserButton, BorderLayout.WEST);

			JScrollPane textScrollPane = new JScrollPane(textArea);
			textScrollPane.setBackground(componentBackgroundColor);
			textScrollPane.setPreferredSize(new Dimension(200, 80));

			add(comboBox, BorderLayout.NORTH);
			add(textScrollPane, BorderLayout.CENTER);
			add(buttonPanel, BorderLayout.SOUTH);

		}

		public void setSelectorButton(AbstractButton button) {
			selectorButton = button;
			button.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					int selectedIndex = comboBox.getSelectedIndex();
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (!valueChanging) {
							valueChanging = true;
							QueryComponent queryComponent = (QueryComponent) componentMap
									.get(filterList.get(selectedIndex));
							queryComponent.setValue(QueryConfigUtils
									.valuePerLineToCsv(textArea.getText()));
							queryComponent.setSelected(true);
							valueChanging = false;
						}
					} else if (e.getStateChange() == ItemEvent.DESELECTED) {
						if (!valueChanging) {
							valueChanging = true;
							((QueryComponent) componentMap.get(filterList
									.get(selectedIndex))).setSelected(false);
							valueChanging = false;
						}
					}
				}
			});

		}

		public int getType() {
			return FILTER;
		}

	}

	class BooleanListFilterComponent extends QueryComponent {
		private static final long serialVersionUID = 1L;

		private DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();

		private JComboBox comboBox;

		private BooleanFilterComponent booleanFilterComponent;

		private Map componentMap = new HashMap();

		private List filterList = new ArrayList();

		private int currentIndex;

		public BooleanListFilterComponent(FilterDescription description,
				MartDataset dataset, List filterComponentList) {
			setLayout(new MinimalLayout05(MinimalLayout05.HORIZONTAL));
			setBackground(componentBackgroundColor);

			comboBox = new JComboBox();
			comboBox.setFont(comboBox.getFont().deriveFont(Font.PLAIN));
			comboBox.setBackground(componentBackgroundColor);
			comboBox.setModel(comboBoxModel);

			for (Iterator iter = filterComponentList.iterator(); iter.hasNext();) {
				BooleanFilterComponent filterComponent = (BooleanFilterComponent) iter
						.next();
				BaseNamedConfigurationObject filterDescription = filterComponent
						.getConfigObject();
				componentMap.put(filterDescription.getInternalName(),
						filterComponent);
				filterList.add(filterDescription.getInternalName());
				comboBoxModel.addElement(filterDescription.getDisplayName());
				filterToDisplayName.put(filterDescription.getInternalName(),
						filterDescription.getDisplayName());
				filterComponent
						.addQueryComponentListener(new QueryComponentAdapter() {
							public void filterAdded(QueryComponentEvent event) {
								comboBox.setSelectedIndex(filterList
										.indexOf(event.getName()));
								selectorButton.setSelected(true);
							}

							public void filterChanged(QueryComponentEvent event) {
								booleanFilterComponent.setValue(event
										.getValue());
							}
						});
				filterComponent.setSelectorButton(new JCheckBox());
			}

			comboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					int selectedIndex = comboBox.getSelectedIndex();
					if (selectorButton.isSelected()) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							QueryComponent queryComponent = (QueryComponent) componentMap
									.get(filterList.get(selectedIndex));
							queryComponent.setValue(booleanFilterComponent
									.getValue());
							queryComponent.setSelected(true);
						} else if (e.getStateChange() == ItemEvent.DESELECTED) {
							((QueryComponent) componentMap.get(filterList
									.get(currentIndex))).setSelected(false);
						}
					}
					currentIndex = selectedIndex;
				}
			});

			booleanFilterComponent = new BooleanFilterComponent(
					new FilterDescription(), dataset);
			booleanFilterComponent
					.addQueryComponentListener(new QueryComponentAdapter() {
						public void filterChanged(QueryComponentEvent event) {
							int selectedIndex = comboBox.getSelectedIndex();
							((QueryComponent) componentMap.get(filterList
									.get(selectedIndex))).setValue(event
									.getValue());
						}
					});

			JPanel comboBoxPanel = new JPanel(new MinimalLayout05());
			comboBoxPanel.setBackground(componentBackgroundColor);
			comboBoxPanel.setBorder(new EmptyBorder(5, 5, 0, 0));
			add(comboBox);
			add(booleanFilterComponent);
		}

		public void setSelectorButton(AbstractButton button) {
			selectorButton = button;
			button.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					int selectedIndex = comboBox.getSelectedIndex();
					if (e.getStateChange() == ItemEvent.SELECTED) {
						QueryComponent queryComponent = (QueryComponent) componentMap
								.get(filterList.get(selectedIndex));
						queryComponent.setValue(booleanFilterComponent
								.getValue());
						queryComponent.setSelected(true);
					} else if (e.getStateChange() == ItemEvent.DESELECTED) {
						((QueryComponent) componentMap.get(filterList
								.get(selectedIndex))).setSelected(false);
					}
				}
			});
		}

		public int getType() {
			return FILTER;
		}

	}

	class BooleanFilterComponent extends QueryComponent {
		private static final long serialVersionUID = 1L;

		private static final String ONLY = "only";

		private static final String EXCLUDED = "excluded";

		private ButtonGroup buttonGroup = new ButtonGroup();

		private JRadioButton only;

		private JRadioButton excluded;

		public BooleanFilterComponent(FilterDescription filterDescription,
				MartDataset dataset) {
			setConfigObject(filterDescription);
			setDataset(dataset);
			setName(filterDescription.getInternalName());
			setBackground(componentBackgroundColor);

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBackground(componentBackgroundColor);

			only = new JRadioButton("Only");
			only.setFont(only.getFont().deriveFont(Font.PLAIN));
			only.setBackground(componentBackgroundColor);
			only.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						fireFilterChanged(new QueryComponentEvent(this,
								getName(), getDataset(), ONLY));
					}
				}
			});
			buttonGroup.add(only);
			add(only);

			excluded = new JRadioButton("Excluded");
			excluded.setFont(excluded.getFont().deriveFont(Font.PLAIN));
			excluded.setBackground(componentBackgroundColor);
			excluded.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						fireFilterChanged(new QueryComponentEvent(this,
								getName(), getDataset(), EXCLUDED));
					}
				}
			});
			buttonGroup.add(excluded);
			add(excluded);

			setValue(filterDescription.getQualifier());
		}

		public int getType() {
			return FILTER;
		}

		public String getValue() {
			if (excluded.isSelected()) {
				return EXCLUDED;
			} else {
				return ONLY;
			}
		}

		public void setValue(String value) {
			if (EXCLUDED.equals(value)) {
				excluded.setSelected(true);
			} else {
				only.setSelected(true);
			}
		}

	}

	class AttributeComponent extends QueryComponent {
		private static final long serialVersionUID = 1L;

		private AbstractButton button;

		private AbstractButton offButton;

		public AttributeComponent(
				BaseNamedConfigurationObject attributeDescription,
				MartDataset dataset, Object offButton) {
			this.offButton = (AbstractButton) offButton;
			setConfigObject(attributeDescription);
			setDataset(dataset);
			setName(attributeDescription.getInternalName());
			if (attributeDescription instanceof AttributeList) {
				setValue(((AttributeList) attributeDescription).getAttributes());
			}
			setLayout(new BorderLayout());
			setBackground(componentBackgroundColor);
			// if there's no display name the attribute isn't displayed
			String displayName = attributeDescription.getDisplayName();
			if (displayName != null) {
				attributeToDisplayName.put(attributeDescription
						.getInternalName(), displayName);
				if (offButton != null) {
					button = new JRadioButton(QueryConfigUtils
							.splitSentence(displayName));
				} else {
					button = new JCheckBox(QueryConfigUtils
							.splitSentence(displayName));
				}
				button.setFont(button.getFont().deriveFont(Font.PLAIN));
				button.setBackground(componentBackgroundColor);
				setSelectorButton(button);

				String description = attributeDescription.getDescription();
				if (description != null) {
					button.setToolTipText(description);
				}

				add(button, BorderLayout.WEST);

				button.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (!settingAttributeState) {
							settingAttributeState = true;
							List attributes = (List) attributeNameToComponentMap
									.get(getQualifiedName());
							if (attributes != null) {
								for (Iterator iter = attributes.iterator(); iter
										.hasNext();) {
									AttributeComponent attribute = (AttributeComponent) iter
											.next();
									if (attribute != AttributeComponent.this) {
										if (e.getStateChange() == ItemEvent.SELECTED) {
											attribute.setSelected(true);
										} else if (e.getStateChange() == ItemEvent.DESELECTED) {
											attribute.setSelected(false);
										}
									}
								}
							}
							settingAttributeState = false;
						}
					}
				});
			}
		}

		public int getType() {
			return ATTRIBUTE;
		}

		public void setSelected(boolean selected) {
			if (offButton != null) {
				if (selected) {
					button.setSelected(true);
				} else {
					offButton.setSelected(true);
				}
			} else if (button != null) {
				button.setSelected(selected);
			}
		}

		public boolean isSelected() {
			return button != null ? button.isSelected() : false;
		}

		public AbstractButton getButton() {
			return button;
		}

	}

	class DatasetLinkComponent extends QueryComponent {
		private static final long serialVersionUID = 1L;

		private JComboBox datasetComboBox;

		private JProgressBar progressBar;

		private Map datasetToLink = new HashMap();

		private MartServiceQueryConfigUIFactory05 factory;

		public DatasetLinkComponent(final JComponent inputPanel,
				final SummaryPanel sumaryPanel) {

			final JComponent attributePanel = createVerticalBox(backgroundColor);
			attributePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

			final JComponent filterPanel = createVerticalBox(backgroundColor);
			filterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

			setBackground(backgroundColor);
			setLayout(new GridBagLayout());
			setBorder(new EmptyBorder(10, 10, 0, 10));

			progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
			progressBar.setStringPainted(true);
			progressBar.setString("Fetching dataset linking information");

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.anchor = GridBagConstraints.NORTHWEST;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;

			add(progressBar, constraints);

			datasetComboBox = new JComboBox();
			datasetComboBox.setFont(datasetComboBox.getFont().deriveFont(
					Font.PLAIN));
			datasetComboBox.addItem("NONE");
			datasetComboBox.setBackground(componentBackgroundColor);
			datasetComboBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					Object selectedItem = e.getItem();
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (selectedItem instanceof DatasetLink) {
							DatasetLink datasetLink = (DatasetLink) selectedItem;
							MartDataset dataset = datasetLink
									.getSourceDataset();
							try {
								MartQuery newQuery = new MartQuery(martService,
										dataset, controller.getMartQuery()
												.getQuery());
								QueryConfigController newController = new QueryConfigController(
										newQuery);
								factory = new MartServiceQueryConfigUIFactory05(
										martService, newController, dataset);
								sumaryPanel.setLinkedDataset(dataset, factory);

								attributePanel.removeAll();
								filterPanel.removeAll();

								generateConfiguration(factory, sumaryPanel,
										inputPanel, attributePanel, filterPanel);

								sumaryPanel.getFilters2Button()
										.addActionListener(
												new ActionListener() {
													public void actionPerformed(
															ActionEvent e) {
														inputPanel.removeAll();
														inputPanel
																.add(filterPanel);
														inputPanel.revalidate();
														inputPanel.repaint();
													}
												});

								sumaryPanel.getAttributes2Button()
										.addActionListener(
												new ActionListener() {
													public void actionPerformed(
															ActionEvent e) {
														inputPanel.removeAll();
														inputPanel
																.add(attributePanel);
														inputPanel.revalidate();
														inputPanel.repaint();
													}
												});

							} catch (MartServiceException e2) {
								JTextArea textArea = new JTextArea();
								textArea
										.append("Error while fetching dataset configuration\n\n");
								textArea.append(e2.getMessage());
								add(textArea);
							}
							fireLinkAdded(new QueryComponentEvent(this, dataset
									.getName(), null, ""));
						} else {
							sumaryPanel.setLinkedDataset(null, null);
						}
					} else if (e.getStateChange() == ItemEvent.DESELECTED) {
						if (selectedItem instanceof DatasetLink) {
							DatasetLink datasetLink = (DatasetLink) selectedItem;
							fireLinkRemoved(new QueryComponentEvent(this,
									datasetLink.getSourceDataset().getName(),
									null));

							if (factory != null) {
								factory.deregisterComponents();
							}
						}
					}
				}

			});

			// calculates the available links in a separate thread as can take
			// some time
			new Thread() {
				public void run() {
					try {
						List datasetLinks = martService
								.getLinkableDatasets(getMartDataset());
						if (datasetLinks.size() > 0) {
							Collections.sort(datasetLinks, DatasetLink
									.getDisplayComparator());

							for (Iterator iter = datasetLinks.iterator(); iter
									.hasNext();) {
								DatasetLink link = (DatasetLink) iter.next();
								datasetComboBox.addItem(link);
								datasetToLink.put(link.getSourceDataset()
										.getName(), link);
							}
							String name = DatasetLinkComponent.this.getName();
							String value = DatasetLinkComponent.this.getValue();
							if (name != null) {
								DatasetLinkComponent.this.setName(name);
							}
							if (value != null) {
								DatasetLinkComponent.this.setValue(value);
							}
							// remove the progress bar
							remove(progressBar);
							// add dataset list
							GridBagConstraints constraints = new GridBagConstraints();
							constraints.anchor = GridBagConstraints.WEST;

							JLabel linkedLabel = new JLabel("Linked:");
							linkedLabel.setFont(getFont()
									.deriveFont(Font.PLAIN));

							constraints.gridx = 0;
							constraints.gridy = 0;
							constraints.ipadx = 30;
							add(linkedLabel, constraints);
							constraints.ipadx = 0;

							constraints.gridx = 1;
							constraints.gridy = 0;
							add(datasetComboBox, constraints);

							Component line = new JPanel();
							line.setPreferredSize(new Dimension(0, 4));
							line.setBackground(borderColor);

							constraints.gridx = 0;
							constraints.gridy = 1;
							constraints.weightx = 1.0;
							constraints.fill = GridBagConstraints.HORIZONTAL;
							constraints.gridwidth = 2;
							constraints.insets = new Insets(20, 0, 20, 0);
							add(line, constraints);

							constraints.weightx = 0.0;
							constraints.gridwidth = 1;
							constraints.insets = new Insets(0, 0, 0, 0);

							constraints.gridx = 0;
							constraints.gridy = 2;
							constraints.fill = GridBagConstraints.VERTICAL;
							constraints.weighty = 1.0;
							add(Box.createGlue(), constraints);

						}
					} catch (MartServiceException e) {
						JTextArea textArea = new JTextArea();
						textArea
								.append("Error while fetching dataset links\n\n");
						textArea.append(e.getMessage());
						remove(progressBar);
						GridBagConstraints constraints = new GridBagConstraints();
						constraints.anchor = GridBagConstraints.NORTHWEST;
						add(textArea, constraints);
					} finally {
						revalidate();
						repaint();
					}
				}
			}.start();

		}

		public int getType() {
			return QueryComponent.LINK;
		}

		public void setValue(String value) {
			super.setValue(value);
		}

		public void setName(String name) {
			super.setName(name);
			datasetComboBox.setSelectedItem(datasetToLink.get(name));
		}

	}

	class DatasetPanel extends JPanel {

		public DatasetPanel() {
			setBackground(backgroundColor);
			setLayout(new GridBagLayout());
			setBorder(new EmptyBorder(10, 10, 0, 10));

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.anchor = GridBagConstraints.WEST;

			JLabel databaseLabel = new JLabel("Database:");
			databaseLabel.setFont(getFont().deriveFont(Font.PLAIN));

			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.ipadx = 30;
			add(databaseLabel, constraints);
			constraints.ipadx = 0;

			JTextField databaseField = new JTextField(martDataset
					.getMartURLLocation().getDisplayName());
			databaseField.setBackground(backgroundColor);
			databaseField.setEditable(false);

			constraints.gridx = 1;
			constraints.gridy = 0;
			add(databaseField, constraints);

			Component line = new JPanel();
			line.setPreferredSize(new Dimension(0, 4));
			line.setBackground(borderColor);

			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.weightx = 1.0;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridwidth = 2;
			constraints.insets = new Insets(20, 0, 20, 0);
			add(line, constraints);

			constraints.weightx = 0.0;
			constraints.fill = GridBagConstraints.NONE;
			constraints.gridwidth = 1;
			constraints.insets = new Insets(0, 0, 0, 0);

			JLabel datasetLabel = new JLabel("Dataset:");
			datasetLabel.setFont(getFont().deriveFont(Font.PLAIN));

			constraints.gridx = 0;
			constraints.gridy = 2;
			add(datasetLabel, constraints);

			JTextField datasetField = new JTextField(martDataset
					.getDisplayName());
			datasetField.setBackground(backgroundColor);
			datasetField.setEditable(false);

			constraints.gridx = 1;
			constraints.gridy = 2;
			add(datasetField, constraints);

			Component line2 = new JPanel();
			line2.setPreferredSize(new Dimension(0, 4));
			line2.setBackground(borderColor);

			constraints.gridx = 0;
			constraints.gridy = 3;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.NORTHWEST;
			constraints.gridwidth = 2;
			constraints.insets = new Insets(20, 0, 20, 0);
			add(line2, constraints);

		}

	}

	class SummaryPanel extends JPanel {
		private Color color = new Color(255, 248, 231);

		private JLabel dataset1Label;

		private JLabel dataset2Label;

		private JLabel dataset1CountLabel;

		private JLabel dataset2CountLabel;

		private JButton dataset1Button;

		private JButton dataset2Button;

		private JButton attributes1Button;

		private JButton attributes2Button;

		private JButton filters1Button;

		private JButton filters2Button;

		private JList attributes1List;

		private JList attributes2List;

		private JList filters1List;

		private JList filters2List;

		private CountQueryListener countQueryListener;

		private MartDataset linkedDataset;

		private MartServiceQueryConfigUIFactory05 linkedDatasetFactory;

		private Component fill = Box.createVerticalGlue();

		public SummaryPanel() {
			setBackground(color);
			setLayout(new GridBagLayout());
			setBorder(new EmptyBorder(15, 15, 15, 15));

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.anchor = GridBagConstraints.NORTHWEST;
			constraints.weightx = 1.0;

			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.insets = new Insets(5, 5, 0, 5);
			constraints.weightx = 0.0;
			add(getDataset1Button(), constraints);
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.weightx = 1.0;
			add(getDataset1CountLabel(), constraints);
			constraints.gridx = 0;
			constraints.gridy = GridBagConstraints.RELATIVE;
			constraints.gridwidth = 2;
			add(getDataset1Label(), constraints);
			constraints.insets = new Insets(5, 15, 0, 5);
			add(getAttributes1Button(), constraints);
			constraints.insets = new Insets(0, 15, 0, 5);
			add(getAttributes1List(), constraints);
			constraints.insets = new Insets(5, 15, 0, 5);
			add(getFilters1Button(), constraints);
			constraints.insets = new Insets(0, 15, 0, 5);
			add(getFilters1List(), constraints);
			constraints.insets = new Insets(5, 5, 0, 5);

			Component line = new JPanel();
			line.setPreferredSize(new Dimension(0, 4));
			line.setBackground(borderColor);

			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.insets = new Insets(20, 0, 15, 0);
			add(line, constraints);
			constraints.fill = GridBagConstraints.NONE;

			constraints.insets = new Insets(5, 5, 0, 5);
			constraints.weightx = 0.0;
			add(getDataset2Button(), constraints);
			constraints.gridx = 1;
			constraints.gridy = 7;
			constraints.weightx = 1.0;
			add(getDataset2CountLabel(), constraints);
			constraints.gridx = 0;
			constraints.gridy = GridBagConstraints.RELATIVE;
			constraints.gridwidth = 2;
			add(getDataset2Label(), constraints);

			constraints.fill = GridBagConstraints.VERTICAL;
			constraints.weighty = 1.0;
			add(fill, constraints);

			countQueryListener = new CountQueryListener();
			controller.getMartQuery().getQuery().addQueryListener(
					countQueryListener);
		}

		protected void finalize() throws Throwable {
			controller.getMartQuery().getQuery().removeQueryListener(
					countQueryListener);
			super.finalize();
		}

		public void setLinkedDataset(MartDataset linkedDataset,
				MartServiceQueryConfigUIFactory05 linkedDatasetFactory) {
			if (this.linkedDataset != linkedDataset) {
				this.linkedDatasetFactory = linkedDatasetFactory;
				if (linkedDataset == null) {
					this.linkedDataset = linkedDataset;
					getDataset2Label().setText("[None Selected]");

					remove(attributes2Button);
					remove(attributes2List);
					remove(filters2Button);
					remove(filters2List);
					attributes2List = null;
					filters2List = null;
				} else if (this.linkedDataset == null) {
					this.linkedDataset = linkedDataset;
					getDataset2Label().setText(linkedDataset.getDisplayName());

					remove(fill);
					GridBagConstraints constraints = new GridBagConstraints();
					constraints.anchor = GridBagConstraints.NORTHWEST;
					constraints.weightx = 1.0;
					constraints.gridx = 0;
					constraints.gridwidth = 2;
					constraints.insets = new Insets(5, 15, 0, 5);
					add(getAttributes2Button(), constraints);
					constraints.insets = new Insets(0, 15, 0, 5);
					getAttributes2List().setModel(
							new QueryListModel(controller.getMartQuery()
									.getQuery(), linkedDataset.getName(),
									QueryListModel.ATTRIBUTE));
					add(getAttributes2List(), constraints);

					constraints.insets = new Insets(5, 15, 0, 5);
					add(getFilters2Button(), constraints);
					constraints.insets = new Insets(0, 15, 0, 5);
					getFilters2List().setModel(
							new QueryListModel(controller.getMartQuery()
									.getQuery(), linkedDataset.getName(),
									QueryListModel.FILTER));
					add(getFilters2List(), constraints);

					constraints.fill = GridBagConstraints.VERTICAL;
					constraints.weighty = 1.0;
					add(fill, constraints);
				} else {
					this.linkedDataset = linkedDataset;
					getDataset2Label().setText(linkedDataset.getDisplayName());
					getAttributes2List().setModel(
							new QueryListModel(controller.getMartQuery()
									.getQuery(), linkedDataset.getName(),
									QueryListModel.ATTRIBUTE));
					getFilters2List().setModel(
							new QueryListModel(controller.getMartQuery()
									.getQuery(), linkedDataset.getName(),
									QueryListModel.FILTER));
				}
				revalidate();
				repaint();
			}
		}

		private JLabel getDataset1Label() {
			if (dataset1Label == null) {
				dataset1Label = new JLabel(martDataset.getDisplayName());
				dataset1Label.setFont(getFont().deriveFont(Font.PLAIN));
				dataset1Label.setBackground(color);
				dataset1Label.setBorder(null);
			}

			return dataset1Label;
		}

		private JLabel getDataset2Label() {
			if (dataset2Label == null) {
				dataset2Label = new JLabel("[None Selected]");
				dataset2Label.setFont(getFont().deriveFont(Font.PLAIN));
				dataset2Label.setBackground(color);
				dataset2Label.setBorder(null);
			}

			return dataset2Label;
		}

		private JLabel getDataset1CountLabel() {
			if (dataset1CountLabel == null) {
				dataset1CountLabel = new JLabel("");
				dataset1CountLabel.setFont(getFont().deriveFont(Font.PLAIN));
				dataset1CountLabel.setBackground(color);
				dataset1CountLabel.setBorder(null);
			}

			return dataset1CountLabel;
		}

		private JLabel getDataset2CountLabel() {
			if (dataset2CountLabel == null) {
				dataset2CountLabel = new JLabel("");
				dataset2CountLabel.setFont(getFont().deriveFont(Font.PLAIN));
				dataset2CountLabel.setBackground(color);
				dataset2CountLabel.setBorder(null);
			}

			return dataset2CountLabel;
		}

		private JButton getDataset1Button() {
			if (dataset1Button == null) {
				dataset1Button = new JButton("Dataset:");
				dataset1Button.setFont(getFont().deriveFont(Font.BOLD));
				dataset1Button.setBackground(color);
				dataset1Button.setBorder(null);
			}

			return dataset1Button;
		}

		private JButton getDataset2Button() {
			if (dataset2Button == null) {
				dataset2Button = new JButton("Dataset:");
				dataset2Button.setFont(getFont().deriveFont(Font.BOLD));
				dataset2Button.setBackground(color);
				dataset2Button.setBorder(null);
			}

			return dataset2Button;
		}

		public JButton getAttributes1Button() {
			if (attributes1Button == null) {
				attributes1Button = new JButton("Attributes");
				attributes1Button.setFont(getFont().deriveFont(Font.BOLD));
				attributes1Button.setBackground(color);
				attributes1Button.setBorder(null);
			}
			return attributes1Button;
		}

		private JButton getAttributes2Button() {
			if (attributes2Button == null) {
				attributes2Button = new JButton("Attributes");
				attributes2Button.setFont(getFont().deriveFont(Font.BOLD));
				attributes2Button.setBackground(color);
				attributes2Button.setBorder(null);
			}
			return attributes2Button;
		}

		public JButton getFilters1Button() {
			if (filters1Button == null) {
				filters1Button = new JButton("Filters");
				filters1Button.setFont(getFont().deriveFont(Font.BOLD));
				filters1Button.setBackground(color);
				filters1Button.setBorder(null);
			}
			return filters1Button;
		}

		private JButton getFilters2Button() {
			if (filters2Button == null) {
				filters2Button = new JButton("Filters");
				filters2Button.setFont(getFont().deriveFont(Font.BOLD));
				filters2Button.setBackground(color);
				filters2Button.setBorder(null);
			}
			return filters2Button;
		}

		private JList getAttributes1List() {
			if (attributes1List == null) {
				attributes1List = new JList();
				attributes1List.setModel(new QueryListModel(controller
						.getMartQuery().getQuery(), martDataset.getName(),
						QueryListModel.ATTRIBUTE));
				attributes1List.setCellRenderer(new QueryListCellRenderer());
				attributes1List.setBackground(color);
			}
			return attributes1List;
		}

		private JList getFilters1List() {
			if (filters1List == null) {
				filters1List = new JList();
				filters1List.setModel(new QueryListModel(controller
						.getMartQuery().getQuery(), martDataset.getName(),
						QueryListModel.FILTER));
				filters1List.setCellRenderer(new QueryListCellRenderer());
				filters1List.setBackground(color);
			}
			return filters1List;
		}

		private JList getAttributes2List() {
			if (attributes2List == null) {
				attributes2List = new JList();
				attributes2List.setModel(new QueryListModel(controller
						.getMartQuery().getQuery(), linkedDataset.getName(),
						QueryListModel.ATTRIBUTE));
				attributes2List.setCellRenderer(new QueryListCellRenderer());
				attributes2List.setBackground(color);
			}
			return attributes2List;
		}

		private JList getFilters2List() {
			if (filters2List == null) {
				filters2List = new JList();
				filters2List.setModel(new QueryListModel(controller
						.getMartQuery().getQuery(), linkedDataset.getName(),
						QueryListModel.FILTER));
				filters2List.setCellRenderer(new QueryListCellRenderer());
				filters2List.setBackground(color);
			}
			return filters2List;
		}

		public void updateDatasets() {
			ListModel listModel = getAttributes1List().getModel();
			if (listModel instanceof QueryListModel) {
				((QueryListModel) listModel).update();
			}
			listModel = getFilters1List().getModel();
			if (listModel instanceof QueryListModel) {
				((QueryListModel) listModel).update();
			}
			if (linkedDataset != null) {
				listModel = getAttributes2List().getModel();
				if (listModel instanceof QueryListModel) {
					((QueryListModel) listModel).update();
				}
				listModel = getFilters2List().getModel();
				if (listModel instanceof QueryListModel) {
					((QueryListModel) listModel).update();
				}
			}
		}

		class QueryListCellRenderer extends DefaultListCellRenderer {

			public QueryListCellRenderer() {
				setBackground(color);
				setBorder(new EmptyBorder(5, 0, 0, 0));
				setFont(getFont().deriveFont(Font.PLAIN));
			}

			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (value instanceof Attribute) {
					Attribute attribute = (Attribute) value;
					String displayName = null;
					if (linkedDataset != null
							&& linkedDataset.getName().equals(
									attribute.getContainingDataset().getName())) {
						displayName = (String) linkedDatasetFactory.attributeToDisplayName
								.get(attribute.getName());
					} else {
						displayName = (String) attributeToDisplayName
								.get(attribute.getName());
					}
					if (displayName == null) {
						displayName = attribute.getName();
					}
					setText(displayName);
				} else if (value instanceof Filter) {
					Filter filter = (Filter) value;
					String displayName = null;
					if (linkedDataset != null
							&& linkedDataset.getName().equals(
									filter.getContainingDataset().getName())) {
						displayName = (String) linkedDatasetFactory.filterToDisplayName
								.get(filter.getName());
					} else {
						displayName = (String) filterToDisplayName.get(filter
								.getName());
					}
					if (displayName == null) {
						displayName = filter.getName();
					}
					if (filter.isList()) {
						setText(displayName + ": [ID-list specified]");
					} else {
						setText(displayName + ": " + filter.getValue());
					}
				} else {
					setText(value.toString());
				}
				return this;
			}

		}

		class CountQueryListener implements QueryListener {
			public void attributeAdded(Attribute attribute, Dataset dataset) {
			}

			public void attributeRemoved(Attribute attribute, Dataset dataset) {
			}

			public void filterAdded(Filter filter, Dataset dataset) {
				if (martDataset.getName().equals(dataset.getName())) {
					getDataset1CountLabel().setText("");
				} else {
					getDataset2CountLabel().setText("");
				}
			}

			public void filterRemoved(Filter filter, Dataset dataset) {
				if (martDataset.getName().equals(dataset.getName())) {
					getDataset1CountLabel().setText("");
				} else {
					getDataset2CountLabel().setText("");
				}
			}

			public void filterChanged(Filter filter, Dataset dataset) {
				if (martDataset.getName().equals(dataset.getName())) {
					getDataset1CountLabel().setText("");
				} else {
					getDataset2CountLabel().setText("");
				}
			}
		}

		class QueryListModel extends AbstractListModel {
			public static final int ATTRIBUTE = 1;

			public static final int FILTER = 2;

			private static final String noneSelectedText = "[None Selected]";

			private String datasetName;

			private Query query;

			private int type;

			private List attributes;

			private List filters;

			private QueryListener queryListener = new QueryListener() {

				public void attributeAdded(Attribute attribute, Dataset dataset) {
					if (type == ATTRIBUTE
							&& datasetName.equals(dataset.getName())) {
						attributes.add(attribute);
						int index = attributes.size() - 1;
						fireIntervalAdded(this, index, index);
					}
				}

				public void attributeRemoved(Attribute attribute,
						Dataset dataset) {
					if (type == ATTRIBUTE
							&& datasetName.equals(dataset.getName())) {
						int index = attributes.indexOf(attribute);
						attributes.remove(index);
						fireIntervalRemoved(this, index, index);
					}
				}

				public void filterAdded(Filter filter, Dataset dataset) {
					if (type == FILTER && datasetName.equals(dataset.getName())) {
						filters.add(filter);
						int index = filters.size() - 1;
						fireIntervalAdded(this, index, index);
					}
				}

				public void filterRemoved(Filter filter, Dataset dataset) {
					if (type == FILTER && datasetName.equals(dataset.getName())) {
						int index = filters.indexOf(filter);
						filters.remove(index);
						fireIntervalRemoved(this, index, index);
					}
				}

				public void filterChanged(Filter filter, Dataset dataset) {
					if (type == FILTER && datasetName.equals(dataset.getName())) {
						int index = filters.indexOf(filter);
						fireContentsChanged(this, index, index);
					}
				}

			};

			public QueryListModel(Query query, String datasetName, int type) {
				this.query = query;
				this.datasetName = datasetName;
				this.type = type;
				Dataset dataset = query.getDataset(datasetName);
				if (dataset != null) {
					attributes = dataset.getAttributes();
					filters = dataset.getFilters();
				} else {
					attributes = new ArrayList();
					filters = new ArrayList();
				}
				query.addQueryListener(queryListener);
			}

			protected void finalize() throws Throwable {
				query.removeQueryListener(queryListener);
				super.finalize();
			}

			public void update() {
				if (type == ATTRIBUTE) {
					if (!attributes.isEmpty()) {
						fireContentsChanged(this, 0, attributes.size());
					}
				} else {
					if (!filters.isEmpty()) {
						fireContentsChanged(this, 0, filters.size());
					}
				}
			}

			public Object getElementAt(int index) {
				if (type == ATTRIBUTE) {
					if (attributes.isEmpty()) {
						return noneSelectedText;
					} else {
						return attributes.get(index);
					}
				} else {
					if (filters.isEmpty()) {
						return noneSelectedText;
					} else {
						return filters.get(index);
					}
				}
			}

			public int getSize() {
				if (type == ATTRIBUTE) {
					if (attributes.isEmpty()) {
						return 1;
					} else {
						return attributes.size();
					}
				} else {
					if (filters.isEmpty()) {
						return 1;
					} else {
						return filters.size();
					}
				}
			}

		}
	}

}

class ExpandableBox extends JPanel {
	private JButton expandButton;

	private JPanel labelBox;

	private boolean expanded = true;

	private boolean animated = false;
	
	private Timer timer = new Timer(1, null);

	private Dimension minSize;

	private Dimension maxSize;
	
	private int height;
	
	private final int increment = 10;
	
	private ActionListener openAction = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			if (height <= maxSize.height) {
				setPreferredSize(new Dimension(maxSize.width,
						height));
				revalidate();
				repaint();
				height += increment;
			} else {
				timer.removeActionListener(this);
				timer.stop();
				setPreferredSize(new Dimension(maxSize.width,
						maxSize.height));
				revalidate();
				repaint();
			}
		}
	};
	
	private ActionListener closeAction = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			if (height >= minSize.height) {
				setPreferredSize(new Dimension(minSize.width,
						height));
				revalidate();
				repaint();
				height -= increment;
			} else {
				timer.removeActionListener(this);
				timer.stop();
				height = minSize.height;
				setPreferredSize(new Dimension(minSize.width,
						height));
				revalidate();
				repaint();
			}
		}
	};
	
	public ExpandableBox(Component titleComponent, Color backgroundColor,
			Color borderColor) {
		this(titleComponent, backgroundColor, borderColor, false);
	}
	
	public ExpandableBox(Component titleComponent, Color backgroundColor,
			Color borderColor, boolean animated) {
		this.animated = animated;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(backgroundColor);
		setBorder(new CompoundBorder(new LineBorder(borderColor, 1),
				new EmptyBorder(10, 10, 10, 10)));

		labelBox = new JPanel();
		labelBox.setLayout(new BoxLayout(labelBox, BoxLayout.X_AXIS));
		labelBox.setBackground(backgroundColor);

		expandButton = new JButton(MartServiceIcons.getIcon("contract"));
		expandButton.setActionCommand("contract");
		expandButton.setBackground(backgroundColor);
		expandButton.setBorder(null);
		expandButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if ("contract".equals(e.getActionCommand())) {
					setExpanded(false);
				} else {
					setExpanded(true);
				}
			}

		});
		labelBox.add(expandButton);
		labelBox.add(Box.createHorizontalStrut(5));
		labelBox.add(titleComponent);
		labelBox.add(Box.createHorizontalGlue());
		add(labelBox);
		minSize = getPreferredSize();
	}

	public void setExpanded(boolean expanded) {
		if (maxSize == null || maxSize.height <= minSize.height) {
			maxSize = getLayout().preferredLayoutSize(this);
		}
		if (this.expanded != expanded) {
			this.expanded = expanded;
			if (expanded) {
				expandButton.setIcon(MartServiceIcons.getIcon("contract"));
				expandButton.setActionCommand("contract");
				if (animated) {
					timer.stop();
					timer.removeActionListener(closeAction);
					timer.addActionListener(openAction);
					timer.start();
				} else {
					setPreferredSize(new Dimension(maxSize.width, maxSize.height));
				}
			} else {
				expandButton.setIcon(MartServiceIcons.getIcon("expand"));
				expandButton.setActionCommand("expand");
				if (animated) {
					timer.stop();
					timer.removeActionListener(openAction);
					timer.addActionListener(closeAction);
					timer.start();
				} else {
					setPreferredSize(new Dimension(minSize.width, minSize.height));
				}
			}
			revalidate();
			repaint();
		}
		expandButton.setSelected(expanded);
	}

}

/**
 * A border drawn only at the top of a component.
 * 
 * @author David Withers
 */
class SideBorder extends AbstractBorder {
	private static final long serialVersionUID = 1L;

	private int side;

	private Color color;

	private int lineWidth;

	public SideBorder(int side, Color color) {
		this(side, color, 1);
	}

	public SideBorder(int side, Color color, int lineWidth) {
		this.side = side;
		this.color = color;
		this.lineWidth = lineWidth;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		width = width - 1;
		height = height - 1;
		Color originalColor = g.getColor();
		g.setColor(color);
		if (side == SwingConstants.TOP) {
			for (int i = 0; i < lineWidth; i++) {
				g.drawLine(x, y + i, x + width, y + i);
			}
		} else if (side == SwingConstants.BOTTOM) {
			for (int i = 0; i < lineWidth; i++) {
				g.drawLine(x, y + height - i, x + width, y + height - i);
			}
		} else if (side == SwingConstants.LEFT) {
			for (int i = 0; i < lineWidth; i++) {
				g.drawLine(x + i, y, x + i, y + height);
			}
		} else if (side == SwingConstants.RIGHT) {
			for (int i = 0; i < lineWidth; i++) {
				g.drawLine(x + width - i, y, x + width - i, y + height);
			}
		}
		g.setColor(originalColor);
	}
}

/**
 * A layout manager that lays out components, either horizontally or vertically,
 * according to their minimum size.
 * 
 * @author David Withers
 */
class MinimalLayout05 implements LayoutManager {
	public static final int HORIZONTAL = 0;

	public static final int VERTICAL = 1;

	private static final int gap = 5;

	private int type;

	public MinimalLayout05() {
		type = HORIZONTAL;
	}

	public MinimalLayout05(int type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
	 */
	public void removeLayoutComponent(Component comp) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
	 */
	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		int x = insets.left;
		int y = insets.top;
		if (type == HORIZONTAL) {
			Component[] components = parent.getComponents();
			for (int i = 0; i < components.length; i++) {
				components[i].setLocation(x, y);
				components[i].setSize(getSize(components[i]));
				x = x + gap + components[i].getWidth();
			}
		} else {
			Component[] components = parent.getComponents();
			for (int i = 0; i < components.length; i++) {
				components[i].setLocation(x, y);
				components[i].setSize(getSize(components[i]));
				y = y + gap + components[i].getHeight();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String,
	 *      java.awt.Component)
	 */
	public void addLayoutComponent(String name, Component comp) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	public Dimension minimumLayoutSize(Container parent) {
		Insets insets = parent.getInsets();
		Dimension minimumSize = new Dimension(insets.left, insets.top);
		if (type == HORIZONTAL) {
			int x = insets.left;
			Component[] components = parent.getComponents();
			for (int i = 0; i < components.length; i++) {
				Dimension size = getSize(components[i]);
				if (insets.top + size.height > minimumSize.height) {
					minimumSize.height = insets.top + size.height;
				}
				minimumSize.width = x + size.width;
				x = x + size.width + gap;
			}
		} else {
			int y = insets.top;
			Component[] components = parent.getComponents();
			for (int i = 0; i < components.length; i++) {
				Dimension size = getSize(components[i]);
				if (insets.left + size.width > minimumSize.width) {
					minimumSize.width = insets.left + size.width;
				}
				minimumSize.height = y + size.height;
				y = y + size.height + gap;
			}
		}
		minimumSize.width = minimumSize.width + insets.right;
		minimumSize.height = minimumSize.height + insets.bottom;

		return (minimumSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 */
	public Dimension preferredLayoutSize(Container parent) {
		return minimumLayoutSize(parent);
	}

	private Dimension getSize(Component component) {
		return component.getPreferredSize();
	}

}
