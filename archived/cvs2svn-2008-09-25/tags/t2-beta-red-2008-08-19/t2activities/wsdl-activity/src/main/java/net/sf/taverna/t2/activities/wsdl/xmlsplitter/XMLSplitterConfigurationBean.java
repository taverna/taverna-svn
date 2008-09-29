package net.sf.taverna.t2.activities.wsdl.xmlsplitter;


import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

public class XMLSplitterConfigurationBean extends ActivityPortsDefinitionBean {
	String wrappedTypeXML;

	public String getWrappedTypeXML() {
		return wrappedTypeXML;
	}

	public void setWrappedTypeXML(String wrappedTypeXML) {
		this.wrappedTypeXML = wrappedTypeXML;
	}
}
