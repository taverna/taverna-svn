/**
 * 
 */
package net.sf.taverna.t2.lineageService;

/**
 * encapsulates an SQL query along with directives on how to interpret the results, i.e., which elements of the select clause 
 * are to be considered relevant. For instance when the query includes a join with Collection, the intent is that lineage should
 * return the collection itself as opposed to any of its elements.  
 * @author paolo
 *
 */
public class LineageSQLQuery {

	String SQLQuery = null;
	
	int nestingLevel = 0;  // =0 => use var values, >0 => use enclosing collection

	/**
	 * @return the sQLQuery
	 */
	public String getSQLQuery() {
		return SQLQuery;
	}

	/**
	 * @param query the sQLQuery to set
	 */
	public void setSQLQuery(String query) {
		SQLQuery = query;
	}

	/**
	 * @return the nestingLevel
	 */
	public int getNestingLevel() {
		return nestingLevel;
	}

	/**
	 * @param nestingLevel the nestingLevel to set
	 */
	public void setNestingLevel(int nestingLevel) {
		this.nestingLevel = nestingLevel;
	}
	
}
