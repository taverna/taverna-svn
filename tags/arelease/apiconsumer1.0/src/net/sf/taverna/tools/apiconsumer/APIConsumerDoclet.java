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

import com.sun.javadoc.*;
import org.jdom.*;
import org.jdom.output.*;
import java.io.*;

/**
 * Generates a wizard style interface to construct the XML document
 * which Taverna will consume to allow access to the 3rd party API
 * @author Tom Oinn
 * @version $Id: APIConsumerDoclet.java,v 1.1.1.1 2005-03-01 16:57:12 mereden Exp $
 */
public class APIConsumerDoclet {

    public static boolean start(RootDoc root) {
		
	// Get all the classes, build them into a tree
	// with the package structure echoed in the
	// tree structure.
	ClassDoc[] classes = root.classes();
	//ClassTreeModel treeModel = new ClassTreeModel(classes);
	ConsumerWizard wizard = new ConsumerWizard(classes);
	// Wait for the wizard to complete
	while (wizard.isRunning()) {
	    try {
		Thread.sleep(1000);
	    }
	    catch (InterruptedException ie) {
		//
	    }
	}
	return true;
    }

}
