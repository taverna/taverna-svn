/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.Exception;
import java.lang.Runnable;
import java.lang.Thread;



/**
 * A splash screen for the workbench, code derived heavily from 
 * http://www.javaworld.com/javaworld/javatips/jw-javatip104.html
 * @author Tom Oinn
 */
class SplashScreen extends JWindow {

    static ImageIcon logo;

    static {
	try {
	    Class c = Class.forName("org.embl.ebi.escience.scuflui.workbench.SplashScreen");
	    logo = new ImageIcon(c.getResource("splashscreen.png"));
	}
	catch (ClassNotFoundException cnfe) {
	    //
	}
    }

    public SplashScreen(int waitTime) {
        super();
        JLabel l = new JLabel(logo);
        getContentPane().add(l, BorderLayout.CENTER);
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = l.getPreferredSize();
        setLocation(screenSize.width/2 - (labelSize.width/2), screenSize.height/2 - (labelSize.height/2));
        addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    setVisible(false);
                    dispose();
                }
            });
        final int pause = waitTime;
        final Runnable closerRunner = new Runnable() {
                public void run() {
                    setVisible(false);
                    dispose();
                }
            };
        Runnable waitRunner = new Runnable() {
                public void run() {
                    try {
			Thread.sleep(pause);
			SwingUtilities.invokeAndWait(closerRunner);
		    }
                    catch(Exception e) {
			e.printStackTrace();
			// can catch InvocationTargetException
			// can catch InterruptedException
		    }
                }
            };
        setVisible(true);
        Thread splashThread = new Thread(waitRunner, "SplashThread");
        splashThread.start();
    }
}
