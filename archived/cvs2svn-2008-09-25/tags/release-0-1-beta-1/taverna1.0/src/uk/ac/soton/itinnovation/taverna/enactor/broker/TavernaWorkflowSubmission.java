////////////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2002
//
// Copyright in this library belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP, UK.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1
// of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation Inc, 59 Temple Place, Suite 330, Boston MA 02111-1307 USA.
//
//      Created By          :   Darren Marvin
//      Created Date        :   2002/08/07
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: dmarvin $
//                              $Date: 2003-05-01 12:11:44 $
//                              $Revision: 1.5 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.taverna.enactor.broker;

public class TavernaWorkflowSubmission {

    private String scuflSpec;
    private String inputData;
    private String userID;
    private String userNamespaceCxt;
    private String notificationEmailAddress;

    /**
     * Constructor used when no email notification is necessary.
     * @param xscuflSpec
     * @param inputData
     * @param userID
     * @param userNamespaceCxt
     */
    public TavernaWorkflowSubmission(String scuflSpec,
        String inputData,
        String userID,
        String userNamespaceCxt) {
        this.scuflSpec = scuflSpec;
        this.inputData = inputData;
        this.userID = userID;
        this.userNamespaceCxt = userNamespaceCxt;
    }

    /**
     * Retrieve the workflowSpec for the submission
     * @return workflow spec
     */
    public String getXScuflDefinition() {
        return scuflSpec;
    }

    /**
     * Retrieve the input data for the submission
     * @return input data
     */
    public String getInputData() {
        return inputData;
    }

    /**
     * Retrieve the user identifier
     * @return user identifier
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Retrieve the context identifier for this user
     * @return context identifier
     */
    public String getUserNamespaceCxt() {
        return userNamespaceCxt;
    }    
}

