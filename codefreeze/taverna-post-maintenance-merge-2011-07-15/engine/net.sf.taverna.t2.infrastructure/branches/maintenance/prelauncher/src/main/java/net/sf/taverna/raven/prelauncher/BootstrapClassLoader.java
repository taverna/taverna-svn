/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
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
 * Filename           $RCSfile: BootstrapClassLoader.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/09/04 14:52:07 $
 *               by   $Author: sowen70 $
 * Created on 1 Nov 2006
 *****************************************************************/
package net.sf.taverna.raven.prelauncher;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * URLClassloader that allows URL's to be added dynamically.
 * 
 * If this classloader is set as the system class loader on the command line
 * (-Djava.system.class.loader=net.sf.taverna.tools.BootstrapClassLoader) this
 * effectively allows the classpath to be set dynamically.
 * 
 * @author David Withers
 */
public class BootstrapClassLoader extends URLClassLoader {

	/**
	 * Constructs an instance of BootstrapClassloader.
	 * 
	 * @param parent
	 */
	public BootstrapClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.net.URLClassLoader#addURL(java.net.URL)
	 */
	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}

}
