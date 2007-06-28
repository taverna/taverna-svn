package net.sf.taverna.service.web.forms;

import javax.servlet.http.HttpServletRequest;

import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.UserDAO;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.validator.ValidatorForm;

public class RegisterForm extends ValidatorForm {

	private String name;
	private String password;
	private String email;
	private String confirm;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getConfirm() {
		return confirm;
	}
	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}
	
	@Override
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = super.validate(mapping, request);
		UserDAO userDao = DAOFactory.getFactory().getUserDAO();
		if (errors.size()==0) {
			if (userDao.readByUsername(getName())!=null) {
				errors.add("duplicatename",new ActionMessage("error.duplicate_username",getName()));
			}
		}
		return errors;
	}	
}
