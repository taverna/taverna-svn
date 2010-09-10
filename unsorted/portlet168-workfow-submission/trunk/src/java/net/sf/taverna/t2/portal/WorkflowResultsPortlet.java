/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.t2.portal;

import java.io.IOException;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * Workflow Results Portlet - enables user to view status of
 * workflow submission jobs submitted to the T2 Server and
 * fetch results of finished jobs.
 *
 * @author Alex Nenadic
 */
public class WorkflowResultsPortlet extends GenericPortlet{

    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException {
            response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_view.jsp");
        dispatcher.include(request, response);
    }

    public void doEdit(RenderRequest request,RenderResponse response) throws PortletException,IOException {
            response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_edit.jsp");
        dispatcher.include(request, response);
    }

    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException,IOException {

        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_help.jsp");
        dispatcher.include(request, response);
    }
    
}
