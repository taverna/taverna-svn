package net.sf.taverna.service.interfaces;


public interface TavernaService {

	public static final String NS = "http://taverna.sf.net/service";	

	
	// Mime types
	
	public static final String restType = "application/vnd.taverna.rest+xml";

	public static final String scuflType = "application/vnd.taverna.scufl+xml";

	public static final String baclavaType = "application/vnd.taverna.baclava+xml";
	
	
	public void register(String username, String password, String email);
	
	public void changePassword(String username, String password, String newPassword);

	
	public String putWorkflow(String username, String password, String scufl);
	
	public String getWorkflow(String username, String password, String id);
	
	
	public String putDataDoc(String username, String password, String baclava);

	public String getDataDoc(String username, String password, String id);

	
	public String addJob(String username, String password, String scuflId, String baclavaId);

	public String getJobs(String username, String password);

	public String getJob(String username, String password, String id);

	
	public String getJobStatus(String username, String password, String job_id);

	
	public String getResultDocument(String username, String password, String job_id) throws UnknownJobException;
	
	public String getProgressReport(String username, String password, String job_id) throws UnknownJobException;

	
	
	public void registerWorker(String username, String password, String workerURL);

	public void unregisterWorker(String username, String password, String workerURL);

	
	public String getQueues(String username, String password, String workerURL);
	
	public String getQueue(String username, String password, String id);
	
	
	

}