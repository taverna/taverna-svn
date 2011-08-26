/*
 * This file is a component of the Taverna project, and is licensed under the
 * GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.biomoby.shared.MobyData;
import org.biomoby.shared.MobyNamespace;
import org.biomoby.shared.MobyPrimaryDataSet;
import org.biomoby.shared.MobyPrimaryDataSimple;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scuflui.actions.AbstractProcessorAction;

/**
 * An action to add a parser from within the Workflow editor
 * 
 * @author Eddie Kawas
 */
public class AddParserAction extends AbstractProcessorAction {
    private static Logger logger = Logger.getLogger(AddParserAction.class);

    /*
         * (non-Javadoc)
         * 
         * @see org.embl.ebi.escience.scuflui.processoractions.AbstractProcessorAction#getComponent(org.embl.ebi.escience.scufl.Processor)
         */

    public JComponent getComponent(Processor processor) {
	// variables i need
	BiomobyProcessor theProcessor = (BiomobyProcessor) processor;
	// Central central = theProcessor.getCentralWorker();
	final Processor theproc = processor;
	final ScuflModel scuflModel = processor.getModel();
	// set up the root node
	String serviceName = theProcessor.getMobyService().getName();
	String description = theProcessor.getDescription();
	MobyServiceTreeNode service = new MobyServiceTreeNode(serviceName,
		description);
	DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(service);

	// now add the child nodes containing useful information about the
	// service
	DefaultMutableTreeNode output = new DefaultMutableTreeNode("Parse:");
	rootNode.add(output);
	// process outputs
	MobyData[] outputs = theProcessor.getMobyService().getPrimaryOutputs();
	for (int i = 0; i < outputs.length; i++) {
	    if (outputs[i] instanceof MobyPrimaryDataSimple) {
		MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple) outputs[i];
		StringBuffer sb = new StringBuffer(
			"Namespaces used by this object: ");
		MobyNamespace[] namespaces = simple.getNamespaces();
		for (int j = 0; j < namespaces.length; j++) {
		    sb.append(namespaces[j].getName() + " ");
		}
		if (namespaces.length == 0)
		    sb.append(" ANY ");
		MobyObjectTreeNode mobyObjectTreeNode = new MobyObjectTreeNode(
			simple.getDataType().getName() + "('"
				+ simple.getName() + "')", sb.toString());
		mobyObjectTreeNode.setNamespaces(simple.getNamespaces());
		output.insert(new DefaultMutableTreeNode(mobyObjectTreeNode),
			output.getChildCount());
	    } else {
		// we have a collection
		MobyPrimaryDataSet collection = (MobyPrimaryDataSet) outputs[i];
		DefaultMutableTreeNode collectionNode = new DefaultMutableTreeNode(
			"Collection('" + collection.getName() + "')");
		output.insert(collectionNode, output.getChildCount());
		MobyPrimaryDataSimple[] simples = collection.getElements();
		for (int j = 0; j < simples.length; j++) {
		    MobyPrimaryDataSimple simple = simples[j];
		    StringBuffer sb = new StringBuffer(
			    "Namespaces used by this object: ");
		    MobyNamespace[] namespaces = simple.getNamespaces();
		    for (int k = 0; k < namespaces.length; k++) {
			sb.append(namespaces[k].getName() + " ");
		    }
		    if (namespaces.length == 0)
			sb.append("ANY ");
		    MobyObjectTreeNode mobyObjectTreeNode = new MobyObjectTreeNode(
			    simple.getDataType().getName() + "('"
				    + simple.getName() + "')", sb.toString());
		    mobyObjectTreeNode.setNamespaces(simple.getNamespaces());
		    collectionNode
			    .insert(new DefaultMutableTreeNode(
				    mobyObjectTreeNode), collectionNode
				    .getChildCount());
		}

	    }
	}
	if (outputs.length == 0) {
	    output.add(new DefaultMutableTreeNode(" None "));
	}

	// finally return a tree describing the object
	final JTree tree = new JTree(rootNode);
	tree.setCellRenderer(new BioMobyServiceTreeCustomRenderer());
	ToolTipManager.sharedInstance().registerComponent(tree);
	tree.addMouseListener(new MouseListener() {
	    public void mouseClicked(MouseEvent me) {
	    }

	    public void mousePressed(MouseEvent me) {
		mouseReleased(me);
	    }

	    public void mouseReleased(MouseEvent me) {
		if (me.isPopupTrigger()) // right click, show popup menu
		{
		    TreePath path = tree.getPathForLocation(me.getX(), me
			    .getY());
		    if (path == null)
			return;
		    if (path.getPathCount() >= 3) {
			if (path.getParentPath().toString().indexOf("Parse:") >= 0
				&& path.getLastPathComponent().toString()
					.indexOf(" None ") == -1) {
			    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
				    .getLastSelectedPathComponent();
			    if (node == null)
				return;
			    final String selectedObject = node.toString();
			    if (!selectedObject.equals(path
				    .getLastPathComponent().toString()))
				return;

			    logger.debug("TreePath " + path.toString());
			    if (
			    // path has a collection in it
			    (path.getPathCount() == 4
				    && path.getParentPath()
					    .getLastPathComponent().toString()
					    .startsWith("Collection(") && (path
				    .getParentPath().toString())
				    .indexOf("Parse:") > 0)
				    // or path is just a simple
				    || (path.toString().indexOf("Collection(") < 0)) {

				final JPopupMenu menu = new JPopupMenu();

				final String potentialCollectionString = path
					.getParentPath().getLastPathComponent()
					.toString();
				final boolean isCollection = potentialCollectionString
					.indexOf("Collection('") >= 0;
				JMenuItem item3 = new JMenuItem(
					"Add parser for " + selectedObject
						+ " to the workflow");
				item3
					.setIcon(getIcon("org/biomoby/client/taverna/plugin/Cut24.gif"));
				item3.addActionListener(new ActionListener() {

				    public void actionPerformed(ActionEvent ae) {
					// you would like to search for
					// selectedObject
					try {
					    String name = selectedObject;
					    if (name.indexOf("(") > 0)
						name = name.substring(0, name
							.indexOf("("));
					    String workflowName = theproc
						    .getModel()
						    .getValidProcessorName(
							    "Parse Moby Data("
								    + name
								    + ")");
					    String articlename = "";
					    if (isCollection) {
						articlename = potentialCollectionString
							.substring(
								potentialCollectionString
									.indexOf("('") + 2,
								potentialCollectionString
									.lastIndexOf("'"));
					    } else {
						articlename = selectedObject
							.substring(
								selectedObject
									.indexOf("'") + 1,
								selectedObject
									.lastIndexOf("'"));
					    }
					    Processor parser;
					    try {
						parser = new MobyParseDatatypeProcessor(
							scuflModel,
							workflowName,
							name,
							articlename,
							((BiomobyProcessor) theproc)
								.getMobyEndpoint());
						scuflModel.addProcessor(parser);
					    } catch (ProcessorCreationException pce) {
						JOptionPane
							.showMessageDialog(
								null,
								"Processor creation exception : \n"
									+ pce
										.getMessage(),
								"Exception!",
								JOptionPane.ERROR_MESSAGE);
						return;
					    } catch (DuplicateProcessorNameException dpne) {
						JOptionPane
							.showMessageDialog(
								null,
								"Duplicate name : \n"
									+ dpne
										.getMessage(),
								"Exception!",
								JOptionPane.ERROR_MESSAGE);
						return;
					    }

					    try {
						if (scuflModel != null) {
						    Port theServiceport = null;
						    if (isCollection)
							theServiceport = theproc
								.locatePort(
									name
										+ "(Collection - '"
										+ (articlename
											.equals("") ? "MobyCollection"
											: articlename)
										+ "' As Simples)",
									false);
						    else
							theServiceport = theproc
								.locatePort(
									name
										+ "("
										+ articlename
										+ ")",
									false);
						    if (theServiceport == null)
							return;
						    scuflModel
							    .addDataConstraint(new DataConstraint(
								    scuflModel,
								    theServiceport,
								    parser
									    .getInputPorts()[0]));
						} else {
						    logger.info("Null model");
						}
					    } catch (DataConstraintCreationException dcce) {
						logger.error(dcce);
						return;
					    } catch (UnknownPortException e) {
						logger.error(e);
						return;
					    }

					} catch (Exception e) {
					    logger.error(e);
					}

				    }
				});

				menu.add(new JSeparator());
				menu.add(new JLabel("Parse Moby Data ... ",
					JLabel.CENTER));
				menu.add(new JSeparator());
				menu.add(item3);
				menu.show(me.getComponent(), me.getX(), me
					.getY());
			    } else {
				logger
					.debug("unexpected situation occured; '"
						+ selectedObject
						+ "' was the object selected and the path is: "
						+ path.toString());
			    }
			}
		    }
		}
	    }

	    public void mouseEntered(MouseEvent me) {
	    }

	    public void mouseExited(MouseEvent me) {
	    }
	});

	tree.getSelectionModel().setSelectionMode(
		TreeSelectionModel.SINGLE_TREE_SELECTION);
	JScrollPane jsp = new JScrollPane(tree);
	JPanel thePanel = new JPanel(new BorderLayout());
	thePanel.add(jsp, BorderLayout.CENTER);
	return thePanel;
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#canHandle(org.embl.ebi.escience.scufl.Processor)
         */
    public boolean canHandle(Processor processor) {
	return (processor instanceof BiomobyProcessor)
		&& (((BiomobyProcessor) processor).getMobyService() != null);
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#getDescription()
         */
    public String getDescription() {
	return "Add BioMOBY Parser ...";
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#getIcon()
         */
    public ImageIcon getIcon() {
	Class<?> cls = this.getClass();
	URL url = cls.getClassLoader().getResource(
		"org/biomoby/client/taverna/plugin/Cut24.gif");
	return new ImageIcon(url);
    }

    /**
         * returns the frame size as a dimension for the content pane housing
         * this action
         */
    public Dimension getFrameSize() {
	return new Dimension(450, 450);
    }

    /**
         * Return an Icon to represent this action
         * 
         * @param loc
         *                the location of the image to use as an icon
         */
    private ImageIcon getIcon(String loc) {
	Class<?> cls = this.getClass();
	URL url = cls.getClassLoader().getResource(loc);
	return new ImageIcon(url);
    }
}