package org.embl.ebi.escience.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Simple file reading
 * 
 * @author Stian Soiland
 *
 */
public class SimpleFile {
	
	/**
	 * Read a file and return the full content.
	 * 
	 * @param filename Path to file to be opened. 
	 * @return The full file content, lines separated by '\n'
	 * @throws IOException If file is not found or cannot be read.
	 */
	public static String readFile(String filename) throws IOException {
		File file = new File(filename);
		return readFile(file);
	}
	
	/**
	 * Read a file and return the full content.
	 * 
	 * @param file File to be opened
	 * @return The full file content, lines separated by '\n'
	 * @throws IOException If file is not found or cannot be read.
	 */
	public static String readFile(File file) throws IOException {
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = new BufferedReader(new FileReader(file));		
		String line;
		while((line=reader.readLine())!=null) {	
			buffer.append(line);
			buffer.append("\n");			
		}
		return buffer.toString();
	}
}
