package net.sourceforge.taverna.io;



public class TransmitterException extends Exception {
    protected String detailMsg = null;

    public TransmitterException() {
        super();
    }

    public TransmitterException(String msg) {
        super(msg);
    }
    
    public TransmitterException(Throwable th){
        super(th);
    }

    public void setDetailMsg(String msg) {
        this.detailMsg = msg;
    }
    
    

    public String getDetailMsg() {
        return (detailMsg == null) ? "" : detailMsg;
    }
}
