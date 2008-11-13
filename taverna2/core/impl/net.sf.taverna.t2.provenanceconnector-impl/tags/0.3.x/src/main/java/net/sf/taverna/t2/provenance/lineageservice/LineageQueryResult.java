/**
 * 
 */
package net.sf.taverna.t2.provenance.lineageservice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author paolo
 * bean to hold results from an SQL query to VarBinding join Var
 */
public class LineageQueryResult {

	final public static String COLL_TYPE = "referenceSetCollection";
	final public static String ATOM_TYPE = "referenceSet";

	List<LineageQueryResultRecord> records = new ArrayList<LineageQueryResultRecord>();

	public ListIterator<LineageQueryResultRecord> iterator() { return records.listIterator(); }
	
	public void addLineageQueryResultRecord(
			String pname,
			String vname,
			String wfInstance,
			String iteration,
			String value,
			String type) {

		LineageQueryResultRecord record = new LineageQueryResultRecord();

		record.setWfInstance(wfInstance);
		record.setPname(pname);
		record.setValue(value);
		record.setVname(vname);
		record.setIteration(iteration);

		records.add(record);
	}
}

