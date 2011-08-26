package net.sourceforge.taverna.publish.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.sourceforge.taverna.publish.RepositoryForm;
import net.sourceforge.taverna.scuflui.actions.AboutAction;
import net.sourceforge.taverna.scuflui.actions.DefaultAction;

/**
 * This class creates a new Repository.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class NewRepositoryAction extends DefaultAction {
    private static final String ACTION_COMMAND_KEY_ABOUT = "new-rep-command";
    private static final String NAME_ABOUT = "New Repository...";
    //private static final String SMALL_ICON_ABOUT = "etc/metal/About16.gif";
    //private static final String LARGE_ICON_ABOUT = "etc/metal/About24.gif";
    private static final String SHORT_DESCRIPTION_ABOUT = "Create a New Repository";
    private static final String LONG_DESCRIPTION_ABOUT = "Create a New Repository To Publish Workflows";
    //private static final int MNEMONIC_KEY_ABOUT = 'A';
   
    public NewRepositoryAction(){
        putValue(AboutAction.NAME, NAME_ABOUT);
        //putValue(AboutAction.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
        //putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
        putValue(AboutAction.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
        putValue(AboutAction.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
        //putValue(AboutAction.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
        putValue(AboutAction.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
        
    }
    
    public void actionPerformed(ActionEvent ae){
		JDialog dialog = new JDialog();
		dialog.setLayout(new BorderLayout());
		dialog.add(new RepositoryForm(), BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		JButton okBtn = new JButton("OK");
		JButton cancelBtn = new JButton("Cancel");
		buttonPanel.add(okBtn);
		buttonPanel.add(cancelBtn);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.setVisible(true);
		
    }

}
