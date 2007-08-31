package net.sf.taverna.t2.cloudone.datamanager.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.EntityNotFoundException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManager;
import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManagerTest;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.ContextualizedIdentifier;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;

public class FileDataManager extends AbstractDataManager {
	
	private File path;

	private int counter = 0;

	public FileDataManager(String namespace, Set<LocationalContext> contexts,
			File path) {
		super(namespace, contexts);
		this.path = path;
	}

	public <EI extends EntityIdentifier> Entity<EI, ?> getEntity(EI identifier)
			throws EntityNotFoundException {
		if (identifier instanceof Literal) {
			return (Entity<EI, ?>) identifier;
		}
		return null;
	}

	public DataDocumentIdentifier registerDocument(
			Set<ReferenceScheme> references) {
		// TODO Auto-generated method stub
		return null;
	}

	public EntityListIdentifier registerEmptyList(int depth) {
		EntityListIdentifier id = nextListIdentifier(depth);
		EntityList newList = new EntityList(id, Collections
				.<EntityIdentifier> emptyList());
		// TODO: STORE IT!
		return id;
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

	protected String generateId(IDType type) {
		if (type.equals(IDType.Literal)) {
			throw new IllegalArgumentException("Can't generate IDs for Literal");
		}
		return "urn:t2data:" + type.uripart + "://" + getCurrentNamespace() + "/"
				+ UUID.randomUUID();
	}


	public ErrorDocumentIdentifier registerError(int depth, int implicitDepth,
			String msg, Throwable throwable) {
		// TODO Auto-generated method stub
		return null;
	}

	protected EntityListIdentifier nextListIdentifier(int depth)
			throws IllegalArgumentException {
		if (depth < 1) {
			throw new IllegalArgumentException("Depth must be at least 1");
		}
		String id = generateId(IDType.List) + "/" + depth;
		return new EntityListIdentifier(id);
	}

	protected ErrorDocumentIdentifier nextErrorIdentifier(int depth,
			int implicitDepth) throws IllegalArgumentException {
		if (depth < 0 || implicitDepth < 0) {
			throw new IllegalArgumentException(
					"Depth and implicit depth must be at least 0");
		}
		String id = generateId(IDType.Error) + "/" + depth + "/"
				+ implicitDepth;
		return new ErrorDocumentIdentifier(id);
	}

	public DataDocumentIdentifier nextDataIdentifier() {
		String id = generateId(IDType.Data);
		return new DataDocumentIdentifier(id);
	}



}
