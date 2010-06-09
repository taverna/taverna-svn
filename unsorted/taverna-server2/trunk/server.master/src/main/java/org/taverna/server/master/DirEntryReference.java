package org.taverna.server.master;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlValue;

import org.taverna.server.master.interfaces.Directory;
import org.taverna.server.master.interfaces.DirectoryEntry;

/**
 * A reference to something that is in a directory below the working directory
 * of a workflow run.
 * 
 * @author Donal Fellows
 */
@XmlRootElement
@XmlSeeAlso( { DirEntryReference.DirectoryReference.class,
		DirEntryReference.FileReference.class })
public abstract class DirEntryReference {
	/**
	 * A link to the entry.
	 */
	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	public URI link;

	/**
	 * The path of the entry.
	 */
	@XmlValue
	public String path;

	public DirEntryReference() {
	}

	// Really returns a subclass, so cannot be constructor
	static DirEntryReference makeDirEntryReference(UriBuilder ub,
			DirectoryEntry entry) {
		DirEntryReference de = (entry instanceof Directory) ? new DirectoryReference()
				: new FileReference();
		String fullname = entry.getFullName();
		de.path = fullname.startsWith("/") ? fullname.substring(1) : fullname;
		if (ub != null)
			de.link = ub.build(entry.getName());
		return de;
	}

	/** A reference to a directory. */
	@XmlRootElement(name = "dir")
	public static class DirectoryReference extends DirEntryReference {
	}

	/** A reference to a file. */
	@XmlRootElement(name = "file")
	public static class FileReference extends DirEntryReference {
	}
}
