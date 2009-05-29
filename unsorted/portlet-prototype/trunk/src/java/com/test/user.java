package com.test;
import javax.portlet.GenericPortlet;
import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import java.io.IOException;
import javax.portlet.PortletRequestDispatcher;

/**
 * user Portlet Class
 */
public class user extends GenericPortlet {

    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {

    }
    
    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException {

        String inspect = null;
        inspect = request.getParameter("inspect");
        //set 'workflow' session attributes
        if(inspect!=null){
        ScopeAttributes.setWorkflowApplicationScopes(request);
        }

        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/user_view.jsp");
        dispatcher.include(request, response);
    }
}