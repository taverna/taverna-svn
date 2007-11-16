package uk.org.mygrid.taverna.processors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.embl.ebi.escience.scuflworkers.java.LocalWorkerWithPorts;
import org.embl.ebi.escience.scuflworkers.java.XMLExtensible;
import org.jdom.Element;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Returns the JSDL string wrapping the given arguments.
 * 
 * @author Bharathi Kattamuri
 */
public class GridSamJSDLWorker implements  LocalWorker, XMLExtensible
{
	
	public static String ftpServer = "ftp://rpc326.cs.man.ac.uk:19245";
	public static String myproxyServer="myproxy.grid-support.ac.uk";
	public static String myproxyServerDN="/C=UK/O=eScience/OU=CLRC/L=DL/CN=host/myproxy.grid-support.ac.uk/E=a.j.richards@dl.ac.uk";
	public static String proxyPort="7512";
	public static String proxyLifetime="7512";		
	
	private static Logger logger = Logger.getLogger(GridSamJSDLWorker.class);

	public String [] inputNames() {
		return new String [] {"executable", "arguments", "myproxyUsername", "myproxyPhrase", "outputFile"};
	}

	public String [] inputTypes() {
		return new String[] {"'text/plain'","'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'"  };
	}
	
	public String [] outputNames() {
		return new String[] {"output", "time"};
	}
	
	public String [] outputTypes() {
		return new String[] {"'text/plain'", "'text/plain'"};
	}
	
	 // JSDL valid to invoke GridSAM-2.0.1 (omii-server3.4.0)WebService.  
	public Map<String, DataThing> execute(Map inputs) throws TaskExecutionException {
		
		try {
			String executable = (String) ((DataThing) inputs.get("executable")).getDataObject();
			String arguments = (String) ((DataThing) inputs.get("arguments")).getDataObject();
			String myproxyUsername = (String) ((DataThing) inputs.get("myproxyUsername")).getDataObject();
			String myproxyPhrase = (String) ((DataThing) inputs.get("myproxyPhrase")).getDataObject();
			String outputFile = (String) ((DataThing) inputs.get("outputFile")).getDataObject();
					
			String timeStr = String.valueOf(System.currentTimeMillis());
			String interOutputfile = myproxyUsername + timeStr;
			
			StringBuffer jsdl=new StringBuffer("<submitJob xmlns=\"http://www.icenigrid.org/service/gridsam\">\n");
			jsdl.append("<JobDescription>");
			jsdl.append("\t<JobDefinition xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl\">\n");
			jsdl.append("\t\t<JobDescription>\n");
			jsdl.append("\t\t\t<Application>\n");
            jsdl.append("\t\t\t\t<POSIXApplication xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl-posix\">\n");
			jsdl.append("\t\t\t\t\t<Executable>"+ executable +"</Executable>\n");
			jsdl.append("\t\t\t\t\t<Argument>"+ arguments + "</Argument>\n");
            jsdl.append("\t\t\t\t\t<Output>"+ interOutputfile +"</Output>\n");
            jsdl.append("\t\t\t\t</POSIXApplication>\n");
            jsdl.append("\t\t\t</Application>\n");
            jsdl.append("\t\t\t<DataStaging>\n");
            jsdl.append("\t\t\t\t<FileName>"+interOutputfile+"</FileName>\n");
            jsdl.append("\t\t\t\t<CreationFlag>overwrite</CreationFlag>\n");
            jsdl.append("\t\t\t\t<Target>\n");
            jsdl.append("\t\t\t\t\t<URI>"+ ftpServer+"/"+ outputFile+"</URI>\n");
            jsdl.append("\t\t\t\t</Target>\n");
            jsdl.append("\t\t\t</DataStaging>\n");
            jsdl.append("\t\t</JobDescription>\n");
            jsdl.append("<MyProxy xmlns=\"urn:gridsam:myproxy\">\n");
            jsdl.append("\t<ProxyServer>"+ myproxyServer +"</ProxyServer>\n");
            jsdl.append("\t<ProxyServerDN>"+ myproxyServerDN +"</ProxyServerDN>\n");
            jsdl.append("\t<ProxyServerPort>" + proxyPort + "</ProxyServerPort>\n");
            jsdl.append("\t<ProxyServerUserName>" + myproxyUsername + "</ProxyServerUserName>\n");
            jsdl.append("\t<ProxyServerPassPhrase>" + myproxyPhrase + "</ProxyServerPassPhrase>\n");
            jsdl.append("\t<ProxyServerLifetime>" + proxyLifetime + "</ProxyServerLifetime>\n");
            jsdl.append("</MyProxy>\n");
            jsdl.append("</JobDefinition>");
            jsdl.append("</JobDescription>");
            jsdl.append("</submitJob>");	
		  
			Map outputs = new HashMap();
			outputs.put("output", new DataThing(jsdl.toString()));
			outputs.put("time", new DataThing(timeStr));
			return outputs;
		}
		catch(Exception e) {
			logger.error("Error in execute!",e);
			throw new TaskExecutionException(e);
		}
		
	}

	public void consumeXML(Element xml) {
		// TODO Auto-generated method stub
		
	}

	public Element provideXML() {
		Element extensions = new Element("extensions", XScufl.XScuflNS);
		Element flattenList = new Element("ftpdserver", XScufl.XScuflNS);
		flattenList.setAttribute("server", "ftp://rpc326.cs.man.ac.uk:19245",
			XScufl.XScuflNS);
		extensions.addContent(flattenList);
		return extensions;
	}

	
}
