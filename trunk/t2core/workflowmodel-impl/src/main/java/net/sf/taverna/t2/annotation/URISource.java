package net.sf.taverna.t2.annotation;

import java.net.URI;

public class URISource implements AnnotationSourceSPI{
	
	private URI uri;

	public URISource(URI uri) {
		this.uri = uri;
	}

}
