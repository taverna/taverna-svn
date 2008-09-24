/*
 * Copyright (C) 2003 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate. Authorship of the modifications
 * may be determined from the ChangeLog placed at the end of this file.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *  
 */
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna;

import org.embl.ebi.escience.baclava.LSIDProvider;

/**
 * Implementation of
 * {@link org.embl.ebi.escience.baclava.LSIDProvider}
 * using time to provide unique ids.
 * 
 * @author dturi
 * @version $Id: TimedLSIDProvider.java,v 1.1 2007-12-14 12:49:16 stain Exp $
 */
public class TimedLSIDProvider implements LSIDProvider {

	public static final String LSID_PREFIX = "urn:lsid:net.sf.taverna:";

	/**
	 * Default constructor so an instance can be created for use by the enactor
	 * framework
	 */
	public TimedLSIDProvider() {
	}

	/**
	 * Returns a particularly dumb implementation of a unique identifier
	 */
	public synchronized String getID(LSIDProvider.NamespaceEnumeration namespace) {
		return LSID_PREFIX + namespace.toString() + ":"
				+ System.currentTimeMillis();
	}

}
