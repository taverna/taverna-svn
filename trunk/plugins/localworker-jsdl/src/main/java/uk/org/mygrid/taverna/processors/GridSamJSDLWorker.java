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
import org.jdom.Attribute;
import org.jdom.DataConversionException;
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
	
	public static String EXTENSIONS = "extensions";
	public static String JSDLWORKER = "jsdlworker";
	public static String FTPSERVER = "ftpserver";
	public static String MYPROXYSERVER="myproxyserver";
	public static String MYPROXYSERVERDN="myproxyserverdn";
	public static String PROXYPORT = "proxyport";
	public static String PROXYLIFETIME = "proxylifetime";
	
	private String userFtpServer = ftpServer;
	private String userMyproxyServer = myproxyServer;
	private String usermyproxyServerDN = myproxyServerDN;
	private String userProxyPort = proxyPort;
	private String userProxyLifetime = proxyLifetime;
	
	private static Logger logger = Logger.getLogger(GridSamJSDLWorker.class);
	
	public String [] inputNames() {
		return new String [] {"executable", "arguments", "myproxyUsername", "myproxyPhrase", "outputFile"};
	}

	public String [] inputTypes() {
		return new String[] {"'text/plain'","'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'"};
	}
	
	public String [] outputNames() {
		return new String[] {"output", "time"};
	}
	
	public String [] outputTypes() {
		return new String[] {"'text/plain'", "'text/plain'"};
	}
	
	 // Produces JSDL, compatible with  GridSAM-2.0.1 (omii-server3.4.0)WebService.  
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
            jsdl.append("\t\t\t\t\t<URI>"+ userFtpServer+"/"+ outputFile+"</URI>\n");
            jsdl.append("\t\t\t\t</Target>\n");
            jsdl.append("\t\t\t</DataStaging>\n");
            jsdl.append("\t\t</JobDescription>\n");
            jsdl.append("<MyProxy xmlns=\"urn:gridsam:myproxy\">\n");
            jsdl.append("\t<ProxyServer>"+ userMyproxyServer +"</ProxyServer>\n");
            jsdl.append("\t<ProxyServerDN>"+ usermyproxyServerDN +"</ProxyServerDN>\n");
            jsdl.append("\t<ProxyServerPort>" + userProxyPort + "</ProxyServerPort>\n");
            jsdl.append("\t<ProxyServerUserName>" + myproxyUsername + "</ProxyServerUserName>\n");
            jsdl.append("\t<ProxyServerPassPhrase>" + myproxyPhrase + "</ProxyServerPassPhrase>\n");
            jsdl.append("\t<ProxyServerLifetime>" + userProxyLifetime + "</ProxyServerLifetime>\n");
            jsdl.append("</MyProxy>\n");
            jsdl.append("</JobDefinition>");
            jsdl.append("</JobDescription>");
            jsdl.append("</submitJob>");	
            if (logger.isDebugEnabled()){
            	logger.debug("jsdl is:" +jsdl.toString());
            }
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
		Element jsdlworkerxml = xml.getChild("jsdlworker", XScufl.XScuflNS);
		if (jsdlworkerxml == null) {
			return;
		}
		Attribute ftpserverxml = jsdlworkerxml.getAttribute(GridSamJSDLWorker.FTPSERVER, XScufl.XScuflNS);
		if (ftpserverxml == null) return;				
		this.setUserFtpServer(ftpserverxml.getValue());
		Attribute myproxyserverxml = jsdlworkerxml.getAttribute(GridSamJSDLWorker.MYPROXYSERVER, XScufl.XScuflNS);
		if  (myproxyserverxml == null ) return;
		this.setUserMyproxyServer(myproxyserverxml.getValue());
		Attribute myproxyserverdnxml = jsdlworkerxml.getAttribute(GridSamJSDLWorker.MYPROXYSERVERDN,XScufl.XScuflNS);
		if (myproxyserverdnxml ==  null) return;
		this.setUsermyproxyServerDN(myproxyserverdnxml.getValue());
		Attribute proxyportxml = jsdlworkerxml.getAttribute(GridSamJSDLWorker.PROXYPORT, XScufl.XScuflNS);
		if (proxyportxml == null) return;
		this.setUserProxyPort(proxyportxml.getValue());
		Attribute proxylifetimexml = jsdlworkerxml.getAttribute(GridSamJSDLWorker.PROXYLIFETIME, XScufl.XScuflNS);
		if (proxylifetimexml == null) return;
		this.setUserProxyLifetime(proxylifetimexml.getValue());		
	}

	
	public Element provideXML() {
		Element extensions = new Element(GridSamJSDLWorker.EXTENSIONS, XScufl.XScuflNS);
		Element jsdlworker = new Element(GridSamJSDLWorker.JSDLWORKER , XScufl.XScuflNS);
		jsdlworker.setAttribute(GridSamJSDLWorker.FTPSERVER, this.getUserFtpServer(), XScufl.XScuflNS);
		jsdlworker.setAttribute(GridSamJSDLWorker.MYPROXYSERVER, this.getUserMyproxyServer(), XScufl.XScuflNS);
		jsdlworker.setAttribute(GridSamJSDLWorker.MYPROXYSERVERDN, this.getUsermyproxyServerDN(), XScufl.XScuflNS);
		jsdlworker.setAttribute(GridSamJSDLWorker.PROXYPORT, this.getUserProxyPort(), XScufl.XScuflNS);
		jsdlworker.setAttribute(GridSamJSDLWorker.PROXYLIFETIME, this.getUserProxyLifetime(), XScufl.XScuflNS);
		extensions.addContent(jsdlworker);
		return extensions;
	}

	public String getUserFtpServer() {
		return userFtpServer;
	}

	public void setUserFtpServer(String userFtpServer) {
		this.userFtpServer = userFtpServer;
	}

	public String getUserMyproxyServer() {
		return userMyproxyServer;
	}

	public void setUserMyproxyServer(String userMyproxyServer) {
		this.userMyproxyServer = userMyproxyServer;
	}

	public String getUsermyproxyServerDN() {
		return usermyproxyServerDN;
	}

	public void setUsermyproxyServerDN(String usermyproxyServerDN) {
		this.usermyproxyServerDN = usermyproxyServerDN;
	}

	public String getUserProxyPort() {
		return userProxyPort;
	}

	public void setUserProxyPort(String userProxyPort) {
		this.userProxyPort = userProxyPort;
	}

	public String getUserProxyLifetime() {
		return userProxyLifetime;
	}

	public void setUserProxyLifetime(String userProxyLifetime) {
		this.userProxyLifetime = userProxyLifetime;
	}
	
}
