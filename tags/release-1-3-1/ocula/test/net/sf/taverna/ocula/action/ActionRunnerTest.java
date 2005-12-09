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
package net.sf.taverna.ocula.action;

import junit.framework.TestCase;
import net.sf.taverna.ocula.Ocula;
import org.jdom.Element;
import java.util.List;


/**
 * Test the ActionRunner class and SPI registry
 * @author Tom Oinn
 */
public class ActionRunnerTest extends TestCase {

    public void testCreation() {
	Ocula o = new Ocula(null);
	ActionRunner a = new ActionRunner(o);
    }

    public void testEmptyActionBlock() {
	Element e = new Element("test");
	Ocula o = new Ocula(null);
	ActionRunner a = new ActionRunner(o);
	a.runAction(e);
    }

    public void testWaitActionBlock() {
	Element e = new Element("test");
	Element waitActionElement = new Element("wait");
	waitActionElement.setAttribute("time","500");
	e.addContent(waitActionElement);
	Ocula o = new Ocula(null);
	ActionRunner a = new ActionRunner(o);
	List errors = a.runAction(e);
	assertTrue("Should be no errors here", errors.size() == 0);
    }

    public void testBrokenWaitActionBlock() {
	Element e = new Element("test");
	Element waitActionElement = new Element("wait");
	//waitActionElement.setAttribute("time","500");
	e.addContent(waitActionElement);
	Ocula o = new Ocula(null);
	ActionRunner a = new ActionRunner(o);
	List errors = a.runAction(e);
	assertTrue("Should be one error from this call", errors.size() == 1);
    }

}
