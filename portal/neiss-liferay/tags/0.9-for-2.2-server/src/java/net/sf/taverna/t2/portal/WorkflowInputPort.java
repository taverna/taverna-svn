
package net.sf.taverna.t2.portal;

/**
 * Represents all information we have about a workflow input
 * port (such as name, depth and annotations), read from the
 * workflow file.
 *
 * @author Alex Nenadic
 */


public class WorkflowInputPort {

    // Input port name
    private String name;

    // Input port depth;
    private int depth;

    // Data value on the input port
    private Object value;

    // Input port description, read from the annotations chain
    // stored in the workflow definition file
    private String description;

    // Input port example value, read from the annotations chain
    // stored in the workflow definition file
    private String exampleValue;

    public WorkflowInputPort(){
    }

    public WorkflowInputPort(String name, int depth, String description, String exampleValue){
        this.name = name;
        this.depth = depth;
        this.description = description;
        this.exampleValue = exampleValue;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * @param depth the depth to set
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
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
     * @return the exampleValue
     */
    public String getExampleValue() {
        return exampleValue;
    }

    /**
     * @param exampleValue the exampleValue to set
     */
    public void setExampleValue(String exampleValue) {
        this.exampleValue = exampleValue;
    }
}
