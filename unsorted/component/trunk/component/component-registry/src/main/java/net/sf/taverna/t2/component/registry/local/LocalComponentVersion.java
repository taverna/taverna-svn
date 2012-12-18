/**
 * 
 */
package net.sf.taverna.t2.component.registry.local;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2DataflowOpener;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class LocalComponentVersion implements ComponentVersion {
	
	private static Logger logger = Logger.getLogger(LocalComponentVersion.class);
	
	
	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();

	private final File componentVersionDir;
	private final LocalComponent component;

	public LocalComponentVersion(LocalComponent component, File componentVersionDir) {
		this.component = component;
		this.componentVersionDir = componentVersionDir;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentVersion#getComponent()
	 */
	@Override
	public Component getComponent() {
		return component;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentVersion#getDescription()
	 */
	@Override
	public String getDescription() {
		// TODO
		return "";
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.ComponentVersion#getVersionNumber()
	 */
	@Override
	public Integer getVersionNumber() {
		return Integer.parseInt(componentVersionDir.getName());
	}

	@Override
	public Dataflow getDataflow() throws ComponentRegistryException {
		T2DataflowOpener opener = new T2DataflowOpener();
		
		DataflowInfo info;
		try {
			info = opener.openDataflow(T2_FLOW_FILE_TYPE, new File(componentVersionDir, "dataflow.t2flow"));
		} catch (OpenException e) {
			logger.error(e);
			throw new ComponentRegistryException("Unable to open dataflow", e);
		}
		
		return info.getDataflow();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
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

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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

}
