package org.embl.ebi.escience.scufl.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.scufl.IProcessor;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.HTMLSummarisableProcessor;

/**
 * A utility class containing a single static method which summarizes the
 * specified workflow into a standard HTML document and returns it as a string.
 * 
 * @author Tom Oinn
 */
public class WorkflowSummaryAsHTML {

	public static String STYLE;

	public static String STYLE_NOBG;

	static {
		StringBuffer sb = new StringBuffer();
		sb.append("<style type=\"text/css\">");
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
		sb.append("</style>");
		STYLE = sb.toString();
		sb = new StringBuffer();
		sb.append("<style type=\"text/css\">");
		sb.append("body {\n");
		sb.append("font-family: Helvetica, Arial, sans-serif;\n");
		sb.append("font-size: 12pt;\n");
		sb.append("}\n");
		sb.append("blockquote {\n");
		sb.append("  padding: 5px;\n");
		sb.append("  background-color: #ffffff;\n");
		sb.append("  border-width: 1px; border-style: solid; border-color: #aaaaaa;\n");
		sb.append("}\n");
		sb.append("</style>");
		STYLE_NOBG = sb.toString();
	}

	public static String nameFor(Map names, Processor p) {
		for (Iterator i = names.keySet().iterator(); i.hasNext();) {
			String name = (String) i.next();
			Processor proc = (Processor) names.get(name);
			if (proc == p) {
				return name;
			}
		}
		return "Not found";
	}

	public static String getSummary(ScuflModel model) {
		// Processor[] processors = model.getProcessors();
		Map<String, List<Processor>> resources = new HashMap<String, List<Processor>>();
		// Create a map of name -> processor including all nested workflows
		Map<String, Processor> names = new HashMap<String, Processor>();
		model.collectAllProcessors(names, null);

		// group by resourceHost
		for (Iterator i = names.keySet().iterator(); i.hasNext();) {
			String procName = (String) i.next();
			Processor p = (Processor) names.get(procName);

			String resourceHost = p.getResourceHost();
			if (resourceHost != IProcessor.ENACTOR) {
				if (resources.containsKey(resourceHost) == false) {
					resources.put(resourceHost, new ArrayList<Processor>());
				}
				List<Processor> processorForResource = resources.get(resourceHost);
				processorForResource.add(p);
			}
		}

		StringBuffer sb = new StringBuffer();
		sb.append("<html><head>");
		sb.append(WorkflowSummaryAsHTML.STYLE);
		sb.append("</head><body>");
		sb.append("<h2>Workflow information</h2>");

		String author = model.getDescription().getAuthor();
		author = (author.equals("") ? "<font color=\"red\">no author</font>" : "<font color=\"green\">" + author
				+ "</font>");

		String title = model.getDescription().getTitle();
		title = (title.equals("") ? "<font color=\"red\">no title</font>" : "<font color=\"green\">" + title
				+ "</font>");

		String lsid = model.getDescription().getLSID();
		lsid = (lsid.equals("") ? "<font color=\"red\">no lsid</font>" : "<font color=\"green\">" + lsid + "</font>");

		sb.append("This report applies to the workflow titled '" + title + "' authored by '" + author
				+ "' and with LSID '" + lsid + "'. The textual description, if any is shown below :<blockquote><em>"
				+ model.getDescription().getText() + "</em></blockquote>");
		sb.append("<h2>Resource usage report</h2>");
		sb
				.append("This display shows the various external resources used by the current workflow. It does not show resources such as local operations or string constants which are run within the enactment engine. Services are categorized by resource host and type, and the name of the instance of each service shown to the right.");
		sb.append("<table border=\"1\" bgcolor=\"white\">");

		// iterate over each resource
		for (String hostName : resources.keySet()) {
			Map<String, List<HTMLSummarisableProcessor>> summarisedProcessors = new HashMap<String, List<HTMLSummarisableProcessor>>();

			List<Processor> usageList = resources.get(hostName);
			sb.append("<tr><td valign=\"top\" bgcolor=\"#bcd2ee\"colspan=\"3\">Resources on   <code><b>" + hostName
					+ "</b></code>, " + usageList.size() + " instance" + (usageList.size() != 1 ? "s" : "")
					+ ".</td></tr>\n");

			// groups summarisable processors together by class type
			String unknownProcessorsText = "";
			for (Processor processor : usageList) {
				if (processor instanceof HTMLSummarisableProcessor) {
					String className = processor.getClass().getName();
					if (summarisedProcessors.get(className) == null) {
						summarisedProcessors.put(className, new ArrayList<HTMLSummarisableProcessor>());
					}
					summarisedProcessors.get(className).add((HTMLSummarisableProcessor) processor);
				}
				else
				{
					unknownProcessorsText+="<tr><td colspan=\"2\">Unknown&nbsp;type</td>";
					unknownProcessorsText+="<td>" + nameFor(names, processor) + "</td></tr>\n";					
				}				
			}

			List<List<HTMLSummarisableProcessor>> values = new ArrayList<List<HTMLSummarisableProcessor>>();
			values.addAll(summarisedProcessors.values());

			// sort according to table placement
			Collections.sort(values, new Comparator<List<HTMLSummarisableProcessor>>() {
				public int compare(List<HTMLSummarisableProcessor> o1, List<HTMLSummarisableProcessor> o2) {
					Integer placement1 = o1.get(0).htmlTablePlacement();
					Integer placement2 = o2.get(0).htmlTablePlacement();
					return placement1.compareTo(placement2);
				}
			});

			for (List<HTMLSummarisableProcessor> list : values) {
				if (list.size() > 0) // should always be
				{
					String html = list.get(0).getHTMLSummary(list, names);
					sb.append(html);
				}
			}
			sb.append(unknownProcessorsText);

		}
		sb.append("</table></body></html>");
		return sb.toString();
	}

	

}
