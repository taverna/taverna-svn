package net.sf.taverna.ocula;

import java.awt.Dimension;
import java.net.URL;

import javax.swing.JFrame;

import junit.framework.TestCase;

public class InputTest extends TestCase {
    
    private void setUp(String exampleURLString) throws Exception	{
	JFrame frame = new JFrame();
	Ocula o = new Ocula(frame);
	frame.getContentPane().add(o);
	URL exampleURL = Thread.currentThread().getContextClassLoader().
	    getResource(exampleURLString);
	frame.setSize(new Dimension(400,300));
	frame.setVisible(true);
	o.load(exampleURL);
	Thread.sleep(3000);
    }
    public void testInputExample() throws Exception{
	setUp("net/sf/taverna/ocula/example/InputExample.xml");
    }
    
    public void testMalformedScript() throws Exception{
	setUp("net/sf/taverna/ocula/MalformedInputScript.xml");
    }
    
    public void testUnsupportedElement() throws Exception	{
	setUp("net/sf/taverna/ocula/UnsupportedInputElement.xml");
    }
    
    public void testUnsupportedSeparatorAttribute() throws Exception	{
	setUp("net/sf/taverna/ocula/UnsupportedSeparatorAttribute.xml");
    }
    
    public void testUnsupportedTextAttribute() throws Exception	{
	setUp("net/sf/taverna/ocula/UnsupportedTextAttribute.xml");
    }
    
    public void testUnsupportedAttribute() throws Exception	{
	setUp("net/sf/taverna/ocula/UnsupportedInputAttribute.xml");
    }
}
