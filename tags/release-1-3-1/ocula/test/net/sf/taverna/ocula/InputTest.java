/*
 * Copyright 2005 University of Manchester
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

package net.sf.taverna.ocula;

import java.awt.Dimension;
import java.net.URL;

import javax.swing.JFrame;

import junit.framework.TestCase;

/**
 * Tests InputFrame and SimpleInputFrame.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 */
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
	Thread.sleep(1000);
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
