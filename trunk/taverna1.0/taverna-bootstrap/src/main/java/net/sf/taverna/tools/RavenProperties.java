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
 * Filename           $RCSfile: RavenProperties.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-05-23 16:17:40 $
 *               by   $Author: sowen70 $
 * Created on 23 Nov 2006
 *****************************************************************/
package net.sf.taverna.tools;


/**
 * Class to handle the raven.properties. Upon construction it first checks for a remote copy, which if found it downloads and stores locally.
 * If it fails to get a remote copy it uses a locally stored copy if present, otherwise as a last resort it uses
 * the copy bundled with the bootstrap jar.
 * 
 * @author Stuart Owen
 *
 */

@SuppressWarnings("serial")
public class RavenProperties extends AbstractConfiguration {

	@Override
	protected String getConfigurationFilename() {
		return "raven.properties";
	}

	@Override
	protected boolean isSystemOverrided() {
		return true;
	}
}
