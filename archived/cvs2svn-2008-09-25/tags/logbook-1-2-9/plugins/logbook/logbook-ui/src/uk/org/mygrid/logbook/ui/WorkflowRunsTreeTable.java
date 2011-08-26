/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: WorkflowRunsTreeTable.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:48:38 $
 *               by   $Author: stain $
 * Created on 20 Nov 2006
 *****************************************************************/
package uk.org.mygrid.logbook.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.embl.ebi.escience.treetable.JTreeTable;
import org.embl.ebi.escience.treetable.TreeTableModel;

import uk.org.mygrid.logbook.ui.util.WorkflowRun;

public class WorkflowRunsTreeTable extends JTreeTable implements MouseListener {

	private static final long serialVersionUID = 1L;

	//private Logger logger = Logger.getLogger(WorkflowRunsTreeTable.class);

	private JPopupMenu menu;

	public WorkflowRunsTreeTable(TreeTableModel model,
			final LogBookUIModel logBookUIModel) {
		super(model);
		addMouseListener(this);
		setDefaultEditor(TreeTableModel.class, new LogBookUICellEditor(this));
	}

	

	public void mouseClicked(MouseEvent me) {
		if (me.getClickCount() == 1
				&& System.getProperty("os.name").equals("Mac OS X")) {
			// Workaround for buggy tree table on OS X. Open/close the path
			// on any click on the column (not just on the > icon)
			for (int counter = getColumnCount() - 1; counter >= 0; counter--) {
				if (getColumnClass(counter) == TreeTableModel.class) {
					MouseEvent newME = new MouseEvent(tree, me.getID(), me
							.getWhen(), me.getModifiers(), me.getX()
							- getCellRect(0, counter, true).x, me.getY(), me
							.getClickCount(), me.isPopupTrigger());
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
				if (o instanceof WorkflowRun) {
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
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
				if (o instanceof WorkflowRun) {
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}

}