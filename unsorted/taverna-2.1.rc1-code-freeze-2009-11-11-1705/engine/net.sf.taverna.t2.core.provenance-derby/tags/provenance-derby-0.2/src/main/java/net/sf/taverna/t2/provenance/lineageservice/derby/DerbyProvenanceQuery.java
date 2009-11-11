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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.utils.Var;

/**
 * Uses Apache Derby to carry out provenance queries
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
				// needs checking - 21.5.09 there are - this kind of custom where clause is very dangerous and 
				//should be refactored at some point
				if (entry.getKey().equals("V.inputOrOutput") || entry.getKey().equals("VB.positionInColl") || entry.getKey().equals("inputOrOutput")) {
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
	
	/**
	 * select Var records that satisfy constraints
	 */
	@Override
	public List<Var> getVars(Map<String, String> queryConstraints)
	throws SQLException {
		List<Var> result = new ArrayList<Var>();

		String q0 = "SELECT  * FROM Var V JOIN WfInstance W ON W.wfnameRef = V.wfInstanceRef";

		String q = addWhereClauseToQuery(q0, queryConstraints, true);
		
		List<String> orderAttr = new ArrayList<String>();
		orderAttr.add("V.reorder");

		String q1 = addOrderByToQuery(q, orderAttr, true);

//		logger.info("q1 = "+q1);
		
		Statement stmt;
		try {
			stmt = getConnection().createStatement();
			boolean success = stmt.execute(q1.toString());

			if (success) {
				ResultSet rs = stmt.getResultSet();

				while (rs.next()) {

					Var aVar = new Var();

					aVar.setWfInstanceRef(rs.getString("WfInstanceRef"));

					if (rs.getInt("inputOrOutput") == 1) {
						aVar.setInput(true);
					} else {
						aVar.setInput(false);
					}
					aVar.setPName(rs.getString("pnameRef"));
					aVar.setVName(rs.getString("varName"));
					aVar.setType(rs.getString("type"));
					aVar.setTypeNestingLevel(rs.getInt("nestingLevel"));
					aVar.setActualNestingLevel(rs.getInt("actualNestingLevel"));
					aVar.setANLset((rs.getInt("anlSet") == 1 ? true : false));
					result.add(aVar);

				}
			}
		} catch (InstantiationException e) {
			logger.warn("Could not execute query: " + e);
		} catch (IllegalAccessException e) {
			logger.warn("Could not execute query: " + e);
		} catch (ClassNotFoundException e) {
			logger.warn("Could not execute query: " + e);
		}

		// System.out.println("getVars: executing query\n"+q.toString());

		return result;
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
