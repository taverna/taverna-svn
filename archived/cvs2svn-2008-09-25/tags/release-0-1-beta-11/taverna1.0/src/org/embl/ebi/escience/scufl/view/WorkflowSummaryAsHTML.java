package org.embl.ebi.escience.scufl.view;

import java.net.*;
import java.util.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor;
import org.embl.ebi.escience.scuflworkers.biomoby.BiomobyProcessor;
import org.embl.ebi.escience.scuflworkers.seqhound.SeqhoundProcessor;

/**
 * A utility class containing a single static method which
 * summarizes the specified workflow into a standard HTML
 * document and returns it as a string.
 * @author Tom Oinn
 */
public class WorkflowSummaryAsHTML {
    
    public static String getSummary(ScuflModel model) {
	Processor[] processors = model.getProcessors();
	Map resources = new HashMap();
	for (int i = 0; i<processors.length; i++) {
	    String resourceHost = processors[i].getResourceHost();
	    if (resourceHost != Processor.ENACTOR) {
		if (resources.containsKey(resourceHost) == false) {
		    resources.put(resourceHost, new ArrayList());
		}
		java.util.List processorForResource = (java.util.List)resources.get(resourceHost);
		processorForResource.add(processors[i]);
	    }
	}
	StringBuffer sb = new StringBuffer();
	sb.append("<html><head><STYLE TYPE=\"text/css\">");
	sb.append("body {\n");
	sb.append("  background-color: #eeeeee;\n");
	sb.append("font-family: Helvetica, Arial, sans-serif;\n");
	sb.append("font-size: 12pt;\n");
	sb.append("}\n");
	sb.append("blockquote {\n");
	sb.append("  padding: 5px;\n");
	sb.append("  background-color: #ffffff;\n");
	sb.append("  border-width: 1px; border-style: solid; border-color: #aaaaaa;\n");
	sb.append("}\n");
	sb.append("</STYLE></head><body>");
	sb.append("<h2>Workflow information</h2>");
	
	String author = model.getDescription().getAuthor();
	author = (author.equals("")?"<font color=\"red\">no author</font>":"<font color=\"green\">"+author+"</font>");
	
	String title = model.getDescription().getTitle();
	title = (title.equals("")?"<font color=\"red\">no title</font>":"<font color=\"green\">"+title+"</font>");
	
	String lsid = model.getDescription().getLSID();
	lsid = (lsid.equals("")?"<font color=\"red\">no lsid</font>":"<font color=\"green\">"+lsid+"</font>");
	
	
	sb.append("This report applies to the workflow titled '"+title+"' authored by '"+author+"' and with LSID '"+lsid+"'. The textual description, if any is shown below :<blockquote><em>"+model.getDescription().getText()+"</em></blockquote>");
	sb.append("<h2>Resource usage report</h2>");
	sb.append("This display shows the various external resources used by the current workflow. It does not show resources such as local operations or string constants which are run within the enactment engine. Services are categorized by resource host and type, and the name of the instance of each service shown to the right.");
	sb.append("<table border=\"1\" bgcolor=\"white\">");
	for (Iterator i = resources.keySet().iterator(); i.hasNext();) {
	    String hostName = (String)i.next();
	    java.util.List usageList = (java.util.List)resources.get(hostName);
	    sb.append("<tr><td valign=\"top\" bgcolor=\"#bcd2ee\"colspan=\"3\">Resources on   <code><b>"+hostName+"</b></code>, "+usageList.size()+" instance"+(usageList.size()!=1?"s":"")+".</td></tr>\n");
	    // Do all web service installations within this host
	    Set temp = new HashSet();
	    for (Iterator j = usageList.iterator(); j.hasNext(); ) {
		Object o = j.next();
		if (o instanceof WSDLBasedProcessor) {
		    temp.add(o);
		}
	    }
	    WSDLBasedProcessor[] wp = (WSDLBasedProcessor[])temp.toArray(new WSDLBasedProcessor[0]);
	    Map wsLocations = new HashMap();
	    for (int j = 0; j < wp.length; j++) {
		String wsdlLocation = "";
		try {
		    URL wsdlURL = new URL(wp[j].getWSDLLocation());
		    wsdlLocation = wsdlURL.getFile();
		}
		catch (MalformedURLException mue) {
		    //
		}
		if (wsLocations.containsKey(wsdlLocation)==false) {
		    wsLocations.put(wsdlLocation, new HashMap());
		}
		Map operationToProcessorName = (Map)wsLocations.get(wsdlLocation);
		String operationName = wp[j].getOperationName();
		if (operationToProcessorName.containsKey(operationName)==false) {
		    operationToProcessorName.put(operationName, new HashSet());
		}
		Set processorNames = (Set)operationToProcessorName.get(operationName);
		processorNames.add(wp[j].getName());
	    }
	    for (Iterator j = wsLocations.keySet().iterator(); j.hasNext();) {
		// Top level iterator over all service locations.
		String location = (String)j.next();
		Map operationToProcessorName = (Map)wsLocations.get(location);
		int rows = 2+operationToProcessorName.size();
		sb.append("<tr>");
		sb.append("<td width=\"80\" valign=\"top\" rowspan=\""+rows+"\" bgcolor=\"#a3cd5a\">Web&nbsp;service</td>");
		sb.append("<td colspan=\"2\" bgcolor=\"#a3cd5a\">WSDL Defined at <em>"+location+"</em></td>");
		sb.append("</tr>");
		sb.append("<tr><td bgcolor=\"#eeeedd\">Operation name</td><td bgcolor=\"#eeeedd\">Processors</td></tr>");
		for (Iterator k = operationToProcessorName.keySet().iterator(); k.hasNext();) {
		    String operationName = (String)k.next();
		    Set processorNames = (Set)operationToProcessorName.get(operationName);
		    sb.append("<tr>");
		    sb.append("<td><font color=\"purple\">"+operationName+"</font></td>");
		    sb.append("<td>");
		    for (Iterator l = processorNames.iterator(); l.hasNext();) {
			sb.append((String)l.next());
			if (l.hasNext()) {
			    sb.append(", ");
			}
		    }
		    sb.append("</td></tr>");
		}
	    }
	    
	    // Do all soaplab installations within this host
	    temp = new HashSet();
	    for  (Iterator j = usageList.iterator(); j.hasNext(); ) {
		Object o = j.next();
		if (o instanceof SoaplabProcessor) {
		    temp.add(o);
		}
	    }
	    SoaplabProcessor sp[] = (SoaplabProcessor[])temp.toArray(new SoaplabProcessor[0]);
	    Map soaplabLocations = new HashMap();
	    for (int j = 0; j < sp.length; j++) {
		String soaplabLocation = sp[j].getServicePath();
		if (soaplabLocations.containsKey(soaplabLocation)==false) {
		    soaplabLocations.put(soaplabLocation, new HashMap());
		}
		Map nameToProcessorNames = (Map)soaplabLocations.get(soaplabLocation);
		String appName = sp[j].getCategory()+"::<font color=\"purple\">"+sp[j].getAppName()+"</font>";
		if (nameToProcessorNames.containsKey(appName)==false) {
		    nameToProcessorNames.put(appName, new HashSet());
		}
		Set processorNames = (Set)nameToProcessorNames.get(appName);
		processorNames.add(sp[j].getName());
	    }
	    for (Iterator j = soaplabLocations.keySet().iterator(); j.hasNext(); ) {
		String location = (String)j.next();
		Map nameToProcessorName = (Map)soaplabLocations.get(location);
		int rows = 2+nameToProcessorName.size();
		sb.append("<tr>");
		sb.append("<td width=\"80\" valign=\"top\" rowspan=\""+rows+"\" bgcolor=\"#faf9d2\">Soaplab</td>");
		sb.append("<td colspan=\"2\" bgcolor=\"faf9d2\">Service rooted at <em>"+location+"</em></td>");
		sb.append("</tr>");
		sb.append("<tr><td bgcolor=\"#eeeedd\">App category and name</td><td bgcolor=\"#eeeedd\">Processors</td></tr>");
		for (Iterator k = nameToProcessorName.keySet().iterator(); k.hasNext();) {
		    String appName = (String)k.next();
		    Set processorNames = (Set)nameToProcessorName.get(appName);
		    sb.append("<tr>");
		    sb.append("<td>"+appName+"</td>");
		    sb.append("<td>");
		    for (Iterator l = processorNames.iterator(); l.hasNext();) {
			sb.append((String)l.next());
			if (l.hasNext()) {
			    sb.append(", ");
			}
		    }
		    sb.append("</td></tr>");
		}
	    }

	    // Do all seqhound installations within this host
	    temp = new HashSet();
	    for (Iterator j = usageList.iterator(); j.hasNext(); ) {
		Object o = j.next();
		if (o instanceof SeqhoundProcessor) {
		    temp.add(o);
		}
	    }
	    SeqhoundProcessor sq[] = (SeqhoundProcessor[])temp.toArray(new SeqhoundProcessor[0]);
	    Map seqhoundLocations = new HashMap();
	    for (int j = 0; j < sq.length; j++) {
		String seqhoundLocation = sq[j].getPath();
		if (seqhoundLocations.containsKey(seqhoundLocation) == false) {
		    seqhoundLocations.put(seqhoundLocation, new HashMap());
		}
		Map nameToProcessorNames = (Map)seqhoundLocations.get(seqhoundLocation);
		String methodName = "<font color=\"purple\">"+sq[j].getMethodName()+"</font>";
		if (nameToProcessorNames.containsKey(methodName) == false) {
		    nameToProcessorNames.put(methodName, new HashSet());
		}
		Set processorNames = (Set)nameToProcessorNames.get(methodName);
		processorNames.add(sq[j].getName());
	    }
	    for (Iterator j = seqhoundLocations.keySet().iterator(); j.hasNext(); ) {
		String location = (String)j.next();
		Map nameToProcessorName = (Map)seqhoundLocations.get(location);
		int rows = 2+nameToProcessorName.size();
		sb.append("<tr>");
		sb.append("<td width=\"80\" valign=\"top\" rowspan=\""+rows+"\" bgcolor=\"#2f25fa\">SeqHound</td>");
		sb.append("<td colspan=\"2\" bgcolor=\"2f25fa\">Service rooted at <em>"+location+"</em></td>");
		sb.append("</tr>");
		sb.append("<tr><td bgcolor=\"#eeeedd\">Method name</td><td bgcolor=\"#eeeedd\">Processors</td></tr>");
		for (Iterator k = nameToProcessorName.keySet().iterator(); k.hasNext();) {
		    String appName = (String)k.next();
		    Set processorNames = (Set)nameToProcessorName.get(appName);
		    sb.append("<tr>");
		    sb.append("<td>"+appName+"</td>");
		    sb.append("<td>");
		    for (Iterator l = processorNames.iterator(); l.hasNext();) {
			sb.append((String)l.next());
			if (l.hasNext()) {
			    sb.append(", ");
			}
		    }
		    sb.append("</td></tr>");
		}
	    }


	    for (Iterator j = usageList.iterator(); j.hasNext(); ) {
		Processor p = (Processor)j.next();
		if (p instanceof WSDLBasedProcessor == false &&
		    p instanceof SoaplabProcessor == false &&
		    p instanceof SeqhoundProcessor == false) {
		    sb.append("<tr>");
		    if (p instanceof BiomobyProcessor) {
			sb.append("<td bgcolor=\"#ffd200\">Biomoby</td>");
			BiomobyProcessor bp = (BiomobyProcessor)p;
			sb.append("<td><font color=\"purple\">"+bp.getServiceName()+"</font>&nbsp;in&nbsp;"+bp.getEndpoint().getFile()+"</td>");
		    }
		    else {
			sb.append("<td colspan=\"2\">Unknown&nbsp;type</td>");
		    }
		    sb.append("<td>"+p.getName()+"</td>");
		    sb.append("</tr>\n");
		}
	    }
	}
	sb.append("</table></body></html>");
	return sb.toString();
    }
    
}
