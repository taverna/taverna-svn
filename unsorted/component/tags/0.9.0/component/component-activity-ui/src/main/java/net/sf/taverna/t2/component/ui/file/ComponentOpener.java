/**
 * 
 */
package net.sf.taverna.t2.component.ui.file;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.registry.ComponentDataflowCache;
import net.sf.taverna.t2.component.registry.ComponentFileType;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceDesc;
import net.sf.taverna.t2.workbench.file.AbstractDataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentOpener extends AbstractDataflowPersistenceHandler
		implements DataflowPersistenceHandler {
	
	private static final ComponentFileType COMPONENT_FILE_TYPE = new ComponentFileType();
	
	private static Logger logger = Logger.getLogger(ComponentOpener.class);
	
	public DataflowInfo openDataflow(FileType fileType, Object source) throws OpenException {
		if (!getOpenFileTypes().contains(fileType)) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		if (!(source instanceof ComponentVersionIdentification)) {
			throw new IllegalArgumentException("Unsupported source type " + source.getClass().getName());
		}
		
		Dataflow d;
		try {
			d = ComponentDataflowCache.getDataflow((ComponentVersionIdentification) source);
		} catch (ComponentRegistryException e) {
			logger.error("Unable to read dataflow", e);
			throw new OpenException("Unable to read dataflow", e);
		}
		return new DataflowInfo(COMPONENT_FILE_TYPE, source, d, new Date());
	}

	@Override
	public List<FileType> getOpenFileTypes() {
		return Arrays.<FileType> asList(COMPONENT_FILE_TYPE);
	}

	@Override
	public List<Class<?>> getOpenSourceTypes() {
		return Arrays.<Class<?>> asList(ComponentVersionIdentification.class);
	}
}
