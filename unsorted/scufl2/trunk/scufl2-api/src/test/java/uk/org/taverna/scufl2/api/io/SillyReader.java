package uk.org.taverna.scufl2.api.io;

import java.util.Collections;
import java.util.Set;

public class SillyReader implements WorkflowBundleReader {

	@Override
	public Set<String> getMediaTypes() {
		return Collections.singleton("application/vnd.example.silly");
	}


}
