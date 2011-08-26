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
 * Filename           $RCSfile: WSDLResolver.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-09-01 09:43:17 $
 *               by   $Author: sowen70 $
 ****************************************************************/
package uk.ac.man.cs.img.fetaClient.util;

import java.io.InputStream;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

import com.ibm.wsdl.xml.WSDLReaderImpl;

/**
 * WSDLResolver.java
 * 
 * 
 * Created: Thu Mar 18 12:17:38 2004
 * 
 * @author <a href="mailto:p.lord@russet.org.uk">Phillip Lord</a>
 * @version 1.0
 */
public class WSDLResolver {
	private WSDLResolver() {
	}

	public static final WSDLResolver instance = new WSDLResolver();

	public static WSDLResolver getInstance() {
		return instance;
	}

	public Definition resolveURL(String url) throws WSDLException {
		Definition definition;

		WSDLReaderImpl reader = new WSDLReaderImpl();
		definition = reader.readWSDL(url);

		return definition;
	}

	public Definition resolveStream(InputStream WSDLStream)
			throws WSDLException {
		Definition definition;

		WSDLReaderImpl reader = new WSDLReaderImpl();
		definition = reader.readWSDL(null, new org.xml.sax.InputSource(
				WSDLStream));

		// definition = reader.readWSDL( url );

		return definition;
	}

} // WSDLResolver
