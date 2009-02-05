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
package net.sf.taverna.t2.activities.apiconsumer.query;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import net.sf.taverna.t2.partition.ActivityQuery;

/**
 * 
 * @author Alex Nenadic
 *
 */
public class ApiConsumerQuery extends ActivityQuery{

	private static Logger logger = Logger.getLogger(ApiConsumerQuery.class);
	
	public ApiConsumerQuery(String property) {
		super(property);
	}

	@Override
	public void doQuery() {
		
		String apiconsumerDefinitionPath = getProperty();
		logger.info("About to parse API Consumer definition: "+ apiconsumerDefinitionPath);

		try {
			File apiconsumerDefinitionFile = new File(apiconsumerDefinitionPath);

			// Load the XML document into a JDOM Document
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new FileInputStream(apiconsumerDefinitionFile));
			Element root = doc.getRootElement();
			String apiConsumerName = root.getAttributeValue("name");
			Element apiConsumerDescriptionElement = root.getChild("Description");
			String apiConsumerDescription = apiConsumerDescriptionElement.getValue();

			// Iterate over the classes...
			Element classesElement = root.getChild("Classes");
			List<?> classesList = classesElement.getChildren("Class");
			for (Iterator<?> i = classesList.iterator(); i.hasNext();) {
				Element classElement = (Element) i.next();
				String className = classElement.getAttributeValue("name");
				// Iterate over methods
				Element methodsElement = classElement.getChild("Methods");
				List<?> methodsList = methodsElement.getChildren("Method");
				for (Iterator<?> j = methodsList.iterator(); j.hasNext();) {
					Element methodElement = (Element) j.next();

					String methodName = methodElement.getAttributeValue("name");
					String methodType = methodElement.getAttributeValue("type");
					boolean methodStatic = methodElement.getAttributeValue(
							"static", "false").equals("true");
					boolean methodConstructor = methodElement
							.getAttributeValue("constructor", "false").equals(
									"true");
					int dimension = Integer.parseInt(methodElement
							.getAttributeValue("dimension"));
					String description = methodElement.getChild("Description")
							.getTextTrim();
					List<?> paramList = methodElement.getChildren("Parameter");
					String[] pNames = new String[paramList.size()];
					String[] pTypes = new String[paramList.size()];
					int[] pDimensions = new int[paramList.size()];
					int count = 0;
					for (Iterator<?> k = paramList.iterator(); k.hasNext();) {
						Element parameterElement = (Element) k.next();
						pNames[count] = parameterElement
								.getAttributeValue("name");
						pTypes[count] = parameterElement
								.getAttributeValue("type");
						pDimensions[count] = Integer.parseInt(parameterElement
								.getAttributeValue("dimension"));
						count++;
					}
					
					ApiConsumerActivityItem activityItem = new ApiConsumerActivityItem();
					activityItem.setApiConsumerName(apiConsumerName);
					activityItem.setApiConsumerDescription(apiConsumerDescription);
					activityItem.setDescription(description);
					activityItem.setClassName(className);
					activityItem.setMethodName(methodName);
					activityItem.setParameterNames(pNames);
					activityItem.setParameterTypes(pTypes);
					activityItem.setParameterDimensions(pDimensions);
					activityItem.setReturnType(methodType);
					activityItem.setReturnDimension(dimension);
					activityItem.setIsConstructor(methodConstructor);
					activityItem.setIsStatic(methodStatic);
					add(activityItem);
				}
			}
		} catch (Exception ex) {
			JOptionPane
			.showMessageDialog(null, "Unable to add new API Consumer activity!\n"
					+ ex.getMessage(), "Error!",
					JOptionPane.ERROR_MESSAGE);
		}
		logger.info("Finished parsing API Consumer definition file : " + apiconsumerDefinitionPath);
	}

}