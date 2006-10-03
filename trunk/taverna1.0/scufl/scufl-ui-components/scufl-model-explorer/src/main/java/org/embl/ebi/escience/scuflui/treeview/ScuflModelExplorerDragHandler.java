/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.treeview;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.TransferHandler;


/**
 * Implements the MouseMotionListener interface to generate drag events from the
 * model explorer
 * 
 * @author Tom Oinn
 */
public class ScuflModelExplorerDragHandler extends MouseMotionAdapter {

	ScuflModelExplorer explorer;

	public ScuflModelExplorerDragHandler(ScuflModelExplorer theExplorer) {
		this.explorer = theExplorer;
	}

	MouseEvent firstMouseEvent = null;

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger() == false) {
			firstMouseEvent = e;
			e.consume();
		}
	}

	public void mouseDragged(MouseEvent e) {
		if (firstMouseEvent != null) {
			e.consume();
			int dx = Math.abs(e.getX() - firstMouseEvent.getX());
			int dy = Math.abs(e.getY() - firstMouseEvent.getY());
			if (dx > 5 || dy > 5) {
				JComponent c = (JComponent) e.getSource();
				TransferHandler handler = c.getTransferHandler();
				handler.exportAsDrag(c, firstMouseEvent, TransferHandler.COPY);
				firstMouseEvent = null;
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		this.firstMouseEvent = null;
	}

}
