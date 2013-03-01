/**
 * 
 */
package net.sf.taverna.t2.component.registry.myexperiment;

/**
 * @author alanrw
 *
 */
public class MyExperimentPublicPolicy extends MyExperimentSharingPolicy {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.SharingPolicy#getName()
	 */
	@Override
	public String getName() {
		return "Public";
	}

	@Override
	public String getPolicyString() {
		StringBuilder contentXml = new StringBuilder();
		contentXml.append("<permissions>");
			contentXml.append("<permission>");
			contentXml.append("<category>public</category>");
			contentXml.append("<privilege type=\"view\" />");
			contentXml.append("<privilege type=\"download\" />");
			contentXml.append("</permission>");
		contentXml.append("</permissions>");
		return contentXml.toString();
	}

}
