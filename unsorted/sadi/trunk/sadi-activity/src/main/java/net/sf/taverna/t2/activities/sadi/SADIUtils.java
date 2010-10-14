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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

import ca.wilkinsonlab.sadi.utils.LSRNUtils;
import ca.wilkinsonlab.sadi.utils.OwlUtils;
import ca.wilkinsonlab.sadi.utils.PatternSubstitution;
import ca.wilkinsonlab.sadi.utils.ResourceFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Utility methods for SADI activities.
 * 
 * @author David Withers
 */
public class SADIUtils {

	private static final Logger logger = Logger.getLogger(SADIUtils.class);

	private static final PatternSubstitution idPattern = new PatternSubstitution(
			".+([^:#/]+)[:#/](.+)", "$2");

	private static final String NEW_LINE = System.getProperty("line.separator");

	public static String uriToId(String uri) {
		if (idPattern.matches(uri)) {
			return idPattern.execute(uri);
		}
		return uri;
	}

	public static String printTree(RestrictionNode node) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent(node.getLevel()));
		sb.append((node.isExclusive() ? "-" : "+") + node.toString() + (node.isSelected() ? "" : "!"));
		sb.append(NEW_LINE);
		for (RestrictionNode child : node.getChildren()) {
			sb.append(printTree(child));
		}
		return sb.toString();
	}

	public static String printTree(DefaultMutableTreeNode node) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent(node.getLevel()));
		sb.append(node.toString());
		sb.append(NEW_LINE);
		Enumeration<?> enumeration = node.children();
		while (enumeration.hasMoreElements()) {
			sb.append(printTree((DefaultMutableTreeNode) enumeration.nextElement()));
		}
		return sb.toString();
	}
	
	public static void printTreeValues(String id, RestrictionNode node) {
		System.out.print(indent(node.getLevel()));
		System.out.println(node.getOntClass().getLocalName());
		System.out.print(indent(node.getLevel()) + " ");
		System.out.println(node.getValues(id));
		for (RestrictionNode child : node.getChildren()) {
			printTreeValues(id, child);
		}
	}

	public static String indent(int size) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append("  ");
		}
		return sb.toString();
	}

	public static String toString(RDFNode node) {
		String string;
		if (node.isLiteral()) {
			string = ((Literal) node).getLexicalForm();
		} else {
			Model model = ResourceUtils.reachableClosure((Resource) node);
			StringWriter stringWriter = new StringWriter();
			model.write(stringWriter, "RDF/XML-ABBREV");
			string = stringWriter.toString();
		}
		return string;
	}

	public static int getMinimumDepth(List<List<String>> paths) {
		int result = paths.size() > 0 ? paths.get(0).size() : 1;
		for (List<String> list : paths) {
			result = Math.min(result, list.size() - 1);
		}
		return result;
	}

	/**
	 * Returns the paths to the first children of the given
	 * RestrictionNode. If a RestrictionNode is exclusive the first sibling is
	 * chosen. If an input/output class is an LSRN class, just use that
	 * for backwards compatibility.
	 * 
	 * @param node
	 *            the root node of the restriction tree
	 * @return the paths to the leaf nodes from the given node
	 */
	public static List<List<String>> getDefaultRestrictionPaths(RestrictionNode node) {
		return getDefaultRestrictionPaths(node, new ArrayList<String>());
	}

	private static List<List<String>> getDefaultRestrictionPaths(RestrictionNode node,
			List<String> path) {
		List<List<String>> defaultRestrictionPaths = new ArrayList<List<String>>();
		path.add(node.toString());
		if (node.getChildCount() == 0 || LSRNUtils.isLSRNType(node.getOntClass())) {
			defaultRestrictionPaths.add(path);
		} else {
			for (RestrictionNode child: node.getChildren()) {
				List<String> newPath = new ArrayList<String>(path);
				newPath.add(child.toString());
				defaultRestrictionPaths.add(newPath);
			}
		}
//		if (node.isLeaf()) {
//			defaultRestrictionPaths.add(path);
//		} else {
//			boolean exclusiveSelected = false;
//			for (RestrictionNode child : node.getChildren()) {
//				if (child.isExclusive()) {
//					if (!exclusiveSelected) {
//						List<String> newPath = new ArrayList<String>();
//						newPath.addAll(path);
//						defaultRestrictionPaths.addAll(getDefaultRestrictionPaths(child, newPath));
//						exclusiveSelected = true;
//					}
//				} else {
//					List<String> newPath = new ArrayList<String>();
//					newPath.addAll(path);
//					defaultRestrictionPaths.addAll(getDefaultRestrictionPaths(child, newPath));
//				}
//			}
//		}
		return defaultRestrictionPaths;
	}

	public static void makeNodesNamesUnique(RestrictionNode node) {
		makeNodesNamesUnique(node, new HashSet<String>());
	}
	
	public static void makeNodesNamesUnique(RestrictionNode node, Set<String> nodeNames) {
		String name = uniqueNodeName(node.getName(), nodeNames);
		node.setName(name);
		nodeNames.add(name);
		for (RestrictionNode restrictionNode : node.getChildren()) {
			makeNodesNamesUnique(restrictionNode, nodeNames);
		}
	}
	
	public static String uniqueNodeName(String suggestedName, Set<String> existingNames) {
		String candidateName = suggestedName;
		long counter = 2;
		while (existingNames.contains(candidateName)) {
			candidateName = suggestedName + "_" + counter++;
		}
		return candidateName;
	}
	
	/**
	 * Returns the {@link RestrictionNode} at the end of the specified path,
	 * using the specified RestrictionNode as the starting point. If the path
	 * does not specify a RestrictionNode in the tree <code>null</code> is returned.
	 * 
	 * @param node
	 *            the root node of the restriction tree
	 * @param path
	 *            the path to the required node
	 * @return the restriction at the end of the specified path
	 */
	public static RestrictionNode getRestriction(RestrictionNode node, List<String> path) {
		RestrictionNode restriction = null;
		if (node.toString().equals(path.get(0))) {
			if (path.size() == 1) {
				restriction = node;
			} else {
				for (RestrictionNode child : node.getChildren()) {
					restriction = getRestriction(child, path.subList(1, path.size()));
					if (restriction != null) {
						break;
					}
				}
			}
		}
		return restriction;
	}

	/**
	 * Builds a tree of {@link RestrictionNode}s from a SADI output class. The
	 * input class is required to remove the input restrictions from the
	 * resulting restriction tree.
	 * 
	 * @param inputClass
	 *            the input class of the SADI service
	 * @param outputClass
	 *            the output class of the SADI service
	 * @return a tree of {@link RestrictionNode}s
	 * @deprecated
	 */
	public static RestrictionNode buildOutputRestrictionTree(OntClass inputClass,
			OntClass outputClass) {
		RestrictionNode node = new RestrictionNode(outputClass);
		buildRestrictionTree(outputClass, new HashSet<OntClass>(), node,
				new HashMap<OntProperty, RestrictionNode>(), OwlUtils.listRestrictions(inputClass),
				false);
		makeNodesNamesUnique(node);
		return node;
	}

	/**
	 * Builds a tree of {@link RestrictionNode}s from a SADI input class.
	 * 
	 * @param inputClass
	 *            the input class of the SADI service
	 * @return a tree of {@link RestrictionNode}s
	 * @deprecated
	 */
	public static RestrictionNode buildInputRestrictionTree(OntClass inputClass) {
		RestrictionNode node = new RestrictionNode(inputClass);
		buildRestrictionTree(inputClass, new HashSet<OntClass>(), node,
				new HashMap<OntProperty, RestrictionNode>(), new HashSet<Restriction>(), false);
		makeNodesNamesUnique(node);
		return node;
	}

	public static RestrictionNode buildRestrictionTree(OntClass root, OntClass relativeTo, List<List<String>> selectedPaths) {
		RestrictionNode rootNode = new RestrictionNode(root);
		buildChildren(rootNode, relativeTo);
		for (List<String> path: selectedPaths) {
			buildRestrictionTreeForPath(rootNode, path);
		}
		makeNodesNamesUnique(rootNode);
		return rootNode;
	}
	
	public static void buildRestrictionTreeForPath(RestrictionNode node, List<String> path) {
		if (path.isEmpty())
			return;
		
		if (node.toString().equals(path.get(0))) {
			if (!node.isExpanded()) {
				buildChildren(node);
			}
			for (RestrictionNode child: node.getChildren()) {
				buildRestrictionTreeForPath(child, path.subList(1, path.size()));
			}
		}
	}
	
	public static void buildChildren(RestrictionNode node) {
		buildChildren(node, null);
	}
	public static void buildChildren(RestrictionNode node, OntClass relativeTo) {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("building children for %s", node));
		}
		for (Restriction r: LocalOwlUtils.listRestrictions(node.getOntClass(), relativeTo)) {
			RestrictionNode child = new RestrictionNode(r.getOnProperty(), LocalOwlUtils.getValuesFromAsClass(r, true));
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("\tadding child %s", child));
			}
			node.add(child);
		}
		node.setExpanded(true);
	}
	
	private static void buildRestrictionTree(OntClass clazz, Set<OntClass> seenClasses,
			RestrictionNode node, Map<OntProperty, RestrictionNode> seenProperties,
			Set<Restriction> ignore, boolean exclusive) {
		if (seenClasses.contains(clazz) || clazz.equals(OWL.Thing) || LSRNUtils.isLSRNType(clazz)) {
			System.out.println(String.format("bailing from recursion into '%s'", OwlUtils.getLabel(clazz)));
			return;
		}
		System.out.println(String.format("visiting '%s' (%s)", OwlUtils.getLabel(clazz), clazz));
		System.out.println(String.format("classes already seen: %s", seenClasses));
		seenClasses.add(clazz);
		
		for (Restriction r: OwlUtils.listRestrictions(clazz)) {
			if (ignore.contains(r))
				continue;
			
			OntProperty property = r.getOnProperty();
			if (property == null) {
				// TOOD this is problematic; create OwlUtils.getOnProperty(r)...
			}
			OntClass valuesFrom = LocalOwlUtils.getValuesFromAsClass(r, false);
			if (valuesFrom != null) {
				RestrictionNode restrictionNode = new RestrictionNode(property, valuesFrom, exclusive);
				// check if we've already seen a restriction on this
				// property
				if (seenProperties.containsKey(property)) {
					RestrictionNode existingRestrictionNode = seenProperties.get(property);
					OntClass existingRestrictionClass = existingRestrictionNode.getOntClass();
					if (existingRestrictionClass.hasSubClass(valuesFrom)) {
						node.remove(existingRestrictionNode);
						node.add(restrictionNode);
						seenProperties.put(property, restrictionNode);
					} else if (existingRestrictionClass.hasSuperClass(valuesFrom)) {
						// do nothing
					} else {
						node.add(restrictionNode);
						// TODO do we need a set of restrictions here?
						seenProperties.put(property, restrictionNode);
					}
				} else {
					node.add(restrictionNode);
					seenProperties.put(property, restrictionNode);
				}
				HashSet<OntClass> seenClassesCopy = new HashSet<OntClass>();
				seenClassesCopy.addAll(seenClasses);
				System.out.println(String.format("recursing into '%s' ('%s' has restriction on '%s'):", OwlUtils.getLabel(valuesFrom), OwlUtils.getLabel(clazz), OwlUtils.getLabel(property)));
				buildRestrictionTree(valuesFrom, seenClassesCopy, restrictionNode,
						new HashMap<OntProperty, RestrictionNode>(), ignore, false);
			}
		}
	}

	/**
	 * Returns a list of {@link Resource}s created from values attached to the
	 * restriction tree.
	 * 
	 * @param node
	 *            the root node of the restriction tree
	 * @param id
	 *            the URI used to create the root Resource
	 * @return a list of {@link Resource}s created from values attached to the
	 *         restriction tree
	 * @throws IOException
	 */
	public static List<Resource> getInputResources(RestrictionNode node, String id)
			throws IOException {
		List<Resource> inputResources = new ArrayList<Resource>();
		Model model = ModelFactory.createDefaultModel();
		OntClass ontClass = node.getOntClass();

		if (node.getValues(id) == null) {
			Resource individidual = model.createResource(id, ontClass);
			inputResources.add(individidual);
			for (RestrictionNode child : node.getChildren()) {
				if (child.isSelected()) {
					while (addInputResources(child, individidual, id));
				}
			}
		} else {
			for (Object value : node.getValues(id)) {
				if (value instanceof RDFNode) {
					RDFNode rdfNode = (RDFNode) value;
					if (rdfNode.isURIResource()) {
						inputResources.add(model.createResource(rdfNode.asNode().getURI(), ontClass));
					} else if (rdfNode.isResource()) {
//						inputResources.add((Resource) rdfNode.inModel(model));
						inputResources.add((Resource) rdfNode);
						model.add(ResourceUtils.reachableClosure((Resource) rdfNode));
					}
				} else if (value instanceof String) {
					String string = (String) value;
					if (isAbsoluteURI(string)) {
						inputResources.add(model.createResource(string, node.getOntClass()));
					} else {
						inputResources.add(ResourceFactory.createInstance(model, node.getOntClass(), string));
					}
				}
			}
		}
		if (logger.isDebugEnabled()) {
			StringWriter stringWriter = new StringWriter();
			model.write(stringWriter, "RDF/XML-ABBREV");
			logger.debug(stringWriter);
		}
		return inputResources;
	}

	private static boolean addInputResources(RestrictionNode node, Resource resource, String id)
			throws IOException {
		boolean result = false;
		if (node.getValues(id) == null && node.isSelected()) {
			Resource type = resource.getModel().createResource(node.getOntClass());
			resource.addProperty(node.getOntProperty(), type);
			for (RestrictionNode child : node.getChildren()) {
				if (child.isSelected()) {
					if (addInputResources(child, type, id)) {
						result = true;
					}
				}
			}
		} else {
			if (addProperties(resource, node, node.getValues(id))) {
				result = true;
			}
		}
		return result;
	}

	private static boolean addProperties(Resource resource, RestrictionNode restriction,
			List<?> values) throws IOException {
		if (values.size() > 0) {
			if (values.get(0) instanceof List<?>) {
				List<?> list = (List<?>) values.get(0);
				if (!addProperties(resource, restriction, list)) {
					values.remove(0);
				}
			} else {
				for (Object value : values) {
					if (value instanceof RDFNode) {
						RDFNode rdfNode = (RDFNode) value;
						resource.addProperty(restriction.getOntProperty(), rdfNode);
					} else if (value instanceof String) {
						String string = (String) value;
						if (isAbsoluteURI(string)) {
							Resource newResource = resource.getModel().createResource(string,
									restriction.getOntClass());
							resource.addProperty(restriction.getOntProperty(), newResource);
						} else if (LSRNUtils.isLSRNType(restriction.getOntClass())) {
							Resource newResource = LSRNUtils.createInstance(resource
									.getModel(), restriction.getOntClass(), string);
							resource.addProperty(restriction.getOntProperty(), newResource);
						} else {
							Literal literal = resource.getModel().createTypedLiteral(string,
									restriction.getOntClass().getURI());
							resource.addProperty(restriction.getOntProperty(), literal);
						}
					} else {
						return false;
					}
				}
				values.clear();
			}
		}
		return values.size() > 0;
	}

	/**
	 * Attaches values, created from the output triples, to the restriction
	 * tree.
	 * 
	 * @param node
	 *            the root node of the restriction tree
	 * @param outputTriples
	 *            the triples output by the service
	 * @param inputResources
	 *            the input to the service
	 */
	public static void putOutputResources(RestrictionNode node, Collection<Triple> outputTriples,
			List<Resource> inputResources, String id) {
		Model model = ModelFactory.createDefaultModel();
		List<RDFNode> rdfNodes = new ArrayList<RDFNode>();
		int[] pos = new int[node.getDepth()];
		for (int i = 0; i < inputResources.size(); i++) {
			pos[node.getLevel()] = i;
			Resource inputResource = inputResources.get(i);
//			Resource output = model.createResource(node.getOntClass());
//			Resource output = model.createResource(inputNode.getURI(), node.getOntClass());
			Resource output = inputResource.isURIResource() ?
					model.createResource(inputResource.getURI(), node.getOntClass()) :
					model.createResource(node.getOntClass());
			rdfNodes.add(output);
			for (RestrictionNode child : node.getChildren()) {
				putOutputResources(child, output, inputResource.asNode(), outputTriples,
						pos, id);
			}
		}
		node.setValues(id, rdfNodes);
		if (logger.isDebugEnabled()) {
			StringWriter stringWriter = new StringWriter();
			model.write(stringWriter, "RDF/XML-ABBREV");
			logger.debug(stringWriter);
		}
	}

	private static void putOutputResources(RestrictionNode node, Resource resource, Node subject,
			Collection<Triple> outputTriples, int[] pos, String id) {
		List<RDFNode> rdfNodes = new ArrayList<RDFNode>();
		if (node.isLeaf()) {
			List<Triple> triples = filterTriples(outputTriples, subject, node.getOntProperty()
					.asNode());
			for (Triple triple : triples) {
				if (triple.getObject().isLiteral()) {
					Literal literal = resource.getModel().createTypedLiteral(
							triple.getObject().getLiteralValue());
					resource.addLiteral(node.getOntProperty(), literal);
					rdfNodes.add(literal);
				} else if (triple.getObject().isURI()) {
					Resource newResource = resource.getModel().createResource(
							triple.getObject().getURI(), node.getOntClass());
					resource.addProperty(node.getOntProperty(), newResource);
					rdfNodes.add(newResource);
				}
			}
		} else {
			List<Triple> triples = filterTriples(outputTriples, subject, node.getOntProperty()
					.asNode());
			for (int i = 0; i < triples.size(); i++) {
				Resource type = resource.getModel().createResource(node.getOntClass());
				resource.addProperty(node.getOntProperty(), type);
				rdfNodes.add(type);
				for (RestrictionNode child : node.getChildren()) {
					pos[node.getLevel()] = i;
					putOutputResources(child, type, triples.get(i).getObject(), outputTriples, pos, id);
				}
			}
		}
		addValue(node, rdfNodes, pos, id);
	}

	@SuppressWarnings("unchecked")
	private static void addValue(RestrictionNode node, List<RDFNode> rdfNodes, int[] pos, String id) {
		int level = node.getLevel() - 1;
		List<Object> values = (List<Object>) node.getValues(id);
		if (values == null) {
			values = new ArrayList<Object>();
			node.setValues(id, values);
		}
		for (int i = 0; i < pos.length; i++) {
			if (i == level) {
				values.add(rdfNodes);
			} else if (i < level) {
				int index = pos[i];
				while (values.size() <= index) {
					values.add(new ArrayList<Object>());
				}
				values = (List<Object>) values.get(index);
			}
		}
	}

	/**
	 * Returns true if <code>id</code> is a URI and it is absolute.
	 * 
	 * @param id
	 *            the id to test
	 * @return true if <code>id</code> is a URI and it is absolute
	 */
	public static boolean isAbsoluteURI(String id) {
		try {
			return new URI(id).isAbsolute();
		} catch (URISyntaxException e) {
			return false;
		}
	}

	/**
	 * Returns the list of triples that match the give subject and predicate. If
	 * the predicate is null any triple with the same subject will be returned.
	 * 
	 * @param triples
	 *            the triples to filter
	 * @param subject
	 *            the subject to match
	 * @param predicate
	 *            the predicate to match
	 * @return the list of triples that match the give subject and predicate
	 */
	public static List<Triple> filterTriples(Collection<Triple> triples, Node subject,
			Node predicate) {
		List<Triple> resultTriples = new ArrayList<Triple>();
		for (Triple triple : triples) {
			if (triple.subjectMatches(subject)) {
				if (predicate == null || triple.predicateMatches(predicate)) {
					resultTriples.add(triple);
				}
			}
		}
		return resultTriples;
	}

	private static class LocalOwlUtils
	{
		public static OntClass getValuesFromAsClass(Restriction restriction)
		{
			return getValuesFromAsClass(restriction, false);
		}

		public static OntClass getValuesFromAsClass(Restriction restriction, boolean fallbackToRange)
		{
			OntResource valuesFrom = getValuesFrom(restriction);
			if (valuesFrom != null && valuesFrom.isClass()) {
				return valuesFrom.asClass();
			} else if (fallbackToRange) {
				OntProperty p = restriction.getOnProperty();
				if (p != null) {
					if (p.isDatatypeProperty())
						return p.getOntModel().getOntClass(RDFS.Literal.getURI());
					else if (p.isObjectProperty())
						return p.getOntModel().getOntClass(OWL.Thing.getURI());
					else
						return p.getOntModel().getOntClass(RDFS.Resource.getURI());
				}
			}
			return restriction.getOntModel().getOntClass(OWL.Thing.getURI());
		}
		
		public static OntResource getValuesFrom(Restriction restriction)
		{
			if (restriction.isAllValuesFromRestriction()) {
				return restriction.getOntModel().getOntResource(restriction.asAllValuesFromRestriction().getAllValuesFrom());
			} else if (restriction.isSomeValuesFromRestriction()) {
				return restriction.getOntModel().getOntResource(restriction.asSomeValuesFromRestriction().getSomeValuesFrom());
			} else {
				return null;
			}
		}
		
		public static Set<Restriction> listRestrictions(OntClass clazz)
		{
			return listRestrictions(clazz, null);
		}
		
		public static Set<Restriction> listRestrictions(OntClass clazz, OntClass relativeTo)
		{
			Set<Restriction> restrictions;
			if (relativeTo != null) {
				restrictions = OwlUtils.listRestrictions(clazz, relativeTo);
			} else {
				restrictions = OwlUtils.listRestrictions(clazz);
			}
			
			// filter restrictions for restrictions on the same property...
			Map<OntProperty, Restriction> seen = new HashMap<OntProperty, Restriction>();
			for (Restriction r: restrictions) {
				OntProperty p = r.getOnProperty();
				if (p == null) {
					logger.warn(String.format("skipping restriction on undefined property %s", r.getPropertyValue(OWL.onProperty)));
					continue;
				}
				if (seen.containsKey(p)) {
					OntClass thisValuesFrom = LocalOwlUtils.getValuesFromAsClass(r);
					OntClass storedValuesFrom = LocalOwlUtils.getValuesFromAsClass(seen.get(p));
					if (storedValuesFrom.hasSubClass(thisValuesFrom)) {
						// this restriction is more specific; use it...
						seen.put(p, r);
					} else if (storedValuesFrom.hasSuperClass(thisValuesFrom)) {
						// stored restriction is more specific; do nothing...
					} else {
						// multiple values from; create a union class...
						//seen.put(p, LocalOwlUtils.createUnionClass(storedValuesFrom, thisValuesFrom));
						// for now, just do what David was doing...
						seen.put(p, r);
					}
				} else {
					seen.put(p, r);
				}
			}
			
			restrictions.clear();
			restrictions.addAll(seen.values());
			return restrictions;
		}

		public static UnionClass createUnionClass(OntClass c1, OntClass c2)
		{
			if (c1.isUnionClass()) {
				UnionClass union = c1.asUnionClass();
				if (c2.isUnionClass()) {
					for (OntClass operand: c2.asUnionClass().listOperands().toList())
						union.addOperand(operand);
				} else {
					union.addOperand(c2);
				}
				return union;
			} else if (c2.isUnionClass()) {
				// quick and dirty...
				return createUnionClass(c2, c1);
			} else {
				RDFList operands = c1.getModel().createList();
				operands.add(c1);
				operands.add(c2);
				return c1.getOntModel().createUnionClass(null, operands);
			}
		}
	}
}
