package org.taverna.server.master.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.taverna.server.master.DirEntryReference;
import org.taverna.server.master.interfaces.DirectoryEntry;

/**
 * The result of a RESTful operation to list the contents of a directory.
 * 
 * @author Donal Fellows
 */
@XmlRootElement
@XmlType(name="DirectoryContents")
@XmlSeeAlso(MakeOrUpdateDirEntry.class)
public class DirectoryContents {
	@XmlElementRef
	public List<DirEntryReference> contents;

	public DirectoryContents() {
	}

	public DirectoryContents(UriInfo ui, Collection<DirectoryEntry> collection) {
		contents = new ArrayList<DirEntryReference>();
		UriBuilder ub = ui.getAbsolutePathBuilder().path("{path}");
		for (DirectoryEntry e : collection) {
			contents.add(DirEntryReference.newInstance(ub, e));
		}
	}
}
