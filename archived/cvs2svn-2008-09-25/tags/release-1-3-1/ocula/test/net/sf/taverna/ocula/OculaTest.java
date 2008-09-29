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

package net.sf.taverna.ocula;

import junit.framework.TestCase;
import javax.swing.JFrame;
import java.awt.Dimension;
import net.sf.taverna.ocula.ui.*;
import java.net.URL;

/**
 * Test the top level Ocula JPanel
 * @author Tom Oinn
 */
public class OculaTest extends TestCase {
    
    public void testCreation() {
	Ocula o = new Ocula(null);
    }

    public void testSetTitle() {
	JFrame f = new JFrame();
	Ocula o = new Ocula(f);
	f.getContentPane().add(o);
	o.setTitle("test title");
	assertTrue(f.getTitle().equals("test title"));
    }

    public void testSetTitleWithNoParent() {
	Ocula o = new Ocula(null);
	o.setTitle("test title");
    }

    public void testUICreation() {
	JFrame frame = new JFrame();
	Ocula o = new Ocula(frame);
	o.mainPanel.add(new ResultSetPanel("A result", Icons.getIcon("components")));
	o.mainPanel.add(new ResultSetPanel("Another sample result", Icons.getIcon("users")));
	frame.getContentPane().add(o);
	o.setTitle("Ocula test");
	frame.setSize(new Dimension(200,200));
	frame.setVisible(true);
	try {
	    Thread.sleep(100);
	}
	catch (Exception ex) {
	    //
	}
    }

    public void testLoadExampleWithFailure() throws Exception {
	JFrame frame = new JFrame();
	Ocula o = new Ocula(frame);	
	frame.getContentPane().add(o);
	URL exampleURL = Thread.currentThread().getContextClassLoader().
	    getResource("net/sf/taverna/ocula/example/example1.xml");
	frame.setSize(new Dimension(400,400));
	frame.setVisible(true);
	o.load(exampleURL);
	Thread.sleep(3000);
    }
    
    public void testLoadExampleWithInitContext() throws Exception {
	JFrame frame = new JFrame();
	Ocula o = new Ocula(frame);	
	frame.getContentPane().add(o);
	URL exampleURL = Thread.currentThread().getContextClassLoader().
	    getResource("net/sf/taverna/ocula/example/example1.xml");
	o.putContext("addressBook",new net.sf.taverna.ocula.example.PhoneBook());
	frame.setSize(new Dimension(400,400));
	frame.setVisible(true);
	o.load(exampleURL);
	Thread.sleep(3000);
    }

    public void testLoadExampleWithInitialAction() throws Exception {
	JFrame frame = new JFrame();
	Ocula o = new Ocula(frame);	
	frame.getContentPane().add(o);
	URL exampleURL = Thread.currentThread().getContextClassLoader().
	    getResource("net/sf/taverna/ocula/example/example1a.xml");
	frame.setSize(new Dimension(400,400));
	frame.setVisible(true);
	o.load(exampleURL);
	Thread.sleep(3000);
    }
}
