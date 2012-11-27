/**
 * 
 */
package net.sf.taverna.t2.component.registry.local;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.impl.T2DataflowSaver;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class LocalComponent implements Component {
	
	private final File componentDir;
	
	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();

	private static Logger logger = Logger.getLogger(LocalComponent.class);

	public LocalComponent(File componentDir) {
		this.componentDir = componentDir;	
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.Component#addVersionBasedOn(net.sf.taverna.t2.workflowmodel.Dataflow)
	 */
	@Override
	public ComponentVersion addVersionBasedOn(Dataflow dataflow) throws ComponentRegistryException {
		Integer nextVersionNumber = Integer.valueOf(1);
		try {
			nextVersionNumber = getComponentVersionMap().lastKey() + 1;
		} catch (NoSuchElementException e) {
			// This is OK
		}
		File newVersionDir = new File(componentDir, nextVersionNumber.toString());
		newVersionDir.mkdirs();
		LocalComponentVersion newComponentVersion = new LocalComponentVersion(this, newVersionDir);
		File dataflowFile = new File (newVersionDir, "dataflow.t2flow");
		T2DataflowSaver saver = new T2DataflowSaver();
		try {
			saver.saveDataflow(dataflow, T2_FLOW_FILE_TYPE, dataflowFile);
		} catch (SaveException e) {
			logger.error("Unable to save component version", e);
			throw new ComponentRegistryException("Unable to save component version", e);
		}
		return newComponentVersion;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.Component#getComponentVersion(java.lang.Integer)
	 */
	@Override
	public ComponentVersion getComponentVersion(Integer version) throws ComponentRegistryException {
		File componentVersionFile = new File(componentDir, version.toString());
		if (componentVersionFile.isDirectory()) {
			return new LocalComponentVersion(this, componentVersionFile);
		}
		throw new ComponentRegistryException("Unable to read component version");
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.Component#getName()
	 */
	@Override
	public String getName() {
		return componentDir.getName();
	}

	@Override
	public SortedMap<Integer, ComponentVersion> getComponentVersionMap() {
		TreeMap<Integer, ComponentVersion> result = new TreeMap<Integer, ComponentVersion>();
		for (File subFile : componentDir.listFiles()) {
			if (subFile.isDirectory()) {
				try {
					Integer i = Integer.valueOf(subFile.getName());
					result.put(i, new LocalComponentVersion(this, subFile));
				}
				catch (NumberFormatException e) {
					// Ignore
				}
			}
		}
		return result;
	}

	@Override
	public URL getComponentURL() {
		try {
			return componentDir.toURI().toURL();
		} catch (MalformedURLException e) {
			logger.error(e);
			return null;
		}
	}

}
