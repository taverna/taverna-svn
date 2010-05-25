package org.taverna.server.master;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

import org.taverna.server.master.interfaces.TavernaRun;

@XmlRootElement(name = "ServerDescription", namespace = "http://taverna.org/server/v2")
public class ServerDescription extends DescriptionElement {
	public List<Uri> runs;
	public Uri runLimit, permittedWorkflows, permittedListeners;
	//Uri database;
	public ServerDescription(){}
	ServerDescription(Map<String,TavernaRun> ws, UriInfo ui) {
		runs = new ArrayList<Uri>(ws.size());
		for (Map.Entry<String, TavernaRun> w: ws.entrySet()) {
			runs.add(new Uri(ui, "runs/{uuid}", w.getKey()));
		}
		runLimit = new Uri(ui, "policy/runLimit");
		permittedWorkflows = new Uri(ui, "policy/permittedWorkflows");
		permittedListeners = new Uri(ui, "policy/permittedListenerTypes");
		//database = new Uri(ui, "database");
	}
}
