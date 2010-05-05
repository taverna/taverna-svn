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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import ca.wilkinsonlab.sadi.client.Service;
import ca.wilkinsonlab.sadi.client.ServiceImpl;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Unit tests for SADIUtils
 *
 * @author David Withers
 */
public class SADIUtilsIT {

	private <T> List<List<T>> listOfLists(T... values) {
		List<List<T>> listOfList = new ArrayList<List<T>>();
		for (T value : values) {
			List<T> list = new ArrayList<T>();
			list.add(value);
			listOfList.add(list);
		}
		return listOfList;
	}
	
	@Test
	public void testGetInputResources() throws Exception {
		String serviceURI = "http://sadiframework.org/examples/ermineJgo";
		String serviceId = serviceURI + UUID.randomUUID();
		Service service = new ServiceImpl(serviceURI);
		
		List<List<String>> hasGoTerm = new ArrayList<List<String>>();
		List<String> goTerms = new ArrayList<String>();
		Collections.addAll(goTerms, "0000166", "0003677", "0017111", "0005515", "0005524",
				"0005663", "0005634", "0003689", "0006260");
		hasGoTerm.add(goTerms);
		goTerms = new ArrayList<String>();
		Collections.addAll(goTerms, "0016740", "0006468", "0007155", "0005515", "0004672",
				"0007169", "0016301", "0004713", "0004872", "0004714", "0000166", "0005524",
				"0016020", "0005887", "0016021");
		hasGoTerm.add(goTerms);
		goTerms = new ArrayList<String>();
		Collections.addAll(goTerms, "0006986", "0006950", "0005524", "0000166");
		hasGoTerm.add(goTerms);
		
		List<List<String>> expressionLevels = listOfLists("0.903370423", "1.903370423", "1.2");

		RestrictionNode node = SADIUtils.buildInputRestrictionTree(service.getInputClass());
		for (List<String> path : SADIUtils.getDefaultRestrictionPaths(node)) {
			RestrictionNode restriction = SADIUtils.getRestriction(node, path);
			restriction.setSelected();
		}
		RestrictionNode firstLeaf = (RestrictionNode) node.getFirstLeaf();
		RestrictionNode nextLeaf = (RestrictionNode) firstLeaf.getNextLeaf();
		System.out.println(firstLeaf.getOntProperty().getLocalName());
		System.out.println(nextLeaf.getOntProperty().getLocalName());
		if (firstLeaf.getOntProperty().getLocalName().equals("hasGOTerm")) {
			firstLeaf.setValues(serviceId, hasGoTerm);
			nextLeaf.setValues(serviceId, expressionLevels);
		} else {
			firstLeaf.setValues(serviceId, expressionLevels);
			nextLeaf.setValues(serviceId, hasGoTerm);
		}
		
		List<Resource> inputResources = SADIUtils.getInputResources(node, serviceId);
		
		Collection<Triple> outputs = service.invokeService(inputResources);
		for (Triple triple : outputs) {
			System.out.println(triple);
		}
		
		RestrictionNode outputNode = SADIUtils.buildOutputRestrictionTree(service.getInputClass(), service.getOutputClass());
		SADIUtils.putOutputResources(outputNode, outputs, inputResources, serviceId);
		SADIUtils.printTreeValues(serviceId, outputNode);
	}
	
	@Test
	public void testGetInputResourcesLinear() throws Exception {
		String serviceURI = "http://sadiframework.org/examples/linear";
		String serviceId = serviceURI + UUID.randomUUID();
		Service service = new ServiceImpl(serviceURI);
		
		List<List<String>> x = listOfLists("1", "3");
		List<List<String>> y = listOfLists("2", "5");
		List<List<String>> date = listOfLists("2009-01-01", "2009-01-03");
		List<List<String>> value = listOfLists("3", "5");

		RestrictionNode inputRestrictionTree = SADIUtils.buildInputRestrictionTree(service.getInputClass());
		Map<String, RestrictionNode> map = new HashMap<String, RestrictionNode>();
		RestrictionNode leaf = (RestrictionNode) inputRestrictionTree.getFirstLeaf();
		map.put(leaf.getOntProperty().getLocalName(), leaf);
		leaf = (RestrictionNode) leaf.getNextLeaf();
		map.put(leaf.getOntProperty().getLocalName(), leaf);
		leaf = (RestrictionNode) leaf.getNextLeaf();
		map.put(leaf.getOntProperty().getLocalName(), leaf);
		leaf = (RestrictionNode) leaf.getNextLeaf();
		map.put(leaf.getOntProperty().getLocalName(), leaf);

		map.get("x").setSelected();
		map.get("x").setValues(serviceId, x);
		map.get("y").setSelected();
		map.get("y").setValues(serviceId, y);
		System.out.println(SADIUtils.printTree(inputRestrictionTree));
				
		Collection<Resource> inputResources = SADIUtils.getInputResources(inputRestrictionTree, serviceId);
		
		Collection<Triple> outputs = service.invokeService(inputResources);
		for (Triple triple : outputs) {
			System.out.println(triple);
		}

		inputRestrictionTree.clearSelected();

		map.get("date").setSelected();
		map.get("date").setValues(serviceId, date);
		map.get("value").setSelected();
		map.get("value").setValues(serviceId, value);
		System.out.println(SADIUtils.printTree(inputRestrictionTree));
	
		inputResources = SADIUtils.getInputResources(inputRestrictionTree, serviceId);
		
		outputs = service.invokeService(inputResources);
		for (Triple triple : outputs) {
			System.out.println(triple);
		}

	}

	@Test
	public void testGetInputResources2() throws Exception {
		String serviceURI = "http://137.82.157.104:8080/sadi-examples/uniprotInfo";
//		String serviceURI = "http://sadiframework.org/examples/uniprotInfo";
		String serviceId = serviceURI + UUID.randomUUID();
		Service service = new ServiceImpl(serviceURI);
		
		List<String> uniprot = Arrays.asList("P68871", "Q7Z591");
//		List<String> uniprot = Arrays.asList("http://lsrn.org/Uniprot:P68871", "http://lsrn.org/Uniprot:Q7Z591");

		RestrictionNode inputRestrictionTree = SADIUtils.buildInputRestrictionTree(service.getInputClass());
		for (List<String> path : SADIUtils.getDefaultRestrictionPaths(inputRestrictionTree)) {
			RestrictionNode restriction = SADIUtils.getRestriction(inputRestrictionTree, path);
			restriction.setSelected();
		}
		RestrictionNode firstLeaf = (RestrictionNode) inputRestrictionTree.getFirstLeaf();
		firstLeaf.setValues(serviceId, uniprot);
		
		Collection<Resource> inputResources = SADIUtils.getInputResources(inputRestrictionTree, serviceId);
		
		Collection<Triple> outputs = service.invokeService(inputResources);
		for (Triple triple : outputs) {
			System.out.println(triple);
		}
	}

	@Test
	public void testGetInputResources3() throws Exception {
		String serviceURI = "http://sadiframework.org/services/getMolecularInteractions";
		String serviceId = serviceURI + UUID.randomUUID();
		Service service = new ServiceImpl(serviceURI);
		
		List<String> uniprot = Arrays.asList("P68871", "Q7Z591");

		RestrictionNode inputRestrictionTree = SADIUtils.buildInputRestrictionTree(service.getInputClass());
		for (List<String> path : SADIUtils.getDefaultRestrictionPaths(inputRestrictionTree)) {
			RestrictionNode restriction = SADIUtils.getRestriction(inputRestrictionTree, path);
			restriction.setSelected();
		}
		RestrictionNode firstLeaf = (RestrictionNode) inputRestrictionTree.getFirstLeaf();
		firstLeaf.setValues(serviceId, uniprot);
		
		Collection<Resource> inputResources = SADIUtils.getInputResources(inputRestrictionTree, serviceId);
		System.out.println(inputResources);
		Collection<Triple> outputs = service.invokeService(inputResources);
		for (Triple triple : outputs) {
			System.out.println(triple);
		}
	}
	
	@Test
	public void testGetInputResources4() throws Exception {
		String serviceURI = "http://sadiframework.org/services/getKEGGIDFromUniProt";
		String serviceId = serviceURI + UUID.randomUUID();
		Service service = new ServiceImpl(serviceURI);
		
		List<String> uniprot = Arrays.asList("P68871", "Q7Z591");

		RestrictionNode inputRestrictionTree = SADIUtils.buildInputRestrictionTree(service.getInputClass());
		for (List<String> path : SADIUtils.getDefaultRestrictionPaths(inputRestrictionTree)) {
			RestrictionNode restriction = SADIUtils.getRestriction(inputRestrictionTree, path);
			restriction.setSelected();
		}
		RestrictionNode firstLeaf = (RestrictionNode) inputRestrictionTree.getFirstLeaf();
		firstLeaf.setValues(serviceId, uniprot);
		
		Collection<Resource> inputResources = SADIUtils.getInputResources(inputRestrictionTree, serviceId);
		System.out.println(inputResources);
		Collection<Triple> outputs = service.invokeService(inputResources);
		for (Triple triple : outputs) {
			System.out.println(triple);
		}
	}
	
}
