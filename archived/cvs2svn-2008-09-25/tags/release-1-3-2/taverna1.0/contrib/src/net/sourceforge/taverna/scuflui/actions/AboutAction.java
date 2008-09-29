package net.sourceforge.taverna.scuflui.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;

/**
 * Represents the About Action.
 *
 * @version 1.1 03/30/00
 * @author  Mark Davidson
 */
public class AboutAction extends DefaultAction {
    //TODO: Find non-swing Icon
    private static final String ACTION_COMMAND_KEY_ABOUT = "about-command";
    private static final String NAME_ABOUT = "About...";
    //private static final String SMALL_ICON_ABOUT = "etc/metal/About16.gif";
    //private static final String LARGE_ICON_ABOUT = "etc/metal/About24.gif";
    private static final String SHORT_DESCRIPTION_ABOUT = "About the Application";
    private static final String LONG_DESCRIPTION_ABOUT = "Provide information regarding the application";
    private static final int MNEMONIC_KEY_ABOUT = 'A';

    /**
     * Constructor
     */
    public AboutAction() {
        putValue(AboutAction.NAME, NAME_ABOUT);
        //putValue(AboutAction.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
        //putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
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
        
        ImageIcon img = new ImageIcon(AboutAction.class.getResource("/org/embl/ebi/escience/scuflui/workbench/splashscreen.png"));
        //Component obj =(Component)ae.getSource();
        final JInternalFrame iframe = new JInternalFrame("Taverna");
        iframe.getContentPane().setLayout(new BorderLayout());
        iframe.getContentPane().add(new JButton(img), BorderLayout.CENTER);
        JButton okBtn = new JButton("OK");
        okBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae2){
                iframe.setVisible(false);
            }
        });
        iframe.getContentPane().add(okBtn, BorderLayout.SOUTH);
        //JOptionPane.showMessageDialog(null,img);
    }

}
