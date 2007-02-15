package net.sf.taverna.service.interfaces;

import java.io.IOException;

public interface TavernaService {

	public static final String NS = "http://service.taverna.sf.net/";	
	
	public String jobs();

	public String runWorkflowFile(String filename, String inputDoc)
		throws IOException, QueueException;

	public String runWorkflow(String scufl, String inputDoc)
		throws IOException, QueueException;

	public String jobStatus(String job_id);

	public String getResultDocument(String job_id) throws UnknownJobException;

	public String getProgressReport(String job_id) throws UnknownJobException;

	public String getWorkflow(String job_id) throws UnknownJobException;

	public String getInputs(String job_id) throws UnknownJobException;

}