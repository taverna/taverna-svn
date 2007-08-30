package net.sf.taverna.t2.cloudone.datamanager.file;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import net.sf.taverna.t2.cloudone.EntityNotFoundException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.ContextualizedIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;

public class FileDataManager /*implements DataManager*/{
	
	private Set<LocationalContext> contexts;
	
	private String namespace;
	
	private File path;
	
	private int counter=0;
	
	public FileDataManager(String namespace, Set<LocationalContext> contexts, File path) {
		this.namespace = namespace;
		this.contexts = contexts;
		this.path = path;
	}

	public <EI extends EntityIdentifier> Entity<EI, ?> getEntity(EI identifier)
			throws EntityNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public DataDocument registerDocument(Set<ReferenceScheme> references) {
		// TODO Auto-generated method stub
		return null;
	}

	public EntityListIdentifier registerEmptyList(int depth) {
		// TODO Auto-generated method stub
		return null;
	}

	public EntityListIdentifier registerList(EntityIdentifier[] identifiers) {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<ContextualizedIdentifier> traverse(
			EntityIdentifier identifier, int desiredDepth) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private EntityListIdentifier nextListIdentifier(int depth) {
		if (depth < 1) {
			throw new IllegalArgumentException("Depth must be at least 1");
		}
		String id = "urn:t2data:list://" + namespace + "/list" + (counter++)
				+ "/" + depth;
		try {
			return new EntityListIdentifier(id);
		} catch (MalformedIdentifierException e) {
			throw new RuntimeException("Malformed list identifier: " + id, e);
		}
	}

}
