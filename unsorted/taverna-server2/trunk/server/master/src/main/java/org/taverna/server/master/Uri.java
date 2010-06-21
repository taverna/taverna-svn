package org.taverna.server.master;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="Location")
public class Uri {
	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	public URI ref;

	public Uri() {
	}

	public Uri(URI ref) {
		this.ref = ref;
	}

	public Uri(UriBuilder ub, String... strings) {
		ref = ub.build((Object[]) strings);
	}

	public Uri(UriInfo ui, String path, String... strings) {
		ref = ui.getAbsolutePathBuilder().path(path).build((Object[]) strings);
	}
}