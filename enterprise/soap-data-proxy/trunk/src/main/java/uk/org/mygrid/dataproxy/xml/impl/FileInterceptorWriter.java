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
 * Filename           $RCSfile: FileInterceptorWriter.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:53 $
 *               by   $Author: sowen70 $
 * Created on 9 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;

import uk.org.mygrid.dataproxy.xml.InterceptorWriter;

/**
 * A fle based InterceptorWriter
 * 
 * @author Stuart Owen
 */
public class FileInterceptorWriter implements InterceptorWriter {
	
	private static Logger logger = Logger
			.getLogger(FileInterceptorWriter.class);
	
	private Writer writer;
	private String destinationReference;
	
	public FileInterceptorWriter(URL fileURL,String reference) throws FileNotFoundException, URISyntaxException {
		if (logger.isDebugEnabled()) logger.debug("Creating new FileInterceptorWriter to file: "+fileURL.toExternalForm());
		FileOutputStream outStream=new FileOutputStream(new File(fileURL.toURI()));
		writer = new BufferedWriter(new OutputStreamWriter(outStream));
		this.destinationReference=reference;
	}
	
	public String getDestinationReference() {
		return this.destinationReference;
	}

	public void write(char[] ch, int start, int length) throws IOException {		
		writer.write(ch,start,length);
		
	}

	public void write(String text) throws IOException {
		writer.write(text);
	}

	public void close() throws IOException{
		writer.close();
	}
	
	

}
