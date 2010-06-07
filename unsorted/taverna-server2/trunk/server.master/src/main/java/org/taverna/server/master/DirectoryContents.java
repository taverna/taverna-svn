package org.taverna.server.master;

import static org.taverna.server.master.DirEntryReference.makeDirEntryReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

import org.taverna.server.master.DirEntryReference.DirectoryReference;
import org.taverna.server.master.DirEntryReference.FileReference;
import org.taverna.server.master.interfaces.DirectoryEntry;

/**
 * The result of a RESTful operation to list the contents of a directory.
 * 
 * @author Donal Fellows
 */
@XmlRootElement
public class DirectoryContents {
	public List<DirectoryReference> directory;
	public List<FileReference> file;

	public DirectoryContents() {
	}

	DirectoryContents(UriInfo ui, Collection<DirectoryEntry> collection) {
		directory = new ArrayList<DirectoryReference>();
		file = new ArrayList<FileReference>();
		UriBuilder ub = ui.getAbsolutePathBuilder().path("{path}");
		for (DirectoryEntry e : collection) {
			DirEntryReference de = makeDirEntryReference(ub, e);
			if (de instanceof DirectoryReference)
				directory.add((DirectoryReference) de);
			else
				file.add((FileReference) de);
		}
	}
}
