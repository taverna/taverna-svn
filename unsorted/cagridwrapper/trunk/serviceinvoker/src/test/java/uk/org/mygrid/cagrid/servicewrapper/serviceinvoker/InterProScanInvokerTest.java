package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.Data;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InputParams;


public class InterProScanInvokerTest {

	private Invoker invoker;

	@Before
	public void findInvoker() throws InvokerException {
		invoker = new InterProScanInvoker();		
	}

	@Ignore
	@Test
	public void invokeInterProScan() throws Exception {
		InterProScanInput analyticalServiceInput = new InterProScanInput();
		InputParams inputParams = new InputParams();
		inputParams.setEmail("mannen@soiland-reyes.com");
		inputParams.setAsync(true);
		inputParams.setSeqtype("P");
		analyticalServiceInput.setParams(inputParams);
		
		Data[] content = new Data[1];
		content[0] = new Data();
		content[0].setContent("uniprot:wap_rat");
		content[0].setType("sequence");
		analyticalServiceInput.setContent(content);
		
		String jobID = invoker.runJob(analyticalServiceInput);
		String status = "RUNNING";
		while (status.equals("RUNNING")) {
			Thread.sleep(1500);
			status = invoker.checkStatus(jobID);
			System.out.println(status);
		}
		byte[] poll = invoker.poll(jobID);
		System.out.println(new String(poll));
	}
	
}
