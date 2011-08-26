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
