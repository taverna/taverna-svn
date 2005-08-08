package net.sf.taverna.ocula;

import java.awt.Dimension;
import java.net.URL;

import javax.swing.JFrame;

import junit.framework.TestCase;

public class InputTest extends TestCase {
    public void testInputExample() throws Exception{
	JFrame frame = new JFrame();
	Ocula o = new Ocula();
	frame.getContentPane().add(o);
	URL exampleURL = Thread.currentThread().getContextClassLoader().
	    getResource("net/sf/taverna/ocula/example/InputExample.xml");
	frame.setSize(new Dimension(400,300));
	frame.setVisible(true);
	o.load(exampleURL);
	Thread.sleep(3000);
    }
    
}
