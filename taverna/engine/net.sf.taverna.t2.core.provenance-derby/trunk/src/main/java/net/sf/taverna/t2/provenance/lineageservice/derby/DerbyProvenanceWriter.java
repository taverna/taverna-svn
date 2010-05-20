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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.utils.Port;

/**
 * Uses Apache Derby database to write workflow provenance
 * 
 * @author Paolo Missier
 * @author Ian Dunlop
 * @author Stuart Owen
 * 
 */
public class DerbyProvenanceWriter extends ProvenanceWriter {
	
	public DerbyProvenanceWriter() {
		
	}	
	
	/**
	 * persists port v back to DB
	 * 
	 * @param v
	 * @throws SQLException
	 */
	@Override
	public void updatePort(Port v) throws SQLException {
		
		PreparedStatement ps = null;
                Connection connection = null;
		
		try {
                        connection = getConnection();
			ps = connection
			.prepareStatement(
					"UPDATE Port SET inputOrOutput=?, nestingLevel = ?,"
					+ "actualNestingLevel = ?, anlSet = ? , Port.reorder = ? WHERE varName = ? AND pnameRef = ? AND wfInstanceRef = ?");
			int i = v.isInputPort() ? 1 : 0;
			ps.setInt(1, i);
			ps.setInt(2, v.getDepth());
			ps.setInt(3, v.getGranularDepth());
			int j = v.isGranularDepthSet() ? 1 : 0;
			ps.setInt(4, j);
			ps.setInt(5, v.getIterationStrategyOrder());
			ps.setString(6, v.getPortName());
			ps.setString(7, v.getProcessorName());
			ps.setString(8, v.getWorkflowId());
			
			ps.execute();
		
		} finally {
                    if (connection != null) connection.close();
                }

	}

}
