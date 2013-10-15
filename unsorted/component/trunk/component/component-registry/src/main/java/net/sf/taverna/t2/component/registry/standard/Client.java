package net.sf.taverna.t2.component.registry.standard;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.registry.standard.annotations.Unused;
import net.sf.taverna.t2.component.registry.standard.myexpclient.MyExperimentClient;
import net.sf.taverna.t2.component.registry.standard.myexpclient.ServerResponse;

import org.apache.log4j.Logger;

class Client {
	private Logger logger;
	private MyExperimentClient myE_Client;
	private URL registryBase;
	private JAXBContext jaxbContext;

	Client(JAXBContext context, Logger logger, URL repository) throws Exception {
		this.logger = logger;
		this.registryBase = repository;
		this.jaxbContext = context;

		myE_Client = new MyExperimentClient(logger);
		myE_Client.setBaseURL(repository.toExternalForm());
		myE_Client.doLogin();
	}

	public boolean verify() {
		try {
			return myE_Client.doMyExperimentGET(
					registryBase + "/component-profiles.xml").getResponseCode() == HTTP_OK;
		} catch (Exception e) {
			logger.info("failed to connect to " + registryBase);
			return false;
		}
	}

	private String url(String uri, String[] arguments)
			throws MalformedURLException, UnsupportedEncodingException {
		StringBuilder uriBuilder = new StringBuilder(uri);
		for (String queryElement : arguments) {
			String[] bits = queryElement.split("=", 2);
			uriBuilder.append(uriBuilder.indexOf("?") < 0 ? "?" : "&")
					.append(bits[0]).append('=')
					.append(URLEncoder.encode(bits[1], "UTF-8"));
		}
		return new URL(registryBase, uriBuilder.toString()).toString();
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
		String url = null;
		try {
			url = url(uri, query);
			ServerResponse response = myE_Client.doMyExperimentGET(url);
			if (response.getResponseCode() != HTTP_OK)
				throw new RegistryException("Unable to perform request "
						+ response.getResponseCode());
			return response.getResponse(jaxbContext, clazz);
		} catch (RegistryException e) {
			throw e;
		} catch (MalformedURLException e) {
			throw new RegistryException("Problem constructing resource URL:"
					+ e.getMessage());
		} catch (JAXBException e) {
			throw new RegistryException("Problem when unmarshalling response",
					e);
		} catch (Exception e) {
			logger.info("failed in GET to " + url, e);
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
		String url = null;
		try {
			url = url(uri, query);
			StringWriter sw = new StringWriter();
			jaxbContext.createMarshaller().marshal(elem, sw);
			ServerResponse response = myE_Client.doMyExperimentPOST(url,
					sw.toString());
			if (response.getResponseCode() >= 400)
				throw new RegistryException("Unable to perform request "
						+ response.getResponseCode());
			return response.getResponse(jaxbContext, clazz);
		} catch (RegistryException e) {
			throw e;
		} catch (MalformedURLException e) {
			throw new RegistryException("Problem constructing resource URL:"
					+ e.getMessage());
		} catch (JAXBException e) {
			throw new RegistryException("Problem when marshalling request", e);
		} catch (Exception e) {
			logger.info("failed in POST to " + url, e);
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
		String url = null;
		try {
			url = url(uri, query);
			StringWriter sw = new StringWriter();
			jaxbContext.createMarshaller().marshal(elem, sw);
			ServerResponse response = myE_Client.doMyExperimentPUT(url,
					sw.toString());
			if (response.getResponseCode() >= 400)
				throw new RegistryException("Unable to perform request "
						+ response.getResponseCode());
			return response.getResponse(jaxbContext, clazz);
		} catch (RegistryException e) {
			throw e;
		} catch (MalformedURLException e) {
			throw new RegistryException("Problem constructing resource URL:"
					+ e.getMessage());
		} catch (JAXBException e) {
			throw new RegistryException("Problem when marshalling request", e);
		} catch (Exception e) {
			logger.info("failed in POST to " + url, e);
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
		String url = null;
		ServerResponse response;
		try {
			url = url(uri, query);
			response = myE_Client.doMyExperimentDELETE(url);
		} catch (MalformedURLException e) {
			throw new RegistryException("Problem constructing resource URL:"
					+ e.getMessage());
		} catch (Exception e) {
			logger.info("failed in DELETE to " + url, e);
			throw new RegistryException("Unable to perform request: "
					+ e.getMessage(), e);
		}
		if (response.getResponseCode() >= 400)
			throw new RegistryException(
					"Unable to perform request: result code "
							+ response.getResponseCode());
		return;
	}
}
