/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: MediumXMLTester.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-14 11:39:46 $
 *               by   $Author: sowen70 $
 * Created on 9 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml.test;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;

import uk.org.mygrid.dataproxy.xml.TagInterceptor;
import uk.org.mygrid.dataproxy.xml.XMLStreamParser;
import uk.org.mygrid.dataproxy.xml.impl.FileInterceptorWriterFactory;
import uk.org.mygrid.dataproxy.xml.impl.IncomingTagInterceptorImpl;
import uk.org.mygrid.dataproxy.xml.impl.XMLStreamParserImpl;

/**
 * Not run as a unit test, as its used to test with HUGE xml files which are too big to put into
 * CVS. Its main use is to check for data corruption.
 * 
 * Test takes URL to xml to be processed as the first parameter and replaces tags of <picture/> with <picture-replaced/>
 * Replaced data is sent to File Writer with its destination as a OS assigned tmp folder (logged to screen).
 * 
 * The resulting XML is sent to stdout
 * 
 * @author Stuart Owen
 *
 */
public class MediumXMLTester {
	
	private static Logger logger = Logger.getLogger(MediumXMLTester.class);
	
	public static void main(String[] args) throws Exception {
		File tmp = File.createTempFile("pictures-test","");
		tmp.delete();
		tmp.mkdir();
		logger.info("Using tmp location for stored data: "+tmp.toURL().toExternalForm());
						
		
		XMLStreamParser parser = new XMLStreamParserImpl();
		
		parser.setOutputStream(System.out);
		
		TagInterceptor interceptor = new IncomingTagInterceptorImpl("picture","picture-replaced",new FileInterceptorWriterFactory(tmp.toURL(),"data"));		
		parser.addTagInterceptor(interceptor);
		parser.read(new URL(args[0]).openStream());				

	}
}