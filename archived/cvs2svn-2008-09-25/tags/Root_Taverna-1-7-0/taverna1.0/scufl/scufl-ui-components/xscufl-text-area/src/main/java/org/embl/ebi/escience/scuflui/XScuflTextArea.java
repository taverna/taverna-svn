/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.JTextArea;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

/**
 * A swing component that provides a textual view of the xscufl corresponding to
 * a given ScuflModel instance
 * 
 * @author Tom Oinn
 */
public class XScuflTextArea extends JTextArea implements
		ScuflModelEventListener, WorkflowModelViewSPI {

	private XScuflView xscufl = null;

	private ScuflModel model = null;

	public XScuflTextArea() {
		super();
		setLineWrap(true);
		setWrapStyleWord(true);
		setEditable(false);
	}

	public void attachToModel(ScuflModel model) {
		if (this.model == null) {
			this.xscufl = new XScuflView(model);
			model.addListener(this);
			updateText();
		}
	}

	public void detachFromModel() {
		if (this.model != null) {
			model.removeListener(this);
			model.removeListener(xscufl);
			this.model = null;
			this.xscufl = null;
			updateText();
		}
	}

	private void updateText() {
		if (this.xscufl != null) {
			setText(xscufl.getXMLText());
		} else {
			setText(null);
		}
		repaint();
	}

	private int updateStatus = 0;

	public void receiveModelEvent(ScuflModelEvent event) {
		if (updateStatus == 0) {
			updateStatus = 1;
			while (updateStatus != 0) {
				updateText();
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
		return "Workflow XML preview";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		detachFromModel();		
	}

}
