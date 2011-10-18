/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.sadi;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;

import ca.wilkinsonlab.sadi.SADIException;
import ca.wilkinsonlab.sadi.ServiceDescription;
import ca.wilkinsonlab.sadi.beans.RestrictionBean;
import ca.wilkinsonlab.sadi.client.Service;
import ca.wilkinsonlab.sadi.rdfpath.RDFPath;
import ca.wilkinsonlab.sadi.rdfpath.RDFPathElement;
import ca.wilkinsonlab.sadi.utils.LSRNUtils;
import ca.wilkinsonlab.sadi.utils.LabelUtils;
import ca.wilkinsonlab.sadi.utils.OnymizeUtils;
import ca.wilkinsonlab.sadi.utils.OwlUtils;
import ca.wilkinsonlab.sadi.utils.PatternSubstitution;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Utility methods for SADI activities.
 * 
 * @author David Withers
 * @author Luke McCarthy
 */
public class SADIUtils {

	private static final Logger logger = Logger.getLogger(SADIUtils.class);

	private static final PatternSubstitution idPattern = new PatternSubstitution(
			".+([^:#/]+)[:#/](.+)", "$2");

	public static String uriToId(String uri) {
		if (idPattern.matches(uri)) {
			return idPattern.execute(uri);
		}
		return uri;
	}
	
	/**
	 * Returns the default input ports for the specified SADI service.
	 * @return the default input ports for the specified SADI service
	 */
	public static Map<String, RDFPath> getDefaultInputPorts(Service service) {
		NameToPathMap inputPortMap = new NameToPathMap();
		if (LSRNUtils.isLSRNType(service.getInputClassURI())) {
			inputPortMap.put(SADIUtils.getInputClassLabel(service), new RDFPath());
		} else {
			/* TODO use service.getInputRestrictionBeans() to avoid loading
			 * the input class; note that this method does not yet exist... 
			 */
			try {
				for (Restriction r: OwlUtils.listRestrictions(service.getInputClass())) {
					RDFPath path = new RDFPath();
					RDFPathElement element = new RDFPathElement(r);
					path.add(element);
					inputPortMap.put(getDefaultPortName(path), path);
				}
			} catch (SADIException e) {
				logger.error(String.format("error loading input OWL class %s", service.getInputClassURI()));
				String portName = LabelUtils.getDefaultLabel(ResourceFactory.createResource(service.getInputClassURI()));
				inputPortMap.put(portName, new RDFPath());
			}
		}
		return inputPortMap.getOneToOneMap();
	}

	/**
	 * Returns the default output ports for the specified SADI service.
	 * @return the default output ports for the specified SADI service
	 */
	public static Map<String, RDFPath> getDefaultOutputPorts(Service service) {
		NameToPathMap outputPortMap = new NameToPathMap();
		Model model = ModelFactory.createDefaultModel();
		for (RestrictionBean r: service.getRestrictionBeans()) {
			RDFPath path = new RDFPath();
			Property p = model.createProperty(r.getOnPropertyURI());
			if (r.getOnPropertyLabel() != null)
				p.addLiteral(RDFS.label, r.getOnPropertyLabel());
			Resource type = null;
			if (r.getValuesFromURI() != null) {
				type = model.createResource(r.getValuesFromURI());
				if (r.getValuesFromLabel() != null)
					type.addLiteral(RDFS.label, r.getValuesFromLabel());
			}
			RDFPathElement element = new RDFPathElement(p, type);
			path.add(element);
			outputPortMap.put(getDefaultPortName(path), path);
		}
		return outputPortMap.getOneToOneMap();
	}
	
	/**
	 * Returns the default name of a port with the specified RDFPath.
	 * @param path
	 * @return the default name of a port with the specified RDFPath
	 */
	public static String getDefaultPortName(RDFPath path)
	{
		if (path.isEmpty())
			return "port";
		else
			return LabelUtils.getLabel(path.getLastPathElement().getProperty());
	}
	
	/**
	 * Add a name-pathspec pair to the specified map for each name-RDFPath
	 * pair in another map.
	 * @param pathSpecMap the name-pathspec map
	 * @param pathMap the name-RDFPath map
	 */
	public static void addPaths(Map<String, String> pathSpecMap, Map<String, RDFPath> pathMap)
	{
		for (Map.Entry<String, RDFPath> entry: pathMap.entrySet()) {
			pathSpecMap.put(entry.getKey(), entry.getValue().toString());
		}
	}
	
	/**
	 * Return a name-RDFPath map corresponding to the specified
	 * name-pathspec map.
	 * @param pathSpecs the name-pathspec map
	 * @return the name-RDFPath map
	 */
	@SuppressWarnings("unchecked")
	public static Collection<RDFPath> convertPaths(Collection<String> pathSpecs)
	{
		return CollectionUtils.collect(pathSpecs, new Transformer() {
			public Object transform(Object input) {
				return new RDFPath((String)input);
			}
		});
	}

	/**
	 * Convert a name-pathspec map to a name-RDFPath map in the context
	 * of the specified ontology model.
	 * @param pathSpecMap the name-pathspec map
	 * @param ontModel the ontology model
	 * @return the name-RDFPath map
	 */
	public static Map<String, RDFPath> convertPathMap(Map<String, String> pathSpecMap, OntModel ontModel)
	{
		Map<String, RDFPath> pathMap = new HashMap<String, RDFPath>();
		for (String name: pathSpecMap.keySet()) {
			RDFPath path;
			try {
				path = new RDFPath(pathSpecMap.get(name));
			} catch (Exception e) {
				logger.error(String.format("error input port %s from %s", name, pathSpecMap.get(name)), e);
				continue;
			}
			try {
				/* this has the important side effect of connecting the
				 * properties and classes with labels from the OntModel...
				 */
				path = OnymizeUtils.deonymizePath(ontModel, path, "UTF-8");
			} catch (Exception e) {
				logger.error(String.format("error deonymizing path %s", path), e);
			}
			pathMap.put(name, path);
		}
		return pathMap;
	}

	/**
	 * Returns the default name of the port that represents the specified
	 * service's entire input class.
	 * @param service the SADI service
	 * @return the default name of the input class port
	 */
	public static String getInputClassLabel(ServiceDescription service) {
		if (service.getInputClassLabel() != null)
			return service.getInputClassLabel();
		else
			return SADIUtils.getLocalName(service.getInputClassURI());
	}

	/**
	 * Returns the default name of the port that represents the specified 
	 * service's entire output class.
	 * @param service the SADI service
	 * @return the default name of the output class port
	 */
	public static String getOutputClassLabel(Service service) {
		/* Service.getOutputClassLabel() should do something like this;
		 * use it instead once it's fixed...
		 */
		if (service.getOutputClassLabel() != null)
			return service.getOutputClassLabel();
		else
			return SADIUtils.getLocalName(service.getOutputClassURI());
	}

	/**
	 * Returns the local portion of the specified URI.
	 * In practice, this should be whatever is between the fragment (if
	 * there is one) or last path element (if there's no fragment)
	 * and the query string.
	 * Or, as a Perl regexp: /.*[#\/]([^?]*)/ && $1
	 * @param uri
	 * @return
	 */
	public static String getLocalName(String uri) {
		// FIXME this could be done more efficiently...
		return LabelUtils.getDefaultLabel(ResourceFactory.createResource(uri));
	}

	/**
	 * Build a unique name-pathspec map based on a collection of RDFPaths
	 * using the supplied map to store the results.
	 * @param portMap the map in which to store the results
	 * @param paths the collection of RDFPaths
	 * @param emptyPathPortName the name to use for empty RDFPaths
	 */
	public static Map<String, String> buildPortMap(Collection<RDFPath> paths, String emptyPathPortName) {
		NameToPathMap nameToPathMap = new NameToPathMap();
		for (RDFPath path: paths) {
			String name = path.isEmpty() ? emptyPathPortName : getDefaultPortName(path);
			// convert anonymous URIs so the path will survive serialization...
			try {
				path = OnymizeUtils.onymizePath(path, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// UTF-8 shouldn't be unsupported...
				logger.error(String.format("error onymizing path %s", path), e);
			}
			nameToPathMap.put(name, path);
		}
		Map<String, String> portMap = new HashMap<String, String>();
		addPaths(portMap, nameToPathMap.getOneToOneMap());
		return portMap;
	}
	
	/**
	 * Used by {@link SADIUtils.replacePortMap}.
	 * @author Luke McCarthy
	 */
	private static class NameToPathMap extends HashMap<String, Collection<RDFPath>>
	{
		private static final long serialVersionUID = 1L;
		
		public NameToPathMap()
		{
			super();
		}
		
		public void put(String key, RDFPath value)
		{
			getPaths(key).add(value);
		}
		
		public Collection<RDFPath> getPaths(String name)
		{
			if (!containsKey(name))
				put(name, new ArrayList<RDFPath>());
			return get(name);
		}
		
		public void uniquify()
		{
			int from = -1;
			Collection<NamePathPair> changes = new LinkedList<NamePathPair>();
			do {
				for (Iterator<NamePathPair> i = changes.iterator(); i.hasNext(); ) {
					NamePathPair pair = i.next();
					getPaths(pair.name).add(pair.path);
					i.remove();
				}
				from -= 1;
				for (Map.Entry<String, Collection<RDFPath>> entry: entrySet()) {
					if (entry.getValue().size() > 1) {
						for (Iterator<RDFPath> i = entry.getValue().iterator(); i.hasNext(); ) {
							RDFPath path = i.next();
							String newName = makeName(safeTaillist(path, from));
							if (!newName.equals(entry.getKey())) {
								i.remove();
								changes.add(new NamePathPair(newName, path));
							}
						}
					}
				}
			} while (!changes.isEmpty());
			
			/* hopefully this last step will never be necessary...
			 */
			for (Map.Entry<String, Collection<RDFPath>> entry: entrySet()) {
				if (entry.getValue().size() > 1) {
					Iterator<RDFPath> i = entry.getValue().iterator();
					int n=1;
					while (i.hasNext()) {
						RDFPath path = i.next();
						i.remove();
						put(String.format("%s %d", entry.getKey(), n++), path);
					}
				}
			}
		}
		
		public Map<String, RDFPath> getOneToOneMap()
		{
			uniquify();
			Map<String, RDFPath> oneToOneMap = new HashMap<String, RDFPath>();
			for (String name: keySet()) {
				Collection<RDFPath> paths = get(name);
				if (paths.isEmpty())
					continue;
				else if (paths.size() > 1)
					throw new RuntimeException("names not unique after uniquify");
				else
					oneToOneMap.put(name, paths.iterator().next());
			}
			return oneToOneMap;
		}
		
		private static String makeName(List<RDFPathElement> path)
		{
			StringBuilder buf = new StringBuilder();
			for (Iterator<RDFPathElement> i = path.iterator(); i.hasNext();) {
				buf.append(i.next().toRestrictionBean().toString());
				if (i.hasNext())
					buf.append(" ");
			}
			// TODO make safe for Taverna/dot; any idea where to find this info...
			return buf.toString();
		}
		
		private static <E> List<E> safeTaillist(List<E> list, int from)
		{
			int size = list.size();
			from += size; // allow negative index
			if (from < 0)
				from = 0;
			return list.subList(from, size);
		}
		
		private static class NamePathPair
		{
			String name;
			RDFPath path;
			
			public NamePathPair(String name, RDFPath path)
			{
				this.name = name;
				this.path = path;
			}
		}
	}
}
