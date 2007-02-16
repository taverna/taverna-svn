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
 * Filename           $RCSfile: BigXMLTester.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-16 14:01:43 $
 *               by   $Author: sowen70 $
 * Created on 9 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml.test;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;

import uk.org.mygrid.dataproxy.xml.ElementDef;
import uk.org.mygrid.dataproxy.xml.InterceptorWriter;
import uk.org.mygrid.dataproxy.xml.ResponseTagInterceptor;
import uk.org.mygrid.dataproxy.xml.WriterFactory;
import uk.org.mygrid.dataproxy.xml.XMLStreamParser;
import uk.org.mygrid.dataproxy.xml.impl.ResponseTagInterceptorImpl;
import uk.org.mygrid.dataproxy.xml.impl.ResponseXMLStreamParserImpl;

public class BigXMLTester {
	
	public static void main(String[] args) throws Exception {
		XMLStreamParser parser = new ResponseXMLStreamParserImpl();
		ResponseTagInterceptor interceptor = new ResponseTagInterceptorImpl(new ElementDef("data",""),new NullWriterFactory());
		parser.addTagInterceptor(interceptor);
		parser.setOutputStream(System.out);
		parser.read(new URL(args[0]).openStream());
	}
}

class NullWriterFactory implements WriterFactory {
	private static Logger logger = Logger
	.getLogger(NullInterceptorWriter.class);
		
	private int c=1;

	public InterceptorWriter newWriter() {
		String dest="http://destination/"+String.valueOf(c);
		c++;
		return new NullInterceptorWriter(dest);
	}
	
	class NullInterceptorWriter implements InterceptorWriter {
		
		
		private String dest;
		
		public NullInterceptorWriter(String dest) {
			this.dest=dest;
		}

		public String getDestinationName() {
			return dest;
		}

		public void write(char[] ch, int start, int length) throws IOException {			
			
		}

		public void write(String text) throws IOException {
			logger.info("Recieved text of length "+text.length());
			
		}

		public void close() throws IOException {			
			
		}
		
	}
}