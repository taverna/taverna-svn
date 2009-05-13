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

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.GroupManager;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetailsManager;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 *
 *
 * @author David Withers
 */
public class AddUserController extends SimpleFormController implements InitializingBean {

	private UserDetailsManager userDetailsManager;
	
	private GroupManager groupManager;

	public void afterPropertiesSet() throws Exception {
        Assert.notNull(userDetailsManager, "userDetailsManager required");
		Assert.notNull(groupManager, "groupManager required");
	}

//    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
//    	return new AddUser();
//    }
    
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws ServletException, IOException, Exception {

        AddUser addUser = (AddUser) command;

        GrantedAuthority grantedAuthority = new GrantedAuthorityImpl("ROLE_USER");
        User user = new User(addUser.getUserName().trim(), addUser.getPassword(), true, true, true, true, Collections.singletonList(grantedAuthority));

        try {
            userDetailsManager.createUser(user);
			groupManager.addUserToGroup(user.getUsername(), "all");
       } catch (DataAccessException existingPermission) {
            existingPermission.printStackTrace();
            errors.rejectValue("userName", "err.userAlreadyExists", "Username already exists.");
            return showForm(request, response, errors);
        }

       return new ModelAndView(new RedirectView(getSuccessView()));
	}
    
	public void setUserDetailsManager(UserDetailsManager userDetailsManager) {
		this.userDetailsManager = userDetailsManager;
	}

	public void setGroupManager(GroupManager groupManager) {
		this.groupManager = groupManager;
	}

}
