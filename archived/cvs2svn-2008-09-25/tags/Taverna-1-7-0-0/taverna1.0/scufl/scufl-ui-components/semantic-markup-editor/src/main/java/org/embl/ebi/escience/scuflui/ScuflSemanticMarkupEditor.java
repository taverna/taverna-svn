/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.SemanticMarkup;
import org.embl.ebi.escience.scufl.semantics.RDFSClassHolder;
import org.embl.ebi.escience.scufl.semantics.RDFSParser;
import org.embl.ebi.escience.scuflui.spi.SemanticMarkupViewSPI;

/**
 * A JPanel that allows editing of the semantic markup object passed into its
 * constructor.
 * 
 * @author Tom Oinn
 */
public class ScuflSemanticMarkupEditor extends JPanel implements
		SemanticMarkupViewSPI {

	private SemanticMarkup theMetadata = null;
	private JTextField selectedOntologyNode = new JTextField();
	private DefaultTreeModel treeModel = new DefaultTreeModel(
			RDFSParser.rootNode);
	private JTree ontologyTree = new JTree(treeModel);
	private JTextArea descriptionText = new JTextArea();
	private DefaultListModel mimeTypeListModel = new DefaultListModel();
	private JList mimeTypeList = new JList(mimeTypeListModel);
	
	private void setSemanticMarkup(SemanticMarkup m) {
		if (m == null) {
			setRecursiveEnabled(false);
			descriptionText.setText("");
			selectedOntologyNode.setText("");
			mimeTypeListModel.clear();
		} else {
			theMetadata = m;
			selectedOntologyNode.setText(m.getSemanticType());
			if (theMetadata.getSemanticType().equals("") == false) {
				String filterString = theMetadata.getSemanticType();
				String[] filter = theMetadata.getSemanticType().split("#");
				if (filter.length == 2) {
					filterString = filter[1];
				}
				ontologyTree.setCellRenderer(getRenderer(filterString));
				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel
				.getRoot();
				Enumeration en = rootNode.depthFirstEnumeration();
				while (en.hasMoreElements()) {
					DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) en
					.nextElement();
					if (theNode.getUserObject().toString().toLowerCase().matches(
							filterString)) {
						TreePath path = new TreePath(treeModel
								.getPathToRoot(theNode));
						ontologyTree.makeVisible(path);
					}
				}
			}
			descriptionText.setText(m.getDescription());
			mimeTypeListModel.clear();
			for (String mime : theMetadata.getMIMETypes()) {
				mimeTypeListModel.addElement(mime);
			}
			setRecursiveEnabled(true);
		}
		
	}
	
	private void setRecursiveEnabled(boolean b) {
		setRecursiveEnabled(b, this);
	}
	
	private void setRecursiveEnabled(boolean b, Component comp) {
		setEnabled(b);
		if (comp instanceof Container) {
			for (Component c : ((Container)comp).getComponents()) {
				setRecursiveEnabled(b, c);
			}
		}
	}
	
	/**
	 * Construct a blank semantic markup editor with no model object assigned
	 */
	public ScuflSemanticMarkupEditor() {
		this(null);
	}
	
	/**
	 * Build a new markup editor attached to the particular SemanticMarkup
	 * object.
	 */
	public ScuflSemanticMarkupEditor(SemanticMarkup m) {
		super(new BorderLayout());
		setPreferredSize(new Dimension(100, 100));
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		// super((JFrame)null,"Semantic metadata markup editor",true);

		JTabbedPane tabbedPane = new JTabbedPane();
		add(tabbedPane);

		JPanel ontologyPanel = new JPanel(new BorderLayout());
		ontologyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Pick from ontology"));
		ontologyPanel.setPreferredSize(new Dimension(400, 400));

		ontologyTree.setCellRenderer(getRenderer(null));

		JScrollPane ontologyTreeDisplayPane = new JScrollPane(ontologyTree);
		ontologyPanel.add(ontologyTreeDisplayPane, BorderLayout.CENTER);
		//final JTextField selectedOntologyNode = new JTextField(theMetadata
		//		.getSemanticType());
		ontologyPanel.add(selectedOntologyNode, BorderLayout.SOUTH);
		JPanel currentTermPanel = new JPanel(new GridLayout(2, 0));
		currentTermPanel.add(new JLabel(
				"Select from ontology or manually edit term below"));
		currentTermPanel.add(selectedOntologyNode);

		ontologyPanel.add(currentTermPanel, BorderLayout.SOUTH);

		// Add the behaviour of putting the selected node, if a class holder,
		// into the box and setting the semantic type field of the metadata
		// holder at the same time.
		ontologyTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) ontologyTree
						.getLastSelectedPathComponent();
				if (node != null) {
					try {
						RDFSClassHolder h = (RDFSClassHolder) node
								.getUserObject();
						selectedOntologyNode.setText(h.getClassName());
						if (theMetadata != null) {
							theMetadata.setSemanticType(h.getClassName());
						}
					} catch (ClassCastException cce) {
						//
					}
				}
			}
		});
		// Add the behaviour to allow the user to manually edit the text as
		// well, always a good idea.
		selectedOntologyNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (theMetadata!=null) {
					theMetadata.setSemanticType(selectedOntologyNode.getText());
				}
			}
		});
		// Show a filter dialog at the top
		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new GridLayout(0, 2));
		final JTextField filterText = new JTextField("");
		filterText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String filterString = filterText.getText();
				if (filterString.equals("")) {
					ontologyTree.setCellRenderer(getRenderer(null));
				} else {
					ontologyTree.setCellRenderer(getRenderer(filterString));
				}
			}
		});
		JButton showFromFilter = new JButton("Find from regex : ");
		showFromFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// Find everything in the tree that matches and
				// ensure that it is viewable
				String filterString = filterText.getText();
				expandAll(ontologyTree, false);
				if (filterString.equals("") == false) {
					ontologyTree.setCellRenderer(getRenderer(filterString));
					DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel
							.getRoot();
					Enumeration en = rootNode.depthFirstEnumeration();
					while (en.hasMoreElements()) {
						DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) en
								.nextElement();
						if (theNode.getUserObject().toString().toLowerCase()
								.matches(filterString)) {
							TreePath path = new TreePath(treeModel
									.getPathToRoot(theNode));
							ontologyTree.makeVisible(path);
						}
					}
				}
			}
		});
		filterPanel.add(showFromFilter);
		filterPanel.add(filterText);
		ontologyPanel.add(filterPanel, BorderLayout.NORTH);

		tabbedPane.addTab("Ontology", ontologyPanel);

		// Free text description
		JPanel descriptionPanel = new JPanel(new BorderLayout());
		descriptionPanel.setPreferredSize(new Dimension(400, 400));
		descriptionPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Edit Description"));
		descriptionText.setLineWrap(true);
		descriptionText.setWrapStyleWord(true);
		descriptionText.setFont(Font.getFont("Dialog"));
		JScrollPane descriptionPane = new JScrollPane(descriptionText);
		descriptionPanel.add(descriptionPane, BorderLayout.CENTER);
		descriptionText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				// Make sure it is updated in case several windows of these
				// metadata are open and the description has been changed from
				// one of the others.
				// FIXME: Do this as an event on theMetadata.
				if (theMetadata != null) {
					descriptionText.setText(theMetadata.getDescription());
				}
			}
			public void focusLost(FocusEvent e) {
				if (theMetadata != null) {
					theMetadata.setDescription(descriptionText.getText());
				}
			}
		});
		tabbedPane.addTab("Description", descriptionPanel);

		// A panel to show the MIME mappings
		JPanel topLevelMimePanel = new JPanel(new BorderLayout());
		JPanel mimePanel = new JPanel(new BorderLayout());
		topLevelMimePanel.add(mimePanel, BorderLayout.CENTER);
		mimePanel.setPreferredSize(new Dimension(400, 400));
		mimePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Current MIME Types"));
		JPanel mimeEditPanel = new JPanel(new BorderLayout());
		mimeEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Enter new MIME Type and hit return"));
		topLevelMimePanel.add(mimeEditPanel, BorderLayout.SOUTH);

		
		JScrollPane mimeListPane = new JScrollPane(mimeTypeList);
		mimePanel.add(mimeListPane, BorderLayout.CENTER);
		final JTextField mimeEntryField = new JTextField();
		mimeEditPanel.add(mimeEntryField, BorderLayout.NORTH);
		JButton clearMimeTypes = new JButton(TavernaIcons.deleteIcon);
		clearMimeTypes.setPreferredSize(new Dimension(32, 32));
		clearMimeTypes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// Clear the list and the metadata container
				if (theMetadata != null) {
					theMetadata.clearMIMETypes();
					mimeTypeListModel.clear();
				}
			}
		});
		mimeEntryField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// Add a new MIME type
				if (theMetadata != null) {
					theMetadata.addMIMEType(mimeEntryField.getText());
					mimeTypeListModel.clear();
					for (String mime : theMetadata.getMIMETypes()) {
						mimeTypeListModel.addElement(mime);
					}
				}
			}
		});
		mimeEditPanel.add(clearMimeTypes, BorderLayout.EAST);
		tabbedPane.addTab("MIME Types", topLevelMimePanel);
		setVisible(true);
		setSemanticMarkup(m);
	}

	public javax.swing.ImageIcon getIcon() {
		return TavernaIcons.classIcon;
	}

	public void attachToModel(SemanticMarkup theModel) {
		setSemanticMarkup(theModel);
	}

	public void detachFromModel() {
		setSemanticMarkup(null);
	}

	public String getName() {
		if (theMetadata == null) {
			// getName could be called during super() call.. :((
			return "Uninitialized markup editor";
		}
		return "Markup editor for " + theMetadata.getSubject().toString();
	}

	/**
	 * If the 'searchRegex' parameter is not null then choose the icon based on
	 * whether a regex match exists between the string and the toString method
	 * on the user object within the tree
	 */
	public TreeCellRenderer getRenderer(String searchRegex) {
		final String searchString = searchRegex;
		return new DefaultTreeCellRenderer() {
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded,
						leaf, row, hasFocus);
				Object userObject = ((DefaultMutableTreeNode) value)
						.getUserObject();
				if (userObject instanceof RDFSClassHolder) {
					if (searchString == null) {
						setIcon(TavernaIcons.classIcon);
					} else {
						if (userObject.toString().toLowerCase().matches(
								searchString)) {
							setIcon(TavernaIcons.selectedClassIcon);
							setText("<html><font color=\"red\">"
									+ userObject.toString() + "</font></html>");
							setBackground(new Color(191, 213, 197));
						} else {
							setIcon(TavernaIcons.classIcon);
						}
					}
				} else {
					if (expanded) {
						setIcon(TavernaIcons.folderOpenIcon);
					} else {
						setIcon(TavernaIcons.folderClosedIcon);
					}
				}
				return this;
			}
		};
	}

	// If expand is true, expands all nodes in the tree.
	// Otherwise, collapses all nodes in the tree.
	public void expandAll(JTree tree, boolean expand) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();
		// Traverse tree from root
		expandAll(tree, new TreePath(root), expand);
	}

	private void expandAll(JTree tree, TreePath parent, boolean expand) {
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}
		// Expansion or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		// TODO Auto-generated method stub
		
	}

}
