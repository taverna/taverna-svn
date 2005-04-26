package net.sourceforge.taverna.io;
/**
 * This class contains utilities used with managing files.
 * 
 * Last edited by $Author: phidias $
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class FileNameUtil {
	
	/**
	 * This method returns the extension of the file.
	 * @param filename  The name of the file.
	 * @return
	 */
	public static String getExtension(String filename){
		int extIndex = filename.indexOf(".");
		return filename.substring(extIndex+1);
	}
	
	/**
	 * This method gets the extension given fully qualified path/to/file.ext
	 * @param path  The complete path to the file.
	 * @return
	 */
	public static String getExtensionFromPath(String path){
		String filename = path.substring(path.lastIndexOf(System.getProperty("file.separator")));
		return getExtension(filename);
	}
	
	/**
	 * This method replaces the extension of the current file.
	 * @param filename  The name of the file. 
	 * @param newExt	The new extension for the file
	 * @return
	 */
	public static String replaceExtension(String filename, String newExt){
		String ext = getExtension(filename);
		String newFilename = filename;
		return newFilename.replace(ext, newExt);
	}
	
	/**
	 * This method replaces the extension of a file given a complete path for the file.
	 * @param path		The complete path to the file.
	 * @param newExt	The new extension for the file.
	 * @return
	 */
	public static String replacePathExtension(String path, String newExt){
		String ext = getExtensionFromPath(path);
		String newFilename = path;
		return newFilename.replace(ext, newExt);
	}

}
