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
 * Filename           $RCSfile: MartService.java,v $errors/failure.html
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-01-31 14:12:04 $
 *               by   $Author: davidwithers $
 * Created on 17-Mar-2006
 *****************************************************************/
package org.biomart.martservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.biomart.martservice.query.Query;
import org.ensembl.mart.lib.config.ConfigurationException;
import org.ensembl.mart.lib.config.DatasetConfig;
import org.ensembl.mart.lib.config.DatasetConfigXMLUtils;
import org.ensembl.mart.lib.config.Exportable;
import org.ensembl.mart.lib.config.Importable;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

/**
 * A class for interacting with a BioMart web service.
 * 
 * @author David Withers
 */
public class MartService {
	private static Logger logger = Logger.getLogger(MartServiceUtils.class);

	private String location;

	private String requestId;

	private File cacheDirectory;

	private MartRegistry registry;

	private Map versionMap = new HashMap();

	private Map datasetsMap = new HashMap();

	private Map datasetConfigMap = new HashMap();

	private Map importablesMap = new HashMap();

	private Map exportablesMap = new HashMap();

	private Map linkableDatasetsMap = new HashMap();

	private Map datasetToLinkSetMap = new HashMap();

	private Map datasetsToPathMap = new HashMap();

	private static final Map martServiceMap = new HashMap();

	private static final String fs = System.getProperty("file.separator");

	private boolean linksCalculated = false;

	/**
	 * Constructs an instance of a <code>MartService</code> with the specified
	 * location.
	 * 
	 * The location must be the URL of a valid BioMart MartService, e.g.
	 * http://www.biomart.org/biomart/martservice
	 * 
	 * @param location
	 *            the URL of the MartService
	 */
	private MartService(String location) {
		this.location = location;
	}

	/**
	 * Returns a <code>MartService</code> for the specified location. If a
	 * <code>MartService</code> does not exist for the location a new one is
	 * constructed.
	 * 
	 * The location must be the URL of a valid BioMart MartService, e.g.
	 * http://www.biomart.org/biomart/martservice
	 * 
	 * @param location
	 *            the URL of the MartService
	 */
	public static MartService getMartService(String location) {
		if (!martServiceMap.containsKey(location)) {
			martServiceMap.put(location, new MartService(location));
		}
		return (MartService) martServiceMap.get(location);
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
	 * @param requestId the new requestId
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * Returns the cacheDirectory.
	 *
	 * @return the cacheDirectory
	 */
	public File getCacheDirectory() {
		return cacheDirectory;
	}

	/**
	 * Sets the cacheDirectory.
	 *
	 * @param cacheDirectory the new cacheDirectory
	 */
	public void setCacheDirectory(File cacheDirectory) {
		this.cacheDirectory = cacheDirectory;
	}

	/**
	 * Returns the URL of the MartService.
	 * 
	 * @return the URL of the MartService
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Removes any cached items.
	 */
	public void clearCache() {
		registry = null;
		datasetsMap.clear();
		datasetConfigMap.clear();
		importablesMap.clear();
		exportablesMap.clear();
	}

	/**
	 * Returns the MartRegistry for this MartService.
	 * 
	 * @return the MartRegistry for this MartService
	 * @throws MartServiceException
	 *             if the MartService returns an error or is unavailable
	 */
	public MartRegistry getRegistry() throws MartServiceException {
		if (registry == null) {
			registry = MartServiceUtils.getRegistry(location, requestId);
		}
		return registry;
	}

	public String getVersion(MartURLLocation martURLLocation)
			throws MartServiceException {
		if (!versionMap.containsKey(martURLLocation)) {
			versionMap.put(martURLLocation, MartServiceUtils.getVersion(
					location, requestId, martURLLocation));
		}
		return (String) versionMap.get(martURLLocation);
	}

	/**
	 * Returns all the datasets available from this MartService.
	 * 
	 * @return all the datasets available from this MartService.
	 * @throws MartServiceException
	 *             if the MartService returns an error or is unavailable
	 */
	public MartDataset[] getDatasets() throws MartServiceException {
		List datasets = new ArrayList();
		MartURLLocation[] locations = getRegistry().getMartURLLocations();
		for (int i = 0; i < locations.length; i++) {
			datasets.addAll(Arrays.asList(getDatasets(locations[i])));
		}
		return (MartDataset[]) datasets
				.toArray(new MartDataset[datasets.size()]);
	}

	/**
	 * Returns the datasets belonging to the virtualSchema.
	 * 
	 * @param virtualSchema
	 *            the virtual schema to include datasets from.
	 * @return the datasets belonging to the virtualSchema.
	 * @throws MartServiceException
	 *             if the MartService returns an error or is unavailable
	 */
	public MartDataset[] getDatasets(String virtualSchema)
			throws MartServiceException {
		List datasets = new ArrayList();
		MartURLLocation[] locations = getRegistry().getMartURLLocations();
		for (int i = 0; i < locations.length; i++) {
			if (virtualSchema == null
					|| virtualSchema.equals(locations[i].getVirtualSchema())) {
				datasets.addAll(Arrays.asList(getDatasets(locations[i])));
			}
		}
		return (MartDataset[]) datasets
				.toArray(new MartDataset[datasets.size()]);
	}

	/**
	 * Returns the datasets specified by martURLLocation.
	 * 
	 * @param martURLLocation
	 *            where to find the datasets.
	 * @return the datasets specified by martURLLocation.
	 * @throws MartServiceException
	 *             if the MartService returns an error or is unavailable
	 */
	public MartDataset[] getDatasets(MartURLLocation martURLLocation)
			throws MartServiceException {
		String name = martURLLocation.getName();
		if (!datasetsMap.containsKey(name)) {
			datasetsMap.put(name, MartServiceUtils.getDatasets(location, requestId,
					martURLLocation));
		}
		return (MartDataset[]) datasetsMap.get(name);
	}

	/**
	 * Returns the dataset specified by a virtualSchema and a dataset name.
	 * 
	 * @param virtualSchema
	 *            the virtualSchema containing the dataset
	 * @param datasetName
	 *            the name of the dataset to return
	 * @return a dataset
	 * @throws MartServiceException
	 *             if the MartService returns an error or is unavailable
	 */
	public MartDataset getDataset(String virtualSchema, String datasetName)
			throws MartServiceException {
		MartDataset result = null;
		MartDataset[] datasets = getDatasets(virtualSchema);
		for (int i = 0; i < datasets.length; i++) {
			if (datasetName.equals(datasets[i].getName())) {
				result = datasets[i];
				break;
			}
		}
		return result;
	}

	/**
	 * Returns the configuration for a dataset.
	 * 
	 * @param dataset
	 *            the dataset to get the configuration for
	 * @return the configuration for a dataset
	 * @throws MartServiceException
	 *             if the MartService returns an error or is unavailable
	 */
	public DatasetConfig getDatasetConfig(MartDataset dataset)
			throws MartServiceException {
		String qualifiedName = dataset.getQualifiedName();
		DatasetConfig datasetConfig;
		if (!datasetConfigMap.containsKey(qualifiedName)) {
			if (dataset.getModified() != null && cacheDirectory != null)	{
				datasetConfig = getDatasetConfigFromCache(dataset);
			} else {
				datasetConfig = MartServiceUtils.getDatasetConfig(location, requestId,
						dataset);	
			}
			datasetConfigMap.put(qualifiedName,
					new SoftReference(datasetConfig));
		} else {
			SoftReference ref = (SoftReference) datasetConfigMap
					.get(qualifiedName);
			datasetConfig = (DatasetConfig) ref.get();
			if (datasetConfig == null) {
				if (dataset.getModified() != null && cacheDirectory != null)	{
					datasetConfig = getDatasetConfigFromCache(dataset);
				} else {
					datasetConfig = MartServiceUtils.getDatasetConfig(location, requestId,
							dataset);	
				}
				datasetConfigMap.put(qualifiedName, new SoftReference(
						datasetConfig));
			}

		}
		return datasetConfig;
	}

	private DatasetConfig getDatasetConfigFromCache(MartDataset dataset) throws MartServiceException {
		DatasetConfig datasetConfig = null;
		MartURLLocation mart = dataset.getMartURLLocation();
		String path = mart.getHost()+fs+mart.getName()+fs+mart.getVirtualSchema();
		File martCacheDir = new File(cacheDirectory, path);
		martCacheDir.mkdirs();
		File cache = new File (martCacheDir, dataset.getName());
		DatasetConfigXMLUtils datasetConfigXMLUtils = new DatasetConfigXMLUtils(true);
		if (cache.exists()) {
			try {
				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(new InputSource(new FileInputStream(cache)));

//			    Document doc = datasetConfigXMLUtils.getDocumentForXMLStream(new FileInputStream(cache));

				datasetConfig = datasetConfigXMLUtils.getDatasetConfigForDocument(doc);
				datasetConfigXMLUtils.loadDatasetConfigWithDocument(datasetConfig,
						doc);
				if (!datasetConfig.getModified().equals(dataset.getModified())) {
					datasetConfig = null;
				}
			} catch (IOException e) {
				logger.debug("error reading cache from " + cache.getPath(), e);
				datasetConfig = null;
			} catch (ConfigurationException e) {
				logger.debug("error parsing from " + cache.getPath(), e);
				datasetConfig = null;
			} catch (JDOMException e) {
				logger.debug("error parsing from " + cache.getPath(), e);
				datasetConfig = null;
			}
		}
		if (datasetConfig == null) {
			datasetConfig = MartServiceUtils.getDatasetConfig(location, requestId, dataset);	
			try {
				datasetConfigXMLUtils.writeDatasetConfigToFile(datasetConfig, cache);
			} catch (ConfigurationException e) {
				logger.debug("error writing cache to " + cache.getPath(), e);
			}
		}
		return datasetConfig;
	}

	/**
	 * Returns the importables for a dataset.
	 * 
	 * @param dataset
	 * @return the importables for a dataset
	 * @throws MartServiceException
	 *             if the MartService returns an error or is unavailable
	 */
	public Importable[] getImportables(MartDataset dataset)
			throws MartServiceException {
		String qualifiedName = dataset.getQualifiedName();
		if (!importablesMap.containsKey(qualifiedName)) {
			importablesMap.put(qualifiedName, getDatasetConfig(dataset)
					.getImportables());
		}
		return (Importable[]) importablesMap.get(qualifiedName);
	}

	/**
	 * Returns the exportables for a dataset.
	 * 
	 * @param dataset
	 * @return the exportables for a dataset
	 * @throws MartServiceException
	 *             if the MartService returns an error or is unavailable
	 */
	public Exportable[] getExportables(MartDataset dataset)
			throws MartServiceException {
		String qualifiedName = dataset.getQualifiedName();
		if (!exportablesMap.containsKey(qualifiedName)) {
			exportablesMap.put(qualifiedName, getDatasetConfig(dataset)
					.getExportables());
		}
		return (Exportable[]) exportablesMap.get(qualifiedName);
	}

	/**
	 * Sends a <code>Query</code> to the MartService and returns the results
	 * of executing the query.
	 * 
	 * The results are returned as an array of lists; one list for each
	 * attribute specified in the query.
	 * 
	 * @param query
	 *            the query to execute
	 * @return the results of executing the query
	 * @throws MartServiceException
	 *             if the MartService returns an error or is unavailable
	 */
	public List[] executeQuery(Query query) throws MartServiceException {
//		System.out.println(MartServiceUtils.queryToXML(query));
//		System.out.println(MartServiceUtils.queryToXML(MartServiceUtils.splitAttributeLists(query)));
		return MartServiceUtils.getResults(location, requestId, query);
	}

	/**
	 * Returns a list of datasets that can be linked to the specified dataset.
	 * 
	 * @param martDataset
	 * @return datasets that can be linked to the specified dataset
	 * @throws MartServiceException
	 */
	public List getLinkableDatasets(MartDataset martDataset)
			throws MartServiceException {
		if (!linkableDatasetsMap.containsKey(martDataset)) {
			List linkableDatasets = new ArrayList();

			Set importableSet = new HashSet();
			Importable[] importables = getImportables(martDataset);
			for (int i = 0; i < importables.length; i++) {
				importableSet.add(importables[i].getLinkName());
			}

			MartDataset[] datasets = getDatasets(martDataset.getVirtualSchema());
			for (int j = 0; j < datasets.length; j++) {
				if (datasets[j].isVisible()
						&& !datasets[j].getName().equals(martDataset.getName())) {
					DatasetLink datasetLink = new DatasetLink(datasets[j],
							martDataset);
					Exportable[] exportables = getExportables(datasets[j]);
					for (int k = 0; k < exportables.length; k++) {
						String link = exportables[k].getLinkName();
						if (importableSet.contains(link)) {
							datasetLink.addLink(link);
						}
					}
					if (datasetLink.hasLinks()) {
						linkableDatasets.add(datasetLink);
					}
				}
			}
			linkableDatasetsMap.put(martDataset, linkableDatasets);
		}

		return (List) linkableDatasetsMap.get(martDataset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null) {
			MartService other = (MartService) obj;
			if (location == null) {
				result = other.location == null;
			} else {
				result = location.equals(other.location);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return location.hashCode();
	}

	public boolean linksCalculated() {
		return linksCalculated;
	}

	// java implementation of the perl stuff that calculates links.
	// all this should go for the next version of biomart.

	public void calculateLinks() throws MartServiceException {
		synchronized (location) {
			if (!linksCalculated) {
				datasetToLinkSetMap.clear();
				datasetsToPathMap.clear();

				MartDataset[] allDatasets = getDatasets();
				for (int i = 0; i < allDatasets.length; i++) {
					MartDataset[] datasets = getDatasets(allDatasets[i]
							.getVirtualSchema());
					for (int j = 0; j < datasets.length; j++) {
						if (!allDatasets[i].getName().equals(
								datasets[j].getName())) {
							linkDatasets(allDatasets[i], datasets[j]);
						}
					}
				}
				for (int i = 0; i < allDatasets.length; i++) {
					datasetsToPathMap.put(allDatasets[i],
							dijkstra(allDatasets[i]));
				}
				linksCalculated = true;
			}
		}
	}

	public List getPath(MartDataset source, MartDataset target) {
		List path = new ArrayList();

		Map pathMap = (Map) datasetsToPathMap.get(source);

		MartDataset currentDataset = target;

		while (currentDataset != null) {
			path.add(0, currentDataset);
			currentDataset = (MartDataset) pathMap.get(currentDataset);
		}

		if (path.size() < 2 || !path.get(0).equals(source)
				|| !path.get(path.size() - 1).equals(target)) {
			return null;
		}

		return path;
	}

	public DatasetLink getLinkBetween(MartDataset exportingDataset,
			MartDataset importingDataset) {
		Set links = (Set) datasetToLinkSetMap.get(exportingDataset);
		for (Iterator iter = links.iterator(); iter.hasNext();) {
			DatasetLink link = (DatasetLink) iter.next();
			MartDataset targetDataset = link.getTargetDataset();
			if (importingDataset.equals(targetDataset)) {
				return link;
			}
		}

		return null;
	}

	public List getLinksFrom(MartDataset dataset) {
		List linksFrom = new ArrayList();
		Set links = (Set) datasetToLinkSetMap.get(dataset);
		if (links != null) {
			for (Iterator iter = links.iterator(); iter.hasNext();) {
				DatasetLink link = (DatasetLink) iter.next();
				if (link.getSourceDataset().equals(dataset)) {
					linksFrom.add(link);
				}
			}
		}
		return linksFrom;
	}

	public void linkDatasets(MartDataset source, MartDataset target)
			throws MartServiceException {
		DatasetLink datasetLink = new DatasetLink(source, target);
		Importable[] importables = getImportables(target);
		for (int i = 0; i < importables.length; i++) {
			Exportable[] exportables = getExportables(source);
			for (int j = 0; j < exportables.length; j++) {
				if (importables[i].getLinkName().equals(
						exportables[j].getLinkName())) {
					String importVersion = importables[i].getLinkVersion();
					String exportVersion = exportables[j].getLinkVersion();
					if (importVersion != null && exportVersion != null) {
						if (importVersion.equals(exportVersion)) {
							datasetLink.addLink(importables[i].getLinkName());
						}
					} else {
						datasetLink.addLink(importables[i].getLinkName());
					}
				}
			}
		}
		if (datasetLink.hasLinks()) {
			if (!datasetToLinkSetMap.containsKey(source)) {
				datasetToLinkSetMap.put(source, new HashSet());
			}
			if (!datasetToLinkSetMap.containsKey(target)) {
				datasetToLinkSetMap.put(target, new HashSet());
			}
			((Set) datasetToLinkSetMap.get(source)).add(datasetLink);
			((Set) datasetToLinkSetMap.get(target)).add(datasetLink);
		}
	}

	public Map dijkstra(MartDataset dataset) throws MartServiceException {
		Map path = new HashMap();
		LinkedList vertices = new LinkedList(Arrays.asList(getDatasets(dataset
				.getVirtualSchema())));
		Map dist = new HashMap();
		for (Iterator iter = vertices.iterator(); iter.hasNext();) {
			dist.put((MartDataset) iter.next(), new Integer(10000));
		}

		dist.put(dataset, new Integer(0));

		while (vertices.size() > 0) {
			int min_vert_idx = 0;
			MartDataset min_vert = (MartDataset) vertices.get(min_vert_idx);
			int min_dist = ((Integer) dist.get(min_vert)).intValue();

			for (int vertex_idx = 0; vertex_idx < vertices.size(); vertex_idx++) {
				MartDataset vertex = (MartDataset) vertices.get(vertex_idx);
				if (((Integer) dist.get(vertex)).intValue() < min_dist) {
					min_vert_idx = vertex_idx;
					min_vert = vertex;
					min_dist = ((Integer) dist.get(vertex)).intValue();
				}
			}

			if (min_dist == 10000) {
				// Exhausted a disjoint set of datasets.
				break;
			}

			vertices.remove(min_vert_idx);

			List edges = getLinksFrom(min_vert);
			for (Iterator iter = edges.iterator(); iter.hasNext();) {
				DatasetLink edge = (DatasetLink) iter.next();
				MartDataset vertex = edge.getTargetDataset();

				if (((Integer) dist.get(vertex)).intValue() > ((Integer) dist
						.get(min_vert)).intValue() + 1) {
					dist.put(vertex, new Integer(((Integer) dist.get(min_vert))
							.intValue() + 1));
					path.put(vertex, min_vert);
				}
			}
		}

		return path;
	}

}
