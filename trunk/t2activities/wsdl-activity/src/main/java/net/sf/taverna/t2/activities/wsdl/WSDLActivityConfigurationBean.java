package net.sf.taverna.t2.activities.wsdl;

/**
 *
 * @author Stuart Owen
 */
public class WSDLActivityConfigurationBean {
    private String wsdl;
    private String operation;
    
    /** Creates a new instance of WSDLActivityConfigurationBean */
    public WSDLActivityConfigurationBean() {
    }

    public String getWsdl() {
        return wsdl;
    }

    public void setWsdl(String wsdl) {
        this.wsdl = wsdl;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
