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
package net.sf.taverna.t2.activities.ncbi.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.taverna.t2.partition.ActivityQuery;

/**
 * Creates the {@link NCBIActivityItem}s from the file ncbi_services on disk.
 * This file contains 2 lines for each service: a line for the type of category
 * and its operation and a line for the wsdl address
 * 
 * @author Ian Dunlop
 * 
 */
public class NCBIQuery extends ActivityQuery {

	public NCBIQuery(String property) {
		super(property);

	}

	/**
	 * Splits up the lines representing the services in ncbi_services and then
	 * calls createItem with its name, wsdl url & operation called to generate
	 * the {@link NCBIActivityItem} The NCBI services can be queried as category
	 * "ncbi".
	 */
	@Override
	public void doQuery() {
		InputStream inputStream = getClass().getResourceAsStream(
				"/ncbi_services");
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(
				inputStream));
		String line = "";
		String name = null;
		String category = null;
		String wsdlOperation = null;
		try {
			while ((line = inputReader.readLine()) != null) {
				if (line.startsWith("category")) {
					String[] split = line.split(":");
					// category = split[1]; not used at the moment
					name = split[2];
					wsdlOperation = split[3];
				} else {
					NCBIActivityItem createItem = createItem(line, name,
							wsdlOperation);
					createItem.setCategory("ncbi");
					add(createItem);
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Create an {@link NCBIActivityItem} with a WSDL url, a web service
	 * operation and a name
	 * 
	 * @param line
	 * @param name
	 * @param wsdlOperation
	 * @return
	 */
	private NCBIActivityItem createItem(String line, String name,
			String wsdlOperation) {
		NCBIActivityItem item = new NCBIActivityItem();
		item.setUrl(line);
		item.setOperation(name);
		item.setWsdlOperation(wsdlOperation);
		return item;
	}

}
