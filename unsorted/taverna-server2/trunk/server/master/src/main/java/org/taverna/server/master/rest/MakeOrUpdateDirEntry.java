package org.taverna.server.master.rest;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * The input to the REST interface for making directories and files, and
 * uploading file contents.
 * 
 * @author Donal Fellows
 */
@XmlType(name="FilesystemCreationOperation")
@XmlSeeAlso( { MakeOrUpdateDirEntry.MakeDirectory.class,
		MakeOrUpdateDirEntry.SetFileContents.class })
public abstract class MakeOrUpdateDirEntry {
	@XmlAttribute
	public String name;
	@XmlValue
	public byte[] contents;

	/**
	 * Create a directory.
	 * 
	 * @author Donal Fellows
	 */
	@XmlRootElement(name = "mkdir")
	@XmlType(name="MakeDirectory")
	public static class MakeDirectory extends MakeOrUpdateDirEntry {
	}

	/**
	 * Create a file or set its contents.
	 * 
	 * @author Donal Fellows
	 */
	@XmlRootElement(name = "upload")
	@XmlType(name="UploadFile")
	public static class SetFileContents extends MakeOrUpdateDirEntry {
	}
}
