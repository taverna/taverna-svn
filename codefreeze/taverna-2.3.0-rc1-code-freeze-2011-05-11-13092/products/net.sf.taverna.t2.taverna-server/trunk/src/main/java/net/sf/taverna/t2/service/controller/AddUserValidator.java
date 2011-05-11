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

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 *
 *
 * @author David Withers
 */
public class AddUserValidator implements Validator {

    private static final int MINIMUM_PASSWORD_LENGTH = 6;
    
    private static final int MINIMUM_USERNAME_LENGTH = 3;
    
    @SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
       return AddUser.class.isAssignableFrom(clazz);
    }
 
    public void validate(Object target, Errors errors) {
       ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "field.required", "User name required");
       ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "field.required", "Password required");
       ValidationUtils.rejectIfEmptyOrWhitespace(errors, "passwordConfirmation", "field.required", "Password confirmation required");
       AddUser user = (AddUser) target;
       if (user.getUserName() != null
    		   && user.getUserName().trim().length() < MINIMUM_USERNAME_LENGTH) {
    	   errors.rejectValue("userName", "field.min.length",
    			   new Object[]{Integer.valueOf(MINIMUM_USERNAME_LENGTH)},
    			   "The user name must be at least [" + MINIMUM_USERNAME_LENGTH + "] characters in length.");
       }
       if (user.getPassword() != null
    		   && user.getPassword().trim().length() < MINIMUM_PASSWORD_LENGTH) {
    	   errors.rejectValue("password", "field.min.length",
    			   new Object[]{Integer.valueOf(MINIMUM_PASSWORD_LENGTH)},
    			   "The password must be at least [" + MINIMUM_PASSWORD_LENGTH + "] characters in length.");
       }
      if (user.getPassword() != null
               && !user.getPassword().equals(user.getPasswordConfirmation())) {
            errors.rejectValue("passwordConfirmation", "field.min.length",
            		"The password confirmation must match the password.");
       }
    }
    
}
