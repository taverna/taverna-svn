/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.shared.XMLTree;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.jdom.Document;

/**
 * A swing component which provides an XML tree view of the ScuflModel instance
 * it is bound to
 * 
 * @author Tom Oinn
 */
public class XScuflTree extends JComponent implements ScuflModelEventListener,
		WorkflowModelViewSPI {

	private ScuflModel model = null;

	private XScuflView xscufl = null;

	public XScuflTree() {
		setLayout(new BorderLayout());
	}

	public void attachToModel(ScuflModel model) {
		if (this.model == null) {
			this.xscufl = new XScuflView(model);
			model.addListener(this);
			update();
		}
	}

	public void detachFromModel() {
		if (this.model != null) {
			model.removeListener(this);
			model.removeListener(xscufl);
			this.model = null;
			this.xscufl = null;
			update();
		}
	}

	private void update() {
		if (this.xscufl != null) {
			try {
				Document xml = xscufl.getDocument();
				removeAll();
				XMLTree display = new XMLTree(xml);
				add(display, BorderLayout.CENTER);
				display.repaint();
				doLayout();
			} catch (Exception e) {
				removeAll();
			}
		} else {
			removeAll();
		}
		repaint();
	}

	private int updateStatus = 0;

	public void receiveModelEvent(ScuflModelEvent event) {
		if (updateStatus == 0) {
			updateStatus = 1;
			while (updateStatus != 0) {
				update();
				if (updateStatus == 2) {
					updateStatus = 1;
				} else {
					updateStatus = 0;
				}
			}
		} else {
			updateStatus = 2;
		}
	}

	public javax.swing.ImageIcon getIcon() {
		return TavernaIcons.xmlNodeIcon;
	}

	/**
	 * A name for this component
	 */
	public String getName() {
		return "DEBUG - Workflow XML preview";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		detachFromModel();
	}

}
