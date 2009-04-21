package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.WSInterProScan;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.WSInterProScanServiceLocator;

public class InterProScanInvoker implements Invoker {

	private WSInterProScan interProScan;

	public InterProScanInvoker() throws InvokerException {
		WSInterProScanServiceLocator locator = new WSInterProScanServiceLocator();
		try {
			interProScan = locator.getWSInterProScan();
		} catch (ServiceException e) {
			throw new InvokerException(e);
		}
	}

	public String checkStatus(String jobID) throws InvokerException {
		try {
			return interProScan.checkStatus(jobID);
		} catch (RemoteException e) {
			throw new InvokerException(e);
		}
	}

	public byte[] poll(String jobID) throws InvokerException {
		try {
			return interProScan.poll(jobID, "toolxml");
		} catch (RemoteException e) {
			throw new InvokerException(e);
		}
	}

	public String runJob(InterProScanInput analyticalServiceInput)
			throws InvokerException {
		String jobID;
		try {
			jobID = interProScan.runInterProScan(analyticalServiceInput
					.getParams(), analyticalServiceInput.getContent());
		} catch (RemoteException e) {
			throw new InvokerException(e);
		}
		return jobID;
	}

}
