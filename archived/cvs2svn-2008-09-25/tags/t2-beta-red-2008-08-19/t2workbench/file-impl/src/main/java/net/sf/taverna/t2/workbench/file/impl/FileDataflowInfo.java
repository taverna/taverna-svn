package net.sf.taverna.t2.workbench.file.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * Information about an open dataflow that was opened from or saved to a
 * {@link File}.
 * 
 * @see DataflowInfo
 * @see FileManager
 * @author Stian Soiland-Reyes
 * 
 */
public class FileDataflowInfo extends DataflowInfo {
	private static Logger logger = Logger.getLogger(FileDataflowInfo.class);

	public FileDataflowInfo(FileType fileType, File source, Dataflow dataflow) {
		super(fileType, canonicalFile(source), dataflow,
				lastModifiedFile(source));
	}

	protected static Date lastModifiedFile(File file) {
		long lastModifiedLong = file.lastModified();
		if (lastModifiedLong == 0) {
			return null;
		}
		return new Date(lastModifiedLong);
	}

	protected static File canonicalFile(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			logger.warn("Could not find canonical file for " + file);
			return file;
		}
	}
}
