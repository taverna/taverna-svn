package uk.ac.manchester.cs.img.esc;

import java.io.Serializable;
import java.net.URI;

/**
 * Example activity configuration bean.
 * 
 */
public class EscActivityConfigurationBean implements Serializable {

	private String name;
	private String id;
	private String url;
	
	private boolean produceReport = false;
	private boolean produceWorkflow = false;
	private boolean debug = true;
	
	private int pollingInterval = 1;
	
	public int getPollingInterval() {
		return pollingInterval;
	}
	public void setPollingInterval(int pollingInterval) {
		this.pollingInterval = pollingInterval;
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public boolean isProduceReport() {
		return produceReport;
	}
	public void setProduceReport(boolean produceReport) {
		this.produceReport = produceReport;
	}
	public boolean isProduceWorkflow() {
		return produceWorkflow;
	}
	public void setProduceWorkflow(boolean produceWorkflow) {
		this.produceWorkflow = produceWorkflow;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

}
