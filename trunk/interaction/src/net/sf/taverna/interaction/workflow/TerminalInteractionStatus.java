/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.interaction.workflow;

public interface TerminalInteractionStatus extends InteractionStatus {

    /**
     * Interaction was rejected by the receipient
     */
    public static int REJECTED = 0;
    
    /**
     * Interaction completed successfuly
     */
    public static int COMPLETED = 0;

    /**
     * Interaction failed due to problems with the service
     */
    public static int FAILED = 0;
    
    /**
     * Return a Object corresponding to the result data, if 
     * any, from the interaction service. The exact type of
     * the Object returned will depend on the interaction
     * service implementation, in the default case this
     * is a Map of DataThing objects
     */
    public Object getResultData();
        
    /**
     * Returns one of the constant codes defined in this interface
     */
    public int getStatusCode();
    
}
