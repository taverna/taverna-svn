package net.sf.taverna.t2.component.registry.standard;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.URLEncoder.encode;
import static org.apache.log4j.Logger.getLogger;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.registry.standard.annotations.Unused;
import net.sf.taverna.t2.component.registry.standard.myexpclient.MyExperimentClient;
import net.sf.taverna.t2.component.registry.standard.myexpclient.MyExperimentClient.ServerResponse;

import org.apache.log4j.Logger;

class Client {
	private static final String API_VERIFICATION_RESOURCE = "/component-profiles.xml";
	private static final Logger logger = getLogger(Client.class);
	private final MyExperimentClient myE_Client;
	private final URL registryBase;
	private final JAXBContext jaxbContext;

	Client(JAXBContext context, URL repository) throws Exception {
		this.registryBase = repository;
		this.jaxbContext = context;

		myE_Client = new MyExperimentClient(repository.toString());
		myE_Client.login();
		logger.info("instantiated client connection engine to " + repository);
	}

	public boolean verify() {
		try {
			String url = url(API_VERIFICATION_RESOURCE);
			logger.info("API verification: HEAD for " + url);
			return myE_Client.HEAD(url).getCode() == HTTP_OK;
		} catch (Exception e) {
			logger.info("failed to connect to " + registryBase, e);
			return false;
		}
	}

	private String url(String uri, String... arguments)
			throws MalformedURLException, UnsupportedEncodingException {
		StringBuilder uriBuilder = new StringBuilder(uri);
		for (String queryElement : arguments) {
			String[] bits = queryElement.split("=", 2);
			uriBuilder.append(uriBuilder.indexOf("?") < 0 ? "?" : "&")
					.append(bits[0]).append('=')
					.append(encode(bits[1], "UTF-8"));
		}
		return new URL(registryBase, uriBuilder.toString()).toString();
	}

	private Marshaller getMarshaller() throws JAXBException {
		return jaxbContext.createMarshaller();
	}

	/**
	 * Does an HTTP GET against the configured repository.
	 * 
	 * @param clazz
	 *            The JAXB-annotated class that the result is supposed to be
	 *            instantiated into.
	 * @param uri
	 *            The path part of the URI within the repository.
	 * @param query
	 *            The strings to put into the query part. Each should be in
	 *            <tt>key=value</tt> form.
	 * @return The deserialized response object.
	 * @throws RegistryException
	 *             If anything goes wrong.
	 */
	public <T> T get(Class<T> clazz, String uri, String... query)
			throws RegistryException {
		try {

			String url = url(uri, query);
			logger.info("GET of " + url);
			ServerResponse response = myE_Client.GET(url);
			if (response.isFailure())
				throw new RegistryException(
						"Unable to perform request (%d): %s",
						response.getCode(), response.getError());
			return response.getResponse(jaxbContext, clazz);

		} catch (RegistryException e) {
			throw e;
		} catch (MalformedURLException e) {
			throw new RegistryException("Problem constructing resource URL", e);
		} catch (JAXBException e) {
			throw new RegistryException("Problem when unmarshalling response",
					e);
		} catch (Exception e) {
			throw new RegistryException("Problem when sending request", e);
		}
	}

	/**
	 * Does an HTTP POST against the configured repository.
	 * 
	 * @param clazz
	 *            The JAXB-annotated class that the result is supposed to be
	 *            instantiated into.
	 * @param elem
	 *            The JAXB element to post to the resource.
	 * @param uri
	 *            The path part of the URI within the repository.
	 * @param query
	 *            The strings to put into the query part. Each should be in
	 *            <tt>key=value</tt> form.
	 * @return The deserialized response object.
	 * @throws RegistryException
	 *             If anything goes wrong.
	 */
	public <T> T post(Class<T> clazz, JAXBElement<?> elem, String uri,
			String... query) throws RegistryException {
		try {

			String url = url(uri, query);
			logger.info("POST to " + url);
			StringWriter sw = new StringWriter();
			getMarshaller().marshal(elem, sw);
			if (logger.isDebugEnabled())
				logger.info("About to post XML document:\n" + sw);
			ServerResponse response = myE_Client.POST(url, sw.toString());
			if (response.isFailure())
				throw new RegistryException(
						"Unable to perform request (%d): %s",
						response.getCode(), response.getError());
			return response.getResponse(jaxbContext, clazz);

		} catch (RegistryException e) {
			throw e;
		} catch (MalformedURLException e) {
			throw new RegistryException("Problem constructing resource URL", e);
		} catch (JAXBException e) {
			throw new RegistryException("Problem when marshalling request", e);
		} catch (Exception e) {
			throw new RegistryException("Problem when sending request", e);
		}
	}

	/**
	 * Does an HTTP PUT against the configured repository.
	 * 
	 * @param clazz
	 *            The JAXB-annotated class that the result is supposed to be
	 *            instantiated into.
	 * @param elem
	 *            The JAXB element to post to the resource.
	 * @param uri
	 *            The path part of the URI within the repository.
	 * @param query
	 *            The strings to put into the query part. Each should be in
	 *            <tt>key=value</tt> form.
	 * @return The deserialized response object.
	 * @throws RegistryException
	 *             If anything goes wrong.
	 */
	@Unused
	public <T> T put(Class<T> clazz, JAXBElement<?> elem, String uri,
			String... query) throws RegistryException {
		try {

			String url = url(uri, query);
			logger.info("PUT to " + url);
			StringWriter sw = new StringWriter();
			getMarshaller().marshal(elem, sw);
			if (logger.isDebugEnabled())
				logger.info("About to post XML document:\n" + sw);
			ServerResponse response = myE_Client.PUT(url, sw.toString());
			if (response.isFailure())
				throw new RegistryException(
						"Unable to perform request (%d): %s",
						response.getCode(), response.getError());
			return response.getResponse(jaxbContext, clazz);

		} catch (RegistryException e) {
			throw e;
		} catch (MalformedURLException e) {
			throw new RegistryException("Problem constructing resource URL", e);
		} catch (JAXBException e) {
			throw new RegistryException("Problem when marshalling request", e);
		} catch (Exception e) {
			throw new RegistryException("Problem when sending request", e);
		}
	}

	/**
	 * Does an HTTP DELETE against the configured repository.
	 * 
	 * @param uri
	 *            The path part of the URI within the repository.
	 * @param query
	 *            The strings to put into the query part. Each should be in
	 *            <tt>key=value</tt> form.
	 * @throws RegistryException
	 *             If anything goes wrong.
	 */
	public void delete(String uri, String... query) throws RegistryException {
		ServerResponse response;
		try {

			String url = url(uri, query);
			logger.info("DELETE of " + url);
			response = myE_Client.DELETE(url);

		} catch (MalformedURLException e) {
			throw new RegistryException("Problem constructing resource URL", e);
		} catch (Exception e) {
			throw new RegistryException("Unable to perform request", e);
		}
		if (response.isFailure())
			throw new RegistryException("Unable to perform request (%d): %s",
					response.getCode(), response.getError());
	}
}
