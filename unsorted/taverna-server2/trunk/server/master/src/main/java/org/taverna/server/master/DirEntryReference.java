package org.taverna.server.master;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.taverna.server.master.interfaces.Directory;
import org.taverna.server.master.interfaces.DirectoryEntry;

/**
 * A reference to something that is in a directory below the working directory
 * of a workflow run, described using JAXB.
 * 
 * @author Donal Fellows
 */
@XmlType(name = "DirectoryEntry")
@XmlSeeAlso( { DirEntryReference.DirectoryReference.class,
		DirEntryReference.FileReference.class })
public abstract class DirEntryReference {
	/** A link to the entry. */
	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	public URI link;

	/** The path of the entry. */
	@XmlValue
	public String path;

	public DirEntryReference() {
	}

	/**
	 * Return the directory entry reference instance subclass suitable for the
	 * given directory entry.
	 * 
	 * @param entry
	 *            The entry to characterise.
	 */
	public static DirEntryReference newInstance(DirectoryEntry entry) {
		return newInstance(null, entry);
	}

	/**
	 * Return the directory entry reference instance subclass suitable for the
	 * given directory entry.
	 * 
	 * @param ub
	 *            Used for constructing URIs.
	 * @param entry
	 *            The entry to characterise.
	 */
	// Really returns a subclass, so cannot be constructor
	public static DirEntryReference newInstance(UriBuilder ub,
			DirectoryEntry entry) {
		DirEntryReference de = (entry instanceof Directory) ? new DirectoryReference()
				: new FileReference();
		String fullname = entry.getFullName();
		de.path = fullname.startsWith("/") ? fullname.substring(1) : fullname;
		if (ub != null)
			de.link = ub.build(entry.getName());
		return de;
	}

	/** A reference to a directory, done with JAXB. */
	@XmlRootElement(name = "dir")
	@XmlType(name = "")
	public static class DirectoryReference extends DirEntryReference {
	}

	/** A reference to a file, done with JAXB. */
	@XmlRootElement(name = "file")
	@XmlType(name = "")
	public static class FileReference extends DirEntryReference {
	}
}
