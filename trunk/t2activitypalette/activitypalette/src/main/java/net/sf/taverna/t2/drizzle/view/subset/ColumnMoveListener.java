/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.subset;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;

/**
 * @author alanrw
 *
 */
public final class ColumnMoveListener implements MouseMotionListener,
		MouseListener {
	private JTableHeader tableHeader;
	private ActivitySubsetPanel subsetPanel;
	private int fromIndex = -1;
	private int toIndex = -1;
	
	public ColumnMoveListener(final JTableHeader tableHeader, final ActivitySubsetPanel subsetPanel) {
		this.tableHeader = tableHeader;
		this.subsetPanel = subsetPanel;
		tableHeader.addMouseListener(this);
		tableHeader.addMouseMotionListener(this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent arg0) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent arg0) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent arg0) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent arg0) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {
		fromIndex = -1;
		toIndex = -1;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent arg0) {
		if (!arg0.isPopupTrigger()) {
			fromIndex = tableHeader.getTable().columnAtPoint(arg0.getPoint());
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent arg0) {
		toIndex = tableHeader.getTable().columnAtPoint(arg0.getPoint());
		if ((fromIndex != -1) && (fromIndex != toIndex)) {
			List<PropertyKeySetting> keySettings = subsetPanel.getKeySettings();
			PropertyKeySetting movedKey = keySettings
					.get(fromIndex);
			keySettings.remove(fromIndex);
			keySettings.add(toIndex, movedKey);
			subsetPanel.setTreeAndTableModels();
		}
		fromIndex = -1;
		toIndex = -1;
	}

}
