/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.tools.apiconsumer;

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
 * A splash screen for the API Consumer, code derived heavily from 
 * http://www.javaworld.com/javaworld/javatips/jw-javatip104.html
 * @author Tom Oinn
 */
class SplashScreen extends JWindow {

    static ImageIcon logo;

    static {
	try {
	    Class c = Class.forName("net.sf.taverna.tools.apiconsumer.SplashScreen");
	    logo = new ImageIcon(c.getResource("images/splashscreen.png"));
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
