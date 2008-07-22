package net.sf.taverna.t2.cloudone.datamanager.file;

import java.io.File;

public class FileUtil {
	
	/**
	 * Wraps the standard java {@link File#mkdirs()} within a synchronised method, making is threadsafe (which that standard mkdirs is not).
	 * 
	 * @param path - the path of the directories to be made
	 * @return true if the directories was successfully created, otherwise false
	 */
	public static synchronized boolean mkdirs(File path) {
		return path.mkdirs();
	}
}
