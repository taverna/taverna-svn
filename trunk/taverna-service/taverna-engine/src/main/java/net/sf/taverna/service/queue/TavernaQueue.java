package net.sf.taverna.service.queue;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.taverna.service.interfaces.QueueException;
import net.sf.taverna.service.queue.Job.State;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


public class TavernaQueue {
	
	private static Logger logger = Logger.getLogger(TavernaQueue.class);
	
	Queue<Job> queue;
	
	public TavernaQueue() {
		queue = new LinkedBlockingQueue<Job>();
	}
	
	public synchronized int size() {		
		return queue.size();
	}
	
	public Job add(String workflow, String inputDoc) throws QueueException {
		ScuflModel model = new ScuflModel();
		try {
			XScuflParser.populate(workflow, model, null);
		} catch (ScuflException ex) {
			logger.warn("Could not load workflow:\n" + workflow, ex);
			throw new QueueException("Could not load workflow", ex);
		}
		Map<String, DataThing> inputs;
		if (inputDoc.equals("")) {
			inputs = new HashMap<String, DataThing>();
		} else {
			try {
				inputs = parseDataDoc(inputDoc);
			} catch (JDOMException e) {
				throw new QueueException("Could not parse input document:\n"+ inputDoc, e);
			}
		}
		
		Job job = new Job(model, inputs);
	
		synchronized(this) {
			if (! queue.offer(job)) {
				throw new QueueException("Can't add to queue");
			}
			job.setState(State.QUEUED);
			this.notifyAll();
			return job;	
		}
	}	

	public synchronized Job peek() {		
		return queue.peek();		
	}

	public synchronized Job poll() {	
		Job job = queue.poll();
		if (job != null) {
			job.setState(State.DEQUEUED);
			this.notifyAll();
		}
		return job;
	}

	public static Map<String, DataThing> parseDataDoc(String xml) throws JDOMException {
		SAXBuilder builder = new SAXBuilder();		
		Document doc;
		try {
			doc = builder.build(new StringReader(xml));
		} catch (IOException e) {
			logger.error("Could not read inputDoc with StringReader", e);
			throw new RuntimeException(e);
		}
		return DataThingXMLFactory.parseDataDocument(doc);				
	}
			
}
