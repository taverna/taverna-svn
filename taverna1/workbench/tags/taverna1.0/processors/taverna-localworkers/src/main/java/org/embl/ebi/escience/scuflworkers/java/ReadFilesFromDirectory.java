/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMOBY Project
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Consumes a directory name and an optional file suffix and returns a list of
 * strings/bytes representing the contents of those files. If a file suffix is not
 * specified, then all files in the directory are read.
 * <p>
 * 
 * @author Edward Kawas
 */
public class ReadFilesFromDirectory implements LocalWorker {

    private static Logger logger = Logger
	    .getLogger(ReadFilesFromDirectory.class);

    public String[] inputNames() {
	return new String[] { "Directory", "FileSuffix" };
    }

    public String[] inputTypes() {
	return new String[] { LocalWorker.STRING, LocalWorker.STRING };
    }

    public String[] outputNames() {
	return new String[] { "files_byte", "files_string" };
    }

    public String[] outputTypes() {
	return new String[] { "l('application/octet-stream')",
		"l('text/plain')" };
    }

    /**
     * Perform the task; take the directory path and the file suffix and get all
     * the files in that directory
     */
    public Map<String, DataThing> execute(Map inputs)
	    throws TaskExecutionException {
	String filePattern = null;
	String directory = null;
	// set the file pattern if it exists
	if (inputs.get(inputNames()[1]) != null) {
	    filePattern = (String) ((DataThing) inputs.get(inputNames()[1]))
		    .getDataObject();

	}
	// get the directory
	if (inputs.get(inputNames()[0]) != null) {
	    directory = (String) ((DataThing) inputs.get(inputNames()[0]))
		    .getDataObject();
	    if (directory.trim().equals(""))
		throw new TaskExecutionException(
			"In order to read from a directory, a non empty string directory must be specified!");
	} else {
	    throw new TaskExecutionException(
		    "In order to read from a directory, one must be specified!");
	}

	File dir = new File(directory);
	if (!dir.isDirectory())
	    throw new TaskExecutionException(
		    "Please specify a directory to read from and not a file!");
	if (!dir.canRead())
	    throw new TaskExecutionException(
		    "Please specify a directory to read from that we can read!");

	// get a listing of the files
	File[] files = dir.listFiles(new ReadFileFilter(
		filePattern == null ? "" : filePattern));
	// our output lists; one for bytes, the other for strings
	ArrayList<byte[]> bytesList = new ArrayList<byte[]>();
	ArrayList<String> stringsList = new ArrayList<String>();

	// fill the lists with the data
	for (File f : files) {
	    try {
		// get the data for the string list
		InputStreamReader is = new InputStreamReader(
			new FileInputStream(f));
		char[] bytes = new char[128];
		int num_read = -1;
		StringWriter sw = new StringWriter();
		while ((num_read = is.read(bytes)) != -1) {
		    sw.write(bytes, 0, num_read);
		}
		is.close();
		stringsList.add(sw.toString());
		// fill the byte list
		bytesList.add(getBytesFromFile(f));
	    } catch (IOException e) {
		// TODO should we continue or die all together if a file fails
		// to be sucked in?
		logger
			.warn(
				"There was a problem reading from a file and the file will be ignored. The error is: ",
				e);
	    }

	}
	// put the data in the output map
	Map<String, DataThing> outputs = new HashMap<String, DataThing>();
	outputs.put(outputNames()[0], new DataThing(bytesList));
	outputs.put(outputNames()[1], new DataThing(stringsList));
	return outputs;
    }

    /*
     * A file filter that filters for any given file suffix
     * 
     */
    private class ReadFileFilter implements FileFilter {

	private String filter;

	/**
	 * Default constructor
	 */
	public ReadFileFilter() {
	    this(null);
	}

	/**
	 * 
	 * @param filter
	 *                the file suffix to use as a filter
	 */
	public ReadFileFilter(String filter) {
	    this.filter = filter == null ? "" : filter;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File pathname) {
	    // only open files in the specified directory
	    if (!pathname.isFile())
		return false;
	    // null means all file types
	    if (filter == null || pathname.getName().endsWith(filter))
		return true;
	    // not what we were looking for
	    return false;
	}
    }

    /*
     * retrieve a byte array from a file; file must be smaller than
     * Integer.MAX_VALUE
     */
    private byte[] getBytesFromFile(File file) throws IOException {
	InputStream is = new FileInputStream(file);

	// Get the size of the file
	long length = file.length();

	// throw exception if the file size is too big
	if (length > Integer.MAX_VALUE) {
	    throw new IOException("Could not completely read file (too large) "
		    + file.getName());
	}

	// Create the byte array to hold the data
	byte[] bytes = new byte[(int) length];

	int offset = 0;
	int numRead = 0;
	while (offset < bytes.length
		&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
	    offset += numRead;
	}

	// check if we read the whole file
	if (offset < bytes.length) {
	    throw new IOException("Could not completely read file "
		    + file.getName());
	}
	is.close();
	return bytes;
    }

}
