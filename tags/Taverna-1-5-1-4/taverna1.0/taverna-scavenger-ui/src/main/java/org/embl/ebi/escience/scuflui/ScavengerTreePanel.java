/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scuflui.shared.ScuflModelExplorerRenderer;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.scuflui.workbench.DefaultScavengerTree;
import org.embl.ebi.escience.scuflui.workbench.FileDrop;
import org.embl.ebi.escience.scuflui.workbench.FileScavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * Wraps a ScavengerTree to provide a toolbar including a search by regular
 * expression within the tree option.
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 */
@SuppressWarnings("serial")
public class ScavengerTreePanel extends JPanel implements WorkflowModelViewSPI {

	private static Logger logger = Logger.getLogger(ScavengerTreePanel.class);
	
	DefaultScavengerTree tree;

	JTextField regex = null;	

	JButton find;
	
	JProgressBar progBar=null;

	JCheckBox watchLoads = new JCheckBox("Watch loads", true);

	boolean isWatchingLoads = true;

	ScuflModelEventListener eventListener = new ScuflModelEventListener() {
		public void receiveModelEvent(ScuflModelEvent event) {
			if (event.getEventType() == ScuflModelEvent.LOAD
					&& ScavengerTreePanel.this.isWatchingLoads) {
				new Thread() {
					public void run() {
						try {
							ScavengerTreePanel.this.tree
									.addScavengersFromModel();
						} catch (Exception ex) {
							// Ignore silently
						}
					}
				}.start();
			}
		}
	};
	
	private boolean initialised = false;
	
	public void startProgressBar(String text)
	{		
		progBar.setString(text);
		progBar.setStringPainted(true);
		progBar.setVisible(true);
	}
	
	public void stopProgressBar()
	{
		progBar.setVisible(false);
	}
	
	private void initialise(boolean populated) {
		setLayout(new BorderLayout());
		// To avoid double horisontal scrollbars, let the treePane be in charge
		this.setPreferredSize(new Dimension(0, 0));
		
		progBar=new JProgressBar();
		progBar.setIndeterminate(true);
		progBar.setVisible(false);		
		//JPanel progPanel=new JPanel();		
		//progPanel.add(progBar,BorderLayout.CENTER);
		add(progBar,BorderLayout.PAGE_END);
				
		tree = new DefaultScavengerTree(populated,this);		
		
		JScrollPane treePane = new JScrollPane(tree);
		treePane.setPreferredSize(new Dimension(0, 0));
		add(treePane, BorderLayout.CENTER);
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.add(new JLabel(" Search "));
		regex = new JTextField();
		regex.setPreferredSize(new Dimension(100, 20));
		regex.setMaximumSize(new Dimension(100, 20));
		toolbar.add(regex);
		find = new JButton(TavernaIcons.findIcon);
		find.setPreferredSize(new Dimension(20, 20));
		find.setEnabled(false);
		toolbar.add(find);
		toolbar.addSeparator();
		toolbar.add(watchLoads);
		toolbar.add(Box.createHorizontalGlue());
		add(toolbar, BorderLayout.PAGE_START);									
		
		
		// Add the filedrop to the toolbar, we can't add it to the main
		// panel because that's already looking out for drag and drop events
		// from the explorer
		new FileDrop(toolbar, new FileDrop.Listener() {
			public void filesDropped(File[] files) {
				for (int i = 0; i < files.length; i++) {
					try {
						ScavengerTreePanel.this.tree
								.addScavenger(new FileScavenger(files[i]));
					} catch (ScavengerCreationException sce) {
						sce.printStackTrace();
					}
				}
			}
		});

		// Add an event listener to kick the contained tree
		// into fetching processor factories from loaded
		// workflows if the watchLoads checkbox is true
		watchLoads.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					ScavengerTreePanel.this.isWatchingLoads = false;
				} else {
					ScavengerTreePanel.this.isWatchingLoads = true;
				}
			}
		});

		// Add an action listener to the button to find the
		// nodes matching the supplied regex.
		find.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jumpToAndHighlight();
			}
		});
		// Add an action listener to the text field to catch
		// return being hit with it in focus
		regex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (regex.getText().equals("") == false) {
					jumpToAndHighlight();
				}
			}
		});
		// Add a document listener to the text field to enable
		// the regex search if there's any text there
		regex.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				checkStatus();
			}

			public void removeUpdate(DocumentEvent e) {
				checkStatus();
			}

			public void changedUpdate(DocumentEvent e) {
				checkStatus();
			}

			private void checkStatus() {
				// Always remove highlight information as it is now
				// no longer in synch with the regex in the text field
				cancelHighlight();
				// Check whether the search button should be enabled
				if (regex.getText().equals("") == false) {
					find.setEnabled(true);
				} else {
					find.setEnabled(false);
				}
			}
		});
	}
	
	protected boolean populate()
	{
		return true;
	}
	
	public void attachToModel(ScuflModel model) {
		if (this.scuflModel!=null) {
			logger.warn("Did not detachFromModel() before attachToModel()");
			detachFromModel();
		}
		if (! initialised) {
		initialise(populate());
			initialised = true;
		}
		this.scuflModel = model;
		tree.attachToModel(model);
		model.addListener(eventListener);
	}

	// FIXME Move to general class
	// If expand is true, expands all nodes in the tree.
	// Otherwise, collapses all nodes in the tree.
	public static void expandAll(JTree tree, boolean expand) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();
		// Traverse tree from root
		expandAll(tree, new TreePath(root), expand);
	}

	// FIXME Move to general class
	private static void expandAll(JTree tree, TreePath parent, boolean expand) {
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

	private void cancelHighlight() {
		ScuflModelExplorerRenderer r = (ScuflModelExplorerRenderer) tree
				.getCellRenderer();
		if (regex.getText().equals("")) {
			r.setPattern(null);
		} else {
			String regexString = ".*" + regex.getText().toLowerCase() + ".*";
			r.setPattern(regexString);
		}
		tree.repaint();
	}

	private void jumpToAndHighlight() {
		String regexString = ".*" + regex.getText().toLowerCase() + ".*";
		// Update the renderer to colour the cells correctly based on match
		ScuflModelExplorerRenderer r = (ScuflModelExplorerRenderer) tree
				.getCellRenderer();
		r.setPattern(regexString);
		expandAll(tree, false);
		DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel
				.getRoot();
		Enumeration en = rootNode.depthFirstEnumeration();
		while (en.hasMoreElements()) {
			DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) en
					.nextElement();
			if (theNode.getUserObject().toString().toLowerCase().matches(
					regexString)) {
				TreePath path = new TreePath(treeModel.getPathToRoot(theNode));
				tree.makeVisible(path);
			}
		}
	}

	private ScuflModel scuflModel = null;

	public void detachFromModel() {
		if (scuflModel != null) {
			scuflModel.removeListener(eventListener);
			tree.detachFromModel();
			this.scuflModel = null;
		}
	}

	public String getName() {
		return "Available services";
	}

	public javax.swing.ImageIcon getIcon() {
		return TavernaIcons.windowScavenger;
	}

	public void onDisplay() {
		
	}

	public void onDispose() {
		detachFromModel();		
	}

}
