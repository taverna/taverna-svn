package com.test;

import javax.portlet.GenericPortlet;
import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import java.io.*;

/**
 * SessionManager Portlet Class
 */
public class SessionManager extends GenericPortlet {

    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {

    }
    
    protected void doView(RenderRequest request, RenderResponse response)throws PortletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        PortletSession session = request.getPortletSession();

        String wfTitle = (String)session.getAttribute("title",PortletSession.APPLICATION_SCOPE);
        String wfImage =
        (String)session.getAttribute("prev",PortletSession.APPLICATION_SCOPE);

        if(wfImage != null){
        out.print("<table align=\"center\">");
        out.print("<tr>");
        out.print("<td align=\"center\">");
        out.print("<img src=\""+wfImage+"\"/>");
        out.print("</td>");
        out.print("</tr>");
        out.print("<tr>");
        out.print("<td align=\"center\">");
        out.print("<b>"+wfTitle+"</b>");
        out.print("</td>");
        out.print("</tr>");
        out.print("</table>");
        }

        else{
            out.print("No workflow currently loaded");
        }

        }
}