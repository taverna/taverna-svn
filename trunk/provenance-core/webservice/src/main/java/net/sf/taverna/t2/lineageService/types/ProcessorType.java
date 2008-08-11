package net.sf.taverna.t2.lineageService.types;

public class ProcessorType  implements ProvenanceEventType {
    private ActivityType[] activity;

    private java.lang.String id;  // attribute

    public ProcessorType() {
    }

    public ProcessorType(
           ActivityType[] activity,
           java.lang.String id) {
           this.activity = activity;
           this.id = id;
    }


    /**
     * Gets the activity value for this ProcessorType.
     * 
     * @return activity
     */
    public ActivityType[] getActivity() {
        return activity;
    }


    /**
     * Sets the activity value for this ProcessorType.
     * 
     * @param activity
     */
    public void setActivity(ActivityType[] activity) {
        this.activity = activity;
    }

    public ActivityType getActivity(int i) {
        return this.activity[i];
    }

    public void setActivity(int i, ActivityType _value) {
        this.activity[i] = _value;
    }


    /**
     * Gets the id value for this ProcessorType.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this ProcessorType.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

}
