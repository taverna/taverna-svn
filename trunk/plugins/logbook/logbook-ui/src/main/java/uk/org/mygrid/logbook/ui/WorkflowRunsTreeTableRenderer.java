package uk.org.mygrid.logbook.ui;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.embl.ebi.escience.scuflui.TavernaIcons;

import uk.org.mygrid.logbook.ui.util.Workflow;
import uk.org.mygrid.logbook.ui.util.WorkflowRun;

public class WorkflowRunsTreeTableRenderer extends DefaultTreeCellRenderer {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkflowRunsTreeTableRenderer() {
        super();
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                row, hasFocus);

        Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
        if (userObject instanceof WorkflowRun) {
            WorkflowRun n = (WorkflowRun) userObject;

            setIcon(n.getIcon());

        } else {
            setIcon(TavernaIcons.folderOpenIcon);

        }

        if (userObject instanceof Workflow) {

            Workflow n = (Workflow) userObject;

            if (n.getDescription() != null) {
                StringBuffer d = new StringBuffer(n.getDescription());
                // add <br> tags in to break up the description into several
                // lines;
                for (int i = 30; i < d.length(); i = i + 30) {
                    if (d.indexOf(" ", i) != -1) {
                        d.insert(d.indexOf(" ", i), "<br>");
                        i = d.indexOf(" ", i);
                    }

                }
            }
        }

        return this;

    }

}
