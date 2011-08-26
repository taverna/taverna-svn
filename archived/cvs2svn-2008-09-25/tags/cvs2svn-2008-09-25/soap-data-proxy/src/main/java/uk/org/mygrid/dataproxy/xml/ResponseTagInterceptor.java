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
 * Filename           $RCSfile: ResponseTagInterceptor.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:52 $
 *               by   $Author: sowen70 $
 * Created on 8 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml;

/**
 * A Tag interceptor that defines an interceptor that is used to intercept a response stream
 * from a SOAP response based on the element. When such an interception takes place the data is redirected
 * to the writer provided by the WriterFactory until a matching endtag is encountered.
 * @author Stuart Owen
 *
 */

public interface ResponseTagInterceptor extends TagInterceptor {	
	public WriterFactory getWriterFactory();		
}
