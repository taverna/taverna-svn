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
package net.sf.taverna.t2.cloudone.refscheme;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Information for reference provides mandatory keys for reference scheme
 * validation.
 *
 * @author Tom Oinn
 * @author Matthew Pocock
 *
 * @param <RS> {@link ReferenceScheme} subclass
 */
@SuppressWarnings("unchecked")
public interface ReferenceSchemeInfoSPI<RS extends ReferenceScheme> {

	/**
	 * The keys that this reference scheme information will require the DataManager
	 * instance to provide values for to handle requests for contextual
	 * validation of reference scheme instances. The set contains lists of
	 * strings, each list is interpreted as a path into a configuration tree -
	 * where this list is a single string this is equivalent to a flat
	 * properties file style configuration document.
	 *
	 * @return Required keys
	 */
	public Map<String, Set<List<String>>> getRequiredKeys();
}
