/*
 * OntologyAnchors.java
 *
 * Created on January 10, 2005, 10:28 AM
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * @author alperp
 */

public class OntologyAnchors {

	private Map anchorTypeToId;

	private Map anchorIdToType;

	public OntologyAnchors() {
		anchorIdToType = new HashMap();
		anchorTypeToId = new HashMap();

		getOntologyAnchors();

	}

	public void getOntologyAnchors() {
		try {

			int i = 0;

			Properties fetaProperties = getProperties();

			while (fetaProperties.containsKey("fetaClient.ontology.anchorType"
					+ i)) {
				String anchorType = fetaProperties
						.getProperty("fetaClient.ontology.anchorType" + i);
				String anchorID = fetaProperties
						.getProperty("fetaClient.ontology.anchorId" + i);

				ArrayList anchIDList;
				if (anchorTypeToId.containsKey(QueryCriteriaType
						.fromAnchorTypeString(anchorType))) {
					anchIDList = (ArrayList) anchorTypeToId
							.get(QueryCriteriaType
									.fromAnchorTypeString(anchorType));
				} else {
					anchIDList = new ArrayList();
				}
				anchIDList.add(anchorID);
				anchorTypeToId.put(QueryCriteriaType
						.fromAnchorTypeString(anchorType), anchIDList);

				// do the reverse as well
				ArrayList anchTypeList;
				if (anchorIdToType.containsKey(anchorID)) {
					anchTypeList = (ArrayList) anchorIdToType.get(anchorID);
				} else {
					anchTypeList = new ArrayList();
				}
				anchTypeList.add(QueryCriteriaType
						.fromAnchorTypeString(anchorType));
				anchorIdToType.put(anchorID, anchTypeList);

				i++;
			}

		} catch (java.io.IOException ex) {
			ex.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public List getTypesForAnchor(String ontoTermID) {
		if (anchorIdToType.containsKey(ontoTermID)) {
			return (List) anchorIdToType.get(ontoTermID);

		} else {
			return null;
		}
	}

	public List getAnchorsForType(QueryCriteriaType queryType) {

		if (anchorTypeToId.containsKey(queryType.getAnchorTypeName())) {
			return (List) anchorTypeToId.get(queryType.getAnchorTypeName());
		} else {
			return null;
		}
	}

	public boolean isAnchor(String ontoTermID) {

		return anchorIdToType.containsKey(ontoTermID);
	}

	public Properties getProperties() throws java.io.IOException {
		return FetaClientProperties.getInstance().getProperties();
	}
}
