
package net.sf.taverna.t2.portal;

/**
 * Represents all information we have about a workflow output
 * port (such as name, depth and data value), parsed from the
 * result Baclava file.
 *
 * @author Alex Nenadic
 */


public class WorkflowOutputPort {

    // Output port name
    private String name;

    // Output port depth;
    private int depth;

    // Data value on the output port
    private Object value;

    // Recognised MIME type of the output port
    private String mimeType;

    public WorkflowOutputPort(){
    }

    public WorkflowOutputPort(String name, int depth){
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

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

}

