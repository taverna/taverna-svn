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
package net.sf.taverna.t2.provenance.lineageservice.mysql;


import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;



/**
 * @author paolo
 *
 */
public class MySQLProvenanceQuery extends ProvenanceQuery {

	
	public MySQLProvenanceQuery() {
		
	}
	
	private String escapeValue(final String s) {
		return s.replaceAll("\\'", "\\\\\\'");
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery#addWhereClauseToQuery(java.lang.String, java.util.Map, boolean)
	 */
	@Override
	protected String addWhereClauseToQuery(String q0,
			Map<String, String> queryConstraints, boolean terminate) {

		// complete query according to constraints
		StringBuffer q = new StringBuffer(q0);

		boolean first = true;
		if (queryConstraints != null && queryConstraints.size() > 0) {
			q.append(" where ");

			for (Entry<String, String> entry : queryConstraints.entrySet()) {
				if (!first) {
					q.append(" and ");
				}
				q.append(" " + entry.getKey() + " = \'" + escapeValue(entry.getValue()) + "\' ");
				first = false;
			}
		}

		return q.toString();
	}
	
	
	
}
