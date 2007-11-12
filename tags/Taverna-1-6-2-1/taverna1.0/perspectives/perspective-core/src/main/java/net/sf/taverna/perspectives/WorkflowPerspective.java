package net.sf.taverna.perspectives;

/**
 * A perspective that displays the current workflow, like the Design perspective. 
 * When performing operations such as File->Open, Taverna will make sure the a
 * WorkflowPerspective instance is the current perspective. 
 * 
 * @author Stian Soiland
 *
 */
public interface WorkflowPerspective extends PerspectiveSPI {

}
