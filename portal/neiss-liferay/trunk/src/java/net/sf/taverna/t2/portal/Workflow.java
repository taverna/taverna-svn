package net.sf.taverna.t2.portal;

import java.util.ArrayList;
import org.jdom.Document;

/**
 * Represents all information we have about a workflow input
 * port (such as name, depth and annotations), read from the
 * workflow file.
 *
 * @author Alex Nenadic
 */
public class Workflow {

    // Workflow author read from annotations.
    private String author;

    // Workflow title read from annotations.
    private String title;

    // Workflow description read from annotations.
    private String description;

    // Workflow file name (with no extension) for local workflows
    private String fileName;

    // Is this a workflow from myExperiment?
    private boolean isMyExperimentWorkflow;

    // Resource URI for workflows from myExperiment (contains the wf id on myExperiment)
    private String myExperimentResource;

    // Version of the workflows on myExperiment (there can be multiple versions of the same wf)
    private int myExperimentWorkflowVersion;

    // XML document parsed out of the workflow stream
    private Document workflowDocument;

    // List of workflow input ports (with optional data on them)
    private ArrayList<WorkflowInputPort> workflowInputPorts;

    public Workflow(){
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the isMyExperimentWorkflow
     */
    public boolean isMyExperimentWorkflow() {
        return isMyExperimentWorkflow;
    }

    /**
     * @param isMyExperimentWorkflow the isMyExperimentWorkflow to set
     */
    public void setIsMyExperimentWorkflow(boolean isMyExperimentWorkflow) {
        this.isMyExperimentWorkflow = isMyExperimentWorkflow;
    }

    /**
     * @return the resource
     */
    public String getMyExperimentResource() {
        return myExperimentResource;
    }

    /**
     * @param resource the resource to set
     */
    public void setMyExperimentResource(String resource) {
        this.myExperimentResource = resource;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the workflowDocument
     */
    public Document getWorkflowDocument() {
        return workflowDocument;
    }

    /**
     * @param workflowDocument the workflowDocument to set
     */
    public void setWorkflowDocument(Document workflowDocument) {
        this.workflowDocument = workflowDocument;
    }

    /**
     * @return the workflowInputPorts
     */
    public ArrayList<WorkflowInputPort> getWorkflowInputPorts() {
        return workflowInputPorts;
    }

    /**
     * @param workflowInputPorts the workflowInputPorts to set
     */
    public void setWorkflowInputPorts(ArrayList<WorkflowInputPort> workflowInputPorts) {
        this.workflowInputPorts = workflowInputPorts;
    }

    /**
     * @return the myExperimentWorkflowVersion
     */
    public int getMyExperimentWorkflowVersion() {
        return myExperimentWorkflowVersion;
    }

    /**
     * @param myExperimentWorkflowVersion the myExperimentWorkflowVersion to set
     */
    public void setMyExperimentWorkflowVersion(int myExperimentWorkflowVersion) {
        this.myExperimentWorkflowVersion = myExperimentWorkflowVersion;
    }
}
