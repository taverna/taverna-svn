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
 * Filename           $RCSfile: DataURLInterceptor.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-16 13:53:15 $
 *               by   $Author: sowen70 $
 * Created on 16 Apr 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.xml.EmbeddedReferenceInterceptor;
import uk.org.mygrid.dataproxy.xml.InterceptorReader;
import uk.org.mygrid.dataproxy.xml.ReaderFactory;

public class DataURLInterceptor implements EmbeddedReferenceInterceptor{

	private static String dataURL;
	private static ReaderFactory factory;
	
	public ReaderFactory getReaderFactory() {
		return factory;
	}

	public boolean referenceMatches(String content) {
		return content.startsWith(getServerDataURL());
	}
	
	private String getServerDataURL() {
		if (dataURL==null) {
			String path=ProxyConfigFactory.getInstance().getContextPath();
			dataURL = path+="data?id=";
			factory=new DataURLReaderFactory(dataURL);
		}
		return dataURL;
	}

}

class DataURLReaderFactory implements ReaderFactory
{
	private static Logger logger = Logger.getLogger(DataURLReaderFactory.class);
	private String dataURL;
	public DataURLReaderFactory(String dataURL) {
		this.dataURL=dataURL;
	}
	
	public InterceptorReader getReaderForContent(String content) {
		InterceptorReader result = null;
		
		String id=content.substring(dataURL.length());
		String[]parts=id.split("-");
		if (parts.length==3) {		
			String fileURL=ProxyConfigFactory.getInstance().getStoreBaseURL().toExternalForm();
			fileURL+=parts[0]+File.separatorChar+parts[1]+File.separatorChar+parts[2];		
			
			try {
				return new FileInterceptorReader(new URL(fileURL));
			} catch (MalformedURLException e) {
				logger.error("Invalid URL",e);
			} catch (IOException e) {
				logger.error("Unable to find file for data for "+content,e);
			}
		}
		else {
			logger.warn("Intercepted URL: '"+content+"' does not contain expected form of id. It should contain 3 parts with a '-' seperator.");
		}
		
		//if no data can be found then pass on the original URL
		if (result==null) result=new StringInterceptorReader(content);
		
		return result;
	}
	
}
