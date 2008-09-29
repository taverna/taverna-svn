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
import java.net.URL;
import java.net.*;
import org.jdom.*;
import java.io.*;

/**
 * Test the page loading
 * @author Tom Oinn
 */
public class PageTest extends TestCase {
    
    static URL emptyPageURL = PageTest.class.getResource("empty_page.xml");
    static URL missingURL = PageTest.class.getResource("no_page_with_this_name_exists.xml");
    static URL badlyFormedDocumentURL = PageTest.class.getResource("malformed_page.xml");

    public void testBasicPageCreation() {
	try {
	    Page p = new Page();
	    assertFalse("Page isn't valid as not loaded yet", p.isValid());
	}
	catch (Exception e) {
	    fail("Exception when loading page");
	}
    }
    
    public void testPageWithURLCreation() {
	try {
	    Page p = new Page(emptyPageURL);
	    assertTrue("Page is declared as valid", p.isValid());
	}
	catch (Exception e) {
	    fail("Exception when loading page");
	}
    }
    
    public void testSetPageLocation() {
	Page p = new Page();
	assertFalse("Page not loaded yet", p.isValid());
	try {
	    p.setLocation(emptyPageURL);
	    assertTrue("Page loaded and valid", p.isValid());
	}
	catch (Exception e) {
	    fail("Exception thrown when loading page");
	}
    }
    
    public void testMissingPageSetLocation() {
	Page p = new Page();
	try {
	    p.setLocation(missingURL);
	    fail("Should have thrown an IOException!");
	}
	catch (IOException ioe) {
	    //
	}
	catch (JDOMException jde) {
	    fail("Shouldn't get a JDOM Exception when there's no definition file");
	}
    }

    public void testBadlyFormedPageSetLocation() {
	Page p = new Page();
	try {
	    p.setLocation(badlyFormedDocumentURL);
	    fail("Should have thrown a JDOM Exception here");
	}
	catch (JDOMException jde) {
	    //
	}
	catch (IOException ioe) {
	    fail("Should have thrown a JDOM Exception not an ioexception");
	}
    }

    public void testCacheClear() {
	Page.clearDefinitionCache();
	assertTrue("Cache empty", Page.pageDefinitionCache.isEmpty());
    }
    
}
