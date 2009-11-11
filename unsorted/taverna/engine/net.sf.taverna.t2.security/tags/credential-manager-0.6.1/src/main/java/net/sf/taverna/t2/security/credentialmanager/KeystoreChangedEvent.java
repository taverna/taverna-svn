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
package net.sf.taverna.t2.security.credentialmanager;

/**
 * An event given to {@link CredentialManager} observers registered using
 * {@link Observable#addObserver(net.sf.taverna.t2.lang.observer.Observer)}
 * to let them know the Keystore or Truststore have been changed.
 * 
 * @author Alex Nenadic
 */
public class KeystoreChangedEvent {
	
	// Whether the change is on the Keystore or the Truststore
	public String keystoreType;
	
	public KeystoreChangedEvent(String keystoreType){
		this.keystoreType = keystoreType;
	}

}
