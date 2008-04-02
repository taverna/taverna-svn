package uk.org.mygrid.logbook.ui;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.embl.ebi.escience.scuflui.TavernaIcons;

import uk.org.mygrid.logbook.ui.util.ProcessRun;
import uk.org.mygrid.logbook.ui.util.WorkflowRun;

public class ProcessRunsTreeTableRenderer extends DefaultTreeCellRenderer {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProcessRunsTreeTableRenderer() {
        super();
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                row, hasFocus);

        Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
        if (userObject instanceof ProcessRun) {
            ProcessRun n = (ProcessRun) userObject;
            setIcon(n.getIcon());

        } else {
            setIcon(TavernaIcons.folderOpenIcon);

        }
        if (userObject instanceof WorkflowRun) {
            WorkflowRun n = (WorkflowRun) userObject;

            setIcon(n.getIcon());

        }

        return this;

    }

}
