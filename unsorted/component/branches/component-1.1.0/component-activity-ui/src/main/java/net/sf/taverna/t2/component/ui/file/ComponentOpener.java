/**
 * 
 */
package net.sf.taverna.t2.component.ui.file;

import static net.sf.taverna.t2.component.registry.ComponentDataflowCache.getDataflow;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.sf.taverna.t2.component.api.ComponentFileType;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.workbench.file.AbstractDataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentOpener extends AbstractDataflowPersistenceHandler
		implements DataflowPersistenceHandler {
	private static final FileType COMPONENT_FILE_TYPE = ComponentFileType.instance;
	private static Logger logger = Logger.getLogger(ComponentOpener.class);

	@Override
	public DataflowInfo openDataflow(FileType fileType, Object source)
			throws OpenException {
		if (!getOpenFileTypes().contains(fileType))
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		if (!(source instanceof ComponentVersionIdentification))
			throw new IllegalArgumentException("Unsupported source type "
					+ source.getClass().getName());

		Dataflow d;
		try {
			d = getDataflow((ComponentVersionIdentification) source);
		} catch (RegistryException e) {
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
