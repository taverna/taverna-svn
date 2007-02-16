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
 * Filename           $RCSfile: StringReaderFactory.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-16 14:01:43 $
 *               by   $Author: sowen70 $
 * Created on 15 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class StringReaderFactory implements ReaderFactory {

	private Map<String, InterceptorReader> readers = new HashMap<String, InterceptorReader>();
	
	public void addStringData(String reference, String data) {
		readers.put(reference, new StringInterceptorReader(data));
	}
	
	public InterceptorReader getReaderForReference(String reference) {
		return readers.get(reference);
	}
		
	class StringInterceptorReader implements InterceptorReader {
		private StringReader reader;
		
		public StringInterceptorReader(String data) {
			reader = new StringReader(data);			
		}

		public int read(char[] buffer, int offset, int len) throws IOException {
			return reader.read(buffer,offset,len);
		}

		public int read(char[] buffer) throws IOException {
			return reader.read(buffer);			
		}
		
	}
	
}