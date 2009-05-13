/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.service.controller;

/**
 *
 *
 * @author David Withers
 */
public class AddUser {
	
	private String userName, password, passwordConfirmation;

	/**
	 * Returns the userName.
	 *
	 * @return the value of userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the userName.
	 *
	 * @param user the new value for userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Returns the password.
	 *
	 * @return the value of password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 *
	 * @param password the new value for password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Returns the passwordConfirmation.
	 *
	 * @return the value of passwordConfirmation
	 */
	public String getPasswordConfirmation() {
		return passwordConfirmation;
	}

	/**
	 * Sets the passwordConfirmation.
	 *
	 * @param passwordConfirmation the new value for passwordConfirmation
	 */
	public void setPasswordConfirmation(String passwordConfirmation) {
		this.passwordConfirmation = passwordConfirmation;
	}

}
