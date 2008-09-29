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
 * Filename           $RCSfile: DataServlet.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:53 $
 *               by   $Author: sowen70 $
 * Created on 15 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;

/**
 * A servlet responsibly for providing a data stream to the data identified by data?id='....'.
 * The ID is of the form <wsdlID>-<invocationID>-<dataID> 
 * 
 * An error is reported back if no data is found that matches the ID.
 * @author Stuart Owen
 *
 */

@SuppressWarnings("serial")
public class DataServlet extends ProxyBaseServlet{
	
	private static Logger logger = Logger.getLogger(DataServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		setContextOnServerInfo();
		
		String id=request.getParameter("id");
		String parts[]=id.split("-");
		if (parts.length!=3) {
			response.getWriter().println("Invalid id, there should be 3 parts to it: <wsdlID>-<invocationID>-<dataID>");
		}
		else {	
			String wsdlID=parts[0];
			String invocationID=parts[1];
			String dataID=parts[2];
							
			File file = null;
			try {				
				URL base = ProxyConfigFactory.getInstance().getStoreBaseURL();
				file = new File(base.toURI());
				file=new File(file,wsdlID);
				file=new File(file,invocationID);
				file=new File(file,dataID);
				logger.info("Reading data from "+file.getAbsolutePath());
				
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
				OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
				char buf[]=new char[1024];
				int l;
				while ((l=reader.read(buf))>0) {					
					writer.write(buf,0,l);					
				}				
				writer.close();
				reader.close();
			}
			catch(FileNotFoundException e) {			
				logger.error("Data did not exist at url:"+file.getAbsolutePath(),e);
				response.getWriter().println("Unable to find data!");
			}	
			catch(URISyntaxException e) {
				logger.error("Error with URI syntax of "+ProxyConfigFactory.getInstance().getStoreBaseURL(),e);
				response.getWriter().println("Unable to find data!");
			}			
		}
	}	
}
