/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: Query.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-06-21 22:49:58 $
 *               by   $Author: davidwithers $
 * Created on 03-Apr-2006
 *****************************************************************/
package org.biomart.martservice.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biomart.martservice.MartServiceException;

/**
 * Class for creating mart queries.
 * 
 * @author David Withers
 */
public class Query {
	private String virtualSchemaName;

	private int count;

	private int uniqueRows;

	private String softwareVersion;

	private String requestId;

	private List datasets = new ArrayList();

	private Map datasetMap = new HashMap();

	private Set links = new HashSet();

	private Map linkSourceMap = new HashMap();

	private List listeners = new ArrayList();

	/**
	 * Constructs an instance of a <code>Query</code> with the specified
	 * <code>virtualSchemaName</code> and a <code>count</code> of 0.
	 * 
	 * @param virtualSchemaName
	 */
	public Query(String virtualSchemaName) {
		this(virtualSchemaName, 0);
	}

	/**
	 * Constructs an instance of a <code>Query</code> with the specified
	 * <code>virtualSchemaName</code> and <code>count</code>.
	 * 
	 * @param virtualSchemaName
	 * @param count
	 */
	public Query(String virtualSchemaName, int count) {
		setVirtualSchemaName(virtualSchemaName);
		setCount(count);
	}

	/**
	 * Constructs an instance of a <code>Query</code> with the specified
	 * <code>virtualSchemaName</code>, <code>softwareVersion</code> and
	 * <code>requestId</code>.
	 * 
	 * @param virtualSchemaName
	 * @param softwareVersion
	 * @param requestId
	 */
	public Query(String virtualSchemaName, String softwareVersion,
			String requestId) {
		this(virtualSchemaName, 0, softwareVersion, requestId);
	}

	/**
	 * Constructs an instance of a <code>Query</code> with the specified
	 * <code>virtualSchemaName</code>, <code>count</code> and
	 * <code>softwareVersion</code>.
	 * 
	 * @param virtualSchemaName
	 * @param count
	 * @param softwareVersion
	 */
	public Query(String virtualSchemaName, int count, String softwareVersion) {
		this(virtualSchemaName, count, softwareVersion, null);
	}

	/**
	 * Constructs an instance of a <code>Query</code> with the specified
	 * <code>virtualSchemaName</code>, <code>count</code>,
	 * <code>softwareVersion</code> and <code>requestId</code>.
	 * 
	 * @param virtualSchemaName
	 * @param count
	 * @param softwareVersion
	 * @param requestId
	 */
	public Query(String virtualSchemaName, int count, String softwareVersion,
			String requestId) {
		setVirtualSchemaName(virtualSchemaName);
		setCount(count);
		setSoftwareVersion(softwareVersion);
		setRequestId(requestId);
	}

	/**
	 * Constructs an instance of a <code>Query</code> which is a deep copy of
	 * another <code>Query</code>.
	 * 
	 * @param query
	 *            the <code>Query</code> to copy
	 * @throws MartServiceException
	 */
	public Query(Query query) {
		setVirtualSchemaName(query.virtualSchemaName);
		setCount(query.count);
		setUniqueRows(query.uniqueRows);
		setSoftwareVersion(query.softwareVersion);
		setRequestId(query.requestId);
		List datasets = query.getDatasets();
		for (Iterator iter = datasets.iterator(); iter.hasNext();) {
			Dataset dataset = (Dataset) iter.next();
			addDataset(new Dataset(dataset));
		}
		Set links = query.getLinks();
		for (Iterator iter = links.iterator(); iter.hasNext();) {
			Link link = (Link) iter.next();
			addLink(new Link(link));
		}
	}

	/**
	 * Returns the virtualSchema.
	 * 
	 * @return the virtualSchema.
	 */
	public String getVirtualSchemaName() {
		return virtualSchemaName;
	}

	/**
	 * Sets the virtualSchema.
	 * 
	 * @param virtualSchemaName
	 *            the virtualSchema to set.
	 */
	public void setVirtualSchemaName(String virtualSchemaName) {
		this.virtualSchemaName = virtualSchemaName;
	}

	/**
	 * Returns the count.
	 * 
	 * @return the count.
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Sets the count.
	 * 
	 * @param count
	 *            the count to set.
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * Returns the uniqueRows query attribute
	 * 
	 * @return the uniqueRows query attribute
	 */
	public int getUniqueRows() {
		return uniqueRows;
	}

	/**
	 * Sets the uniqueRows query attribute.
	 * 
	 * Valid values are 0 or 1.
	 * 
	 * @param uniqueRows value for the uniqueRows query attribute
	 */
	public void setUniqueRows(int uniqueRows) {
		this.uniqueRows = uniqueRows;
	}

	/**
	 * Returns the softwareVersion.
	 * 
	 * @return the softwareVersion
	 */
	public String getSoftwareVersion() {
		return softwareVersion;
	}

	/**
	 * Sets the softwareVersion.
	 * 
	 * @param softwareVersion
	 *            the new softwareVersion
	 */
	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	/**
	 * Returns the requestId.
	 * 
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * Sets the requestId.
	 * 
	 * @param requestId
	 *            the new requestId
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * Adds a Dataset to the Query.
	 * 
	 * The Dataset is added at the end of the list of Datasets.
	 * 
	 * @param dataset
	 *            the Dataset to add
	 */
	public void addDataset(Dataset dataset) {
		addDataset(datasets.size(), dataset);
	}

	/**
	 * Adds a Dataset to the Query at the specified position.
	 * 
	 * @param index
	 *            the position to add the Dataset
	 * @param dataset
	 *            the Dataset to add
	 */
	public void addDataset(int index, Dataset dataset) {
		datasets.add(index, dataset);
		datasetMap.put(dataset.getName(), dataset);
		if (dataset.getContainingQuery() != null) {
			dataset.getContainingQuery().removeDataset(dataset);
		}
		dataset.setContainingQuery(this);
	}

	/**
	 * Removes a Dataset from the Query.
	 * 
	 * @param dataset
	 *            the Dataset to remove
	 */
	public void removeDataset(Dataset dataset) {
		datasets.remove(dataset);
		datasetMap.remove(dataset.getName());
		dataset.setContainingQuery(null);
	}

	/**
	 * Removes all the Datasets from the Query.
	 */
	public void removeAllDatasets() {
		for (Iterator iter = datasets.iterator(); iter.hasNext();) {
			Dataset dataset = (Dataset) iter.next();
			dataset.setContainingQuery(null);
		}
		datasets.clear();
		datasetMap.clear();
	}

	/**
	 * Returns the Datasets that this Query contains.
	 * 
	 * @return the Datasets that this Query contains.
	 */
	public List getDatasets() {
		return new ArrayList(datasets);
	}

	/**
	 * Returns a Dataset with the given datasetName. If the Query has no Dataset
	 * with the given datasetName null is returned.
	 * 
	 * @param datasetName
	 * @return a Dataset with the given datasetName
	 */
	public Dataset getDataset(String datasetName) {
		return (Dataset) datasetMap.get(datasetName);
	}

	/**
	 * Returns true if this Query contains a Dataset with the name
	 * <code>datasetName</code>.
	 * 
	 * @param datasetName
	 * @return true if this Query contains a Dataset with the name
	 *         <code>datasetName</code>.
	 */
	public boolean containsDataset(String datasetName) {
		return datasetMap.containsKey(datasetName);
	}

	/**
	 * Adds a Link to the Query.
	 * 
	 * @param link
	 *            the Link to add
	 */
	public void addLink(Link link) {
		links.add(link);
		linkSourceMap.put(link.getSource(), link);
		if (link.getContainingQuery() != null) {
			link.getContainingQuery().removeLink(link);
		}
		link.setContainingQuery(this);
	}

	/**
	 * Removes a link from the Query
	 * 
	 * @param link
	 *            the Link to remove
	 */
	public void removeLink(Link link) {
		links.remove(link);
		linkSourceMap.remove(link.getSource());
		link.setContainingQuery(null);
	}

	/**
	 * Returns the Links that this Query contains.
	 * 
	 * @return the Links that this Query contains.
	 */
	public Set getLinks() {
		return new HashSet(links);
	}

	/**
	 * Returns a Link with the given source. If the Query has no Link with the
	 * given source null is returned.
	 * 
	 * @param source
	 *            the source of the link
	 * @return a Link with the given source
	 */
	public Link getLink(String source) {
		return (Link) linkSourceMap.get(source);
	}

	/**
	 * Returns true if this Query contains a Link with the same source.
	 * 
	 * @param source
	 *            the source of the link
	 * @return true if this Query contains a Link with the same source.
	 */
	public boolean containsLink(String source) {
		return linkSourceMap.containsKey(source);
	}

	/**
	 * Returns a List of Links with the given target. If the Query has no Link
	 * with the given target an empty List is returned.
	 * 
	 * @param target
	 *            the target of the link
	 * @return a Link with the given target
	 */
	public Set getLinks(String target) {
		Set result = new HashSet();
		Set links = getLinks();
		for (Iterator iter = links.iterator(); iter.hasNext();) {
			Link link = (Link) iter.next();
			if (link.getTarget().equals(target)) {
				result.add(link);
			}
		}
		return result;
	}

	/**
	 * Returns all the Attributes from all the Datasets in this Query.
	 * 
	 * @return all the Attributes from all the Datasets in this Query.
	 */
	public List getAttributes() {
		List attributes = new ArrayList();
		for (Iterator iter = datasets.iterator(); iter.hasNext();) {
			Dataset dataset = (Dataset) iter.next();
			attributes.addAll(dataset.getAttributes());
		}
		return attributes;
	}

	/**
	 * Returns all the Filters from all the Datasets in this Query.
	 * 
	 * @return all the Filters from all the Datasets in this Query.
	 */
	public List getFilters() {
		List attributes = new ArrayList();
		for (Iterator iter = datasets.iterator(); iter.hasNext();) {
			Dataset dataset = (Dataset) iter.next();
			attributes.addAll(dataset.getFilters());
		}
		return attributes;
	}

	/**
	 * Adds the specified query listener to receive query events. If
	 * <code>listener</code> is null, no exception is thrown and no action is
	 * performed.
	 * 
	 * @param listener
	 *            the query listener
	 */
	public void addQueryListener(QueryListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes the specified query listener so that it no longer receives query
	 * events. This method performs no function, nor does it throw an exception,
	 * if <code>listener</code> was not previously added to this component. If
	 * <code>listener</code> is null, no exception is thrown and no action is
	 * performed.
	 * 
	 * @param listener
	 *            the query listener
	 */
	public void removeQueryListener(QueryListener listener) {
		listeners.remove(listener);
	}

	void fireAttributeAdded(Attribute attribute, Dataset dataset) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			QueryListener listener = (QueryListener) iter.next();
			listener.attributeAdded(attribute, dataset);
		}
	}

	void fireAttributeRemoved(Attribute attribute, Dataset dataset) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			QueryListener listener = (QueryListener) iter.next();
			listener.attributeRemoved(attribute, dataset);
		}
	}

	void fireFilterAdded(Filter filter, Dataset dataset) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			QueryListener listener = (QueryListener) iter.next();
			listener.filterAdded(filter, dataset);
		}
	}

	void fireFilterRemoved(Filter filter, Dataset dataset) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			QueryListener listener = (QueryListener) iter.next();
			listener.filterRemoved(filter, dataset);
		}
	}

	void fireFilterChanged(Filter filter, Dataset dataset) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			QueryListener listener = (QueryListener) iter.next();
			listener.filterChanged(filter, dataset);
		}
	}

}
