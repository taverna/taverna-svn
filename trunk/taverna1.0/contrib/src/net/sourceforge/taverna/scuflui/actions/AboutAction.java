package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;

/**
 * Represents the About Action.
 *
 * @version 1.1 03/30/00
 * @author  Mark Davidson
 */
public class AboutAction extends DefaultAction {

    private static final String ACTION_COMMAND_KEY_ABOUT = "about-command";
    private static final String NAME_ABOUT = "About...";
    private static final String SMALL_ICON_ABOUT = "etc/metal/About16.gif";
    private static final String LARGE_ICON_ABOUT = "etc/metal/About24.gif";
    private static final String SHORT_DESCRIPTION_ABOUT = "About the Application";
    private static final String LONG_DESCRIPTION_ABOUT = "Provide information regarding the application";
    private static final int MNEMONIC_KEY_ABOUT = 'A';

    /**
     * Constructor
     */
    public AboutAction() {
        putValue(AboutAction.NAME, NAME_ABOUT);
        putValue(AboutAction.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
        putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
        putValue(AboutAction.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
        putValue(AboutAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
        putValue(AboutAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
        putValue(AboutAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
    }
    
    /**
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae){
        
    }

}
