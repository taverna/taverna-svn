/*
 *
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
 */
package uk.ac.man.cs.img.fetaEngine.store;

import java.util.Set;

import uk.ac.man.cs.img.fetaEngine.webservice.CannedQueryType;

public interface IFetaModel {

	/* INQUIRY RELATED */
	public String freeFormQuery(String rdfQueryStatement)
			throws FetaEngineException;

	public Set cannedQuery(CannedQueryType queryType, String paramValue)
			throws FetaEngineException;

	public String retrieveByLSID(String lsid) throws FetaEngineException;

	/* PUBLICATION RELATED */

	// this one is for supporting registration of Feta XML descriptions
	public void publishDescription(String operationURLstr, String content)
			throws FetaEngineException;

	public void publishDescription(String operationURLstr)
			throws FetaEngineException;

	public void removeDescription(String operationURLstr)
			throws FetaEngineException;

	/* ADMINISTRATIVE OPERATION */
	public String getStoreContent() throws FetaEngineException;

	public FetaPersistentRegistryIndex getRegistryIndex() throws FetaEngineException;

}
