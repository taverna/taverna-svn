package org.taverna.server.master.common;

import static org.taverna.server.master.common.Namespaces.XLINK;

import java.net.URI;

import javax.annotation.PreDestroy;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.web.PortMapper;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * A class that makes it simpler to work with an element with a {@link URI} in
 * an <tt>href</tt> attribute. Done with JAXB.
 * 
 * @author Donal Fellows
 */
@XmlType(name = "Location")
public class Uri {
	private static final String SECURE_SCHEME = "https";
	/**
	 * This type is characterised by an attribute that is the reference to some
	 * other element.
	 */
	@XmlAttribute(name = "href", namespace = XLINK)
	public URI ref;

	/** Make a reference that points nowhere. */
	public Uri() {
	}

	/**
	 * Make a reference to the given location.
	 * 
	 * @param ref
	 *            Where to point to.
	 */
	public Uri(@NonNull URI ref) {
		this.ref = ref;
	}

	/**
	 * Make a reference from the factory with the given parameters.
	 * 
	 * @param ub
	 *            The configured factory.
	 * @param strings
	 *            The parameters to the factory.
	 */
	public Uri(@NonNull UriBuilder ub, String... strings) {
		ref = ub.build((Object[]) strings);
	}

	/**
	 * Make a reference from the factory with the given parameters.
	 * 
	 * @param ui
	 *            The factory factory.
	 * @param path
	 *            The path to configure the factory with.
	 * @param strings
	 *            The parameters to the factory.
	 */
	public Uri(@NonNull UriInfo ui, @NonNull String path, String... strings) {
		this(ui.getAbsolutePathBuilder().path(path), strings);
	}

	/**
	 * Make a reference from the factory with the given parameters.
	 * 
	 * @param ui
	 *            The factory factory.
	 * @param secure
	 *            Whether the URI should be required to use HTTPS.
	 * @param path
	 *            The path to configure the factory with.
	 * @param strings
	 *            The parameters to the factory.
	 */
	public Uri(@NonNull UriInfo ui, boolean secure, @NonNull String path,
			String... strings) {
		UriBuilder ub = ui.getAbsolutePathBuilder();
		if (secure) {
			ub = Rewriter.getSecuredUriBuilder(ub);
		}
		ref = ub.path(path).build((Object[]) strings);
	}

	/**
	 * A bean that allows configuration of how to rewrite generated URIs to be
	 * secure.
	 * 
	 * @author Donal Fellows
	 */
	public static class Rewriter {
		private static Rewriter instance;
		private PortMapper portMapper;
		private boolean suppress;

		@Autowired
		@Required
		public void setPortMapper(PortMapper portMapper) {
			this.portMapper = portMapper;
		}

		/**
		 * Whether to suppress rewriting of URIs to be secure.
		 * 
		 * @param suppressSecurity
		 *            True if no rewriting should be done.
		 */
		public void setSuppressSecurity(boolean suppressSecurity) {
			suppress = suppressSecurity;
		}

		@SuppressWarnings
		public Rewriter() {
			instance = this;
		}

		@PreDestroy
		@SuppressWarnings
		public void done() {
			instance = null;
		}

		@NonNull
		static UriBuilder getSecuredUriBuilder(@NonNull UriBuilder ub) {
			if (instance != null && instance.suppress)
				return ub;
			Integer secPort = null;
			if (instance != null && instance.portMapper != null)
				secPort = instance.portMapper.lookupHttpsPort(ub.build()
						.getPort());
			if (secPort == null || secPort == -1)
				return ub.scheme(SECURE_SCHEME);
			return ub.scheme(SECURE_SCHEME).port(secPort);
		}
	}
}