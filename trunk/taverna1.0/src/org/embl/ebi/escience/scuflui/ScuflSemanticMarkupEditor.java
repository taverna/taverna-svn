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
	
	theMetadata = m;

	JPanel ontologyPanel = new JPanel(new BorderLayout());
	ontologyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
								 "Pick from ontology"));
	final JTree ontologyTree = new JTree(RDFSParser.rootNode);
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



	add(ontologyPanel);
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
