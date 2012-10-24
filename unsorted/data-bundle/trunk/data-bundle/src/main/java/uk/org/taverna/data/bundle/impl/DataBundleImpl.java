/**
 * 
 */
package uk.org.taverna.data.bundle.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.org.taverna.data.bundle.api.DataBundle;
import uk.org.taverna.platform.data.api.Data;
import uk.org.taverna.platform.data.api.DataNature;
import uk.org.taverna.platform.data.api.DataReference;
import uk.org.taverna.scufl2.api.common.URITools;
import uk.org.taverna.scufl2.ucfpackage.UCFPackage;

/**
 * @author alanrw
 *
 */
public class DataBundleImpl implements DataBundle {
	
	private static Logger logger = Logger.getLogger(DataBundleImpl.class);
	
	protected Map<Data, String> seenReferences = new HashMap<Data, String>();
	
	private UCFPackage ucfPackage = null;
	
	public DataBundleImpl() throws IOException {
		super();
			ucfPackage = new UCFPackage();
			ucfPackage.setPackageMediaType(UCFPackage.MIME_DATA_BUNDLE);
	}
	
	private URITools uriTools = new URITools();


	private boolean seenReference(Data data) {
		return seenReferences.containsKey(data);
	}
	
	private boolean seenReference(Data data, String path) {
		if (seenReference(data)) {
			return true;
		}
		return seenReferences.put(data, path) != null;

	}
	
	@Override
	public String saveData(Data data, boolean snapshot) throws IOException {
		// Avoid double-saving
		String path = seenReferences.get(data);
		if (path != null) {
			return path;
		}

		Data copy = new DataBundleDataImpl(this, data, snapshot);
		path = copy.getID();
		seenReference(data, path);
		return path;
	}



	
	// This is only here in place of something better
	// Reading the value is not sensible
	private static InputStream getValueInputStream(Data data) {
		InputStream inputStream = null;
			Object value = data.getExplicitValue();
			if (value instanceof byte[]) {
				byte[] byteArray = (byte[]) value;
			inputStream = new ByteArrayInputStream(byteArray);
		} else if (value instanceof String) {
			String string = (String) value;
			inputStream = new ByteArrayInputStream(string.getBytes(Charset.forName("Utf-8")));
		}
		return inputStream;
	}


	@Override
	public void saveData(Set<Data> setOfData, boolean snapshot) throws IOException {
		for (Data data : setOfData) {
			saveData(data, snapshot);
		}
	}


	@Override
	public void save(File destination) throws IOException {
		ucfPackage.save(destination);
	}
	
	@Override
	public void save(OutputStream stream) throws IOException {
		ucfPackage.save(stream);
	}
	
	void addResource(String stringValue, String path, String mediaType) throws IOException {
		ucfPackage.addResource(stringValue, path, mediaType);
	}

	public void addResource(byte[] explicitValue, String path, String mimeType) throws IOException {
		ucfPackage.addResource(explicitValue, path, mimeType);
	}

	public OutputStream addResourceUsingOutputStream(String path,
			String mediaType) throws IOException {
		return ucfPackage.addResourceUsingOutputStream(path, mediaType);
	}

	@Override
	public Data get(String ID) {
		return new DataBundleDataImpl(this, ID);
	}

	@Override
	public boolean delete(String ID) {
		ucfPackage.removeResource(DataBundleDataImpl.getBasePath(ID));
		return true;
	}

	@Override
	public Data create(DataNature nature) throws IOException {
		Data result = new DataBundleDataImpl(this, UUID.randomUUID().toString());
		result.setDataNature(nature);
		return result;
	}

	public String getResourceAsString(String path) throws IOException {
		return ucfPackage.getResourceAsString(path);
	}

	public Object getResourceEntry(String path) {
		return ucfPackage.getResourceEntry(path);
	}

	public byte[] getResourceAsBytes(String path) throws IOException {
		return ucfPackage.getResourceAsBytes(path);
	}

	@Override
	public DataReference createDataReference() {
		DataReference result = new DataBundleDataReferenceImpl(this, UUID.randomUUID().toString());
		return result;
	}

	@Override
	public void open(File source) throws IOException {
		ucfPackage = new UCFPackage(source);
	}

	@Override
	public void open(InputStream s) throws IOException {
		ucfPackage = new UCFPackage(s);
	}



}
