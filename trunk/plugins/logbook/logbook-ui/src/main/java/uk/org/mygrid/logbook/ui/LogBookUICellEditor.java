package uk.org.mygrid.logbook.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;

import org.embl.ebi.escience.treetable.JTreeTable;
import org.embl.ebi.escience.treetable.TreeTableModel;
import org.embl.ebi.escience.treetable.JTreeTable.TreeTableTextField;

public class LogBookUICellEditor extends DefaultCellEditor {

	
	JTreeTable treeTable;
	
	
		public LogBookUICellEditor(JTreeTable treeTable) {
			super(new TreeTableTextField());
			this.treeTable = treeTable;
			
		}

		public boolean isCellEditable(EventObject e) {
			// Edit on double click rather than the default triple
			
			if (e instanceof MouseEvent) {
				MouseEvent me = (MouseEvent) e;
				if (me.getClickCount() == 1
						&& System.getProperties().getProperty(
								"taverna.osxpresent") != null) {
					for (int counter = treeTable.getColumnCount() - 1; counter >= 0; counter--) {
						if (treeTable.getColumnClass(counter) == TreeTableModel.class) {
							MouseEvent newME = new MouseEvent(treeTable.getTree(), me.getID(),
									me.getWhen(), me.getModifiers(), me.getX()
											- treeTable.getCellRect(0, counter, true).x,
									me.getY(), me.getClickCount(), me
											.isPopupTrigger());
							
							treeTable.getTree().dispatchEvent(newME);

							Point p = new Point(me.getX(), me.getY());
							int row = treeTable.rowAtPoint(p);
							int column = treeTable.columnAtPoint(p);
							if (column == 0) {
								boolean isExpanded = treeTable.getTree().isExpanded(treeTable.getTree()
										.getPathForRow(row));
								if (isExpanded == false) {
									treeTable.getTree().expandPath(treeTable.getTree().getPathForRow(row));
								} else {
									treeTable.getTree().collapsePath(treeTable.getTree().getPathForRow(row));
								}
							}

							break;
						}
					}

				}
				if (me.getClickCount() >= 3) {
					return false;
				}
			}
			if (e == null) {
				return true;
			}
			return false;
		}
		
		
	
	
	
	
}
