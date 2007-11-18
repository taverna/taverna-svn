/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

/**
 * @author alanrw
 *
 */
public class ActivityPalettePanel extends JPanel implements WorkflowModelViewSPI,
ActivityPaletteModelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -891768552987961118L;
	
	private ActivityPaletteModel paletteModel = null;
	
	private JTabbedPane tabbedPane;
	
	public ActivityPalettePanel() {
		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
//		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		this.add(this.tabbedPane);
		
		this.paletteModel = new ActivityPaletteModel();
		this.paletteModel.addListener(this);
		this.paletteModel.initialize();
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

	@Override
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

	public void tabModelAdded(ActivityPaletteModel activityPaletteModel,
			ActivityTabModel tabModel) {
		this.tabbedPane.addTab (tabModel.getName(), new JLabel(tabModel.getName()));
	}

}
