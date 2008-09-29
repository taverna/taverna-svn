package net.sf.taverna.t2.workbench.file.impl;

import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializationConstants;

public class T2FlowFileType extends FileType {

	@Override
	public String getDescription() {
		return "Taverna 2 workflow";
	}

	@Override
	public String getExtension() {
		return "t2flow";
	}

	@Override
	public String getMimeType() {
		// "application/vnd.taverna.t2flow+xml";
		return XMLSerializationConstants.WORKFLOW_DOCUMENT_MIMETYPE;
	}

}