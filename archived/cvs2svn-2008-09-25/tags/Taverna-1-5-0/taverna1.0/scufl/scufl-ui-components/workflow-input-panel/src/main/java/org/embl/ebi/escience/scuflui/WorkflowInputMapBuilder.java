/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright 2004 University of Nottingham
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.baclava.factory.Flavours;
import org.embl.ebi.escience.baclava.store.BaclavaDataService;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scuflui.renderers.RendererException;
import org.embl.ebi.escience.scuflui.renderers.RendererRegistry;
import org.embl.ebi.escience.scuflui.shared.BytesSelection;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.shared.XMLTree;
import org.embl.ebi.escience.scuflui.spi.RendererSPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Panel to construct the input for a workflow.
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @author Stian Soiland
 * @version $Revision: 1.11 $
 */
public class WorkflowInputMapBuilder extends JPanel implements
        WorkflowModelViewSPI, ScuflModelEventListener {

    private static Logger logger = Logger
            .getLogger(WorkflowInputMapBuilder.class);

    class PortTreeMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent event) {
            if (event.isPopupTrigger()) {
                try {
                    // TODO NullPointer if not over node
                    PanelTreeNode node = (PanelTreeNode) portTree
                            .getPathForLocation(event.getX(), event.getY())
                            .getLastPathComponent();
                    JPopupMenu popup = new JPopupMenu();
                    node.fillMenu(popup);
                    popup
                            .show(event.getComponent(), event.getX(), event
                                    .getY());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class PortTreeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent event) {
            if (event.getPath().getLastPathComponent() instanceof PanelTreeNode) {
                PanelTreeNode node = (PanelTreeNode) event.getPath()
                        .getLastPathComponent();
                splitter.setRightComponent(node.getPanel());
                if (node instanceof InputsRootNode) {
                    loadInputsButton.setEnabled(false);
                    newInputButton.setEnabled(false);
                    newListButton.setEnabled(false);
                    removeButton.setEnabled(false);
                } else if (node instanceof InputPortNode) {
                    loadInputsButton.setEnabled(true);
                    newInputButton
                            .setEnabled(canAddInputs((InputListNode) node));
                    newListButton.setEnabled(canAddLists((InputListNode) node));
                    removeButton.setEnabled(false);
                } else if (node instanceof InputListNode) {
                    loadInputsButton.setEnabled(true);
                    removeButton.setEnabled(true);

                    InputListNode parent = (InputListNode) ((InputListNode) node)
                            .getParent();
                    if (parent != null) {
                        boolean canAddList = true;
                        boolean canAddInput = true;
                        for (int index = 0; index < parent.getChildCount(); index++) {
                            InputListNode aListNode = (InputListNode) parent
                                    .getChildAt(index);
                            if (aListNode.getChildCount() > 0) {
                                canAddList = aListNode.getFirstChild() instanceof InputListNode;
                                canAddInput = !canAddList;
                                break;
                            }
                        }
                        newListButton.setEnabled(canAddList);
                        newInputButton.setEnabled(canAddInput);
                    } else {
                        newInputButton.setEnabled(false);
                        newListButton.setEnabled(false);
                    }
                } else if (node instanceof InputDataThingNode) {
                    InputDataThingNode thingNode = (InputDataThingNode) node;
                    InputListNode parent = (InputListNode) thingNode
                            .getParent();
                    if (parent == null) {
                        newInputButton.setEnabled(false);
                        newListButton.setEnabled(false);
                    } else {
                        newInputButton.setEnabled(canAddInputs(parent));
                        newListButton.setEnabled(canAddLists(parent));
                    }
                    loadInputsButton.setEnabled(true);
                    removeButton.setEnabled(true);
                }
            } else {
                if (splitter.getRightComponent() != null) {
                    splitter.remove(splitter.getRightComponent());
                }
                loadInputsButton.setEnabled(false);
                newInputButton.setEnabled(false);
                newListButton.setEnabled(false);
                removeButton.setEnabled(false);
            }
            splitter.validate();
        }

        private boolean canAddLists(InputListNode node) {
            if (node.getChildCount() > 0) {
                if (node.getFirstChild() instanceof InputDataThingNode) {
                    return false;
                }
            }
            return true;
        }

        private boolean canAddInputs(InputListNode node) {
            if (node.getChildCount() > 0) {
                if (node.getFirstChild() instanceof InputListNode) {
                    return false;
                }
            }
            return true;
        }
    }

    private interface PanelTreeNode {
        public JComponent getPanel();

        public void fillMenu(JPopupMenu menu);
    }

    private interface DataThingNode {
        public DataThing getDataThing();
    }

    private ActionListener loadInputDocAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            try {
                int returnVal = fileChooser
                        .showOpenDialog(WorkflowInputMapBuilder.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    InputStreamReader stream = new InputStreamReader(
                            new FileInputStream(file), Charset.forName("UTF-8"));
                    Document inputDoc = new SAXBuilder(false).build(stream);
                    Map<String, DataThing> inputMap = DataThingXMLFactory
                            .parseDataDocument(inputDoc);
                    try {
                        setWorkflowInputs(inputMap);
                    } catch (InputsNotMatchingException iex) {
                        logger.error(
                                "Could not load input document, missing input",
                                iex);
                        JOptionPane
                                .showMessageDialog(
                                        WorkflowInputMapBuilder.this,
                                        "Could not load input document.\n"
                                                + "Input document is missing input port '"
                                                + iex.getMessage() + "'.",
                                        "Missing input",
                                        JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // FIXME: Force update
                    // getPanel();
                }
            } catch (Exception ex) {
                logger.error("Could not load input document", ex);
                JOptionPane.showMessageDialog(WorkflowInputMapBuilder.this,
                        "Problem loading input document: \n" + ex,
                        "Exception!", JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    private ActionListener saveInputDocAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            try {
                int returnVal = fileChooser
                        .showSaveDialog(WorkflowInputMapBuilder.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    // FileFilter fileFilter =
                    // fileChooser.getFileFilter();
                    OutputStreamWriter fileWriter = new OutputStreamWriter(
                            new FileOutputStream(file), Charset
                                    .forName("UTF-8"));
                    BufferedWriter writer = new BufferedWriter(fileWriter);
                    // XMLOutputter outputter = new XMLOutputter(Format
                    // .getCompactFormat());
                    XMLOutputter outputter = new XMLOutputter(Format
                            .getPrettyFormat());
                    BufferedReader reader = new BufferedReader(
                            new StringReader(outputter
                                    .outputString(DataThingXMLFactory
                                            .getDataDocument(bakeInputMap()))));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                    writer.flush();
                    fileWriter.flush();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(WorkflowInputMapBuilder.this,
                        "Problem opening content from web : \n"
                                + ex.getMessage(), "Exception!",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    public class TreeTransferHandler implements DropTargetListener {
        private JTree tree;

        public TreeTransferHandler(JTree tree) {
            this.tree = tree;
            new DropTarget(tree, this);
        }

        public boolean canDrop(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(Flavours.DATATHING_FLAVOUR)) {
                try {
                    Point pt = dtde.getLocation();
                    TreePath pathTarget = tree.getPathForLocation(pt.x, pt.y);
                    Object targetNode = pathTarget.getLastPathComponent();
                    return targetNode instanceof InputListNode;
                } catch (NullPointerException e) {
                    return false;
                }
            }
            if (dtde.isDataFlavorSupported(Flavours.LSID_FLAVOUR)) {
                return true;
            }
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
         */
        public void dragEnter(DropTargetDragEvent dtde) {
            int action = dtde.getDropAction();
            if (canDrop(dtde)) {
                dtde.acceptDrag(action);
            } else {
                dtde.rejectDrag();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
         */
        public void dragOver(DropTargetDragEvent dtde) {
            // Point pt = dtde.getLocation();
            int action = dtde.getDropAction();
            if (canDrop(dtde)) {
                dtde.acceptDrag(action);
            } else {
                dtde.rejectDrag();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
         */
        public void dropActionChanged(DropTargetDragEvent dtde) {
            // Nowt to do 'ere. Well, I don't think so anyway
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
         */
        public void drop(DropTargetDropEvent dtde) {
            try {
                int action = dtde.getDropAction();
                Transferable transferable = dtde.getTransferable();
                Point pt = dtde.getLocation();
                TreePath pathTarget = tree.getPathForLocation(pt.x, pt.y);
                TreeNode targetNode = (TreeNode) pathTarget
                        .getLastPathComponent();
                if (executeDrop(targetNode, transferable, action)) {
                    dtde.acceptDrop(action);
                    dtde.dropComplete(true);
                    return;
                }
                dtde.rejectDrop();
                dtde.dropComplete(false);
            } catch (Exception e) {
                logger.warn("Could not accept drop", e);
                dtde.rejectDrop();
                dtde.dropComplete(false);
            }
        }

        /**
         * @param targetNode
         * @param transferable
         * @param action
         * @return <code>true</code> if the drop executed successfully
         */
        private boolean executeDrop(TreeNode targetNode,
                Transferable transferable, int action) {
            if (transferable.isDataFlavorSupported(Flavours.DATATHING_FLAVOUR)) {
                if (targetNode instanceof InputListNode) {
                    try {
                        String elementText = (String) transferable
                                .getTransferData(Flavours.DATATHING_FLAVOUR);
                        Document inputDoc = new SAXBuilder(false)
                                .build(new StringReader(elementText));
                        DataThing thing = new DataThing(inputDoc
                                .getRootElement());
                        InputListNode node = (InputListNode) targetNode;
                        node.setDataThing(thing);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
         */
        public void dragExit(DropTargetEvent dte) {
            // Not doin' owt here
        }
    }

    private class InputsRootNode extends DefaultMutableTreeNode implements
            PanelTreeNode {
        JComponent panel;

        XMLTree xmlTree;

        JScrollPane scrollPane;

        /*
         * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#getPanel()
         */
        public JComponent getPanel() {
            if (panel == null) {
                // FIXME: Should be initialize()
                panel = new JPanel(new BorderLayout());

                scrollPane = new JScrollPane();
                scrollPane.setPreferredSize(new Dimension(0, 0));
                JToolBar toolbar = new JToolBar();
                // JButton loadInputDocButton = new JButton("Load Input Doc",
                // TavernaIcons.openIcon);
                // JButton saveInputDocButton = new JButton("Save Input Doc",
                // TavernaIcons.saveIcon);
                //
                // loadInputDocButton.setToolTipText("Load Input Document");
                // loadInputDocButton.addActionListener(loadInputDocAction);
                //
                // saveInputDocButton.setToolTipText("Save Input Document");
                // saveInputDocButton.addActionListener(saveInputDocAction);

                toolbar.setFloatable(false);
                toolbar.setRollover(true);
                // toolbar.add(loadInputDocButton);
                // toolbar.add(saveInputDocButton);

                panel.add(scrollPane, BorderLayout.CENTER);
                panel.add(toolbar, BorderLayout.NORTH);
                panel.setPreferredSize(new Dimension(0, 0));
            }
            try {
                setWorkflowInputs(bakeInputMap());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return panel;
        }

        private void populateInputs(Map<String, DataThing> inputMap)
                throws InputsNotMatchingException {
            for (int index = 0; index < rootNode.getChildCount(); index++) {
                InputPortNode portNode = (InputPortNode) rootNode
                        .getChildAt(index);
                DataThing thing = (DataThing) inputMap.get(portNode.toString());
                if (thing == null) {
                    throw new InputsNotMatchingException(portNode.toString());
                }
                portNode.removeAllChildren();
                portNode.addDataThing(thing);
            }
        }

        public void setWorkflowInputs(Map<String, DataThing> inputs)
                throws InputsNotMatchingException {
            if (panel == null) {
                // Initialize
                getPanel();
            }
            populateInputs(inputs);
            scrollPane.setViewportView(new XMLTree(DataThingXMLFactory
                    .getDataDocument(inputs)));
        }

        /*
         * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#fillMenu(javax.swing.JPopupMenu)
         */
        public void fillMenu(JPopupMenu menu) {
            JMenuItem loadItem = new JMenuItem("Load Input Document",
                    TavernaIcons.openIcon);
            loadItem.addActionListener(loadInputDocAction);
            JMenuItem saveItem = new JMenuItem("Save Input Document",
                    TavernaIcons.saveIcon);
            saveItem.addActionListener(saveInputDocAction);
            menu.add(loadItem);
            // menu.add(importItem);
            menu.add(saveItem);
        }

        public String toString() {
            return "Input Document";
        }
    }

    private class InputListNode extends DefaultMutableTreeNode implements
            PanelTreeNode, DataThingNode {
        private DataThing thing;

        public InputListNode(Object stuff) {
            super(stuff);
        }

        public InputListNode(DataThing thing) {
            this.thing = thing;
        }

        public Port getPort() {
            return ((InputListNode) parent).getPort();
        }

        public boolean isText() {
            return ((InputListNode) parent).isText();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#getPanel()
         */
        public JComponent getPanel() {
            getDataThing();
            if (thing == null) {
                return null;
            }
            RendererRegistry registry = RendererRegistry.instance();
            RendererSPI renderer = registry.getRenderer(thing);
            if (renderer == null) {
                logger.warn("Could not find renderer for: " + thing);
                return null;
            }
            try {
                JScrollPane scrollPane = new JScrollPane(renderer.getComponent(
                        registry, thing));
                scrollPane.setPreferredSize(new Dimension(0, 0));
                return scrollPane;
            } catch (RendererException e) {
                logger.warn("Could not render: " + thing, e);
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#fillMenu(javax.swing.JPopupMenu)
         */
        public void fillMenu(JPopupMenu menu) {
            JMenuItem createItem = new JMenuItem("New Input Value",
                    TavernaIcons.newInputIcon);
            createItem.addActionListener(newInputAction);
            JMenuItem createListItem = new JMenuItem("New List",
                    TavernaIcons.newListIcon);
            createListItem.addActionListener(newListAction);
            JMenuItem removeItem = new JMenuItem("Remove",
                    TavernaIcons.deleteIcon);
            removeItem.addActionListener(removeAction);

            menu.add(createItem);
            menu.add(createListItem);
            menu.add(removeItem);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.DataThingNode#getDataThing()
         */
        public DataThing getDataThing() {
            ArrayList inputList = new ArrayList();
            HashMap dataThingList = new HashMap();
            for (int index = 0; index < getChildCount(); index++) {
                DataThingNode inputNode = (DataThingNode) getChildAt(index);
                DataThing childThing = inputNode.getDataThing();
                inputList.add(childThing.getDataObject());
                dataThingList.put(childThing.getDataObject(), childThing);
            }
            DataThing result = DataThingFactory.bake(inputList);
            // Make sure metadata is persisted
            for (int index = 0; index < inputList.size(); index++) {
                result.copyMetadataFrom((DataThing) dataThingList.get(inputList
                        .get(index)));
            }
            if (thing != null) {
                result.copyMetadataFrom(thing);
            }
            thing = result;
            return thing;
        }

        public String toString() {
            return "Input List";
        }

        public void setDataThing(DataThing thing) {
            if (thing != null) {
                Object dataObject = thing.getDataObject();
                if (dataObject instanceof Element) {
                    dataObject = DataThingXMLFactory.configureDataThing(
                            (Element) dataObject, thing);
                }
                if (dataObject instanceof Collection) {
                    Iterator iterator = thing.childIterator();
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        if (next instanceof DataThing) {
                            DataThing childThing = (DataThing) next;
                            addDataThing(childThing);
                        }
                    }
                } else {
                    addDataThing(thing);
                }
                treeModel.nodeStructureChanged(this);
                this.thing = thing;
            }
        }

        public void addDataThing(DataThing thing) {
            if (thing.getDataObject() instanceof Collection) {
                InputListNode child = new InputListNode(thing);
                add(child);
                child.setDataThing(thing);
            } else {
                InputDataThingNode child = new InputDataThingNode(getPort(),
                        thing);
                add(child);
            }
        }
    }

    private class InputPortNode extends InputListNode {
        private Port port;

        private JPanel portPanel;

        public InputPortNode(Port port) {
            super(port);
            this.port = port;
        }

        public Port getPort() {
            return port;
        }

        /*
         * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#getPanel()
         */
        // public JComponent getPanel() {
        // if (portPanel == null) {
        // portPanel = new JPanel(new BorderLayout(3, 3));
        // portPanel
        // .setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        // JPanel descriptionPanel = new JPanel();
        // StringBuffer sb = new StringBuffer();
        // sb.append("<html><h2>Workflow Input : " + port.getName()
        // + "</h2>");
        // sb
        // .append("<table border=\"1\"><tr><td bgcolor=\"#ddeeff\"
        // colspan=\"2\"><b>Input Metadata</b></td></tr><tr><td
        // bgcolor=\"#ddeeff\"><b>Semantic type</b></td><td>\n");
        // if (port.getMetadata().getSemanticType() != null
        // && port.getMetadata().getSemanticType() != "") {
        // sb.append(port.getMetadata().getSemanticType());
        // } else {
        // sb
        // .append("<font color=\"#666666\"><i>not specified</i></font>");
        // }
        // sb.append("</td></tr>\n");
        // sb
        // .append("<tr><td bgcolor=\"#ddeeff\"><b>Syntactic
        // type</b></td><td>");
        // String[] bits = port.getSyntacticType().split("'");
        // sb.append(bits[0]);
        // String[] types = port.getMetadata().getMIMETypes();
        // for (int k = 0; k < types.length; k++) {
        // if (k > 0) {
        // sb.append(",");
        // }
        // sb.append(types[k]);
        // }
        // sb.append(bits[bits.length - 1]);
        // sb.append("</td></tr></table><p>");
        // if (port.getMetadata().getDescription() != null
        // && port.getMetadata().getDescription() != "") {
        // sb.append(port.getMetadata().getDescription());
        // } else {
        // sb.append("<font color=\"#666666\"><i>no description</i></font>");
        // }
        // sb.append("</p></html>");
        // JEditorPane portDetails = new JEditorPane("text/html", sb
        // .toString());
        // portDetails.setEditable(false);
        // JScrollPane scrollPane = new JScrollPane(portDetails);
        // scrollPane.setPreferredSize(new Dimension(0, 0));
        //
        // descriptionPanel.setLayout(new BoxLayout(descriptionPanel,
        // BoxLayout.Y_AXIS));
        // descriptionPanel.add(scrollPane);
        // portPanel.add(descriptionPanel, BorderLayout.CENTER);
        // }
        // return portPanel;
        // }
        public boolean isText() {
            logger.debug(port.getMetadata().getDisplayTypeList());
            for (int index = 0; index < port.getMetadata().getMIMETypeList()
                    .size(); index++) {
                String mimeType = (String) port.getMetadata().getMIMETypeList()
                        .get(index);
                if (textPattern.matcher(mimeType).matches()) {
                    return true;
                }
            }
            return false;
        }

        /*
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return port.toString();
        }

        /*
         * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#fillMenu(javax.swing.JPopupMenu)
         */
        public void fillMenu(JPopupMenu menu) {
            JMenuItem createItem = new JMenuItem("New Input Value",
                    TavernaIcons.newInputIcon);
            createItem.addActionListener(newInputAction);
            JMenuItem createListItem = new JMenuItem("New List",
                    TavernaIcons.newListIcon);
            createListItem.addActionListener(newListAction);

            menu.add(createItem);
            menu.add(createListItem);
        }

        public DataThing getDataThing() {
            if (getChildCount() == 1) {
                return ((DataThingNode) getFirstChild()).getDataThing();
            }
            return super.getDataThing();
        }
    }

    public class PortDescription extends JPanel {
        Port port;

        public PortDescription(Port port) {
            super(new BorderLayout());
            this.port = port;
            add(makeTitle(), BorderLayout.NORTH);
            add(makeDescription(), BorderLayout.CENTER);
        }

        public ShadedLabel makeTitle() {
            return new ShadedLabel(port.getName(), ShadedLabel.TAVERNA_BLUE);
        }

        public JTextArea makeDescription() {
            String descriptionTxt = port.getMetadata().getDescription();
            JTextArea description = new JTextArea(descriptionTxt);
            description.setEditable(false);
            description.setLineWrap(true);
            description.setOpaque(false);
            description.setWrapStyleWord(true);
            // Avoid stealing all width of the split pane
            description.setMinimumSize(new Dimension(25, 10));
            return description;
        }
    }

    private class InputDataThingNode extends DefaultMutableTreeNode implements
            PanelTreeNode, DataThingNode {
        private DataThing thing;

        JComponent panel;

        private List mimeTypes;

        JTextArea editor;

        private ActionListener loadURLAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = (String) JOptionPane.showInputDialog(
                            WorkflowInputMapBuilder.this, "URL to open?",
                            "URL Required", JOptionPane.QUESTION_MESSAGE, null,
                            null, "http://");
                    if (name != null) {
                        InputStream is = new URL(name).openStream();
                        if (isText()) {
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(is));
                            StringBuffer sb = new StringBuffer();
                            String s = null;
                            while ((s = reader.readLine()) != null) {
                                sb.append(s);
                                sb.append("\n");
                            }
                            editor.setText(sb.toString());
                        } else {
                            // FIXME: Avoid doing 1 byte at a time (SLOW)
                            int input = 0;
                            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                            while ((input = is.read()) != -1) {
                                byteStream.write(input);
                            }
                            byteStream.flush();
                            setUserObject(byteStream.toByteArray());
                            panel = null;
                            updatePanel();
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "Problem opening content from web : \n"
                                    + ex.getMessage(), "Exception!",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        private ActionListener loadFileAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int returnVal = fileChooser
                            .showOpenDialog(WorkflowInputMapBuilder.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        if (isText()) {
                            BufferedReader reader = new BufferedReader(
                                    new FileReader(file));
                            StringBuffer sb = new StringBuffer();
                            String s = null;
                            while ((s = reader.readLine()) != null) {
                                sb.append(s);
                                sb.append("\n");
                            }
                            editor.setText(sb.toString());
                        } else {
                            byte[] bytes = FileUtils.readFileToByteArray(file);
                            setUserObject(bytes);
                            panel = null;
                            updatePanel();
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(WorkflowInputMapBuilder.this,
                            "Problem opening content from file : \n"
                                    + ex.getMessage(), "Exception!",
                            JOptionPane.ERROR_MESSAGE);
                    logger.error("Error opening file:", ex);
                }
            }
        };

        private ActionListener pasteAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Clipboard clipboard = Toolkit.getDefaultToolkit()
                            .getSystemClipboard();

                    Transferable contents = clipboard.getContents(null);
                    if (isText()) {
                        boolean hasTransferableText = (contents != null)
                                && contents
                                        .isDataFlavorSupported(DataFlavor.stringFlavor);
                        if (hasTransferableText) {
                            String text = (String) contents
                                    .getTransferData(DataFlavor.stringFlavor);
                            editor.setText(text);
                        }
                    } else {
                        boolean hasTransferableBytes = (contents != null)
                                && contents
                                        .isDataFlavorSupported(BytesSelection.bytesFlavor);
                        if (hasTransferableBytes) {
                            byte[] bytes = (byte[]) contents
                                    .getTransferData(BytesSelection.bytesFlavor);
                            setUserObject(bytes);
                            panel = null;
                            updatePanel();
                        }
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(WorkflowInputMapBuilder.this,
                            "Problem pasting content from clipboard : \n"
                                    + ex.getMessage(), "Exception!",
                            JOptionPane.ERROR_MESSAGE);
                    logger.error("Error pasting from clipboard:", ex);
                }
            }
        };

        private ActionListener loadLSIDAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String lsid = (String) JOptionPane.showInputDialog(
                            WorkflowInputMapBuilder.this, "LSID to open?",
                            "LSID Required", JOptionPane.QUESTION_MESSAGE,
                            null, null, "URN:LSID:");
                    if (lsid != null && store != null) {
                        DataThing thing = store.fetchDataThing(lsid);
                        setDataThing(thing);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(WorkflowInputMapBuilder.this,
                            "Problem opening content from web : \n"
                                    + ex.getMessage(), "Exception!",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        private Port port;

        public InputDataThingNode(Port port, Object inputValue, List mimeTypes) {
            super(inputValue);
            this.port = port;
            this.mimeTypes = mimeTypes;
        }

        public InputDataThingNode(Port port, DataThing thing) {
            super(thing.getDataObject());
            this.port = port;
            this.thing = thing;
            this.mimeTypes = thing.getMetadata().getMIMETypeList();
        }

        /*
         * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#getPanel()
         */
        public JComponent getPanel() {
            if (panel != null) {
                return panel;
            }
            // JToolBar toolbar = new JToolBar();
            // JButton loadButton = new JButton("Load", TavernaIcons.openIcon);
            // JButton loadURLButton = new JButton("Load from URL",
            // TavernaIcons.webIcon);
            // loadButton.setToolTipText("Load from File");
            // loadButton.addActionListener(loadFileAction);
            // loadURLButton.addActionListener(loadURLAction);
            // toolbar.setFloatable(false);
            // toolbar.setRollover(true);
            // toolbar.add(loadButton);
            // toolbar.add(loadURLButton);
            // if (store != null) {
            // JButton loadLSIDButton = new JButton("Load LSID");
            // loadLSIDButton.addActionListener(loadLSIDAction);
            // toolbar.add(loadLSIDButton);
            // }
            panel = new JPanel(new BorderLayout());
            // panel.add(toolbar, BorderLayout.NORTH);

            panel.add(new PortDescription(port), BorderLayout.NORTH);

            if (isText()) {
                editor = new JTextArea();
                editor.setText((String) getUserObject());
                editor.getDocument().addDocumentListener(
                        new DocumentListener() {
                            public void insertUpdate(DocumentEvent e) {
                                setUserObject(editor.getText());
                                treeModel.nodeChanged(InputDataThingNode.this);
                            }

                            public void removeUpdate(DocumentEvent e) {
                                setUserObject(editor.getText());
                                treeModel.nodeChanged(InputDataThingNode.this);
                            }

                            public void changedUpdate(DocumentEvent e) {
                                setUserObject(editor.getText());
                                treeModel.nodeChanged(InputDataThingNode.this);
                            }
                        });

                JScrollPane scrollPane = new JScrollPane(editor);
                scrollPane.setPreferredSize(new Dimension(0, 0));

                panel.add(scrollPane, BorderLayout.CENTER);

            } else {
                RendererRegistry registry = RendererRegistry.instance();
                RendererSPI renderer = registry.getRenderer(getDataThing());
                try {
                    JScrollPane scrollPane = new JScrollPane(renderer
                            .getComponent(registry, thing));
                    scrollPane.setPreferredSize(new Dimension(0, 0));
                    panel.add(scrollPane, BorderLayout.CENTER);
                } catch (RendererException e) {
                    e.printStackTrace();
                }
            }
            return panel;
        }

        public DataThing getDataThing() {
            DataThing newThing = DataThingFactory.bake(getUserObject());
            if (thing != null) {
                newThing.getMetadata().setMIMETypes(mimeTypes);
                newThing.copyMetadataFrom(thing);
            } else {
                newThing.getMetadata().setMIMETypes(mimeTypes);
            }
            thing = newThing;
            return thing;
        }

        public boolean isText() {
            for (int index = 0; index < mimeTypes.size(); index++) {
                String mimeType = (String) mimeTypes.get(index);
                if (textPattern.matcher(mimeType).matches()) {
                    return true;
                }
            }
            return false;
        }

        public void setDataThing(DataThing thing) {
            this.thing = thing;
            setUserObject(thing.getDataObject());
            panel = null;
        }

        /*
         * @see java.lang.Object#toString()
         */
        public String toString() {
            String summaryText = "bleh";
            if (userObject instanceof String) {
                summaryText = (String) userObject;
                if (summaryText.length() > 25) {
                    summaryText = "<html><em>Click to edit...</em></html>";
                }
            } else {
                DataThing thing = getDataThing();
                summaryText = thing.getMostInterestingMIMETypeForObject(thing
                        .getDataObject());
            }
            return summaryText;
        }

        /*
         * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#fillMenu(javax.swing.JPopupMenu)
         */
        public void fillMenu(JPopupMenu menu) {
            JMenuItem removeItem = new JMenuItem("Remove",
                    TavernaIcons.deleteIcon);
            removeItem.addActionListener(removeAction);
            JMenuItem loadFileItem = new JMenuItem("Load Input from File",
                    TavernaIcons.openIcon);
            loadFileItem.addActionListener(loadFileAction);
            JMenuItem loadURLItem = new JMenuItem("Load Input from URL",
                    TavernaIcons.openIcon);
            loadURLItem.addActionListener(loadURLAction);
            JMenuItem loadClipboardItem = new JMenuItem(
                    "Paste", TavernaIcons.pasteIcon);
            loadClipboardItem.setAccelerator(KeyStroke.getKeyStroke(
                    "ctrl V"));
            loadClipboardItem.setMnemonic(KeyEvent.VK_PASTE);
            loadClipboardItem.addActionListener(pasteAction);
            menu.add(removeItem);
            menu.add(loadFileItem);
            menu.add(loadURLItem);
            menu.add(loadClipboardItem);
            if (store != null) {
                JMenuItem loadLSIDItem = new JMenuItem("Load Input from LSID");
                loadLSIDItem.addActionListener(loadLSIDAction);
                menu.add(loadLSIDItem);
            }
        }
    }

    private class InputNodeRenderer extends DefaultTreeCellRenderer {
        /*
         * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
         *      java.lang.Object, boolean, boolean, boolean, int, boolean)
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf, row, hasFocus);
            if (value instanceof InputPortNode) {
                setIcon(TavernaIcons.inputIcon);
            } else if (value instanceof InputDataThingNode) {
                setIcon(TavernaIcons.inputValueIcon);
            } else if (value instanceof InputListNode) {
                if (expanded) {
                    setIcon(TavernaIcons.folderOpenIcon);
                } else {
                    setIcon(TavernaIcons.folderClosedIcon);
                }
            } else if (expanded) {
                setIcon(TavernaIcons.folderOpenIcon);
            } else {
                setIcon(TavernaIcons.folderClosedIcon);
            }
            return this;
        }
    }

    // not static, then it would keep a reference to us (!)
    JFileChooser fileChooser = new JFileChooser();

    static BaclavaDataService store = null;

    ScuflModel model = null;

    InputsRootNode rootNode = new InputsRootNode();

    DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

    JSplitPane splitter;

    JTree portTree;

    static Pattern textPattern = Pattern.compile(".*text/.*");;

    JButton loadInputsButton;

    JButton newInputButton;

    JButton newListButton;

    JButton removeButton;

    ActionListener newInputAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            InputListNode parent = null;
            if (portTree.getSelectionPath().getLastPathComponent() instanceof InputDataThingNode) {
                InputDataThingNode node = (InputDataThingNode) portTree
                        .getSelectionPath().getLastPathComponent();
                parent = (InputListNode) node.getParent();
            } else {
                parent = (InputListNode) portTree.getSelectionPath()
                        .getLastPathComponent();
            }
            InputDataThingNode newNode;
            if (parent.isText()) {
                newNode = new InputDataThingNode(parent.getPort(),
                        "Some input data goes here", parent.getPort()
                                .getMetadata().getMIMETypeList());
            } else {
                newNode = new InputDataThingNode(parent.getPort(), new byte[0],
                        parent.getPort().getMetadata().getMIMETypeList());
            }
            parent.add(newNode);
            treeModel.nodeStructureChanged(parent);
            portTree.setSelectionPath(new TreePath(newNode.getPath()));
        }
    };

    ActionListener newListAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) portTree
                    .getSelectionPath().getLastPathComponent();
            if (parent instanceof InputDataThingNode) {
                parent = (DefaultMutableTreeNode) parent.getParent();
            }
            InputListNode newNode = new InputListNode(null);
            parent.add(newNode);
            treeModel.nodeStructureChanged(parent);
            portTree.setSelectionPath(new TreePath(newNode.getPath()));
        }
    };

    ActionListener removeAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) portTree
                    .getSelectionPath().getLastPathComponent();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
                    .getParent();
            DefaultMutableTreeNode previousNode = node.getPreviousNode();
            parent.remove(node);
            treeModel.nodeStructureChanged(parent);
            portTree.setSelectionPath(new TreePath(previousNode.getPath()));
        }
    };

    ActionListener loadFilesAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            fileChooser.setMultiSelectionEnabled(true);
            int returnVal = fileChooser
                    .showOpenDialog(WorkflowInputMapBuilder.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                InputListNode parent = null;
                if (portTree.getSelectionPath().getLastPathComponent() instanceof InputDataThingNode) {
                    InputDataThingNode node = (InputDataThingNode) portTree
                            .getSelectionPath().getLastPathComponent();
                    parent = (InputListNode) node.getParent();
                } else {
                    parent = (InputListNode) portTree.getSelectionPath()
                            .getLastPathComponent();
                }
                InputDataThingNode newNode = null;
                for (int index = 0; index < files.length; index++) {
                    try {
                        if (parent.isText()) {
                            BufferedReader reader = new BufferedReader(
                                    new FileReader(files[index]));
                            StringBuffer stringBuffer = new StringBuffer();
                            String string = null;
                            while ((string = reader.readLine()) != null) {
                                stringBuffer.append(string);
                                stringBuffer.append("\n");
                            }
                            newNode = new InputDataThingNode(parent.getPort(),
                                    stringBuffer.toString(), parent.getPort()
                                            .getMetadata().getMIMETypeList());
                            parent.add(newNode);
                        } else {
                            File file = files[index];
                            byte[] bytes = FileUtils.readFileToByteArray(file);
                            // Store as datathing
                            newNode = new InputDataThingNode(parent.getPort(),
                                    bytes, parent.getPort().getMetadata()
                                            .getMIMETypeList());
                            parent.add(newNode);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                treeModel.nodeStructureChanged(parent);
                if (newNode != null) {
                    portTree.setSelectionPath(new TreePath(newNode.getPath()));
                }
            }
            fileChooser.setMultiSelectionEnabled(false);
        }
    };

    private boolean initialized;

    static {
        String storageClassName = System.getProperty("taverna.datastore.class");
        if (storageClassName != null) {
            try {
                Class c = Class.forName(storageClassName);
                store = (BaclavaDataService) c.newInstance();
            } catch (Exception ex) {
                logger.error("Unable to initialize data store class : "
                        + storageClassName, ex);
            }
        }
    }

    public Map<String, DataThing> bakeInputMap() {
        HashMap<String, DataThing> inputMap = new HashMap<String, DataThing>();
        Enumeration children = rootNode.children();
        while (children.hasMoreElements()) {
            InputPortNode portNode = (InputPortNode) children.nextElement();
            inputMap.put(portNode.getPort().getName(), portNode.getDataThing());
        }
        return inputMap;
    }

    public void setWorkflowInputs(Map<String, DataThing> inputs)
            throws InputsNotMatchingException {
        rootNode.setWorkflowInputs(inputs);
    }

    /*
     * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#attachToModel(org.embl.ebi.escience.scufl.ScuflModel)
     */
    public void attachToModel(ScuflModel model) {
        if (!initialized) {
            init();
        }
        if (this.model == model) {
            return;
        }
        if (this.model != null) {
            logger.warn("Didn't call detachFromModel() before attachToModel()");
            detachFromModel();
        }
        this.model = model;
        model.addListener(this);
        updateModel();
        setVisible(true);
    }

    void init() {
        portTree = new JTree(treeModel);
        portTree.setRowHeight(0);
        portTree.setCellRenderer(new InputNodeRenderer());
        portTree.setDragEnabled(true);
        portTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        portTree.addMouseListener(new PortTreeMouseListener());
        new TreeTransferHandler(portTree);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(portTree);
        // Avoid the leftcomponent to become very tiny
        scrollPane.setMinimumSize(new Dimension(100, 100));

        splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setContinuousLayout(false);
        splitter.setLeftComponent(scrollPane);
        splitter.setPreferredSize(new Dimension(0, 0));

        JButton loadInputDocButton = new JButton("Load Input Doc",
                TavernaIcons.openIcon);
        JButton saveInputDocButton = new JButton("Save Input Doc",
                TavernaIcons.saveIcon);

        loadInputDocButton.setToolTipText("Load Input Document");
        loadInputDocButton.addActionListener(loadInputDocAction);

        saveInputDocButton.setToolTipText("Save Input Document");
        saveInputDocButton.addActionListener(saveInputDocAction);

        loadInputsButton = new JButton("Load Inputs", TavernaIcons.openIcon);
        loadInputsButton.setEnabled(false);
        loadInputsButton.addActionListener(loadFilesAction);
        newInputButton = new JButton("New Input", TavernaIcons.newInputIcon);
        newInputButton.setEnabled(false);
        newInputButton.addActionListener(newInputAction);
        newListButton = new JButton("New List", TavernaIcons.newListIcon);
        newListButton.setEnabled(false);
        newListButton.addActionListener(newListAction);
        removeButton = new JButton("Remove", TavernaIcons.deleteIcon);
        removeButton.setEnabled(false);
        removeButton.addActionListener(removeAction);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.add(loadInputDocButton);
        toolbar.add(saveInputDocButton);

        toolbar.add(loadInputsButton);
        toolbar.add(newInputButton);
        toolbar.add(newListButton);
        toolbar.add(removeButton);

        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(splitter, BorderLayout.CENTER);

        // Add listeners after all the GUI stuff has been created
        portTree.addTreeSelectionListener(new PortTreeSelectionListener());
        initialized = true;
        // Will be set visible when attachToModel()
    }

    /*
     * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#detachFromModel()
     */
    public void detachFromModel() {
        setVisible(false);
        if (model == null) {
            return;
        }
        model.removeListener(this);
        model = null;
    }

    private void updateModel() {
        Port[] inputs = model.getWorkflowSourcePorts();
        for (int index = 0; index < inputs.length; index++) {
            InputPortNode portNode = null;
            for (int nodeIndex = index; nodeIndex < rootNode.getChildCount(); nodeIndex++) {
                InputPortNode tempNode = (InputPortNode) rootNode
                        .getChildAt(nodeIndex);
                if (inputs[index].equals(tempNode.getPort())) {
                    rootNode.remove(nodeIndex);
                    portNode = tempNode;
                }
            }
            if (portNode == null) {
                portNode = new InputPortNode(inputs[index]);
            }
            rootNode.insert(portNode, index);
        }
        while (rootNode.getChildCount() > inputs.length) {
            rootNode.remove(inputs.length);
        }
        treeModel.nodeStructureChanged(rootNode);
    }

    /*
     * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#getName()
     */
    public String getName() {
        return "Run Workflow";
    }

    public ImageIcon getIcon() {
        return TavernaIcons.windowInput;
    }

    /*
     * @see org.embl.ebi.escience.scufl.ScuflModelEventListener#receiveModelEvent(org.embl.ebi.escience.scufl.ScuflModelEvent)
     */
    public void receiveModelEvent(ScuflModelEvent event) {
        updateModel();
    }

    void updatePanel() {
        if (portTree.getSelectionPath().getLastPathComponent() instanceof PanelTreeNode) {
            PanelTreeNode node = (PanelTreeNode) portTree.getSelectionPath()
                    .getLastPathComponent();
            splitter.setRightComponent(node.getPanel());
        }
    }

    public void onDisplay() {
    }

    public void onDispose() {
        detachFromModel();
    }
}