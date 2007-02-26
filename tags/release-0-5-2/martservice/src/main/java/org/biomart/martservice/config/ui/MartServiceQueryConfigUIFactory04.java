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
 * Filename           $RCSfile: MartServiceQueryConfigUIFactory04.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-01-31 14:12:08 $
 *               by   $Author: davidwithers $
 * Created on 04-Apr-2006
 *****************************************************************/
package org.biomart.martservice.config.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
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
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
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
public class MartServiceQueryConfigUIFactory04 implements QueryConfigUIFactory {
	private String version;
	
	private Color borderColor = new Color(51, 102, 102);

	private Color backgroundColor = new Color(238, 238, 238);

	private Color componentBackgroundColor = Color.WHITE;

	private MartService martService;

	private QueryConfigController controller;

	private MartDataset martDataset;

	private DatasetConfig datasetConfig;

	private Map filterNameToComponentMap = new HashMap();

	private Map attributeNameToComponentMap = new HashMap();

	private boolean settingAttributeState = false;

	private List componentRegister = new ArrayList();

	public MartServiceQueryConfigUIFactory04(MartService martService,
			QueryConfigController controller, MartDataset martDataset)
			throws MartServiceException {
		this.martService = martService;
		this.controller = controller;
		this.martDataset = martDataset;
		version = martService.getVersion(martDataset.getMartURLLocation());
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
		final JComponent panel = createVerticalBox(backgroundColor);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		final JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString("Fetching dataset configuration");
		panel.add(progressBar);

		new Thread("DatasetConfigUI") {
			public void run() {
				try {

					FilterPage[] filterPages = getDatasetConfig()
							.getFilterPages();
					AttributePage[] attributePages = getDatasetConfig()
							.getAttributePages();

					final Component filterPagesComponent = getFilterPagesUI(
							filterPages, null);
					final Component attributePagesComponent = getAttributePagesUI(
							attributePages, null);

					ButtonGroup buttonGroup = new ButtonGroup();

					final JToggleButton filterButton = new JToggleButton(
							"Filters");
					filterButton.setForeground(borderColor);
					buttonGroup.add(filterButton);
					final JToggleButton attributeButton = new JToggleButton(
							"Attributes");
					attributeButton.setForeground(borderColor);
					buttonGroup.add(attributeButton);

					filterButton.setSelected(true);

					filterButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							panel.remove(attributePagesComponent);
							panel.add(filterPagesComponent);
							panel.revalidate();
							panel.repaint();
						}
					});

					attributeButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							panel.remove(filterPagesComponent);
							panel.add(attributePagesComponent);
							panel.revalidate();
							panel.repaint();
						}
					});

					JPanel buttonPanel = new JPanel(new MinimalLayout04());
					buttonPanel.setBackground(backgroundColor);
					buttonPanel.add(filterButton);
					buttonPanel.add(attributeButton);

					panel.remove(progressBar);
					panel.add(buttonPanel);
					panel.add(filterPagesComponent);

					registerComponents();
				} catch (MartServiceException e) {
					JTextArea textArea = new JTextArea();
					textArea
							.append("Error while fetching dataset configuration\n\n");
					textArea.append(e.getMessage());
					panel.removeAll();
					panel.add(textArea);
				} catch (Exception e) {
					JTextArea textArea = new JTextArea();
					textArea
							.append("Error while generating the Query Editor\n\n");
					textArea.append(e.toString());
					panel.removeAll();
					panel.add(textArea);
				} finally {
					panel.revalidate();
					panel.repaint();
				}
			}
		}.start();
		
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setBackground(backgroundColor);
		northPanel.add(panel, BorderLayout.NORTH);
	
		JScrollPane scrollPane = new JScrollPane(northPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		
		return scrollPane;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributePagesUI(org.ensembl.mart.lib.config.AttributePage[])
	 */
	public Component getAttributePagesUI(AttributePage[] attributePages,
			Object data) throws MartServiceException {
		final JComponent box = createVerticalBox(backgroundColor);

		box.add(Box.createVerticalStrut(10));

		JComponent pageBox = createBox(new JLabel("Select the Attribute Page"),
				true);

		box.add(pageBox);

		pageBox.add(Box.createVerticalStrut(10));

		final JComponent selectPanel = new JPanel(new BorderLayout());
		selectPanel.setBackground(backgroundColor);
		box.add(selectPanel);

		Vector attributePageComponents = new Vector();

		for (int i = 0; i < attributePages.length; i++) {
			if (QueryConfigUtils.display(attributePages[i])) {
				Component component = getAttributePageUI(attributePages[i],
						data);
				attributePageComponents.add(new ConfigDisplayObject(
						attributePages[i], component));
			}
		}

		final JComboBox comboBox = new JComboBox(attributePageComponents);
		comboBox.setBackground(componentBackgroundColor);
		comboBox.addItemListener(new ItemListener() {
			Component lastSelectedComponent;

			public void itemStateChanged(ItemEvent e) {
				ConfigDisplayObject displayObject = (ConfigDisplayObject) e
						.getItem();
				if (displayObject != null) {
					Component selectedComponent = displayObject.getComponent();
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (lastSelectedComponent != null) {
							Map selected = new HashMap();
							// find all attributes on the last page that were
							// selected
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
							// remove attibutes that are already selected on the
							// new page
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
							// deselect any attributes on the old page that are
							// not on the new page
							Collection stillSelected = selected.values();
							for (Iterator iter = stillSelected.iterator(); iter
									.hasNext();) {
								AttributeComponent attributeComponent = (AttributeComponent) iter
										.next();
								attributeComponent.setSelected(false);
							}
						}
						selectPanel.add(selectedComponent, BorderLayout.NORTH);
						box.revalidate();
						box.repaint();
					} else if (e.getStateChange() == ItemEvent.DESELECTED) {
						selectPanel.removeAll();
						lastSelectedComponent = selectedComponent;
					}
				}
			}

			private List getAttributeComponents(Component component) {
				List attributeComponents = new ArrayList();
				if (component instanceof AttributeComponent) {
					attributeComponents.add(component);
				} else if (component instanceof Container) {
					Component[] children = ((Container) component)
							.getComponents();
					for (int i = 0; i < children.length; i++) {
						attributeComponents
								.addAll(getAttributeComponents(children[i]));
					}
				}
				return attributeComponents;
			}
		});

		// force an item change event
		comboBox.setSelectedIndex(-1);
		comboBox.setSelectedIndex(0);

		JPanel comboBoxPanel = new JPanel(new BorderLayout());
		comboBoxPanel.setBackground(componentBackgroundColor);
		comboBoxPanel.add(comboBox, BorderLayout.WEST);
		
		pageBox.add(comboBoxPanel);

		return box;
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
				box.add(Box.createVerticalStrut(10));
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
		String description = attributeGroup.getDescription();
		if (description != null) {
			title.setToolTipText(description);
		}
		JComponent box = createBox(title, true);

		AttributeCollection[] attributeCollections = attributeGroup
				.getAttributeCollections();
		box.add(getAttributeCollectionsUI(attributeCollections, data));

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

		AttributeDescription[] attributeDiscriptions = (AttributeDescription[]) attributeCollection
				.getAttributeDescriptions()
				.toArray(new AttributeDescription[0]);
		JLabel sequenceLabel = null;
		if ("seq_scope_type".equals(attributeCollection.getInternalName())) {
			sequenceLabel = new JLabel(MartServiceIcons
					.getIcon("gene_schematic"));
			box = createBox(sequenceLabel, false);
		} else if (attributeDiscriptions.length > 1) {
			// more than one attribute so create a box with the collection name
			// as a header
			JLabel title = new JLabel(attributeCollection.getDisplayName());
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
			box.add(getAttributeDescriptionsUI(attributeDiscriptions,
					new Object[] { SINGLE_SELECTION, sequenceLabel }));
		} else {
			box.add(getAttributeDescriptionsUI(attributeDiscriptions,
					new Object[] { MULTIPLE_SELECTION, sequenceLabel }));
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
					if (QueryConfigUtils
							.isFilterReference(attributeDescriptions[i], version)) {
						Component filterComponent = getFilterDescriptionUI(
								QueryConfigUtils
										.getReferencedFilterDescription(attributeDescriptions[i], version),
								data);
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
			if (QueryConfigUtils.isFilterReference(attributeDescription, version)) {
				FilterDescription filter = QueryConfigUtils
						.getReferencedFilterDescription(martService, dataset,
								attributeDescription, version);
				if (filter == null) {
					return null;
				}
				displayedAttribute = attributeDescription;
				displayedAttribute.setDisplayName(filter.getDisplayName());
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
				displayedAttribute, dataset, dataArray[0]);
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
			component.addQueryComponentListener(new QueryComponentAdapter() {
				public void attributeAdded(QueryComponentEvent event) {
					String name = component.getName();
					String dataset = component.getDataset().getName();
					if (name.equals("coding_gene_flank")
							|| name.equals("coding_transcript_flank")
							|| name.equals("transcript_flank")
							|| name.equals("gene_flank")) {
						QueryComponent filterComponent = (QueryComponent) filterNameToComponentMap
								.get(dataset + ".upstream_flank");
						if (filterComponent != null) {
							filterComponent.setSelected(true);
						}
					}
				}

			});
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
	public Component getAttributeListsUI(
			AttributeList[] attributeLists, Object data)
			throws MartServiceException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeListUI(org.ensembl.mart.lib.config.AttributeList,
	 *      int)
	 */
	public Component getAttributeListUI(
			AttributeList attributeList, Object data)
			throws MartServiceException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterPagesUI(org.ensembl.mart.lib.config.FilterPage[])
	 */
	public Component getFilterPagesUI(FilterPage[] filterPages, Object data)
			throws MartServiceException {
		final JComponent box = createVerticalBox(backgroundColor);

		JComponent dataset1Panel = new JPanel(new BorderLayout());
		dataset1Panel.setBackground(backgroundColor);
		dataset1Panel.setBorder(new EmptyBorder(10, 0, 0, 0));
		dataset1Panel.add(new JLabel("DATASET 1"), BorderLayout.WEST);
		box.add(dataset1Panel);

		for (int i = 0; i < filterPages.length; i++) {
			if (QueryConfigUtils.display(filterPages[i])) {
				box.add(getFilterPageUI(filterPages[i], data));
			}
		}

		box.add(Box.createVerticalStrut(10));

		Component linkComponent = new DatasetLinkComponent();
		box.add(linkComponent);
		componentRegister.add(linkComponent);

		box.add(Box.createVerticalGlue());

		return box;
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
				box.add(Box.createVerticalStrut(10));
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
		String description = filterGroup.getDescription();
		if (description != null) {
			title.setToolTipText(description);
		}
		JComponent box = createBox(title, true);

		FilterCollection[] filterCollections = filterGroup
				.getFilterCollections();
		box.add(getFilterCollectionsUI(filterCollections, data));

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
				if ("boolean_list".equals(filterDescriptions[0].getType())
						|| QueryConfigUtils
								.isBooleanList(filterDescriptions[0])) {
					box = createBox(null, false);
					box.add(filterComponent);
				} else if ("id_list".equals(filterDescriptions[0].getType())
						|| QueryConfigUtils.isIdList(filterDescriptions[0])) {
					box = createBox(null, false);
					box.add(filterComponent);
				} else {
					box = createBox(null, false);
					JComponent grid = new JPanel(new GridLayout(1, 2));
					grid.setBackground(componentBackgroundColor);
					JPanel buttonPanel = new JPanel(new MinimalLayout04());
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
				JLabel displayLabel = new JLabel(QueryConfigUtils
						.splitSentence(displayName));
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
		JComponent component;

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
		// start hard coded hack
		if (displayedFilter.getInternalName().equals("upstream_flank")
				|| displayedFilter.getInternalName().equals("downstream_flank")) {
			displayedFilter.setDefaultValue("100");
			displayedFilter.setType("text");
		}
		// end hard coded hack
		String type = displayedFilter.getType();
		if (type == null) {
			type = "text";// default filter type
		}

		if (type.equals("boolean") || type.equals("boolean_num")) {
			component = new BooleanFilterComponent(displayedFilter, dataset);
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
							booleanFilterDescription, dataset);
					filters.add(queryComponent);
					componentRegister.add(queryComponent);
				}
				component = new BooleanListFilterComponent(displayedFilter,
						dataset, filters);
			} else if (type.equals("id_list")
					|| QueryConfigUtils.isIdList(displayedFilter)) {
				Option[] options = displayedFilter.getOptions();
				List filters = new ArrayList();
				for (int i = 0; i < options.length; i++) {
					FilterDescription idFilterDescription = new FilterDescription(
							options[i]);
					idFilterDescription.setType("id_list");
					QueryComponent queryComponent = new TextFilterComponent(
							idFilterDescription, dataset);
					filters.add(queryComponent);
					componentRegister.add(queryComponent);
				}
				component = new IdListFilterComponent(displayedFilter, dataset,
						filters);
			} else if (QueryConfigUtils.isNestedList(displayedFilter)) {
				TextFilterComponent filterComponent = new TextFilterComponent(
						displayedFilter, dataset);
				filterComponent.add(QueryConfigUtils.getOptionButton(
						displayedFilter, filterComponent));
				component = filterComponent;
				componentRegister.add(component);
			} else {
				ListFilterComponent filterComponent = new ListFilterComponent(
						getDatasetConfig().getDataset(), displayedFilter,
						dataset, filterNameToComponentMap);
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
			component = new TextFilterComponent(displayedFilter, dataset);
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
			box.setBorder(new CompoundBorder(new TopBorder04(borderColor),
					new EmptyBorder(5, 10, 0, 10)));
		}
		if (titleComponent != null) {
			JComponent labelBox = createHorizontalBox(componentBackgroundColor);
			labelBox.add(titleComponent);
			labelBox.add(Box.createHorizontalGlue());
			box.add(labelBox);
		}
		return box;
	}

	class DatasetLinkComponent extends QueryComponent {
		private static final long serialVersionUID = 1L;

		private JComponent secondDatasetBox;

		private JComboBox linkComboBox;

		private JComboBox datasetComboBox;

		private JComponent dataset2Panel;

		private Component filterPage;

		private JProgressBar progressBar;

		private Map datasetToLink = new HashMap();

		private MartServiceQueryConfigUIFactory04 factory;

		public DatasetLinkComponent() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			dataset2Panel = new JPanel(new BorderLayout());
			dataset2Panel.setBackground(backgroundColor);
			dataset2Panel.setBorder(new EmptyBorder(10, 0, 0, 0));
			dataset2Panel.add(new JLabel("DATASET 2"), BorderLayout.WEST);

			secondDatasetBox = createBox(
					new JLabel("Select the second dataset"), true);
			add(secondDatasetBox);

			secondDatasetBox.add(Box.createVerticalStrut(10));

			progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
			progressBar.setStringPainted(true);
			progressBar.setString("Fetching dataset linking information");
			secondDatasetBox.add(progressBar);

			linkComboBox = new JComboBox();
			linkComboBox.setBackground(componentBackgroundColor);
			linkComboBox.addItem("undefined");
			linkComboBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (datasetComboBox.getSelectedItem() instanceof DatasetLink) {
							DatasetLink link = (DatasetLink) datasetComboBox
									.getSelectedItem();
							String linkId = (String) linkComboBox
									.getSelectedItem();
							fireLinkChanged(new QueryComponentEvent(this, link
									.getTargetDataset().getName(), null, linkId));
						}
					}
				}

			});

			datasetComboBox = new JComboBox();
			datasetComboBox.addItem("--NONE--");
			datasetComboBox.setBackground(componentBackgroundColor);
			datasetComboBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					Object selectedItem = e.getItem();
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (selectedItem instanceof String) {
							linkComboBox.addItem("undefined");
							revalidate();
							repaint();
							if (factory != null) {
								factory.deregisterComponents();
							}
						} else if (selectedItem instanceof DatasetLink) {
							DatasetLink datasetLink = (DatasetLink) selectedItem;
							MartDataset dataset = datasetLink
									.getSourceDataset();
							String[] links = datasetLink.getLinks();
							for (int i = 0; i < links.length; i++) {
								linkComboBox.addItem(links[i]);
							}
							try {
								MartQuery newQuery = new MartQuery(martService,
										dataset, controller.getMartQuery()
												.getQuery());
								QueryConfigController newController = new QueryConfigController(
										newQuery);
								factory = new MartServiceQueryConfigUIFactory04(
										martService, newController, dataset);
								DatasetConfig datasetConfig = factory
										.getDatasetConfig();
								FilterPage[] filterPages = datasetConfig
										.getFilterPages();
								for (int i = 0; i < filterPages.length; i++) {
									if (QueryConfigUtils
											.display(filterPages[i])) {
										filterPage = factory.getFilterPageUI(
												filterPages[i], null);
										add(dataset2Panel);
										add(filterPage);
										break;
									}
								}
								factory.registerComponents();
							} catch (MartServiceException e2) {
								JTextArea textArea = new JTextArea();
								textArea
										.append("Error while fetching dataset configuration\n\n");
								textArea.append(e2.getMessage());
								add(textArea);
							}
							revalidate();
							repaint();
							fireLinkAdded(new QueryComponentEvent(this, dataset
									.getName(), null, (String) linkComboBox
									.getSelectedItem()));
						}
					} else if (e.getStateChange() == ItemEvent.DESELECTED) {
						if (selectedItem instanceof String) {
							linkComboBox.removeAllItems();
						} else if (selectedItem instanceof DatasetLink) {
							DatasetLink datasetLink = (DatasetLink) selectedItem;
							fireLinkRemoved(new QueryComponentEvent(this,
									datasetLink.getSourceDataset().getName(),
									null));
							linkComboBox.removeAllItems();
							remove(dataset2Panel);
							if (filterPage != null) {
								remove(filterPage);
							}

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
							secondDatasetBox.remove(progressBar);
							// add dataset and link id lists
							JPanel datasetPanel = new JPanel(
									new MinimalLayout04());
							datasetPanel
									.setBackground(componentBackgroundColor);
							datasetPanel.add(new JLabel("Second Dataset:"));
							datasetPanel.add(datasetComboBox);

							secondDatasetBox
									.add(createBox(datasetPanel, false));

							secondDatasetBox.add(Box.createVerticalStrut(10));

							JComponent linkPanel = new JPanel(
									new MinimalLayout04());
							linkPanel.setBackground(componentBackgroundColor);
							linkPanel.add(new JLabel("Link Dataset Via:"));
							linkPanel.add(linkComboBox);

							secondDatasetBox.add(createBox(linkPanel, false));
						} else {
							// add no datasets message
							JLabel noDatasets = new JLabel(
									"No linkable datasets available");
							secondDatasetBox.remove(progressBar);
							secondDatasetBox.add(noDatasets);
						}
					} catch (MartServiceException e) {
						JTextArea textArea = new JTextArea();
						textArea
								.append("Error while fetching dataset links\n\n");
						textArea.append(e.getMessage());
						secondDatasetBox.remove(progressBar);
						secondDatasetBox.add(textArea);
						secondDatasetBox.revalidate();
						secondDatasetBox.repaint();
					} finally {
						secondDatasetBox.revalidate();
						secondDatasetBox.repaint();
					}
				}
			}.start();

		}

		public int getType() {
			return QueryComponent.LINK;
		}

		public void setValue(String value) {
			super.setValue(value);
			linkComboBox.setSelectedItem(value);
		}

		public void setName(String name) {
			super.setName(name);
			datasetComboBox.setSelectedItem(datasetToLink.get(name));
			// if (datasetToLink.containsKey(name)) {
			// System.out.println("Setting link to " + name);
			// datasetComboBox.setSelectedItem(datasetToLink.get(name));
			// }
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
			setLayout(new MinimalLayout04(MinimalLayout04.VERTICAL));
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

		private Map optionMap = new HashMap();

		private List optionList = new ArrayList();

		private String dataset;

		private Map componentMap;

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

			comboBox = new JComboBox();
			comboBox.setBackground(componentBackgroundColor);
			comboBox.setModel(comboBoxModel);

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
					}
				}

			});

			add(comboBox, BorderLayout.WEST);
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
				setValue(options[0].getValue());
			}
		}

		public void clearOptions() {
			comboBox.setSelectedIndex(-1);
			comboBoxModel.removeAllElements();
			optionMap.clear();
			optionList.clear();
		}

		public void setValue(String value) {
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

		private JCheckBox checkBox;

		private JTextArea textArea;

		private Map componentMap = new HashMap();

		private List filterList = new ArrayList();

		private int currentIndex;

		private boolean valueChanging;

		public IdListFilterComponent(FilterDescription description,
				MartDataset dataset, List filterComponentList) {
			setLayout(new GridLayout(1, 2));
			setBackground(componentBackgroundColor);

			checkBox = new JCheckBox();
			checkBox.setBackground(componentBackgroundColor);
			checkBox.setBorder(null);

			comboBox = new JComboBox();
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
				filterComponent
						.addQueryComponentListener(new QueryComponentAdapter() {
							public void filterAdded(QueryComponentEvent event) {
								if (!valueChanging) {
									valueChanging = true;
									comboBox.setSelectedIndex(filterList
											.indexOf(event.getName()));
									checkBox.setSelected(true);
									valueChanging = false;
								}
							}

							public void filterRemoved(QueryComponentEvent event) {
								if (!valueChanging) {
									valueChanging = true;
									checkBox.setSelected(false);
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
					if (checkBox.isSelected()) {
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

			checkBox.addItemListener(new ItemListener() {
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

			JPanel comboBoxPanel = new JPanel(new MinimalLayout04());
			comboBoxPanel.setBackground(componentBackgroundColor);
			comboBoxPanel.setBorder(new EmptyBorder(5, 5, 0, 0));
			comboBoxPanel.add(checkBox);

			String displayName = description.getDisplayName();
			if (displayName != null) {
				JPanel labelPanel = new JPanel(new MinimalLayout04(
						MinimalLayout04.VERTICAL));
				labelPanel.setBackground(componentBackgroundColor);
				JLabel label = new JLabel(displayName);
				label.setBackground(componentBackgroundColor);
				labelPanel.add(label);
				labelPanel.add(comboBox);
				comboBoxPanel.add(labelPanel);
			} else {
				comboBoxPanel.add(comboBox);
			}

			JPanel panel = new JPanel(new BorderLayout());
			panel.setBackground(componentBackgroundColor);
			panel.add(comboBoxPanel, BorderLayout.WEST);
			panel.add(Box.createVerticalStrut(100), BorderLayout.EAST);

			JPanel buttonPanel = new JPanel(new BorderLayout());
			buttonPanel.setBackground(componentBackgroundColor);
			buttonPanel.add(chooserButton, BorderLayout.WEST);

			JScrollPane textScrollPane = new JScrollPane(textArea);
			textScrollPane.setBackground(componentBackgroundColor);
			textScrollPane.setBorder(new CompoundBorder(new EmptyBorder(0, 0,
					0, 40), textScrollPane.getBorder()));

			JPanel panel2 = new JPanel(new BorderLayout());
			panel2.setBackground(componentBackgroundColor);
			panel2.add(textScrollPane, BorderLayout.CENTER);
			panel2.add(buttonPanel, BorderLayout.SOUTH);

			add(panel);
			add(panel2);
		}

		public int getType() {
			return FILTER;
		}

	}

	class BooleanListFilterComponent extends QueryComponent {
		private static final long serialVersionUID = 1L;

		private DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();

		private JComboBox comboBox;

		private JCheckBox checkBox;

		private BooleanFilterComponent booleanFilterComponent;

		private Map componentMap = new HashMap();

		private List filterList = new ArrayList();

		private int currentIndex;

		public BooleanListFilterComponent(FilterDescription description,
				MartDataset dataset, List filterComponentList) {
			setLayout(new GridLayout(1, 2));
			setBackground(componentBackgroundColor);

			checkBox = new JCheckBox();
			checkBox.setBackground(componentBackgroundColor);
			checkBox.setBorder(null);

			comboBox = new JComboBox();
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
				filterComponent
						.addQueryComponentListener(new QueryComponentAdapter() {
							public void filterAdded(QueryComponentEvent event) {
								comboBox.setSelectedIndex(filterList
										.indexOf(event.getName()));
								checkBox.setSelected(true);
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
					if (checkBox.isSelected()) {
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

			checkBox.addItemListener(new ItemListener() {
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

			JPanel comboBoxPanel = new JPanel(new MinimalLayout04());
			comboBoxPanel.setBackground(componentBackgroundColor);
			comboBoxPanel.setBorder(new EmptyBorder(5, 5, 0, 0));
			comboBoxPanel.add(checkBox);

			String displayName = description.getDisplayName();
			if (displayName != null) {
				JPanel labelPanel = new JPanel(new MinimalLayout04(
						MinimalLayout04.VERTICAL));
				labelPanel.setBackground(componentBackgroundColor);
				JLabel label = new JLabel(displayName);
				label.setBackground(componentBackgroundColor);
				labelPanel.add(label);
				labelPanel.add(comboBox);
				comboBoxPanel.add(labelPanel);
			} else {
				comboBoxPanel.add(comboBox);
			}

			add(comboBoxPanel);
			add(booleanFilterComponent);
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

		public AttributeComponent(AttributeDescription attributeDescription,
				MartDataset dataset, Object offButton) {
			this.offButton = (AbstractButton) offButton;
			setConfigObject(attributeDescription);
			setDataset(dataset);
			setName(attributeDescription.getInternalName());
			setLayout(new BorderLayout());
			setBackground(componentBackgroundColor);
			// if there's no display name the attribute isn't displayed
			String displayName = attributeDescription.getDisplayName();
			if (displayName != null) {
				if (offButton != null) {
					button = new JRadioButton(QueryConfigUtils
							.splitSentence(displayName));
				} else {
					button = new JCheckBox(QueryConfigUtils
							.splitSentence(displayName));
				}
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

}

/**
 * A border drawn only at the top of a component.
 * 
 * @author David Withers
 */
class TopBorder04 extends AbstractBorder {
	private static final long serialVersionUID = 1L;

	private Color color;

	public TopBorder04(Color color) {
		this.color = color;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		Color originalColor = g.getColor();
		g.setColor(color);
		g.drawLine(x, y, x + width, y);
		g.setColor(originalColor);
	}

}

/**
 * A layout manager that lays out components, either horizontally or vertically,
 * according to their minimum size.
 * 
 * @author David Withers
 */
class MinimalLayout04 implements LayoutManager {
	public static final int HORIZONTAL = 0;

	public static final int VERTICAL = 1;

	private static final int gap = 5;

	private int type;

	public MinimalLayout04() {
		type = HORIZONTAL;
	}

	public MinimalLayout04(int type) {
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
