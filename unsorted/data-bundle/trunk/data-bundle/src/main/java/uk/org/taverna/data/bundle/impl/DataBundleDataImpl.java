/**
 * 
 */
package uk.org.taverna.data.bundle.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import uk.org.taverna.platform.data.api.Data;
import uk.org.taverna.platform.data.api.DataNature;
import uk.org.taverna.platform.data.api.DataReference;

/**
 * @author alanrw
 *
 */
public class DataBundleDataImpl implements Data {
	
	private DataBundleImpl containingBundle;
	
	private String id;
	
	private static Logger logger = Logger.getLogger(DataBundleDataImpl.class);
	
	public DataBundleDataImpl(DataBundleImpl dataBundleImpl, String id) {
		this.containingBundle = dataBundleImpl;
		this.id = id;
	}
	
	public DataBundleDataImpl(DataBundleImpl dataBundleImpl, Data original,
			boolean snapshot) throws IOException {
		this(dataBundleImpl, original.getID());
		
		this.setDataNature(original.getDataNature());
		
		if (original.hasDataNature(DataNature.LIST)) {
			StringBuffer buffer = new StringBuffer();
			// Write a kind of text/uri-list (but with relative URIs)

			for (Data elem : original.getElements()) {
				String relRef;

				if (elem == null) {
					relRef = "missing";
				}
				else {
					Data elementCopy = new DataBundleDataImpl(containingBundle, elem, snapshot);

					relRef = getBasePath(elementCopy.getID());
					
				}
				buffer.append(relRef + "\n");
			}

			this.setElementsString(buffer.toString());
		} else if (original.hasDataNature(DataNature.TEXT_VALUE) || original.hasDataNature(DataNature.BINARY_VALUE)){

			if (original.hasReferences()) {
				copyDataReferences(getBasePath(), original);
			}
			
			if (original.hasExplicitValue()) {
					
					this.setExplicitValue(original.getExplicitValue());

			} else if (original.hasReferences() && snapshot) {
				String mimeType = calculateMimeType(original);
				writeValueStream(getReferenceInputStream(original), mimeType);
			}
			if ((!this.hasExplicitValue()) && (!this.hasReferences())) {
				this.setDataNature(DataNature.NULL);
			}
		}
		
	}

	private void copyDataReferences(String path, Data original) throws IOException {
		String mimeType;
		mimeType = "text/uri";
		StringBuffer buffer = new StringBuffer();
		// Write a kind of text/uri-list

		int i = 0;
		for (DataReference elem : original.getReferences()) {
			DataReference newRef = containingBundle.createDataReference();
			newRef.setURI(elem.getURI());
			Charset charset = elem.getCharset();
			if (charset != null) {
				newRef.setCharset(charset);
			}
			buffer.append(DataBundleDataReferenceImpl.getBasePath(newRef) + "\n");
		}
		containingBundle.addResource(buffer.toString(),
				getReferenceListResourcePath(), mimeType);
	}

	private void writeValueString(String value, String mimeType)
			throws IOException {
		containingBundle.addResource(value, getValueResourcePath(), mimeType);
	}

	private void writeValueBytes(byte[] value, String mimeType)
			throws IOException {
		containingBundle.addResource(value, getValueResourcePath(), mimeType);
	}

	private static String calculateMimeType(Data data) {
		if (data.hasDataNature(DataNature.TEXT_VALUE)) {
			return "text/plain";
		} else {
			return "application/octet-stream";
		}
		
	}
	
	// This is only here in place of something better
	// Reading the value is not sensible
	private static InputStream getReferenceInputStream(Data data) {
		InputStream inputStream = null;
		// TODO try them until you get one that works
		try {
			for (DataReference dr : data.getReferences()) {
				try {
					inputStream = dr.getURI().toURL().openStream();
				} catch (MalformedURLException e) {
					logger.error(e);
				} catch (IOException e) {
					logger.error(e);
				}
				if (inputStream != null) {
					break;
				}
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return inputStream;
	}

	private void writeValueStream(InputStream inputStream, String mimeType)
			throws IOException {
		OutputStream output = containingBundle.addResourceUsingOutputStream(getValueResourcePath(), mimeType);
		try {
			if (inputStream == null) {
				logger.error("Unable to get input stream to data");
			} else {
				IOUtils.copyLarge(inputStream, output);
			}
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				logger.error("Failed to close data inputstream", e);
			}
			try {
				output.close();
			} catch (IOException e) {
				logger.error("Failed to close file outputstream", e);
			}
		}

		output.close();
	}

	private String getDataNatureResourcePath() {
		return (getBasePath() + "/DataNature");
	}
	
	private String getElementsResourcePath() {
		return getBasePath() + "/Elements";
	}
	
	private String getReferencesResourcePath() {
		return getBasePath() + "/References";
	}
	
	private String getReferenceListResourcePath() {
		return getBasePath() + "/ReferenceList";
	}
	
	private String getValueResourcePath() {
		return (getBasePath() + "/Value");
	}
	
	private String getDepthResourcePath() {
		return getBasePath() + "/Depth";
	}
	
	public String getDataReferenceResourcePath(int index) {
		return getReferencesResourcePath() + "/DataReference/" + index + "/URI";
	}
	
	public String getSizeResourcePath() {
		return getBasePath() + "/Size";
	}
	
	/* (non-Javadoc)
	 * @see uk.org.taverna.platform.data.api.Data#getID()
	 */
	@Override
	public String getID() {
		return id;
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.platform.data.api.Data#hasDataNature(uk.org.taverna.platform.data.api.DataNature)
	 */
	@Override
	public boolean hasDataNature(DataNature nature) {
		return nature.equals(getDataNature());
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.platform.data.api.Data#getDataNature()
	 */
	@Override
	public DataNature getDataNature() {
		try {
			return DataNature.valueOf(containingBundle.getResourceAsString(getDataNatureResourcePath()));
		} catch (IOException e) {
			logger.error(e);
			return DataNature.NULL;
		}
	}

	@Override
	public void setDataNature(DataNature dataNature) throws IOException {
		containingBundle.addResource(dataNature.toString(), getDataNatureResourcePath(), "text/plain");
	}
	
	public void setDepth(int depth) throws IOException {
		containingBundle.addResource(Integer.toString(depth), getDepthResourcePath(), "text/plain");
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.platform.data.api.Data#getDepth()
	 */
	@Override
	public int getDepth() {
		try {
			return Integer.parseInt(containingBundle.getResourceAsString(getDepthResourcePath()));
		} catch (NumberFormatException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.platform.data.api.Data#hasReferences()
	 */
	@Override
	public boolean hasReferences() {
		return containingBundle.getResourceEntry(getReferencesResourcePath()) != null;
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.platform.data.api.Data#getReferences()
	 */
	@Override
	public Set<DataReference> getReferences() throws IOException {
		Set<DataReference> result = new HashSet<DataReference>();
		String dataReferenceString = containingBundle.getResourceAsString(getReferenceListResourcePath());
		String[] elementParts = StringUtils.split(dataReferenceString);
		for (String elementPath : elementParts) {
			String lastPart = StringUtils.substringAfterLast(elementPath, "/");
			DataReference newElem = new DataBundleDataReferenceImpl(containingBundle, lastPart);
			result.add(newElem);
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see uk.org.taverna.platform.data.api.Data#hasExplicitValue()
	 */
	@Override
	public boolean hasExplicitValue() {
		return containingBundle.getResourceEntry(getValueResourcePath()) != null;
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.platform.data.api.Data#getExplicitValue()
	 */
	@Override
	public Object getExplicitValue() {
		if (hasDataNature(DataNature.TEXT_VALUE)) {
			try {
				return containingBundle.getResourceAsString(getValueResourcePath());
			} catch (IOException e) {
				logger.error(e);
				return null;
			}
		} else {
			try {
				return containingBundle.getResourceAsBytes(getValueResourcePath());
			} catch (IOException e) {
				logger.error(e);
				return null;
			}
		}
	}
	
	public void setExplicitValue(Object o) throws IOException {
		if (o == null) {
			this.setDataNature(DataNature.NULL);
		}
		else if (this.hasDataNature(DataNature.TEXT_VALUE)) {
			writeValueString((String) o, "text/plain");
		} else {
			writeValueBytes((byte[]) o, "application/octet-stream");
		}
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.platform.data.api.Data#getElements()
	 */
	@Override
	public List<Data> getElements() {
		List<Data> result = new ArrayList<Data>();
		try {
			String elementsString = containingBundle.getResourceAsString(getElementsResourcePath());
			String[] elementParts = StringUtils.split(elementsString);
			for (String elementPath : elementParts) {
				String lastPart = StringUtils.substringAfterLast(elementPath, "/");
				Data newElem = new DataBundleDataImpl(containingBundle, lastPart);
				result.add(newElem);
			}
		} catch (IOException e) {
			logger.error(e);
			return result;
		}

		return result;
	}
	
	private void setElementsString(String string) throws IOException {
		containingBundle.addResource(string, getElementsResourcePath(), "text/uri-list");
	}

	static String getBasePath(String id) {
		try {
			id = UUID.fromString(id).toString();
		} catch (IllegalArgumentException ex) {
			logger.error("Unable to convert id", ex);
		}
		return id.substring(
				0, 2) + "/" +  id;
	}
	
	private static String getBasePath(Data data) {
		String id = data.getID();
		return getBasePath(id);
	}

	private String getBasePath() {
		return getBasePath(this);
	}

	@Override
	public void setReferences(Set<DataReference> references) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setElements(List<Data> elements) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getApproximateSizeInBytes() {
		try {
			return Long.parseLong(containingBundle.getResourceAsString(getSizeResourcePath()));
		} catch (NumberFormatException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return -1;
	}

	@Override
	public void setApproximateSizeInBytes(long size) throws IOException {
		containingBundle.addResource(Long.toString(size), getSizeResourcePath(), "text/plain");
	}

}
