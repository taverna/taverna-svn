package net.sf.taverna.t2.annotation;

import java.net.URI;

public class URISource implements AnnotationSourceSPI{
	
	private URI uri;
	
	public URISource() {
		
	}

	public URISource(URI uri) {
		this.uri = uri;
	}

	public void setUri(URI uri) {
//		if (uri != null) {
//			throw new RuntimeException("URI has already been set");
//		}
		this.uri = uri;
	}

	public URI getUri() {
		return uri;
	}

}
