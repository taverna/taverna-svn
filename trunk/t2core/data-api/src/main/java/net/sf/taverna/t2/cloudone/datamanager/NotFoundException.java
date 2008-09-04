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
package net.sf.taverna.t2.cloudone.datamanager;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

/**
 * Thrown when a request is made for resolution of an entity or blob that cannot
 * be found by the store to which the request is made.
 *
 * @author Tom Oinn
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class NotFoundException extends Exception {

	private static final long serialVersionUID = -1069998094174721609L;

	public NotFoundException() {
	}

	@SuppressWarnings("unchecked")
	public NotFoundException(ReferenceScheme reference) {
		super("Can't find blob " + reference);
	}

	public NotFoundException(EntityIdentifier id) {
		super("Can't find entity " + id);
	}

	public NotFoundException(String msg) {
		super(msg);
	}

	public NotFoundException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public NotFoundException(Throwable throwable) {
		super(throwable);
	}

}
