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
package net.sf.taverna.t2.provenance.lineageservice.derby;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;

/**
 * Add your own database and carry out provenance queries
 * 
 * @author Ian Dunlop
 * 
 */
public class DerbyProvenanceQuery extends ProvenanceQuery {

	public DerbyProvenanceQuery() {

	}

	/*
	 * Overrides super class because Derby has issues with non-numerical values
	 * requiring quotes
	 * 
	 * @see
	 * net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#addWhereCaluseToQuery
	 */
	@Override
	@SuppressWarnings("unused")
	protected String addWhereClauseToQuery(String q0,
			Map<String, String> queryConstraints, boolean terminate) {
		//FIXME terminate not required (I think!)

		// complete query according to constraints
		StringBuffer q = new StringBuffer(q0);

		boolean first = true;
		if (queryConstraints != null && queryConstraints.size() > 0) {
			q.append(" where ");

			for (Entry<String, String> entry : queryConstraints.entrySet()) {
				if (!first) {
					q.append(" and ");
				}
				// FIXME there may be more numerical inputs than inputOrOutput,
				// needs checking
				if (entry.getKey().equalsIgnoreCase("V.inputOrOutput")) {
					q.append(" " + entry.getKey() + " = " + entry.getValue());
				} else {
					q.append(" " + entry.getKey() + " = \'" + entry.getValue()
							+ "\' ");
				}
				first = false;
			}
		}

		return q.toString();
	}

	@Override
	protected void openConnection() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		getClass().getClassLoader().loadClass(
				"org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		try {
			connection = DriverManager.getConnection(getDbURL());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
