/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.Border;
import org.embl.ebi.escience.scufl.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import org.embl.ebi.escience.scufl.semantics.*;

// Utility Imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;




/**
 * A JPanel that allows editing of the semantic markup
 * object passed into its constructor.
 * @author Tom Oinn
 */
public class ScuflSemanticMarkupEditor extends JPanel implements ScuflUIComponent {
       
    private final SemanticMarkup theMetadata;
 
    /**
     * Build a new markup editor attached to the particular
     * SemanticMarkup object.
     */
    public ScuflSemanticMarkupEditor(SemanticMarkup m) {
	
	super(new BorderLayout());
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	//super((JFrame)null,"Semantic metadata markup editor",true);
	
	JTabbedPane tabbedPane = new JTabbedPane();
	add(tabbedPane);


	theMetadata = m;

	JPanel ontologyPanel = new JPanel(new BorderLayout());
	ontologyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								 "Pick from ontology"));
	ontologyPanel.setPreferredSize(new Dimension(400,400));
	final JTree ontologyTree = new JTree(RDFSParser.rootNode);
	ontologyTree.setCellRenderer(new DefaultTreeCellRenderer() {
		public Component getTreeCellRendererComponent(JTree tree,
							      Object value,
							      boolean sel,
							      boolean expanded,
							      boolean leaf,
							      int row,
							      boolean hasFocus) {
		    super.getTreeCellRendererComponent(tree, value, sel,
						       expanded, leaf, row,
						       hasFocus);
		    Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
		    if (userObject instanceof RDFSClassHolder) {
			setIcon(ScuflIcons.classIcon);
		    }
		    return this;
		} 
	    });
	JScrollPane ontologyTreeDisplayPane = new JScrollPane(ontologyTree);
	ontologyPanel.add(ontologyTreeDisplayPane, BorderLayout.CENTER);
	final JTextField selectedOntologyNode = new JTextField(theMetadata.getSemanticType());
	ontologyPanel.add(selectedOntologyNode, BorderLayout.SOUTH);
	// Add the behaviour of putting the selected node, if a class holder,
	// into the box and setting the semantic type field of the metadata
	// holder at the same time.
	ontologyTree.addTreeSelectionListener(new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent e) {
		    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
			ontologyTree.getLastSelectedPathComponent();
		    if (node != null) {
			try {
			    RDFSClassHolder h = (RDFSClassHolder)node.getUserObject();
			    selectedOntologyNode.setText(h.getClassName());
			    theMetadata.setSemanticType(h.getClassName());
			}
			catch (ClassCastException cce) {
			    //
			}
		    }
		}
	    });
	// Add the behaviour to allow the user to manually edit the text as
	// well, always a good idea.
	selectedOntologyNode.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    theMetadata.setSemanticType(selectedOntologyNode.getText());
		}
	    });
	tabbedPane.addTab("Ontology",ontologyPanel);

	// Free text description
	JPanel descriptionPanel = new JPanel(new BorderLayout());
	descriptionPanel.setPreferredSize(new Dimension(400,400));
	descriptionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
							      "Edit Description"));
	final JTextArea descriptionText = new JTextArea(theMetadata.getDescription());
	JScrollPane descriptionPane = new JScrollPane(descriptionText);
	descriptionPanel.add(descriptionPane, BorderLayout.CENTER);
	JButton descriptionUpdateButton = new JButton("Update");
	descriptionUpdateButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    theMetadata.setDescription(descriptionText.getText());
		}
	    });
	descriptionPanel.add(descriptionUpdateButton, BorderLayout.SOUTH);
	tabbedPane.addTab("Description",descriptionPanel);

	// A panel to show the MIME mappings...
	JPanel topLevelMimePanel = new JPanel(new BorderLayout());
	JPanel mimePanel = new JPanel(new BorderLayout());
	topLevelMimePanel.add(mimePanel, BorderLayout.CENTER);
	mimePanel.setPreferredSize(new Dimension(400,400));
	mimePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
							      "Current MIME Types"));
	JPanel mimeEditPanel = new JPanel(new BorderLayout());
	mimeEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
							      "Enter new MIME Type and hit return"));
	topLevelMimePanel.add(mimeEditPanel, BorderLayout.SOUTH);
	
	final JList mimeTypeList = new JList(theMetadata.getMIMETypes());
	JScrollPane mimeListPane = new JScrollPane(mimeTypeList);
	mimePanel.add(mimeListPane, BorderLayout.CENTER);
	final JTextField mimeEntryField = new JTextField();
	mimeEditPanel.add(mimeEntryField, BorderLayout.NORTH);
	JButton clearMimeTypes = new JButton(ScuflIcons.deleteIcon);
	clearMimeTypes.setPreferredSize(new Dimension(32,32));
	clearMimeTypes.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    // Clear the list and the metadata container
		    theMetadata.clearMIMETypes();
		    mimeTypeList.setModel(new DefaultListModel());
		}
	    });
	mimeEntryField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    // Add a new MIME type
		    theMetadata.addMIMEType(mimeEntryField.getText());
		    mimeTypeList.setModel(new DefaultListModel());
		    String[] types = theMetadata.getMIMETypes();
		    for (int i = 0; i < types.length; i++) {
			((DefaultListModel)mimeTypeList.getModel()).add(i, types[i]);
		    }
		}
	    });
	mimeEditPanel.add(clearMimeTypes, BorderLayout.EAST);
	tabbedPane.addTab("MIME Types",topLevelMimePanel);

	show();
    }
    
    public void attachToModel(ScuflModel theModel) {
	//
    }

    public void detachFromModel() {
	//
    }

    public String getName() {
	return "Markup editor for "+theMetadata.getSubject().toString();
    }
}
