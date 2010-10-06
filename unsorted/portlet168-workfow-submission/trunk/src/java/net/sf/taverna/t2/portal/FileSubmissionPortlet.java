/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.t2.portal;

import java.io.IOException;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 *
 * @author alex
 */
public class FileSubmissionPortlet extends GenericPortlet{

    private String FILE_SERVLET_URL;

    @Override
    public void init(){
        FILE_SERVLET_URL = getPortletContext().getInitParameter(Constants.FILE_SERVLET_URL);
    }

    @Override
    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException {
        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher;
        request.getPortletSession().setAttribute("FILENAME", "test",
                                     PortletSession.APPLICATION_SCOPE);

        dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/FileSubmission_view.jsp");
        dispatcher.include(request, response);
    }

        @Override
    public void doEdit(RenderRequest request,RenderResponse response) throws PortletException,IOException {
            response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/FileSubmission_view.jsp");
        dispatcher.include(request, response);
    }

    @Override
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException,IOException {

        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/FileSubmission_view.jsp");
        dispatcher.include(request, response);
    }
}
