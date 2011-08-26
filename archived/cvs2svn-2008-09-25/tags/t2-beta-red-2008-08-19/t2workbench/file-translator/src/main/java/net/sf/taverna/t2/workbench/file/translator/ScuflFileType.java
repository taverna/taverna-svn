package net.sf.taverna.t2.workbench.file.translator;

import net.sf.taverna.t2.workbench.file.FileType;

public class ScuflFileType extends FileType {

	@Override
	public String getDescription() {
		return "Taverna 1 SCUFL workflow";
	}

	@Override
	public String getExtension() {
		return "xml";
	}

	@Override
	public String getMimeType() {
		return "application/vnd.taverna.scufl+xml";
	}
}
