////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// University of Southampton IT Innovation Centre, 2002
//
// Copyright in this software belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP,
// UK.
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement or otherwise without the prior written
// consent of the copyright owner.
//
//	Created By :          Darren Marvin
//	Created Date :        2002/4/10
//	Created for Project:  MYGRID
//	Dependencies:
//
//	Last commit info :    $Author: mereden $
//                        $Date: 2004-01-27 12:57:52 $
//                        $Revision: 1.8 $
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.dispatcher;

import uk.ac.soton.itinnovation.mygrid.workflow.enactor.invocation.InvocationDescription;

// Network Imports
import java.net.URL;

import java.lang.IllegalArgumentException;
import java.lang.String;



public class TavernaInvocationDescription implements InvocationDescription {

  private String description;
  private String portTypeName;
  private String operationName;
  private URL selectedWSDLURL;
  private String requestMessageName;
  private String responseMessageName;
  private int timeout = 0; //indefinate

  public TavernaInvocationDescription(String description,
                                   String portTypeName,
                                   String operationName,
                                   URL selectedWSDLURL,
                                   String requestMessageName,
                                   String responseMessageName,
								int socketTimeout) throws IllegalArgumentException{
    if(description==null||
       portTypeName==null||
       operationName==null||
       selectedWSDLURL==null||
       requestMessageName==null||
       responseMessageName==null||socketTimeout<0)
      throw new IllegalArgumentException();
    this.description = description;
    this.portTypeName = portTypeName;
    this.operationName = operationName;
    this.selectedWSDLURL = selectedWSDLURL;
    this.requestMessageName = requestMessageName;
    this.responseMessageName = responseMessageName;
		this.timeout = socketTimeout;
  }

  /**
   * Obtain a description associated with this invocation,
   * @return description
   */
  public String getName() {
    return description;
  }

  /**
   * Obtain the WSDL portType identifier required for this invocation.
   * @return identifier
   */
  public String getPortType() {
    return portTypeName;
  }

  /**
   * Obtain the WSDL Operation identifier required for this invocation
   * @return identifier
   */
  public String getOperation() {
    return operationName;
  }

  /**
   * Obtain the WSDL selected for invocation against
   * @return URL location
   */
  public URL getSelectedServiceWSDLURL() {
    return selectedWSDLURL;
  }

  /**
   * Obtain the WSDL RequestMessageName for the operation
   * @return identifier
   */
  public String getRequestMessageName() {
    return requestMessageName;
  }

  /**
   * Obtain the WSDL ResponseMessageName for the operation
   * @return identifier
   */
  public String getResponseMessageName() {
    return responseMessageName;
  }

	/**
	 * Retrieve the timeout for socket invocations
	 * @return int
	 */
	public int getSocketTimeOut() {
		return timeout;
	}
}