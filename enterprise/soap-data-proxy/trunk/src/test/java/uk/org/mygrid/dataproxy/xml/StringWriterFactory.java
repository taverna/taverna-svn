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
 * Filename           $RCSfile: StringWriterFactory.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-16 16:13:58 $
 *               by   $Author: sowen70 $
 * Created on 9 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * WriterFactory that creates an InterceptorWriter that uses StringWriter for testing purposes.
 * The desitnation name is simply a numerical count of each writer as they are requested. 
 * @author sowen
 *
 */
public class StringWriterFactory implements WriterFactory {
	
	private int writerCount=0;
	
	private final List<StringWriter> writers = new ArrayList<StringWriter>();

	public InterceptorWriter newWriter() {
		writerCount++;
		StringWriter writer = new StringWriter();
		writers.add(writer);
		return new StringInterceptorWriter(writer,writerCount);
		
	}
	
	public List<String> getOutputsWritten() {
		List<String> result = new ArrayList<String>();
		for (StringWriter writer : writers) {
			result.add(writer.getBuffer().toString());
		}
		return result;
	}
	
	class StringInterceptorWriter implements InterceptorWriter {

		private StringWriter writer = new StringWriter();
		private String destinationName;
		
		public StringInterceptorWriter(StringWriter writer, int count) {
			this.writer=writer;
			this.destinationName=String.valueOf(count);
		}
		
		public String getDestinationReference() {
			return destinationName;
		}

		public void write(char[] ch, int start, int length) throws IOException {
			writer.write(ch,start,length);
		}

		public void write(String text) throws IOException {
			writer.write(text);			
		}

		public void close() throws IOException {
			writer.close();			
		}		
		
	}
}
