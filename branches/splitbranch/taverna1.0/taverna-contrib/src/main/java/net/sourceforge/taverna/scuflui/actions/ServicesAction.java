package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.taverna.scuflui.workbench.Workbench;

import org.embl.ebi.escience.scuflui.ScavengerTreePanel;
import org.embl.ebi.escience.scuflui.UIUtils;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class ServicesAction extends DefaultAction {
	private static final String ACTION_COMMAND_KEY_ABOUT = "view-services-command";

	private static final String NAME_ABOUT = "View Services Window";

	private static final String SMALL_ICON_ABOUT = "org/embl/ebi/escience/scuflui/icons/windows/scavenger.gif";

	private static final String LARGE_ICON_ABOUT = "org/embl/ebi/escience/scuflui/icons/windows/scavenger.gif";

	private static final String SHORT_DESCRIPTION_ABOUT = "View Services Window";

	private static final String LONG_DESCRIPTION_ABOUT = "View Services Window";

	// private static final int MNEMONIC_KEY_ABOUT = 'N';
	// private static final Character ACCELERATOR_KEY = new Character('N');

	/**
	 * Constructor
	 */
	public ServicesAction() {

		putValue(Action.NAME, NAME_ABOUT);
		putValue(Action.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
		putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
		putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
		putValue(NewAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
		// putValue(NewAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
		putValue(NewAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
		// putValue(Action.ACCELERATOR_KEY, getKeyStroke(ACCELERATOR_KEY));
	}

	public void actionPerformed(ActionEvent ae) {
		UIUtils.createFrame(Workbench.workbench.model, new ScavengerTreePanel(), 540, 120, 300, 720);

	}
}
