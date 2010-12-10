package uk.org.taverna.scufl2.ucfpackage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.oasis_open.names.tc.opendocument.xmlns.container.Container;
import org.oasis_open.names.tc.opendocument.xmlns.container.Container.RootFiles;
import org.oasis_open.names.tc.opendocument.xmlns.container.ObjectFactory;
import org.oasis_open.names.tc.opendocument.xmlns.container.RootFile;
import org.w3c.dom.Document;

import uk.org.taverna.scufl2.ucfpackage.impl.odfdom.pkg.OdfPackage;
import uk.org.taverna.scufl2.ucfpackage.impl.odfdom.pkg.manifest.OdfFileEntry;

public class UCFPackage {

	private static final String CONTAINER_XML = "META-INF/container.xml";
	private static final Charset UTF_8 = Charset.forName("utf-8");
	public static final String MIME_BINARY = "application/octet-stream";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String MIME_TEXT_XML = "text/xml";
	public static final String MIME_RDF = "application/rdf+xml";
	public static final String MIME_EPUB = "application/epub+zip";
	public static final String MIME_WORKFLOW_BUNDLE = "application/vnd.taverna.workflow-bundle";
	public static final String MIME_DATA_BUNDLE = "application/vnd.taverna.data-bundle";
	public static final String MIME_WORKFLOW_RUN_BUNDLE = "application/vnd.taverna.workflow-run-bundle";
	public static final String MIME_SERVICE_BUNDLE = "application/vnd.taverna.service-bundle";

	private static Charset ASCII = Charset.forName("ascii");
	private OdfPackage odfPackage;
	private static JAXBContext jaxbContext;
	private JAXBElement<Container> containerXml;
	private boolean createdContainerXml = false;
	private static ObjectFactory containerFactory = new ObjectFactory();

	public UCFPackage() throws Exception {
		odfPackage = OdfPackage.create();
		// odfPackage.setMediaType(MIME_EPUB);
		parseContainerXML();
	}

	public UCFPackage(File containerFile) throws Exception {
		odfPackage = OdfPackage.loadPackage(containerFile);
		parseContainerXML();
	}

	public UCFPackage(InputStream inputStream) throws Exception {
		odfPackage = OdfPackage.loadPackage(inputStream);
		parseContainerXML();
	}

	@SuppressWarnings("unchecked")
	protected void parseContainerXML() throws Exception {
		createdContainerXml = false;
		InputStream containerStream = getResourceAsInputStream(CONTAINER_XML);
		if (containerStream == null) {
			// Make an empty containerXml
			Container container = containerFactory.createContainer();
			containerXml = containerFactory.createContainer(container);
			createdContainerXml = true;
			return;
		}
		Unmarshaller unMarshaller = createUnMarshaller();
		containerXml = (JAXBElement<Container>) unMarshaller
				.unmarshal(containerStream);

	}

	public String getPackageMediaType() {
		return odfPackage.getMediaType();
	}

	public void setPackageMediaType(String mediaType) {
		if (mediaType == null || !mediaType.contains("/")) {
			throw new IllegalArgumentException("Invalid media type "
					+ mediaType);
		}
		if (!ASCII.newEncoder().canEncode(mediaType)) {
			throw new IllegalArgumentException("Media type must be ASCII: "
					+ mediaType);
		}
		odfPackage.setMediaType(mediaType);
	}

	public void save(File packageFile) throws IOException {
		if (getPackageMediaType() == null) {
			throw new IllegalStateException("Package media type must be set");
		}

		// Write using temp file, and do rename in the end
		File tempFile = File.createTempFile("." + packageFile.getName(),
				".tmp", packageFile.getParentFile());
		try {
			prepareContainerXML();
			odfPackage.save(tempFile);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("Could not save bundle to " + packageFile, e);
		} finally {
			odfPackage.close();
		}
		try {
			// To be safe we'll reload from 'our' tempFile
			odfPackage = OdfPackage.loadPackage(tempFile);
		} catch (Exception e) {
			throw new IOException("Could not reload package from "
					+ packageFile);
		}
		if (!tempFile.renameTo(packageFile)) {
			throw new IOException("Could not rename temp file " + tempFile
					+ " to " + packageFile);
		}
	}

	protected void prepareContainerXML() throws Exception {
		if (containerXml == null || createdContainerXml
				&& containerXml.getValue().getRootFilesOrAny() == null) {
			return;
		}

		/* Check if we should prune <rootFiles> */
		Iterator<Object> iterator = containerXml.getValue().getRootFilesOrAny().iterator();
		boolean foundAlready = false;
		while (iterator.hasNext()) {
			Object anyOrRoot = iterator.next();
			if (! (anyOrRoot instanceof JAXBElement)) {
				continue;
			}
			@SuppressWarnings("rawtypes")
			JAXBElement elem = (JAXBElement)anyOrRoot;
			if (! elem.getDeclaredType().equals(RootFiles.class)) {
				continue;
			}
			RootFiles rootFiles = (RootFiles) elem.getValue();
			if (foundAlready ||
					(rootFiles.getOtherAttributes().isEmpty() && rootFiles.getAnyOrRootFile().isEmpty())) {
				// Delete it!
				System.err.println("Deleting unneccessary <rootFiles>");
				iterator.remove();
			}
			foundAlready = true;
		}

		Marshaller marshaller = createMarshaller();
		OutputStream outStream = odfPackage.insertOutputStream(CONTAINER_XML);
		try {
			// XMLStreamWriter xmlStreamWriter = XMLOutputFactory
			// .newInstance().createXMLStreamWriter(outStream);
			// xmlStreamWriter.setDefaultNamespace(containerElem.getName()
			// .getNamespaceURI());
			//
			// xmlStreamWriter.setPrefix("dsig",
			// "http://www.w3.org/2000/09/xmldsig#");
			// xmlStreamWriter.setPrefix("xmlenc",
			// "http://www.w3.org/2001/04/xmlenc#");

			// FIXME: Set namespace prefixes and default namespace

			marshaller.setProperty("jaxb.formatted.output", true);

			// TODO: Ensure using default namespace
			marshaller.marshal(containerXml, outStream);

		} finally {
			outStream.close();
		}
	}

	protected static synchronized Marshaller createMarshaller()
			throws JAXBException {
		return getJaxbContext().createMarshaller();
	}

	protected static synchronized Unmarshaller createUnMarshaller()
			throws JAXBException {
		Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();

		return unmarshaller;
	}

	protected static synchronized JAXBContext getJaxbContext()
			throws JAXBException {
		if (jaxbContext == null) {
			jaxbContext = JAXBContext.newInstance(
					"org.oasis_open.names.tc.opendocument.xmlns.container:"
							+ "org.w3._2000._09.xmldsig_:"
							+ "org.w3._2001._04.xmlenc_",
					UCFPackage.class.getClassLoader());
		}
		return jaxbContext;
	}

	public void addResource(String stringValue, String path, String mediaType)
			throws Exception {
		odfPackage.insert(stringValue.getBytes(UTF_8), path, mediaType);
		if (path.equals(CONTAINER_XML)) {
			parseContainerXML();
		}
	}

	public void addResource(byte[] bytesValue, String path, String mediaType)
			throws Exception {
		odfPackage.insert(bytesValue, path, mediaType);
		if (path.equals(CONTAINER_XML)) {
			parseContainerXML();
		}
	}

	public void addResource(Document document, String path, String mediaType)
			throws Exception {
		odfPackage.insert(document, path, mediaType);
		if (path.equals(CONTAINER_XML)) {
			parseContainerXML();
		}
	}

	public void addResource(InputStream inputStream, String path,
			String mediaType) throws Exception {
		odfPackage.insert(inputStream, path, mediaType);
		if (path.equals(CONTAINER_XML)) {
			parseContainerXML();
		}
	}

	public void addResource(URI uri, String path, String mediaType)
			throws Exception {
		odfPackage.insert(uri, path, mediaType);
		if (path.equals(CONTAINER_XML)) {
			parseContainerXML();
		}
	}

	public String getResourceAsString(String path) throws Exception {
		return new String(odfPackage.getBytes(path), UTF_8);
	}

	public byte[] getResourceAsBytes(String path) throws Exception {
		return odfPackage.getBytes(path);
	}

	public InputStream getResourceAsInputStream(String path) throws Exception {
		return odfPackage.getInputStream(path);
	}

	public Map<String, ResourceEntry> listResources() {
		return listResources("", false);
	}

	public Map<String, ResourceEntry> listResources(String folderPath) {
		return listResources(folderPath, false);
	}

	protected Map<String, ResourceEntry> listResources(String folderPath,
			boolean recursive) {
		if (!folderPath.isEmpty() && !folderPath.endsWith("/")) {
			folderPath = folderPath + "/";
		}
		HashMap<String, ResourceEntry> content = new HashMap<String, ResourceEntry>();

		for (Entry<String, OdfFileEntry> entry : odfPackage
				.getManifestEntries().entrySet()) {
			String entryPath = entry.getKey();
			if (!entryPath.startsWith(folderPath)) {
				continue;
			}
			String subPath = entryPath.substring(folderPath.length(),
					entryPath.length());
			if (subPath.isEmpty()) {
				// The folder itself
				continue;
			}
			int firstSlash = subPath.indexOf("/");
			if (!recursive && firstSlash > -1
					&& firstSlash < subPath.length() - 1) {
				// Children of a folder (note that we'll include the folder
				// itself which ends in /)
				continue;
			}
			content.put(subPath, new ResourceEntry(entry.getValue()));
		}
		return content;
	}

	public void removeResource(String path) {
		if (!odfPackage.contains(path)) {
			return;
		}
		if (path.endsWith("/")) {
			for (ResourceEntry childEntry : listResources(path).values()) {
				removeResource(childEntry.getPath());
			}
		}
		odfPackage.remove(path);
	}

	public class ResourceEntry {

		private final String path;
		private final long size;
		private String mediaType;

		protected ResourceEntry(OdfFileEntry odfEntry) {
			path = odfEntry.getPath();
			size = odfEntry.getSize();
			mediaType = odfEntry.getMediaType();
		}

		public String getPath() {
			return path;
		}

		public long getSize() {
			return size;
		}

		public String getMediaType() {
			return mediaType;
		}

		public boolean isFolder() {
			return path.endsWith("/");
		}

	}

	public Map<String, ResourceEntry> listAllResources() {
		return listResources("", true);
	}

	@SuppressWarnings("rawtypes")
	public void setRootFile(String path) {
		ResourceEntry rootFile = getResourceEntry(path);
		if (rootFile == null) {
			throw new IllegalArgumentException("Unknown resource: " + path);
		}

		Container container = containerXml.getValue();

		RootFiles rootFiles = getRootFiles(container);
		String mediaType = rootFile.getMediaType();
		boolean foundExisting = false;
		// Check any existing files for matching path/mime type
		Iterator<Object> anyOrRootIt = rootFiles.getAnyOrRootFile().iterator();
		while (anyOrRootIt.hasNext()) {
			Object anyOrRoot = anyOrRootIt.next();
			if (anyOrRoot instanceof JAXBElement) {
				anyOrRoot = ((JAXBElement) anyOrRoot).getValue();
			}
			if (!(anyOrRoot instanceof RootFile)) {
				continue;
			}
			RootFile rootFileElem = (RootFile) anyOrRoot;
			if (!rootFileElem.getFullPath().equals(path)
					&& !rootFileElem.getMediaType().equals(mediaType)) {
				// Different path and media type - ignore
				continue;
			}
			if (foundExisting) {
				// Duplicate path/media type, we'll remove it
				anyOrRootIt.remove();
				continue;
			}
			rootFileElem.setFullPath(rootFile.getPath());
			if (mediaType != null) {
				rootFileElem.setMediaType(mediaType);
			}
			foundExisting = true;
		}
		if (!foundExisting) {
			RootFile rootFileElem = containerFactory.createRootFile();
			rootFileElem.setFullPath(rootFile.getPath());
			rootFileElem.setMediaType(mediaType);
			rootFiles.getAnyOrRootFile().add(
					containerFactory
							.createContainerRootFilesRootFile(rootFileElem));
			// rootFiles.getAnyOrRootFile().add(rootFileElem);
		}
	}

	protected RootFiles getRootFiles(Container container) {

		for (Object o : container.getRootFilesOrAny()) {
			if (o instanceof JAXBElement) {
				@SuppressWarnings("rawtypes")
				JAXBElement jaxbElement = (JAXBElement) o;
				o = jaxbElement.getValue();
			}
			if (o instanceof RootFiles) {
				return (RootFiles) o;
			}
		}
		// Not found - add it
		RootFiles rootFiles = containerFactory.createContainerRootFiles();
		container.getRootFilesOrAny().add(
				containerFactory.createContainerRootFiles(rootFiles));
		return rootFiles;
	}

	@SuppressWarnings("rawtypes")
	public List<ResourceEntry> getRootFiles() {
		ArrayList<UCFPackage.ResourceEntry> rootFiles = new ArrayList<UCFPackage.ResourceEntry>();
		if (containerXml == null) {
			return rootFiles;
		}

		RootFiles rootFilesElem = getRootFiles(containerXml.getValue());
		for (Object anyOrRoot : rootFilesElem.getAnyOrRootFile()) {
			if (anyOrRoot instanceof JAXBElement) {
				anyOrRoot = ((JAXBElement) anyOrRoot).getValue();
			}
			if (!(anyOrRoot instanceof RootFile)) {
				continue;
			}
			RootFile rf = (RootFile) anyOrRoot;
			ResourceEntry entry = getResourceEntry(rf.getFullPath());
			if (rf.getMediaType() != null
					&& rf.getMediaType() != entry.mediaType) {
				// Override the mime type in the returned entry
				entry.mediaType = rf.getMediaType();
			}
			rootFiles.add(entry);
		}
		return rootFiles;
	}

	public ResourceEntry getResourceEntry(String path) {
		OdfFileEntry odfFileEntry = odfPackage.getManifestEntries().get(path);
		if (odfFileEntry == null) {
			return null;
		}
		return new ResourceEntry(odfFileEntry);
	}

	@SuppressWarnings("rawtypes")
	public void unsetRootFile(String path) {
		Container container = containerXml.getValue();
		RootFiles rootFiles = getRootFiles(container);
		Iterator<Object> anyOrRootIt = rootFiles.getAnyOrRootFile().iterator();
		while (anyOrRootIt.hasNext()) {
			Object anyOrRoot = anyOrRootIt.next();
			if (anyOrRoot instanceof JAXBElement) {
				anyOrRoot = ((JAXBElement) anyOrRoot).getValue();
			}
			if (!(anyOrRoot instanceof RootFile)) {
				continue;
			}
			RootFile rootFileElem = (RootFile) anyOrRoot;
			if (rootFileElem.getFullPath().equals(path)) {
				anyOrRootIt.remove();
			}
		}
	}

	protected JAXBElement<Container> getContainerXML() {
		return containerXml;
	}
}
