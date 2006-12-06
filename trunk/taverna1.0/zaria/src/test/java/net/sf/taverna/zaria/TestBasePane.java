package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import junit.framework.TestCase;

public class TestBasePane extends TestCase {

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException, ParseException {
		// Create the JFrame first as I want to keep the windows etc decorations.
		JFrame myFrame = new JFrame("ZPane test");
		try {
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
		}
		catch (Exception ex) {
			// don't have synthetica installed, shame. It looks
			// a lot better.
		}
		myFrame.setSize(300,300);
		ZBasePane pane = new ZBasePane() {

			@Override
			public JComponent getComponent(Class theClass) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public JMenuItem getMenuItem(Class theClass) {
				// TODO Auto-generated method stub
				return null;
			}

			public void discard() {
				// TODO Auto-generated method stub
				
			}			
			
		};
		pane.setEditable(true);
		myFrame.getContentPane().add(pane, BorderLayout.CENTER);
		myFrame.setVisible(true);
		Thread.sleep(1000*20);
		pane.lockFrame();
		Thread.sleep(1000*30);
	}
	
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
