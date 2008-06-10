package net.sf.taverna.t2.workbench.configuration.mimetype;

import org.junit.Test;

public class TestMimeTypes {
	
	@Test
	public void getMimeTypes() {
		MimeTypeManager instance = MimeTypeManager.getInstance();
		instance.getDefaultPropertyMap();
	}

}
