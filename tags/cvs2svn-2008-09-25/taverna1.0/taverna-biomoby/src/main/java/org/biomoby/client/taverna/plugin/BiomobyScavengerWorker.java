/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.biomoby.client.CentralDigestCachedImpl;
import org.biomoby.client.CentralImpl;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyResourceRef;
import org.biomoby.shared.MobyService;

/**
 * 
 * @author Edward Kawas
 * 
 */
public class BiomobyScavengerWorker {

    private static Logger log = Logger.getLogger(BiomobyScavengerWorker.class);

    public static void main(String[] args) throws MobyException {
	BiomobyScavengerWorker b = new BiomobyScavengerWorker();
	System.out.println(b.getDataTypes().getLeafCount());
	System.out.println(b.getServices());
    }

    private String cache;

    private CentralDigestCachedImpl central;

    private String registryUrl = CentralImpl.DEFAULT_ENDPOINT;

    private String registryUri = CentralImpl.DEFAULT_NAMESPACE;
    
    private String datatypesRdfUrl = "";
    private String namespacesRdfUrl = "";


    /**
     * Parameterized constructor. Aligns itself to the main biomoby registry.
     * 
     */
    public BiomobyScavengerWorker() throws MobyException {
	this(null, null);
    }

    /**
     * Parameterized constructor. Used for creating a scavenger for a custom
     * Biomoby registry.
     * 
     * @param url -
     *                the url of the registry
     * @param uri -
     *                the namespace for the registry
     */
    public BiomobyScavengerWorker(String url, String uri) throws MobyException {

	if (uri != null)
	    this.registryUri = uri;
	if (url != null)
	    this.registryUrl = url;
	// check for taverna.home property
	String cacheLoc = System.getProperty("taverna.home");
	// if not there see if we can get taverna.repository
	if (cacheLoc == null || cacheLoc.trim().length() == 0)
	    cacheLoc = System.getProperty("taverna.repository");
	// not there? then make it the empty string
	if (cacheLoc == null || cacheLoc.trim().length() == 0)
	    cacheLoc = "";
	// append a /
	if (!cacheLoc.endsWith(System.getProperty("file.separator")))
	    cacheLoc += File.separator;
	// append moby-cache to it
	cache = cacheLoc + "moby-cache";
	if (log.isDebugEnabled())
	    log.debug("creating moby cache in: '" + cache + "'");
	// this class throws the exception; construct a cache client to the registry
	central = new CentralDigestCachedImpl(registryUrl, registryUri, cache);
	
	MobyResourceRef[] resources = central.getResourceRefs();
	for (MobyResourceRef ref : resources) {
	    if (ref.getResourceName().equals(Central.DATA_TYPES_RESOURCE_NAME)) {
		this.datatypesRdfUrl = ref.getResourceLocation().toExternalForm();
		
	    }
	    if (ref.getResourceName().equals(Central.NAMESPACES_RESOURCE_NAME)) {
		this.namespacesRdfUrl = ref.getResourceLocation().toExternalForm();
		
	    }
	}

    }

    /**
     * 
     * @return a DefaultMutableTreeNode that represents the BioMOBY root
     *         datatype 'Object' with all of its children
     */
    @SuppressWarnings("unchecked")
    public DefaultMutableTreeNode getDataTypes() throws MobyException {

	try {
	    // create the datatype tree
	    DefaultMutableTreeNode objectRootNode = new DefaultMutableTreeNode("MOBY Objects");
	    BiomobyObjectProcessorFactory f = new BiomobyObjectProcessorFactory(
		    registryUrl, "", "Object");
	    DefaultMutableTreeNode root = new DefaultMutableTreeNode(f);
	    objectRootNode.add(root);
	    // datatypes always reads the from the URL and not from cache
	    HashMap<String, Household> hashmap = TreeUtils.createHomes(central.getResource(Central.DATA_TYPES_RESOURCE_NAME));
	    TreeUtils.fillSubTree(root, hashmap.get("Object").getChildren(),
		    hashmap, registryUrl);
	    return objectRootNode;

	} catch (Exception e) {
	    throw new MobyException("Error creating Datatype tree: "
		    + e.getMessage());
	}
    }

    /**
     * 
     * @return a Map whose keys are service provider authorities mapped to an
     *         arraylist of treenodes
     * @throws MobyException
     *                 if something goes wrong
     */
    public synchronized ArrayList<DefaultMutableTreeNode> getServices()
	    throws MobyException {
	central.updateCache(CentralDigestCachedImpl.CACHE_PART_SERVICES);
	MobyService[] services = central.getServices();
	SortedMap<String, SortedSet<MobyService>> map = new TreeMap<String, SortedSet<MobyService>>();
	for (MobyService service : services) {
		String authority = service.getAuthority();
		SortedSet<MobyService> set;
		if (map.containsKey(authority)) {
			set = map.remove(authority);
			set.add(service);
			map.put(authority, set);
		} else {
			set = new TreeSet<MobyService>(new Comparator<MobyService>() {
				public int compare(MobyService o1, MobyService o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			set.add(service);
			map.put(authority, set);
		}
	}
	ArrayList<DefaultMutableTreeNode> authorityList = new ArrayList<DefaultMutableTreeNode>();
	for (String authority_name : map.keySet()) {
		DefaultMutableTreeNode authority = new DefaultMutableTreeNode(
				authority_name);
		authorityList.add(authority);

	    for (MobyService service : map.get(authority_name)) {
		String serviceName = service.getName();
		BiomobyProcessorFactory f = new BiomobyProcessorFactory(central
			.getRegistryEndpoint(), central.getRegistryNamespace(),
			authority_name, serviceName);
		if (service.getStatus() != MobyService.UNCHECKED) {
		    f.setAlive((service.getStatus() & MobyService.ALIVE) == 2);
		}
		authority.add(new DefaultMutableTreeNode(f));
	    }
	}
	
	return authorityList;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("Details for registry: " + registryUrl);
	sb.append("\n\tCache location: " + central.getCacheDir());
	long age = central.getCacheAge();
	sb.append("\n\tCache Creation Date: "
		+ (age == -1 ? "n/a" : new Date(age).toString()));
	return sb.toString();
    }

    /**
     * 
     * @return the CacheImpl object for this scavenger
     */
    public String getCacheLocation() {
	return cache;
    }
    
    public String getRegistryUrl() {
	return this.registryUrl;
    }
    public String getRegistryUri() {
	return this.registryUri;
    }

    public String getDatatypesRdfUrl() {
        return datatypesRdfUrl;
    }

    public void setDatatypesRdfUrl(String datatypesRdfUrl) {
        this.datatypesRdfUrl = datatypesRdfUrl;
    }

    public String getNamespacesRdfUrl() {
        return namespacesRdfUrl;
    }

    public void setNamespacesRdfUrl(String namespacesRdfUrl) {
        this.namespacesRdfUrl = namespacesRdfUrl;
    }

}
