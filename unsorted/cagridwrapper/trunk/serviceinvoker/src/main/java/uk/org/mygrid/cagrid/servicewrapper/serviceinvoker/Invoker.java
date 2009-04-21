package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker;

public interface Invoker {

	public String runJob(InterProScanInput analyticalServiceInput) throws InvokerException ;

	public String checkStatus(String jobID) throws InvokerException;

	public byte[] poll(String jobID) throws InvokerException;
}
