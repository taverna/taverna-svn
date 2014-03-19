/**
 * 
 */
package net.sf.taverna.t2.component.registry.local;

import static java.lang.Integer.parseInt;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.log4j.Logger.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2DataflowOpener;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
class LocalComponentVersion extends ComponentVersion {
	private static Logger logger = getLogger(LocalComponentVersion.class);

	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();

	private final File componentVersionDir;

	protected LocalComponentVersion(LocalComponent component,
			File componentVersionDir) {
		super(component);
		this.componentVersionDir = componentVersionDir;
	}

	@Override
	protected final String internalGetDescription() {
		File descriptionFile = new File(componentVersionDir, "description");
		try {
			if (descriptionFile.isFile())
				return readFileToString(descriptionFile);
		} catch (IOException e) {
			logger.error("failed to get description from " + descriptionFile, e);
		}
		return "";
	}

	@Override
	protected final Integer internalGetVersionNumber() {
		return parseInt(componentVersionDir.getName());
	}

	@Override
	protected final Dataflow internalGetDataflow() throws RegistryException {
		T2DataflowOpener opener = new T2DataflowOpener();

		File filename = new File(componentVersionDir, "dataflow.t2flow");
		try {
			return opener.openDataflow(T2_FLOW_FILE_TYPE, filename)
					.getDataflow();
		} catch (OpenException e) {
			logger.error(
					"failed to get component realization from " + filename, e);
			throw new RegistryException("Unable to open dataflow", e);
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((componentVersionDir == null) ? 0 : componentVersionDir
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalComponentVersion other = (LocalComponentVersion) obj;
		if (componentVersionDir == null) {
			if (other.componentVersionDir != null)
				return false;
		} else if (!componentVersionDir.equals(other.componentVersionDir))
			return false;
		return true;
	}

	@Override
	public URL getHelpURL() {
		return null;
	}

}
