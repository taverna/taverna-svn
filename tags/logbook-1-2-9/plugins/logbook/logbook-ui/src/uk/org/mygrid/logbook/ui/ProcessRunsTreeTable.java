package uk.org.mygrid.logbook.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.dnd.FactorySpecFragment;
import org.embl.ebi.escience.scuflui.dnd.SpecFragmentTransferable;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.embl.ebi.escience.treetable.JTreeTable;
import org.embl.ebi.escience.treetable.TreeTableModel;
import org.jdom.Element;

import uk.org.mygrid.logbook.ui.util.ProcessRun;
import uk.org.mygrid.logbook.ui.util.ProcessRunImpl;
import uk.org.mygrid.logbook.ui.util.Utilities;
import uk.org.mygrid.logbook.ui.util.WorkflowRunImpl;

public class ProcessRunsTreeTable extends JTreeTable implements
        DragGestureListener, DragSourceListener, MouseListener, ActionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Logger logger = Logger.getLogger(ProcessRunsTreeTable.class);

    final LogBookUIModel logBookUIModel;

    JPopupMenu menu;

    JPopupMenu workflowMenu;

    JMenuItem add;

    JMenuItem addLinks;

    JMenuItem exportItem;

    JMenuItem viewSubWorkflow;

    JList IterationsList;

    JScrollPane IterationsScrollPane;

    JWindow IterationsPopup;

    ProcessRunsTreeTableModel processTableModel;

    ProcessRunsPane processRunsPane;

    public ProcessRunsTreeTable(ProcessRunsTreeTableModel processTableModel,
            LogBookUIModel logBookUIModel, ProcessRunsPane processRunsPane) {
        super(processTableModel);

        this.processTableModel = processTableModel;
        this.logBookUIModel = logBookUIModel;
        this.processRunsPane = processRunsPane;
        addMouseListener(this);
        ListToTreeIntervalSelectionModelWrapper selectionWrapper = new ListToTreeIntervalSelectionModelWrapper();
        tree.setSelectionModel(selectionWrapper);
        setDefaultEditor(TreeTableModel.class, new LogBookUICellEditor(this));

        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY_OR_MOVE, this);
        // this.setDragEnabled(true);
        IterationsScrollPane = new JScrollPane();
        IterationsScrollPane.getViewport().setView(IterationsList);
        IterationsPopup = new JWindow();
        isPopupVisible = false;
        IterationsPopup.getContentPane().add(IterationsScrollPane);
        IterationsPopup.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {

            }

            public void focusLost(FocusEvent e) {

                IterationsPopup.setVisible(false);
            }

        });

        IterationsList = new JList();

        setGridColor(new Color(235, 235, 235));
        setSelectionBackground(new Color(232, 242, 254));
        setSelectionForeground(Color.BLACK);
        setIntercellSpacing(new Dimension(0, 1));
        setShowVerticalLines(false);
        setMinimumSize(new Dimension(500, 900));
        // setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        getColumnModel().getColumn(0).setMinWidth(30);

        getColumnModel().getColumn(0).setResizable(true);

        menu = new JPopupMenu();
        viewSubWorkflow = new JMenuItem("Explore Nested Workflow");
        viewSubWorkflow.addActionListener(this);
        if (logBookUIModel.getModel() != null) {
            add = new JMenuItem("Add to workflow");
            add.addActionListener(this);
            addLinks = new JMenuItem("Add to workflow (Maintain Links)");
            addLinks.addActionListener(this);
            menu.add(add);
            menu.add(addLinks);
        }

        exportItem = new JMenuItem("Export", LogBookIcons.rdfIcon);
        exportItem.addActionListener(this);
        menu.add(exportItem);

        // if (enactorInvocationBrowserModel.getModel() != null) {
        // workflowMenu = new JPopupMenu();
        // new JMenuItem("Explore Workflow");
        // viewSubWorkflow.addActionListener(this);
        // add = new JMenuItem("Add to workflow");
        // add.addActionListener(this);
        // addLinks = new JMenuItem("Add to workflow (Maintain Links)");
        // addLinks.addActionListener(this);
        // menu.add(add);
        // menu.add(addLinks);
        // }

    }

    public TableCellRenderer getCellRenderer(int row, int column) {

        return super.getCellRenderer(row, column);

    }

    public TableCellEditor getCellEditor(int row, int column) {

        return super.getCellEditor(row, column);

    }

    public void dragGestureRecognized(DragGestureEvent e) {
        logger.debug("dragGestureRecognized() - start");

        int row = getSelectedRow();
        if (row > 1) {

            // TODO maybe move alot of this into the Model to stick with MVC
            // ProcessRun processRun = ((EnactorBrowserProcessTableModel) this
            // .getModel()).getProcessAt(row);
            Object o = ((DefaultMutableTreeNode) getTree().getSelectionPath()
                    .getLastPathComponent()).getUserObject();
            ProcessRun processRun = null;
            if (o instanceof ProcessRun) {

                processRun = (ProcessRun) o;
            } else {
                return;
            }

            ScuflModel model = new ScuflModel();
            logger.debug("Process Run = " + processRun.getName());
            try {
                model = logBookUIModel.retrieveWorkflow(processRun
                        .getWorkflowLSID());
            } catch (Exception ex) {
                Object[] options = { "Ok" };
                JOptionPane
                        .showOptionDialog(
                                null,
                                "There was an error whilst loading the workflow",
                                "Load Failed", JOptionPane.YES_OPTION,
                                JOptionPane.QUESTION_MESSAGE, null, options,
                                options[0]);

                logger.error("Error loading the Workflow", ex);
            }
            try {
                Processor p = model.locateProcessor(processRun.getName());

                Element el = ProcessorHelper.elementForProcessor(p, true);
                FactorySpecFragment fsf = new FactorySpecFragment(el,
                        processRun.getName());
                Transferable t = new SpecFragmentTransferable(fsf);
                logger.debug("Dragging");
                e.startDrag(DragSource.DefaultCopyDrop, t, this);

            } catch (Exception ex) {

                logger.error("Processor for " + processRun.getName()
                        + " does not exist", ex);

            }

        }

    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        // TODO Auto-generated method stub

    }

    public void dragEnter(DragSourceDragEvent dsde) {
        // TODO Auto-generated method stub

    }

    public void dragExit(DragSourceEvent dse) {
        // TODO Auto-generated method stub

    }

    public void dragOver(DragSourceDragEvent dsde) {
        // TODO Auto-generated method stub

    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
        // TODO Auto-generated method stub

    }

    public void mouseClicked(MouseEvent me) {
        if (me.getClickCount() == 1
                && System.getProperty("os.name").equals("Mac OS X")) {
            // Workaround for buggy tree table on OS X. Open/close the path
            // on any click on the column (not just on the > icon)
            for (int counter = getColumnCount() - 1; counter >= 0; counter--) {
                if (getColumnClass(counter) == TreeTableModel.class) {
                    MouseEvent newME = new MouseEvent(tree, me.getID(),
                            me.getWhen(), me.getModifiers(), me.getX()
                                    - getCellRect(0, counter, true).x,
                            me.getY(), me.getClickCount(), me
                                    .isPopupTrigger());
                    tree.dispatchEvent(newME);

                    Point p = new Point(me.getX(), me.getY());
                    int row = rowAtPoint(p);
                    int column = columnAtPoint(p);
                    if (column == 0) {
                        boolean isExpanded = tree.isExpanded(tree
                                .getPathForRow(row));
                        if (isExpanded == false) {
                            tree.expandPath(tree.getPathForRow(row));
                        } else {
                            tree.collapsePath(tree.getPathForRow(row));
                        }
                    }

                    break;
                }
            }

        }
    }

    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    int row;

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            row = rowAtPoint(new Point(e.getX(), e.getY()));

            if (row > 0 && getTree().getSelectionPath() != null) {

                Object o = ((DefaultMutableTreeNode) getTree()
                        .getSelectionPath().getLastPathComponent())
                        .getUserObject();
                if (o instanceof ProcessRun) {

                    ProcessRun processRun = (ProcessRun) o;

                    if (processRun.isSubWorkflow()) {
                        menu.add(viewSubWorkflow);
                    }
                }

                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            row = rowAtPoint(new Point(e.getX(), e.getY()));

            if (row > 0 && getTree().getSelectionPath() != null) {

                Object o = ((DefaultMutableTreeNode) getTree()
                        .getSelectionPath().getLastPathComponent())
                        .getUserObject();
                if (o instanceof ProcessRun) {

                    ProcessRun processRun = (ProcessRun) o;

                    if (processRun.isSubWorkflow()) {
                        menu.add(viewSubWorkflow);
                    }
                }

                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        logger.debug(e.getActionCommand());
        if (e.getActionCommand() == viewSubWorkflow.getActionCommand()) {

            Object o = ((DefaultMutableTreeNode) getTree().getSelectionPath()
                    .getLastPathComponent()).getUserObject();
            ProcessRun processRun;
            if (o instanceof ProcessRun) {

                processRun = (ProcessRun) o;
            } else {
                return;
            }
            ProcessRunsPane newBrowser = new ProcessRunsPane(logBookUIModel);
            List<ProcessRun> a = logBookUIModel
                    .getProcessesForWorkflowRun(logBookUIModel
                            .getNestedWorkflow(processRun.getLsid()));
            // TODO: replace with List<ProcessRun> a = processRun.getNestedWorkflowRun().getProcessRuns();

            logger.debug(processRun.getLsid());
            newBrowser.setProcessData(new WorkflowRunImpl(logBookUIModel
                    .getNestedWorkflow(processRun.getLsid()),
                    TavernaIcons.windowExplorer), a);
            UIUtils.createFrame(null, newBrowser, 20, 20, 500, 500);

        } else if (e.getActionCommand() == add.getActionCommand()) {
            TreePath[] paths = getTree().getSelectionModel()
                    .getSelectionPaths();
            logger.debug(paths.length);
            List<ProcessRunImpl> processRuns = new ArrayList<ProcessRunImpl>();
            for (int i = 0; i < paths.length; i++) {
                Object o = ((DefaultMutableTreeNode) paths[i]
                        .getLastPathComponent()).getUserObject();
                if (o instanceof ProcessRun) {
                    ProcessRunImpl processRun = (ProcessRunImpl) o;
                    processRuns.add(processRun);
                }
            }
            logBookUIModel.reloadProcesses(processRuns
                    .toArray(new ProcessRunImpl[0]), false);
        } else if (e.getActionCommand() == addLinks.getActionCommand()) {

            TreePath[] paths = getTree().getSelectionModel()
                    .getSelectionPaths();

            ArrayList<ProcessRunImpl> processRuns = new ArrayList<ProcessRunImpl>();
            for (int i = 0; i < paths.length; i++) {
                Object o = ((DefaultMutableTreeNode) paths[i]
                        .getLastPathComponent()).getUserObject();
                if (o instanceof ProcessRun) {
                    ProcessRunImpl processRun = (ProcessRunImpl) o;
                    processRuns.add(processRun);
                }
            }
            logBookUIModel.reloadProcesses(processRuns
                    .toArray(new ProcessRunImpl[0]), true);

        } else if (e.getActionCommand() == exportItem.getActionCommand()) {

            Object o = ((DefaultMutableTreeNode) getTree().getSelectionPath()
                    .getLastPathComponent()).getUserObject();
            ProcessRun processRun;
            if (o instanceof ProcessRun) {
                processRun = (ProcessRun) o;
            } else {
                return;
            }
            String rdf = logBookUIModel.toRDF(processRun
                    .getLsid());
            Utilities.exportRDF(rdf);
        }

    }

    boolean isPopupVisible;

    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    public void showPopup(boolean show) {
        if (show) {
            // change to CalculatePopupPosition
            // IterationsPopup.setLocation(calculatePopupPosition());
            IterationsList.setMinimumSize(IterationsPopup.getSize());
            IterationsPopup.setVisible(true);
            this.isPopupVisible = true;
        } else {
            IterationsPopup.setVisible(false);
            IterationsList.clearSelection();
            this.isPopupVisible = false;
        }

    }

    public void setValueAt(Object aValue, int row, int column) {

        return;
    }

    public Point calculatePopupPosition() {

        return null;
    }

    public void setPopupSize(Dimension d) {

        IterationsPopup.setSize(d);
        IterationsScrollPane.setSize(d);
        IterationsList.setSize(d);
    }

    /**
     * This is a copy of the ListToTreeSelectionModelWrapper in
     * org.embl.ebi.escience.treetable with the only difference being it
     * allowing multiple/interval selection.
     * 
     * @author gamblem3
     * 
     */
    class ListToTreeIntervalSelectionModelWrapper extends
            DefaultTreeSelectionModel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/** Set to true when we are updating the ListSelectionModel. */
        protected boolean updatingListSelectionModel;

        public ListToTreeIntervalSelectionModelWrapper() {
            super();
            getListSelectionModel().addListSelectionListener(
                    createListSelectionListener());
        }

        /**
         * Returns the list selection model. ListToTreeSelectionModelWrapper
         * listens for changes to this model and updates the selected paths
         * accordingly.
         */
        ListSelectionModel getListSelectionModel() {
            return listSelectionModel;
        }

        /**
         * This is overridden to set <code>updatingListSelectionModel</code>
         * and message super. This is the only place DefaultTreeSelectionModel
         * alters the ListSelectionModel.
         */
        public void resetRowSelection() {
            if (!updatingListSelectionModel) {
                updatingListSelectionModel = true;
                try {
                    super.resetRowSelection();
                } finally {
                    updatingListSelectionModel = false;
                }
            }
            // Notice how we don't message super if
            // updatingListSelectionModel is true. If
            // updatingListSelectionModel is true, it implies the
            // ListSelectionModel has already been updated and the
            // paths are the only thing that needs to be updated.
        }

        /**
         * Creates and returns an instance of ListSelectionHandler.
         */
        protected ListSelectionListener createListSelectionListener() {
            return new ListSelectionHandler();
        }

        /**
         * If <code>updatingListSelectionModel</code> is false, this will
         * reset the selected paths from the selected rows in the list selection
         * model.
         */
        protected void updateSelectedPathsFromSelectedRows() {
            if (!updatingListSelectionModel) {
                updatingListSelectionModel = true;
                try {
                    // This is way expensive, ListSelectionModel needs an
                    // enumerator for iterating.
                    int min = listSelectionModel.getMinSelectionIndex();
                    int max = listSelectionModel.getMaxSelectionIndex();

                    clearSelection();
                    if (min != -1 && max != -1) {
                        for (int counter = min; counter <= max; counter++) {
                            if (listSelectionModel.isSelectedIndex(counter)) {
                                TreePath selPath = tree.getPathForRow(counter);

                                if (selPath != null) {
                                    addSelectionPath(selPath);
                                }
                            }
                        }
                    }
                } finally {
                    updatingListSelectionModel = false;
                }
            }
        }

        /**
         * Class responsible for calling updateSelectedPathsFromSelectedRows
         * when the selection of the list changse.
         */
        class ListSelectionHandler implements ListSelectionListener {
            public void valueChanged(ListSelectionEvent e) {
                updateSelectedPathsFromSelectedRows();
            }
        }
    }

}