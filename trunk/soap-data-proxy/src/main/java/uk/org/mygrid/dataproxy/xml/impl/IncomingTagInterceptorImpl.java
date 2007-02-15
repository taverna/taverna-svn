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
 * Filename           $RCSfile: IncomingTagInterceptorImpl.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-15 14:34:23 $
 *               by   $Author: sowen70 $
 * Created on 8 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml.impl;

import uk.org.mygrid.dataproxy.xml.ElementDef;
import uk.org.mygrid.dataproxy.xml.TagInterceptor;
import uk.org.mygrid.dataproxy.xml.WriterFactory;

public class IncomingTagInterceptorImpl implements TagInterceptor {

	private ElementDef target;	
	private WriterFactory writerFactory;
	
	public IncomingTagInterceptorImpl(ElementDef targetElement, WriterFactory writerFactory) {
		this.target=targetElement;		
		this.writerFactory=writerFactory;
	}	

	public ElementDef getTargetElementDef() {
		return target;
	}

	public WriterFactory getWriterFactory() {
		return writerFactory;
	}

}
