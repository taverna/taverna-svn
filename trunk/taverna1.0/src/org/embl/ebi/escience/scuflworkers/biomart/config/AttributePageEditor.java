/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.*;
import org.embl.ebi.escience.scuflworkers.*;
import org.embl.ebi.escience.scuflworkers.biomart.*;
import org.ensembl.mart.lib.*;
import org.ensembl.mart.lib.config.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.ImageIcon;
import java.util.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Container;
import java.awt.event.*;

/**
 * JPanel subclass for an Attribute Page
 * @author Tom Oinn
 */
public class AttributePageEditor extends JPanel {

    private Query query;
    private AttributePage page;
    private QueryListener queryListener;

    public AttributePageEditor(Query query, AttributePage page) {
	super(new BorderLayout());
	//UIManager.put("CheckBox.background",  Color.WHITE);
	this.query = query;
	this.page = page;
	setOpaque(false);
	String title = page.getDescription();
	if (title == null) {
	    title = page.getDisplayName();
	}
	add(new ShadedLabel(title, ShadedLabel.TAVERNA_GREEN, true),
	    BorderLayout.NORTH);
	add(Box.createRigidArea(new Dimension(10,10)),
	    BorderLayout.WEST);
	add(Box.createRigidArea(new Dimension(10,10)),
	    BorderLayout.SOUTH);
	add(Box.createRigidArea(new Dimension(10,10)),
	    BorderLayout.EAST);
	JTabbedPane groups = new JTabbedPane();
	List groupList = page.getAttributeGroups();
	for (Iterator i = groupList.iterator(); i.hasNext();) {
	    Object o = i.next();
	    if (o instanceof AttributeGroup) {
		// Generic Attribute Group
		AttributeGroup ag = (AttributeGroup)o;
		groups.add(ag.getDisplayName(), new AttributeGroupEditor(query, ag));
	    }
	    else if (o instanceof DSAttributeGroup) {
		// Domain Specific Attribute Group
		DSAttributeGroup ag = (DSAttributeGroup)o;
		// Only handle hardcoded support for sequences at the moment
		if (ag.getHandler().equalsIgnoreCase("sequence")) {
		    groups.add(ag.getDisplayName(), new SequenceGroupEditor(query, ag));
		}
		else {
		    System.out.println("Unknown domain specific attribute group "+o);
		}
	    }
	    else {
		System.out.println("Unknown attribute page child type"+o);
	    }
	}
	add(groups, BorderLayout.CENTER);
	// Create a query listener to detect changes in the Query object and update
	// the attribute checkboxes appropriately
	queryListener = new QueryAdaptor() {
		public void attributeAdded(Query sourceQuery, int index, Attribute attribute) {
		    if (attribute instanceof FieldAttribute) {
			FieldAttribute fa = (FieldAttribute)attribute;
			String key = fa.getField()+fa.getKey()+fa.getTableConstraint();
			JCheckBox cb = (JCheckBox)checkBoxLocations.get(key);
			//System.out.println(attribute+" added with key "+key);
			if (cb != null &&
			    cb.isSelected() == false) {
			    cb.setSelected(true);
			    //System.out.println("  Checkbox selected");
			}
		    }
		}
		public void attributeRemoved(Query sourceQuery, int index, Attribute attribute) {
		    if (attribute instanceof FieldAttribute) {
			FieldAttribute fa = (FieldAttribute)attribute;
			String key = fa.getField()+fa.getKey()+fa.getTableConstraint();
			JCheckBox cb = (JCheckBox)checkBoxLocations.get(key);
			//System.out.println(attribute+" removed with key "+key);
			if (cb != null &&
			    cb.isSelected() == true) {
			    cb.setSelected(false);
			    //System.out.println("  Checkbox deselected");
			}
		    }
		}
	    };
	query.addQueryChangeListener(queryListener);
    }
    
    /**
     * Remove the listener when the class is finalized
     */
    protected void finalize() throws Throwable {
	super.finalize();
	query.removeQueryChangeListener(queryListener);
    }
    
    
    Map checkBoxLocations = new HashMap();
    
    /**
     * Set the current selected tab to show the specified attribute, if any
     */
    public void jumpTo(AttributeDescription aDesc) {
	String key = aDesc.getField()+aDesc.getKey()+aDesc.getTableConstraint();
	JCheckBox checkBox = (JCheckBox)checkBoxLocations.get(key);
	if (checkBox != null) {
	    Container lastComponent = checkBox;
	    while (lastComponent != this &&
		   lastComponent != null) {
		Container currentComponent = lastComponent.getParent();
		if (currentComponent instanceof JTabbedPane) {
		    JTabbedPane pane = (JTabbedPane)currentComponent;
		    pane.setSelectedComponent((Component)lastComponent);
		}
		lastComponent = currentComponent;
	    }
	}
    }

    protected void paintComponent(Graphics g) {
	final int width = getWidth();
	final int height = getHeight();
	Graphics2D g2d = (Graphics2D)g;
	Paint oldPaint = g2d.getPaint();
	g2d.setPaint(new GradientPaint(0, 0, ShadedLabel.TAVERNA_GREEN, width, 0, ShadedLabel.halfShade(ShadedLabel.TAVERNA_GREEN)));
	g2d.fillRect(0, 0, width, height);
	g2d.setPaint(oldPaint);
	super.paintComponent(g);
    }



    class AttributeGroupEditor extends JPanel {

	AttributeGroupEditor(Query query, AttributeGroup group) {
	    super(new BorderLayout());
	    setOpaque(false);
	    String title = group.getDescription();
	    if (title == null) {
		title = group.getDisplayName();
	    }
	    add(new ShadedLabel(title, ShadedLabel.TAVERNA_BLUE, true), BorderLayout.NORTH);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.WEST);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.EAST);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.SOUTH);
	    AttributeCollection[] collections = group.getAttributeCollections();
	    JPanel cp = new JPanel();
	    cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));
	    for (int i = 0; i < collections.length; i++) {
		cp.add(new AttributeCollectionEditor(query, collections[i]));
	    }
	    cp.add(Box.createVerticalGlue());
	    JScrollPane sp = new JScrollPane(cp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    cp.setBackground(Color.WHITE);
	    sp.setBackground(Color.WHITE);
	    sp.setPreferredSize(new Dimension(0,0));
	    add(sp, BorderLayout.CENTER);
	}
	protected void paintComponent(Graphics g) {
	    final int width = getWidth();
	    final int height = getHeight();
	    Graphics2D g2d = (Graphics2D)g;
	    Paint oldPaint = g2d.getPaint();
	    g2d.setPaint(new GradientPaint(0, 0, ShadedLabel.TAVERNA_BLUE, width, 0, ShadedLabel.halfShade(ShadedLabel.TAVERNA_BLUE)));
	    g2d.fillRect(0, 0, width, height);
	    g2d.setPaint(oldPaint);
	    super.paintComponent(g);
	}
    }

    class AttributeCollectionEditor extends JPanel {
    
	AttributeCollectionEditor(Query theQuery, AttributeCollection collection) {
	    super(new BorderLayout());
	    final Query query = theQuery;
	    Attribute[] queryAttributes = query.getAttributes();
	    setBackground(Color.WHITE);
	    String title = collection.getDescription();
	    if (title == null) {
		title = collection.getDisplayName();
	    }
	    add(new ShadedLabel("</b>"+title+"<b>", ShadedLabel.TAVERNA_ORANGE, true),
		BorderLayout.NORTH);
	
	    JPanel attributePanel = new JPanel();
	    attributePanel.setBackground(Color.WHITE);
	    List attributes = collection.getAttributeDescriptions();
	    int numberOfAttributes = attributes.size();
	    int rows = numberOfAttributes / 3;
	    if (rows * 3 < numberOfAttributes) {
		rows++;
	    }
	    attributePanel.setLayout(new GridLayout(rows,2));
	    for (Iterator i = attributes.iterator(); i.hasNext();) {
		final AttributeDescription desc = (AttributeDescription)i.next();
		final String myID = desc.getField()+desc.getKey()+desc.getTableConstraint();
		JCheckBox cb = new JCheckBox(desc.getDisplayName());
	    
		// Add the checkbox to the location field
		AttributePageEditor.this.checkBoxLocations.put(desc.getField()+desc.getKey()+desc.getTableConstraint(),
							       cb);
		cb.setContentAreaFilled(false);
		//cb.setOpaque(true);
		//cb.setBackground(Color.WHITE);
		cb.setMaximumSize(new Dimension(200,20));
		String description = desc.getDescription();
		if (description != null) {
		    cb.setToolTipText(description);
		}
		if (queryAttributes.length > 0) {
		    // Determine whether the query contains this attribute checked
		    for (int j = 0; j < queryAttributes.length; j++) {
			if (queryAttributes[j] instanceof FieldAttribute) {
			    FieldAttribute f = (FieldAttribute)queryAttributes[j];
			    String itsID = f.getField()+f.getKey()+f.getTableConstraint();
			    //System.out.println("  "+itsID);
			    if (myID.equals(itsID)) {
				cb.setSelected(true);
			    }
			}
		    }

		}
		// Register a listener on the checkbox to add the specified
		// attribute to the query or remove it based on the selection
		// state.
		cb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    // Is the attribute present in the query
			    // already?
			    FieldAttribute queryAttribute = null;
			    Attribute[] queryAttributes = query.getAttributes();
			    for (int j = 0; j < queryAttributes.length; j++) {
				if (queryAttributes[j] instanceof FieldAttribute) {
				    FieldAttribute fa = (FieldAttribute)queryAttributes[j];
				    if ((fa.getField()+fa.getKey()+fa.getTableConstraint()).equals(myID)) {
					queryAttribute = fa;
				    }
				}
			    }
			    // If queryAttribute is null then it wasn't present
			    if (e.getStateChange() == ItemEvent.SELECTED &&
				queryAttribute == null) {
				// Add a new query attribute
				FieldAttribute fa = new FieldAttribute(desc.getField(),
								       desc.getTableConstraint(),
								       desc.getKey());
				System.out.println("Adding : "+fa);
				query.addAttribute(fa);
			    }
			    else if (e.getStateChange() == ItemEvent.DESELECTED &&
				     queryAttribute != null) {
				// Remove the query attribute
				System.out.println("Removing : "+queryAttribute);
				query.removeAttribute(queryAttribute);
			    }
			}
		    });
		// Add to the panel
		attributePanel.add(cb);	    
	    }
	    // Fix up any blank space caused by only having one or two
	    // items to show
	    while (numberOfAttributes < 3) {
		numberOfAttributes++;
		attributePanel.add(Box.createHorizontalGlue());
	    }
	    attributePanel.setMaximumSize(new Dimension(5000,rows*20));
	    add(attributePanel,
		BorderLayout.CENTER);
	    setMaximumSize(new Dimension(5000,rows*20+25));
	}

    }

    class SequenceGroupEditor extends JPanel {
    
	SequenceGroupEditor(Query query, DSAttributeGroup group) {
	    super(new BorderLayout());
	    setOpaque(false);
	    String title = "Sequence export options.";
	    add(new ShadedLabel(title, ShadedLabel.TAVERNA_BLUE, true), BorderLayout.NORTH);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.WEST);	
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.EAST);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.SOUTH);
	    JPanel sequencePanel = new SequenceEditor(query);
	    sequencePanel.setLayout(new BoxLayout(sequencePanel, BoxLayout.PAGE_AXIS));
	    sequencePanel.add(Box.createVerticalGlue());
	    JScrollPane sp = new JScrollPane(sequencePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    sequencePanel.setBackground(Color.WHITE);
	    sp.setBackground(Color.WHITE);
	    sp.setPreferredSize(new Dimension(0,0));
	    add(sp, BorderLayout.CENTER);
	}

	protected void paintComponent(Graphics g) {
	    final int width = getWidth();
	    final int height = getHeight();
	    Graphics2D g2d = (Graphics2D)g;
	    Paint oldPaint = g2d.getPaint();
	    g2d.setPaint(new GradientPaint(0, 0, ShadedLabel.TAVERNA_BLUE, width, 0, ShadedLabel.halfShade(ShadedLabel.TAVERNA_BLUE)));
	    g2d.fillRect(0, 0, width, height);
	    g2d.setPaint(oldPaint);
	    super.paintComponent(g);
	}
    }

    class SequenceEditor extends JPanel {

	Query query;
	final JComboBox geneTranscriptSelect = new JComboBox(new String[]{
	    "None","Transcripts / Proteins","Genes"});
	JComboBox sequenceOptionsSelect = new JComboBox();
	final JTextArea fiveFlankLength = new JTextArea("1000");
	final JTextArea threeFlankLength = new JTextArea("1000");
    
	// Options available for both genes and transcripts
	String[] geneOptions = new String[]{"Gene sequence",
					    "Gene plus 5' and 3' flanks",
					    "Gene plus 5' flank",
					    "Gene plus 3' flank",
					    "5' upstream only",
					    "3' downstream only",
					    "Exon sequences",
					    "Exons plus 5' and 3' flanks",
					    "Exons plus 5' flanks",
					    "Exons plus 3' flanks"};
    
	// Options only available for transcripts
	String[] transcriptOnlyOptions = new String[]{"5' UTR only",
						      "5' UTR and upstream",
						      "3' UTR only",
						      "3' UTR and downstream",
						      "cDNA sequence only",
						      "Coding  equence only",
						      "Peptide"};
    
	// Image names for each option type within the transcript selection
	String[] transcriptImageNames = new String[]{"gene_schematic_gene_only.gif",
						     "gene_schematic_gene_5_3.gif",
						     "gene_schematic_gene_5.gif",
						     "gene_schematic_gene_3.gif",
						     "gene_schematic_5_only.gif",
						     "gene_schematic_3_only.gif",
						     "gene_schematic_exons.gif",
						     "gene_schematic_exons_5_3.gif",
						     "gene_schematic_exons_5.gif",
						     "gene_schematic_exons_3.gif",
						     "gene_schematic_upstream_utr.gif",
						     "gene_schematic_upstream_utr_5.gif",
						     "gene_schematic_downstream_utr.gif",
						     "gene_schematic_downstream_utr_3.gif",
						     "gene_schematic_cdna.gif",
						     "gene_schematic_coding.gif",
						     "gene_schematic_coding.gif"};
	// Image names for each option type within the gene selection
	String[] geneImageNames = new String[]{"gene_schematic_extent_gene_only.gif",
					       "gene_schematic_extent_gene_5_3.gif",
					       "gene_schematic_extent_gene_5.gif",
					       "gene_schematic_extent_gene_3.gif",
					       "gene_schematic_extent_5_only.gif",
					       "gene_schematic_extent_3_only.gif",
					       "gene_schematic_extent_exons.gif",
					       "gene_schematic_extent_exons_5_3.gif",
					       "gene_schematic_extent_exons_5.gif",
					       "gene_schematic_extent_exons_3.gif"};


    
	// Whether to enable the flank length extension inputs
	// given an option index
	boolean[] has3Flank = new boolean[]{false,true,false,true,false,true,false,true,false,true,false,false,false,true,false,false,false};
	boolean[] has5Flank = new boolean[]{false,true,true,false,true,false,false,true,true,false,false,true,false,false,false,false,false};
    
	int[] geneTypes = new int[]{SequenceDescription.GENEEXONINTRON,
				    SequenceDescription.GENEEXONINTRON,
				    SequenceDescription.GENEEXONINTRON,
				    SequenceDescription.GENEEXONINTRON,
				    SequenceDescription.GENEFLANKS,
				    SequenceDescription.GENEFLANKS,
				    SequenceDescription.GENEEXONS,
				    SequenceDescription.GENEEXONS,
				    SequenceDescription.GENEEXONS,
				    SequenceDescription.GENEEXONS};
	int[] transcriptTypes = new int[]{SequenceDescription.TRANSCRIPTEXONINTRON,
					  SequenceDescription.TRANSCRIPTEXONINTRON,
					  SequenceDescription.TRANSCRIPTEXONINTRON,
					  SequenceDescription.TRANSCRIPTEXONINTRON,
					  SequenceDescription.TRANSCRIPTFLANKS,
					  SequenceDescription.TRANSCRIPTFLANKS,
					  SequenceDescription.TRANSCRIPTEXONS,
					  SequenceDescription.TRANSCRIPTEXONS,
					  SequenceDescription.TRANSCRIPTEXONS,
					  SequenceDescription.TRANSCRIPTEXONS,
					  SequenceDescription.UPSTREAMUTR,
					  SequenceDescription.UPSTREAMUTR,
					  SequenceDescription.DOWNSTREAMUTR,
					  SequenceDescription.DOWNSTREAMUTR,
					  SequenceDescription.TRANSCRIPTCDNA,
					  SequenceDescription.TRANSCRIPTCODING,
					  SequenceDescription.TRANSCRIPTPEPTIDE};
				

	// Construct the entire list of options available when the 
	// transcripts / proteins option is selected from the combobox
	String[] transcriptOptions;
    
	SequenceEditor(Query query) {
	    super();
	    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	    this.query = query;
	    List transcriptOptionsList = new ArrayList(Arrays.asList(geneOptions));
	    transcriptOptionsList.addAll(Arrays.asList(transcriptOnlyOptions));
	    transcriptOptions = (String[])transcriptOptionsList.toArray(new String[0]);
	
	    JPanel seqTypePanel = new JPanel(new BorderLayout());
	    seqTypePanel.add(new ShadedLabel("Type of sequence to fetch",ShadedLabel.TAVERNA_ORANGE, true),
			     BorderLayout.NORTH);
	    seqTypePanel.add(geneTranscriptSelect,
			     BorderLayout.CENTER);
	    seqTypePanel.setMaximumSize(new Dimension(6000,45));
	    add(seqTypePanel);
	
	    // Change the options available when the sequence type is changed
	    final JComboBox sOptions = sequenceOptionsSelect;
	    geneTranscriptSelect.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			JComboBox source = (JComboBox)ae.getSource();
			int index = source.getSelectedIndex();
			if (index == 0) {
			    // Deselect and update query
			    sOptions.removeAllItems();
			    sOptions.setSelectedIndex(-1);
			    sOptions.setEnabled(false);
			}
			else if (index == 1) {
			    // Transcripts and proteins
			    sOptions.setEnabled(false);
			    sOptions.removeAllItems();
			    for (int i = 0; i < transcriptOptions.length; i++) {
				sOptions.addItem(transcriptOptions[i]);
			    }
			    sOptions.setSelectedIndex(-1);
			    sOptions.setEnabled(true);
			}
			else if (index == 2) {
			    // Genes
			    sOptions.setEnabled(false);
			    sOptions.removeAllItems();
			    for (int i = 0; i < geneOptions.length; i++) {
				sOptions.addItem(transcriptOptions[i]);
			    }
			    sOptions.setSelectedIndex(-1);
			    sOptions.setEnabled(true);
			}
			
		    }
		});

	    JPanel optionsPanel = new JPanel(new BorderLayout());
	    optionsPanel.add(new ShadedLabel("Desired sequence options",ShadedLabel.TAVERNA_ORANGE, true),
			     BorderLayout.NORTH);
	    optionsPanel.add(sequenceOptionsSelect);
	    optionsPanel.setMaximumSize(new Dimension(600,45));
	    add(optionsPanel);
	
	    JPanel extentPanel = new JPanel(new BorderLayout());
	    extentPanel.setBackground(Color.WHITE);
	    extentPanel.add(new ShadedLabel("Extents", ShadedLabel.TAVERNA_ORANGE, true),
			    BorderLayout.NORTH);
	    JPanel internalExtentPanel = new JPanel();
	    internalExtentPanel.setBackground(Color.WHITE);
	    internalExtentPanel.setLayout(new GridLayout(1,3));
	    JLabel fiveLabel = new JLabel("5' flank");
	    fiveLabel.setOpaque(false);
	    fiveLabel.setBackground(Color.WHITE);
	    fiveLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
	    internalExtentPanel.add(fiveLabel);
	
	    internalExtentPanel.add(fiveFlankLength);
	    JLabel threeLabel = new JLabel("3' flank");
	    threeLabel.setOpaque(false);
	    threeLabel.setBackground(Color.WHITE);
	    threeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
	    internalExtentPanel.add(threeLabel);
	    internalExtentPanel.add(threeFlankLength);
	    extentPanel.add(internalExtentPanel, BorderLayout.CENTER);
	    extentPanel.setMaximumSize(new Dimension(600,45));
	    add(extentPanel);


	    JPanel imagePanel = new JPanel(new BorderLayout());
	    imagePanel.setBackground(Color.WHITE);
	    imagePanel.add(new ShadedLabel("Sequence glyph", ShadedLabel.TAVERNA_ORANGE, true),
			   BorderLayout.NORTH);
	    final JPanel internalImage = new JPanel(new BorderLayout());
	    internalImage.setBackground(Color.WHITE);
	    imagePanel.add(internalImage, BorderLayout.CENTER);
	
	    // Change the image when an option is selected and set the appropriate
	    // flanking options, also update the query.
	    sequenceOptionsSelect.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			fiveFlankLength.setEnabled(false);
			threeFlankLength.setEnabled(false);
			JComboBox source = (JComboBox)ae.getSource();
			int index = source.getSelectedIndex();
			if (source.isEnabled() && index > -1) {
			    String imageURL = null;
			    if (geneTranscriptSelect.getSelectedIndex() == 1) {
				// Transcripts
				imageURL = "org/embl/ebi/escience/scuflworkers/biomart/config/glyphs/"+transcriptImageNames[index];
			    }
			    else {
				// Genes
				imageURL = "org/embl/ebi/escience/scuflworkers/biomart/config/glyphs/"+
				    geneImageNames[index];
			    }
				
			    internalImage.removeAll();
			    internalImage.add(new JLabel(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(imageURL))),
					      BorderLayout.CENTER);
			    fiveFlankLength.setEnabled(has5Flank[index]);
			    threeFlankLength.setEnabled(has3Flank[index]);
			}
			else {
			    internalImage.removeAll();
			}
			updateQuery();
		    }
		});
	    internalImage.setMaximumSize(new Dimension(250,70));
	    internalImage.setMinimumSize(new Dimension(250,70));
	    imagePanel.setMaximumSize(new Dimension(600,95));
	    add(imagePanel);
	    updateUIPanel();
	}
    
	// Called when the query needs to be updated from the UI
	void updateQuery() {
	    SequenceDescription sd = query.getSequenceDescription();
	    SequenceDescription newDesc = getSequenceDescription();
	    if (sd == null && newDesc == null) {
		return;
	    }
	    if (sd == null ||
		sd.equals(newDesc) == false) {
		query.setSequenceDescription(newDesc);
	    }
	}
    
	SequenceDescription getSequenceDescription() {
	    if (geneTranscriptSelect.getSelectedIndex() > 0 &&
		sequenceOptionsSelect.getSelectedIndex() > -1) {
		int sdType = (geneTranscriptSelect.getSelectedIndex() == 1 ? 
			      transcriptTypes[sequenceOptionsSelect.getSelectedIndex()] : 
			      geneTypes[sequenceOptionsSelect.getSelectedIndex()]);
		int fiveFlankLengthValue = (fiveFlankLength.isEnabled() ?
					    new Integer(fiveFlankLength.getText()).intValue() :
					    0);
		int threeFlankLengthValue = (threeFlankLength.isEnabled() ?
					     new Integer(threeFlankLength.getText()).intValue() :
					     0);
		try {
		    return new SequenceDescription(sdType,
						   fiveFlankLengthValue,
						   threeFlankLengthValue);
		}
		catch (InvalidQueryException iqe) {
		    iqe.printStackTrace();
		    return null;
		}
	    }
	    else {
		return null;
	    }
	}

	// Called when the ui needs to be updated from the query
	void updateUIPanel() {
	    SequenceDescription sd = query.getSequenceDescription();
	    if (sd == null) {
		// No query
		geneTranscriptSelect.setSelectedIndex(0);
	    }
	    else {
		// Are we looking at genes or transcripts?
		int sType = sd.getType();
		boolean genes = (sType == SequenceDescription.GENEEXONINTRON ||
				 sType == SequenceDescription.GENEFLANKS ||
				 sType == SequenceDescription.GENEEXONS);
		int fiveFlankValue = sd.getLeftFlank();
		int threeFlankValue = sd.getRightFlank();
		boolean hasFiveFlank = (fiveFlankValue > 0);
		boolean hasThreeFlank = (threeFlankValue > 0);
		if (genes) {
		    geneTranscriptSelect.setSelectedIndex(2);
		    for (int i = 0; i < geneTypes.length; i++) {
			if (geneTypes[i] == sType &&
			    has5Flank[i] == hasFiveFlank &&
			    has3Flank[i] == hasThreeFlank) {
			    sequenceOptionsSelect.setSelectedIndex(i);
			    break;
			}
		    }
		}
		else {
		    geneTranscriptSelect.setSelectedIndex(1);
		    for (int i = 0; i < transcriptTypes.length; i++) {
			if (transcriptTypes[i] == sType &&
			    has5Flank[i] == hasFiveFlank &&
			    has3Flank[i] == hasThreeFlank) {
			    sequenceOptionsSelect.setSelectedIndex(i);
			    break;
			}
		    }
		}
	    }
	}

    }

}
