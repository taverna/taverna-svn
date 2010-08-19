package net.sf.taverna.t2.portal;
import java.io.FilenameFilter;
import java.io.File;
import javax.portlet.GenericPortlet;
import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.portlet.PortletRequestDispatcher;

/**
 * WorkflowSubmission Portlet Class
 */
public class WorkflowSubmissionPortlet extends GenericPortlet {

    private static final String WORKFLOWS_DIRECTORY = "/WEB-INF/workflows";
    private static final String WORKFLOW_FILE_NAMES = "workflow_file_names";
    private static final String WORKFLOW_SELECTION_SUBMISSION = "workflow_selection";
    private static final String SELECTED_WORKFLOW = "selected_workflow";
    private static final String RUN_WORKFLOW = "run_workflow";
    private static final String WORKFLOW_NAME = "workflow_name";

    // List of workflow file names. Workflow files are located in /WEB-INF/workflows folder in the app root.
    public static ArrayList<String> workflowFileNamesList;

    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {
        System.out.println("Workflow Submission Portlet: procesing action");
        System.out.println();

        Enumeration names = request.getParameterNames();
        while(names.hasMoreElements()){
            String parameterName = (String) names.nextElement();
            System.out.println("Workflow Submission Portlet: parameter name: " + parameterName);
            System.out.println("Workflow Submission Portlet:: parameter value: " + request.getParameter(parameterName));
            System.out.println();
        }
        System.out.println();

        // Pass the input parameters over to the doView() and other render stage methods
        response.setRenderParameters(request.getParameterMap());
    }

    // Load the workflows once at initialisation time
    @Override
    public void init(){
        // Load the workflows
        workflowFileNamesList = new ArrayList<String>();

        // Directory containing workflows
        File dir = new File(getPortletContext().getRealPath(WORKFLOWS_DIRECTORY));
       
        // Filter only workflows i.e. files of type .t2flow
        FilenameFilter t2flowFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".t2flow");
            }
        };

        String[] workflowFiles = dir.list(t2flowFilter);

        for (int i=0; i<workflowFiles.length; i++)
        { // Get the workflow filename (without the extension)
            String workflowFileName = workflowFiles[i].substring(0, workflowFiles[i].lastIndexOf('.'));
            workflowFileNamesList.add(workflowFileName);
        }
    }

    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException {
        response.setContentType("text/html");

        // Set the workflow file names list in the PortletSession if not already set
        if (request.getPortletSession().getAttribute(WORKFLOW_FILE_NAMES) == null){
            request.getPortletSession().setAttribute(WORKFLOW_FILE_NAMES, workflowFileNamesList);
        }

        // If a workflow has been selected - then also print the
        // input form for the selected workflow
        PortletRequestDispatcher dispatcher;
        dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowSubmission_view.jsp");
        dispatcher.include(request, response);
        if (request.getParameter(WORKFLOW_SELECTION_SUBMISSION) != null){
            response.getWriter().println("<br />");
            response.getWriter().println("<hr />");
            response.getWriter().println("<br />");
            String selectedWorkflowFileName = request.getParameter(SELECTED_WORKFLOW);
            // If we have a corresponding JSP file containing workflow's input form - dispatch to it
            File selectedWorkflowJSPFile = new File(getPortletContext().getRealPath(WORKFLOWS_DIRECTORY + "/" + selectedWorkflowFileName + ".jsp"));
            if (selectedWorkflowJSPFile.exists()){
                dispatcher = getPortletContext().getRequestDispatcher(WORKFLOWS_DIRECTORY + "/" + selectedWorkflowFileName + ".jsp");
                dispatcher.include(request, response);
            }
            else{ // We have to parse the workflow file and figure out the inputs ourselves

            }

        }
    }
    public void doEdit(RenderRequest request,RenderResponse response) throws PortletException,IOException {
            response.setContentType("text/html");        
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowSubmission_edit.jsp");
        dispatcher.include(request, response);
    }
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException,IOException {

        response.setContentType("text/html");        
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowSubmission_help.jsp");
        dispatcher.include(request, response);
    }
}