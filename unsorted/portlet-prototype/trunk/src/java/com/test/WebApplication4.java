package com.test;

import javax.portlet.*;
import java.io.*;


/**
 * WebApplication4 Portlet Class
 */
public class WebApplication4 extends GenericPortlet {

    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {
    }
    
    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException {
        response.setContentType("text/html");

        

        PrintWriter out = response.getWriter();
        PortletSession session = request.getPortletSession();

        //get attributes for chosen workflow
        String wfTitle =
        (String)session.getAttribute("title",PortletSession.APPLICATION_SCOPE);
        String wfDesc =
        (String)session.getAttribute("desc",PortletSession.APPLICATION_SCOPE);
        String wfURL =
        (String)session.getAttribute("URL",PortletSession.APPLICATION_SCOPE);
        String wfTags =
        (String)session.getAttribute("tags",PortletSession.APPLICATION_SCOPE);
        String wfUpl =
        (String)session.getAttribute("upl",PortletSession.APPLICATION_SCOPE);
        String wfProcs =
        (String)session.getAttribute("procs",PortletSession.APPLICATION_SCOPE);
        String wfThumb =
        (String)session.getAttribute("thumb",PortletSession.APPLICATION_SCOPE);

        if (wfTitle != null) {
            out.print("<table border=\"6\" bordercolor=\"white\" cellpadding=\"5\" cellspacing=\"6\" >");
            out.print("<tr>");
            out.print("<td align=\"right\" valign=\"top\">");
            out.print("<b>Loaded workflow</b>  ");
            out.print("</td>");
            out.print("<td valign=\"top\">");
            out.print(wfTitle);
            out.print("</td>");
            out.print("<td align=\"center\" rowspan=\"5\">");
                out.print("<table border=\"1\" bordercolor=\"#E3E3E6\" align=\"center\" cellpadding=\"30px\" cellspacing=\"6\"");
                out.print("<tr>");
                out.print("<td align=\"center\">");
                out.print("<img src=\""+wfThumb+"\"/><br>");
                out.print("<a href=\""+wfURL+"\"><input type=\"button\" value=\"Download\"/></a>");
                out.print("</td>");
                out.print("</tr>");
                out.print("</table>");
            out.print("</td>");
            out.print("</tr>");

            out.print("<tr>");
            out.print("<td align=\"right\" valign=\"top\">");
            out.print("<b>Description</b>");
            out.print("</td>");
            out.print("<td valign=\"top\">");
            out.print(wfDesc);
            out.print("</td>");
            out.print("</tr>");

            out.print("<tr>");
            out.print("<td align=\"right\" valign=\"top\">");
            out.print("<b>Uploader</b>");
            out.print("</td>");
            out.print("<td valign=\"top\">");
            out.print(wfUpl);
            out.print("</td>");
            out.print("</tr>");

            out.print("<tr>");
            out.print("<td align=\"right\" valign=\"top\">");
            out.print("<b>Tags</b>");
            out.print("</td>");
            out.print("<td valign=\"top\">");
            out.print(wfTags);            
            out.print("</td>");
            out.print("</tr>");

            out.print("<tr>");
            out.print("<td align=\"right\" valign=\"top\">");
            out.print("<b>Processors</b>");
            out.print("</td>");
            out.print("<td valign=\"top\">");
            out.print(wfProcs);
            out.print("</td>");
            out.print("</tr>");
            out.print("<tr>");
            out.print("<td colspan=\"3\">");
            out.print("<hr>");
            out.print("</td>");
            out.print("</tr>");


            out.print("</table>");
        }

        else{
            out.print("No workflow loaded into portlet");
        }       

        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WebApplication4_view.jsp");
        dispatcher.include(request, response);
        

        
    }
    public void doEdit(RenderRequest request,RenderResponse response) throws PortletException,IOException {
        response.setContentType("text/html");       

        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WebApplication4_edit.jsp");
        dispatcher.include(request, response);


    }
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException,IOException {

        response.setContentType("text/html");        
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WebApplication4_help.jsp");
        dispatcher.include(request, response);
    }

}