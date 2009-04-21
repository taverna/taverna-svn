package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker;

import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.Data;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InputParams;

public class InterProScanInput {

	private InputParams params;

	private Data[] content;

	public InputParams getParams() {
		return params;
	}

	public void setParams(InputParams params) {
		this.params = params;
	}

	public Data[] getContent() {
		return content;
	}

	public void setContent(Data[] content) {
		this.content = content;
	}

}
