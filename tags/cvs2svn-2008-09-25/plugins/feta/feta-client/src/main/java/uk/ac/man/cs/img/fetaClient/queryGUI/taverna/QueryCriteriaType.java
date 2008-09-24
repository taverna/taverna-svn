/*
 *
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 */
package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.util.HashMap;
import java.util.Map;

import uk.ac.man.cs.img.fetaEngine.util.AbstractEnumeration;
import uk.ac.man.cs.img.fetaEngine.webservice.CannedQueryType;

/**
 * @author alperp
 * 
 * 
 * 
 */
public final class QueryCriteriaType extends AbstractEnumeration {

	private Class modelClass;

	private String webServiceType;

	private String ontologyAnchorType;

	// private Map wsTypeNametoCriteriaType;
	private static Map anchorTypeNametoCriteriaType = new java.util.HashMap();

	private static String taskCriteriaStr = "performs task";

	private static String taskAnchorStr = "task";

	private static String methodCriteriaStr = "uses method";

	private static String methodAnchorStr = "method";

	private static String resourceCriteriaStr = "uses resource";

	private static String resourceAnchorStr = "resource";

	// ONTO-CHNG-PROPGTN private static String resourceContentCriteriaStr = "has
	// resource content";
	// ONTO-CHNG-PROPGTN private static String resourceContentAnchorStr =
	// "resource-content";

	// ONTO-CHNG-PROPGTN private static String applicationCriteriaStr = "is
	// function of";
	// ONTO-CHNG-PROPGTN private static String applicationAnchorStr =
	// "application";

	private static String inputCriteriaStr = "accepts input";

	private static String outputCriteriaStr = "produces output";

	private static String inputOutputAnchorStr = "input-output";

	private static String typeCriteriaStr = "has type";

	private static String nameCriteriaStr = "name contains";

	private static String descCriteriaStr = "description contains";

	private QueryCriteriaType(String toString) {
		super(toString);
		anchorTypeNametoCriteriaType = new HashMap();
		// wsTypeNametoCriteriaType = new HashMap();

	} // constructor

	private QueryCriteriaType(String toString, Class modelClass) {
		this(toString);
		this.modelClass = modelClass;
	} // constructor

	private QueryCriteriaType(String toString, Class modelClass, String wsType) {
		this(toString);
		this.webServiceType = wsType;
	} // constructor

	private QueryCriteriaType(String toString, Class modelClass, String wsType,
			String ontologyAnchorType) {
		this(toString);
		this.webServiceType = wsType;
		this.ontologyAnchorType = ontologyAnchorType;
	} // constructor

	public static final QueryCriteriaType TASK_CRITERIA_TYPE = new QueryCriteriaType(
			taskCriteriaStr, OntologyClass.class, CannedQueryType.ByTask
					.toString(), taskAnchorStr);

	public static final QueryCriteriaType METHOD_CRITERIA_TYPE = new QueryCriteriaType(
			methodCriteriaStr, OntologyClass.class, CannedQueryType.ByMethod
					.toString(), methodAnchorStr);

	public static final QueryCriteriaType RESOURCE_CRITERIA_TYPE = new QueryCriteriaType(
			resourceCriteriaStr, OntologyClass.class,
			CannedQueryType.ByResource.toString(), resourceAnchorStr);

	// ONTO-CHNG-PROPGTN public static final QueryCriteriaType
	// RESOURCE_CONTENT_CRITERIA_TYPE =
	// ONTO-CHNG-PROPGTN new
	// QueryCriteriaType(resourceContentCriteriaStr,OntologyClass.class,
	// CannedQueryType.ByResourceContent.toString(), resourceContentAnchorStr);
	// ONTO-CHNG-PROPGTN public static final QueryCriteriaType
	// APPLICATION_CRITERIA_TYPE =
	// ONTO-CHNG-PROPGTN new
	// QueryCriteriaType(applicationCriteriaStr,OntologyClass.class,
	// CannedQueryType.ByApplication.toString(), applicationAnchorStr);
	public static final QueryCriteriaType INPUT_CRITERIA_TYPE = new QueryCriteriaType(
			inputCriteriaStr, OntologyClass.class, CannedQueryType.ByInput
					.toString(), inputOutputAnchorStr);

	public static final QueryCriteriaType OUTPUT_CRITERIA_TYPE = new QueryCriteriaType(
			outputCriteriaStr, OntologyClass.class, CannedQueryType.ByOutput
					.toString(), inputOutputAnchorStr);

	public static final QueryCriteriaType TYPE_CRITERIA_TYPE = new QueryCriteriaType(
			typeCriteriaStr, OntologyClass.class, CannedQueryType.ByType
					.toString());

	public static final QueryCriteriaType NAME_CRITERIA_TYPE = new QueryCriteriaType(
			nameCriteriaStr, String.class, CannedQueryType.ByName.toString());

	public static final QueryCriteriaType DESCRIPTION_CRITERIA_TYPE = new QueryCriteriaType(
			descCriteriaStr, String.class, CannedQueryType.ByDescription
					.toString());

	static {

		anchorTypeNametoCriteriaType.put(taskAnchorStr, TASK_CRITERIA_TYPE);
		anchorTypeNametoCriteriaType.put(methodAnchorStr, METHOD_CRITERIA_TYPE);
		anchorTypeNametoCriteriaType.put(resourceAnchorStr,
				RESOURCE_CRITERIA_TYPE);
		// ONTO-CHNG-PROPGTN
		// anchorTypeNametoCriteriaType.put(resourceContentAnchorStr,
		// RESOURCE_CONTENT_CRITERIA_TYPE);
		// ONTO-CHNG-PROPGTN
		// anchorTypeNametoCriteriaType.put(applicationAnchorStr,
		// APPLICATION_CRITERIA_TYPE);
		anchorTypeNametoCriteriaType.put(inputOutputAnchorStr,
				INPUT_CRITERIA_TYPE);
		// this causes one line of copying map contents for input key to output
		// key
		//

	}

	/**
	 * @return
	 */
	public Class getModelClass() {
		return modelClass;
	}

	public String getWebServiceCompatibleType() {
		return webServiceType;
	}

	public String getAnchorTypeName() {
		return ontologyAnchorType;
	}

	public static QueryCriteriaType fromAnchorTypeString(String anchorTypeString) {
		if (anchorTypeNametoCriteriaType.containsKey(anchorTypeString)) {
			return (QueryCriteriaType) anchorTypeNametoCriteriaType
					.get(anchorTypeString);
		} else {
			return null;
		}
	} // constructor

}
