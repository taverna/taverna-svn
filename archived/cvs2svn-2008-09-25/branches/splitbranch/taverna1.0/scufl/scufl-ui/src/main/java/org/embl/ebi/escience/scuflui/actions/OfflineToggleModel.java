/*
 * Created on Jun 7, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ItemEvent;

import javax.swing.JToggleButton.ToggleButtonModel;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.SetOnlineException;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1.2.1 $
 */
public class OfflineToggleModel extends ToggleButtonModel implements
		ScuflModelEventListener {
	ScuflModel model;

	/**
	 * 
	 */
	public OfflineToggleModel(ScuflModel model) {
		super();
		this.model = model;
		this.model.addListener(this);
	}

	public boolean isSelected() {
		return model.isOffline();
	}

	public void setSelected(boolean b) {
		try {
			model.setOffline(b);
		} catch (SetOnlineException e) {
			// TODO Handle SetOnlineException
			e.printStackTrace();
		}
	}

	public void receiveModelEvent(ScuflModelEvent event) {
		if (event.getSource() == model) {
			// Send ChangeEvent
			fireStateChanged();

			// Send ItemEvent
			fireItemStateChanged(new ItemEvent(this,
					ItemEvent.ITEM_STATE_CHANGED, this,
					this.isSelected() ? ItemEvent.SELECTED
							: ItemEvent.DESELECTED));

		}
	}
}
