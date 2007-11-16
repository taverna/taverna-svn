/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

/**
 * @author alanrw
 *
 */
public class ActivityPalettePanel extends JPanel implements WorkflowModelViewSPI {

	/**
	 * 
	 */
	private static final long serialVersionUID = -891768552987961118L;
	
	private ActivityPaletteModel model = null;
	
	public ActivityPalettePanel() {
		model = new ActivityPaletteModel();
	}

	public void attachToModel(ScuflModel model) {
		// TODO Auto-generated method stub
		
	}

	public void detachFromModel() {
		// TODO Auto-generated method stub
		
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		// TODO Auto-generated method stub
		
	}

}
