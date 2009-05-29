package com.test;

import javax.portlet.GenericPortlet;
import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import java.io.*;


/**
 * MyExperiment Portlet Class
 */
public class MyExperiment extends GenericPortlet {

    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {

    }
    
    protected void doView(RenderRequest request, RenderResponse response)throws PortletException, IOException {

        response.setContentType("text/html");        
        String inspect = null;
        inspect = request.getParameter("inspect");
        
        ScopeAttributes.setUserVariables(request);

        //set 'workflow' session attributes
        if(inspect!=null){
        ScopeAttributes.setWorkflowApplicationScopes(request);
        }
        
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/myexp_view.jsp");
        dispatcher.include(request, response);
       
        }
    public void doEdit(RenderRequest request,RenderResponse response) throws PortletException,IOException {

        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/myexp_edit.jsp");
        dispatcher.include(request, response);
        
        
    }
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException,IOException {

        response.setContentType("text/html");        
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WebApplication4_help.jsp");
        dispatcher.include(request, response);
    }
}