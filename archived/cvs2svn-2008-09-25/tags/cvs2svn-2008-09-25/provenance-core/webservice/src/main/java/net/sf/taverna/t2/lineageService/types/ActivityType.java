package net.sf.taverna.t2.lineageService.types;

public class ActivityType  implements ProvenanceEventType {
    private IterationType[] iteration;

    private java.lang.String id;  // attribute

    public ActivityType() {
    }

    public ActivityType(
           IterationType[] iteration,
           java.lang.String id) {
           this.iteration = iteration;
           this.id = id;
    }


    /**
     * Gets the iteration value for this ActivityType.
     * 
     * @return iteration
     */
    public IterationType[] getIteration() {
        return iteration;
    }


    /**
     * Sets the iteration value for this ActivityType.
     * 
     * @param iteration
     */
    public void setIteration(IterationType[] iteration) {
        this.iteration = iteration;
    }

    public IterationType getIteration(int i) {
        return this.iteration[i];
    }

    public void setIteration(int i, IterationType _value) {
        this.iteration[i] = _value;
    }


    /**
     * Gets the id value for this ActivityType.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this ActivityType.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

}
