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

/**
 * Returned by the InteractionService proxy when an InteractionRequest
 * object is submitted. Allows registration of callbacks to handle
 * submission state changes
 * @author Tom Oinn
 */
public interface InteractionReceipt {
    
    /**
     * Register a new listener, all previous events receieved by
     * this InteractionReceipt will be replayed to the listener
     * before any new ones are processed
     */
    public void addInteractionStateListener(InteractionStateListener listener);

    /**
     * Get the InteractionRequest that generated this InteractionReceipt
     */
    public InteractionRequest getRequest();

    /**
     * Get the InteractionService proxy that is handling this receipt
     */
    public InteractionService getService();

}
