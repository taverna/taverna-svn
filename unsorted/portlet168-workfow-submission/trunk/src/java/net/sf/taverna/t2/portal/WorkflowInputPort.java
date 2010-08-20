
package net.sf.taverna.t2.portal;

/**
 * Represents all information we have about a workflow input
 * (such as name, depth and annotations) port read from the
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

    WorkflowInputPort(String name, int depth){
        this.name = name;
        this.depth = depth;
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
}
