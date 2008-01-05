/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Wei Tan, Ravi Madduri, U Chicago
 */


package org.embl.ebi.escience.scuflworkers.gt4;
import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPBodyElement;

import javax.xml.xpath.*;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;





public class IndexService {

	/**
	 * @param args
	 */
	
	public static void getServiceMetaData(String indexServiceAddress, ServiceMetaData serviceMetaData) {
		// TODO Auto-generated method stub
		
		//Java DII web service client
		
		//indexServiceAddress = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";
        
        
		try {
			
			
			//1 create service and call
			 Service  service = new Service();
			 Call     call    = (Call) service.createCall();
			 call.setTargetEndpointAddress( new java.net.URL(indexServiceAddress));
			 
			 /*the request SOAP body
			  * <m:QueryResourceProperties xmlns:m="http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd">
			<m:QueryExpression Dialect="http://www.w3.org/TR/1999/REC-xpath-19991116">/</m:QueryExpression>
			  * */

			// 2.1.1 Now we create a root element (<QueryResourceProperties> the name of the method)
			 DocumentBuilder builder;
		     builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		        Document doc;
		        doc = builder.newDocument();

		        String nameSpace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd";
		        String methodName = "QueryResourceProperties";
		        String parameterName = "QueryExpression";
		        Element methodElement;
		        methodElement = doc.createElementNS(nameSpace,methodName);
		        // 2.1.2 The next element we need is the parameter (<QueryExpression>) ...
		        Element parameterElement;
		       
		        parameterElement = doc.createElementNS(nameSpace, parameterName);
		        parameterElement.setAttribute("Dialect", "http://www.w3.org/TR/1999/REC-xpath-19991116");
		        // 2.1.3 ... and it's corresponding content (example /)
		        Node parameter;
		        parameter = doc.createTextNode("/");
		        // 2.1.4 Now we assemble the document
		        parameterElement.appendChild(parameter);
		        methodElement.appendChild(parameterElement);
		        
		     // 3. Invocation of the service
		        // 3.1 Now we have to create one new SOAPBody element for our request ...
		        SOAPBodyElement[] request = new SOAPBodyElement[1];
		        // 3.2 ... and we add to this our request document
		        request[0] = new SOAPBodyElement(methodElement);
		        // System.out.println(input[0].toString()); print it if you want
		        // 3.3 And now the real invocation
		        Vector resultVector = (Vector) call.invoke( request );


		        // 4. Extracting the result document
		        // 4.1 Wrapped document style web services will return only one document
		        SOAPBodyElement resultElements = (SOAPBodyElement) resultVector.get(0);
		        // 4.2 We convert it to a DOM object for later processing
		        Element resultElement  = resultElements.getAsDOM();
		        Document d = resultElement.getOwnerDocument();

		        // 5. Now we do something with it
		        // 5.1 E.g. we print it out
		      // System.out.println(XMLUtils.ElementToString(resultElement));
		        
		        
		        //use XPath & XQuery to get services/operations name
		        XPath xpath = XPathFactory.newInstance().newXPath();
		        String expression2 = "//*[namespace-uri()='http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd' and local-name()='Entry']";
		        String expression ="//*[namespace-uri()='http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd' " +
		        		"and local-name()='Entry'] [ *[namespace-uri()='http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd' " +
		        		"and local-name()='MemberServiceEPR']/wsa:Address/text() ='https://cagrid-dorian.nci.nih.gov:8443/wsrf/services/cagrid/Dorian' ][1] " +
		        		"/*[namespace-uri()='http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd' and local-name()='Content']" +
		        		"/*[namespace-uri()='http://mds.globus.org/aggregator/types' and local-name()='AggregatorData']/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata' " +
		        		"and local-name()='ServiceMetadata']/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata' and local-name()='serviceDescription']" +
		        		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' " +
		        		"and local-name()='Service']/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' and local-name()='serviceContextCollection']" +
		        		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' " +
		        		"and local-name()='ServiceContext']/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' and local-name()='operationCollection']" +
		        		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' and local-name()='Operation']";
		        
		        String addressExp = "//*[namespace-uri()='http://mds.globus.org/index' and local-name()='IndexRP']" +
		        		"/*[namespace-uri()='http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd' and local-name()='Entry']" +
		        		" [count (*[namespace-uri()='http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd' and local-name()='Content']" +
		        		"/*[namespace-uri()='http://mds.globus.org/aggregator/types' and local-name()='AggregatorData']" +
		        		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata' and local-name()='ServiceMetadata']" +
		        		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata' and local-name()='serviceDescription']" +
		        		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' and local-name()='Service']" +
		        		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' and local-name()='serviceContextCollection']" +
		        		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' and local-name()='ServiceContext']" +
		        		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' and local-name()='operationCollection'])!=0]" +
		        		"/*[namespace-uri()='http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd' and local-name()='MemberServiceEPR']" +
		        		"/*[namespace-uri()='http://schemas.xmlsoap.org/ws/2004/03/addressing' and local-name()='Address']";
		        
		          //System.out.println(addressExp);
		          Object nodes = xpath.evaluate(addressExp, d, XPathConstants.NODESET);
		          NodeList serviceNodeList = (NodeList) nodes;
		          serviceMetaData.serviceAddress = new String [serviceNodeList.getLength()];
		          serviceMetaData.operationName = new String [serviceNodeList.getLength()][];
		          for (int i = 0; i < serviceNodeList.getLength(); i++) {
		              Element serviceElement = (Element) serviceNodeList.item(i);
		              serviceMetaData.serviceAddress[i] = serviceElement.getTextContent();
		              System.out.println("***"+serviceElement.getTextContent());
		              String operationExp = "//*[namespace-uri()='http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd' " +
		              		"and local-name()='Entry'] " +
		              		"[ *[namespace-uri()='http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd' and local-name()='MemberServiceEPR']" +
		              		"/*[namespace-uri()='http://schemas.xmlsoap.org/ws/2004/03/addressing' and local-name()='Address']" +
		              		"/text()='"+ serviceMetaData.serviceAddress[i] +"' ][1] " +
		              		"/*[namespace-uri()='http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd' and local-name()='Content']" +
		              		"/*[namespace-uri()='http://mds.globus.org/aggregator/types' and local-name()='AggregatorData']" +
		              		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata' and local-name()='ServiceMetadata']" +
		              		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata' and local-name()='serviceDescription']" +
		              		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' and local-name()='Service']" +
		              		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' and local-name()='serviceContextCollection']" +
		              		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' and local-name()='ServiceContext']" +
		              		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' and local-name()='operationCollection']" +
		              		"/*[namespace-uri()='gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service' and local-name()='Operation']";
		              //System.out.println(operationExp);
			          Object nodes2 = xpath.evaluate(operationExp, d, XPathConstants.NODESET);
			          NodeList operationNodeList = (NodeList) nodes2;
			          serviceMetaData.operationName [i] = new String [operationNodeList.getLength()];
		              for(int j=0;j<operationNodeList.getLength();j++){
		            	  Element operationElement = (Element) operationNodeList.item(j);
		            	  serviceMetaData.operationName[i][j] = operationElement.getAttribute("name");
			              System.out.println(serviceMetaData.operationName[i][j]);
		              }
		        	
		          }

		        
		        //System.out.println(XMLUtils.ElementToString(operationElement));
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
      


	}

}
