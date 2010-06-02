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
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.utils.Var;

/**
 * Uses Apache Derby database to write workflow provenance
 * 
 * @author Paolo Missier
 * @author Ian Dunlop
 * 
 */
public class DerbyProvenanceWriter extends ProvenanceWriter {
	
	public DerbyProvenanceWriter() {
		
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
	
	/**
	 * persists var v back to DB
	 * 
	 * @param v
	 * @throws SQLException
	 */
	@Override
	public void updateVar(Var v) throws SQLException {
		// Statement stmt;
		PreparedStatement ps = null;
		// String u = "UPDATE Var " + "SET type = \'" + v.getType() + "\'"
		// + ", inputOrOutput = \'" + (v.isInput() ? 1 : 0) + "\' "
		// + ", nestingLevel = \'" + v.getTypeNestingLevel() + "\' "
		// + ", actualNestingLevel = \'" + v.getActualNestingLevel()
		// + "\' " + ", anlSet = \'" + (v.isANLset() ? 1 : 0) + "\' "
		// + "WHERE varName = \'" + v.getVName() + "\' "
		// + "AND pnameRef = \'" + v.getPName() + "\' "
		// + "AND wfInstanceRef = \'" + v.getWfInstanceRef() + "\'";
		try {
			ps = getConnection()
			.prepareStatement(
					"UPDATE Var SET type = ?, inputOrOutput=?, nestingLevel = ?,"
					+ "actualNestingLevel = ?, anlSet = ? , Var.reorder = ? WHERE varName = ? AND pnameRef = ? AND wfInstanceRef = ?");
			ps.setString(1, v.getType());
			int i = v.isInput() ? 1 : 0;
			ps.setInt(2, i);
			ps.setInt(3, v.getTypeNestingLevel());
			ps.setInt(4, v.getActualNestingLevel());
			int j = v.isANLset() ? 1 : 0;
			ps.setInt(5, j);
			ps.setInt(6, v.getPortNameOrder());
			ps.setString(7, v.getVName());
			ps.setString(8, v.getPName());
			ps.setString(9, v.getWfInstanceRef());

			// stmt = getConnection().createStatement();
			//			
			// System.out.println("executing: "+u);

			boolean success = ps.execute();
		} catch (InstantiationException e) {
			logger.warn("Could not execute query: " + e);
		} catch (IllegalAccessException e) {
			logger.warn("Could not execute query: " + e);
		} catch (ClassNotFoundException e) {
			logger.warn("Could not execute query: " + e);
		}

		// System.out.println("update executed");
	}

}
