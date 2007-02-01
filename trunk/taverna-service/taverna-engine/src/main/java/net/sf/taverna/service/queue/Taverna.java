//
//
//package uk.org.mygrid.tavernaservice.wsdl;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.Hashtable;
//import java.util.Map;
//import java.util.Properties;
//import java.util.UUID;
//import java.util.Map.Entry;
//
//import net.sf.taverna.raven.repository.Repository;
//import net.sf.taverna.raven.repository.impl.LocalRepository;
//import net.sf.taverna.tools.Bootstrap;
//import net.sf.taverna.utils.MyGridConfiguration;
//
//import org.embl.ebi.escience.baclava.DataThing;
//import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
//import org.embl.ebi.escience.utils.TavernaSPIRegistry;
//import org.jdom.Document;
//import org.jdom.output.Format;
//import org.jdom.output.XMLOutputter;
//
//import uk.org.mygrid.tavernaservice.queue.Job;
//import uk.org.mygrid.tavernaservice.queue.QueueException;
//import uk.org.mygrid.tavernaservice.queue.QueueListener;
//import uk.org.mygrid.tavernaservice.queue.TavernaQueue;
//import uk.org.mygrid.tavernaservice.queue.TavernaQueueListener;
//
//@SuppressWarnings("deprecation")
//public class Taverna {
//	
//	static URL mygridRepository;
//	static URL mavenRepository;
//	static URL mobyRepository;
//	
//	static {
//		try {
//			mavenRepository = new URL("http://www.ibiblio.org/maven2/");
//			mygridRepository = new URL("http://www.mygrid.org.uk/maven/repository/");
//			mobyRepository = new URL("http://mobycentral.icapture.ubc.ca/maven/");
//		} catch (MalformedURLException e1) {
//		}
//		
//		System.out.println("Hei");
//		Bootstrap.findUserDir();
//		Properties properties = Bootstrap.findProperties();
//		Bootstrap.initialiseProfile(properties.getProperty("raven.remoteprofile"));
//		System.out.println("uhu " + properties.getProperty("raven.remoteprofile"));
//		
//		
//		File tmpDir;
//		try {
//			tmpDir = File.createTempFile("taverna", "raven");
//			tmpDir.delete();
//			Repository tempRepository = LocalRepository.getRepository(tmpDir);
//			tmpDir.isDirectory();
//			tempRepository.addRemoteRepository(mygridRepository);
//			tempRepository.addRemoteRepository(mavenRepository);
//			tempRepository.addRemoteRepository(mobyRepository);
//			TavernaSPIRegistry.setRepository(tempRepository);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		MyGridConfiguration.loadMygridProperties();
//	}	
//	
//	Map<String, Job> jobs;	
//	TavernaQueue queue;
//	QueueListener listener;
//	Thread listenerThread;
//	
//	public Taverna() {
//		jobs = new Hashtable<String, Job>();
//		queue = new TavernaQueue();
//		QueueListener listener = new TavernaQueueListener(queue);
//		listenerThread = new Thread(listener);
//		listenerThread.start();
//	}
//	
//	protected void finalize() {
//		listener.stop();
//	}
//	
//	
//	public String runWorkflow(String workflow, String inputs) throws QueueException {				
//		String job_id = UUID.randomUUID().toString();
//		Job job = queue.add(workflow);
//		jobs.put(job_id, job);
//		return job_id;
//	}
//	
//	public String jobStatus(String job_id) {
//		Job job = jobs.get(job_id);	
//		if (job == null) {
//			return "UNKNOWN";
//		}
//		return job.getState().toString();
//	}
//	
//	
//	public String getResultDocument(String job_id) {
//		Job job = jobs.get(job_id);
//		Map<String, DataThing> results = job.getResults();
//		Document doc = DataThingXMLFactory.getDataDocument(results);
//		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
//		String xmlString = xo.outputString(doc);	
//		return xmlString;
//	}
//	
//	/*
//	public ResultBean[] getResults(String job_id) {
//		Job job = jobs.get(job_id);
//		Map<String, DataThing> results = job.getResults();
//		ResultBean[] beans = new ResultBean[results.size()];
//		int i=0;
//		for (Entry<String, DataThing> entry : results.entrySet()) {
//			ResultBean bean = new ResultBean(entry.getKey(), entry.getValue());
//			beans[i++] = bean; 
//		}
//		return beans;
//	}
//	*/
//	
//		
//}
