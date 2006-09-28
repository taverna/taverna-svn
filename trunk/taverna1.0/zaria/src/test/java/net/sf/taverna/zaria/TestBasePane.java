package net.sf.taverna.zaria;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import junit.framework.TestCase;

public class TestBasePane extends TestCase {

	public void testBasePane() throws InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        /*  Does not run in head-less mode */
		/**
		UIManager.setLookAndFeel(UIManager
				.getSystemLookAndFeelClassName());
		JFrame myFrame = new JFrame("ZPane test");
		myFrame.setSize(300,300);
		ZPane pane = new ZBasePane() {

			@Override
			public JComponent getComponent(Class theClass) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		pane.setEditable(true);
		myFrame.getContentPane().add(pane, BorderLayout.CENTER);
		myFrame.setVisible(true);
		Thread.sleep(1000*30);
        */
	}
	
}
