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
 * Filename           $RCSfile: FileInterceptorWriterFactory.java,v $
 * Revision           $Revision: 1.10 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:53 $
 *               by   $Author: sowen70 $
 * Created on 9 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml.impl;

import java.net.URL;

import org.apache.log4j.Logger;

import uk.org.mygrid.dataproxy.xml.InterceptorWriter;
import uk.org.mygrid.dataproxy.xml.WriterFactory;

/**
 * A Factory that creates a new FileInterceptorWriter when requested.
 * @author Stuart Owen
 *
 */
public class FileInterceptorWriterFactory implements WriterFactory {
	
	private static Logger logger = Logger
			.getLogger(FileInterceptorWriterFactory.class);

	private URL baseURL=null;
	private int c=1;
	private String prefix;
	private String baseReference;
	
	public FileInterceptorWriterFactory(URL baseURL, String baseReference, String dataFilePrefix) {
		this.baseURL=baseURL;
		this.prefix=dataFilePrefix;
		this.baseReference=baseReference;
	}
	
	/**
	 * Creates a new file based FileInterceptorWriter. The file is based upon the baseURL and the dataFilePrefix. The datafile
	 * has a count appended which increments for each element if contained in a list.
	 */
	public InterceptorWriter newWriter() throws Exception {
		String fileName=prefix+String.valueOf(c++);				
		URL fileURL=new URL(baseURL,fileName);
		String reference=baseReference+"-"+fileName;
		if (logger.isDebugEnabled()) logger.debug("Created FileInterceptorWriter to write to file:"+fileURL.toExternalForm()+", for href:"+reference);
		return new FileInterceptorWriter(fileURL,reference);
	}	
}
