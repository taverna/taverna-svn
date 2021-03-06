package net.sf.taverna.t2.reference;

/**
 * The T2Reference interface is used to identify several different kinds of
 * information, namely ReferenceSet, IdentifiedList and ErrorDocument. Because
 * the top level reference service needs to determine which sub-service to
 * delegate to when resolving references we carry this information in each
 * T2Reference in the form of one of these enumerated types.
 * 
 * @author Tom Oinn
 * 
 */
public enum T2ReferenceType {

	/**
	 * A reference to a ReferenceSet
	 */
	ReferenceSet,
	
	/**
	 * A reference to an IdentifiedList of other T2References
	 */
	IdentifiedList,
	
	/**
	 * A reference to an ErrorDocument
	 */
	ErrorDocument;

}
