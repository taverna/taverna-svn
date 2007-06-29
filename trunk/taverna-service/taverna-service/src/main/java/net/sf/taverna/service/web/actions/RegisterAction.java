package net.sf.taverna.service.web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.UserDAO;
import net.sf.taverna.service.web.forms.RegisterForm;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class RegisterAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		RegisterForm regForm = (RegisterForm)form;
		
		UserDAO userDao=DAOFactory.getFactory().getUserDAO();
		
		User user = new User(regForm.getName());
		user.setAdmin(false);
		user.setPassword(regForm.getPassword());
		user.setEmail(regForm.getEmail());
		userDao.create(user);
		DAOFactory.getFactory().commit();
		DAOFactory.getFactory().close();
		return mapping.findForward("success");
	}

	
}
