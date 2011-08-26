/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;
import org.biomoby.client.CentralImpl;
import org.biomoby.client.ui.graphical.applets.shared.Household;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyResourceRef;
import org.biomoby.shared.MobyService;
import org.biomoby.shared.extended.ServiceInstanceParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.ibm.lsid.LSID;

/**
 * 
 * @author Edward Kawas
 *
 */
public class BiomobyScavengerWorker {

	private static Logger log = Logger
			.getLogger(BiomobyScavengerWorker.class);

	public static ThreadLocal DOCUMENT_BUILDER_FACTORIES = new ThreadLocal() {
		protected synchronized Object initialValue() {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			return dbf;
		}
	};

	public static Document loadDocument(InputStream input) throws MobyException {
		try {
			DocumentBuilderFactory dbf = (DocumentBuilderFactory) DOCUMENT_BUILDER_FACTORIES
					.get();
			DocumentBuilder db = dbf.newDocumentBuilder();
			return (db.parse(input));
		} catch (Exception e) {
			throw new MobyException("Problem with reading XML input: "
					+ e.toString(), e);
		}
	}

	public static void main(String[] args) throws MobyException {
		BiomobyScavengerWorker b = new BiomobyScavengerWorker();
		b.getDataTypes();
		
		System.out.println(b.getServices());
	}

	private CacheImpl cache;

	private CentralImpl central;

	private String REGISTRY_URL = CentralImpl.DEFAULT_ENDPOINT;

	private String REGISTRY_URI = CentralImpl.DEFAULT_NAMESPACE;

	// if value stays null, then we use api
	private String REMOTE_DATATYPE_RDF_URL = null;

	// if value stays null, then we use api
	private String REMOTE_SERVICE_RDF_URL = null;

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
	 *            the url of the registry
	 * @param uri -
	 *            the namespace for the registry
	 * @param scav -
	 *            the BiomobyScavenger to be used with this class
	 */
	public BiomobyScavengerWorker(String url, String uri)
			throws MobyException {
		try {
			if (uri != null)
				this.REGISTRY_URI = uri;
			if (url != null)
				this.REGISTRY_URL = url;
			String cacheLoc = System.getProperty("taverna.home");
			if (cacheLoc == null || cacheLoc.trim().length() == 0)
				cacheLoc = System.getProperty("taverna.repository");
			if (cacheLoc == null || cacheLoc.trim().length() == 0)
				cacheLoc = "";
			if (!cacheLoc.endsWith(System.getProperty("file.separator")))
				cacheLoc += File.separator;
			cache = new CacheImpl(REGISTRY_URL, REGISTRY_URI, cacheLoc + "moby-cache");
		} catch (MobyException e) {
			// this would flag that caching is disabled ;-) TODO use api instead
			log
					.warn(
							"There was a problem in initializing the caching agent, therefor caching is disabled.",
							e);
			e.printStackTrace();
		}
		// an exception here states that we are SOL!
		central = new CentralImpl(REGISTRY_URL, REGISTRY_URI);
		getRDFLocations();

	}

	/*
	 * method from MobyTree.java, but logic has changed.
	 */
	private DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
			DefaultMutableTreeNode child) {
		parent.add(child);
		return child;
	}

	/*
	 * copied over from MobyTree.java - replaced call to RESOURCES with an http
	 * call
	 */
	@SuppressWarnings("unchecked")
	private HashMap<String, Household> createHomes(String url) {
		HashMap<String, Household> homes = new HashMap<String, Household>(); // (key=parent,val=household)
		try {
			// create an empty model
			Model model = ModelFactory.createDefaultModel();

			InputStream in = (new URL(url).openStream());

			// read the RDF/XML data
			model.read((in), "");
			StmtIterator iter;
			Statement stmt;

			iter = model.listStatements();
			while (iter.hasNext()) {
				stmt = (Statement) iter.next();
				String sub = stmt.getSubject().getURI();
				String obj = stmt.getObject().toString();
				if (sub != null) {
					try {
					    	if (sub.indexOf("/MOBY_SUB_COMPONENT/") > 0) continue;
						if (sub.indexOf("#") > 0)
							sub = sub.substring(sub.indexOf("#") + 1, sub
									.length());
						else if(sub.lastIndexOf("/Objects/") > 0) {
						    sub = sub.substring(sub.lastIndexOf("/Objects/")+"/Objects/".length());
						}
						LSID lsid = new LSID(sub);
						sub = lsid.getObject();
					} catch (Exception e) {
					}
				}
				if (obj != null) {
				    	if (obj.indexOf("/MOBY_SUB_COMPONENT/") > 0) continue;
					if (obj.indexOf("#") > 0)
						obj = obj.substring(obj.indexOf("#") + 1, obj.length());
					else if(obj.lastIndexOf("/Objects/") > 0) {
					    obj = obj.substring(obj.lastIndexOf("/Objects/")+"/Objects/".length());
					}
					try {
						LSID lsid = new LSID(obj);
						obj = lsid.getObject();
					} catch (Exception e) {
					}
				}

				if (stmt.getPredicate().getURI().indexOf("subClassOf") > 0) {
					// System.out.println(obj);
					// we have the relationship that we want in the tree
					if (homes.containsKey(obj)) {
						// hash contains the home -> get the home and add child
						// to household
						Household h = homes.get(obj);
						ArrayList<String> ch = h.getChildren();
						ch.add(sub);
						h.setChildren(ch);
						homes.put(obj, h);
					} else {
						// hash doesn't have the parent, so add the parent and
						// child to a new household
						ArrayList<String> ch = new ArrayList<String>();
						ch.add(sub); // add the child
						Household h = new Household(obj, ch);
						homes.put(obj, h);
					}
				} // hashmap is created

			}

		} catch (Exception e) {
			System.err.println("Failed: " + e.getMessage() + "\n"
					+ e.getStackTrace());
		}

		return homes;
	}

	private String doCall(String method, Object[] parameters)
			throws MobyException {

		Call call = null;
		try {
			Service service = new Service();
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(REGISTRY_URL);
			call.setTimeout(new Integer(0));

			call.setSOAPActionURI(REGISTRY_URI + "#" + method);
			return resultToString(call.invoke(REGISTRY_URI, method, parameters));

		} catch (AxisFault e) {
			throw new MobyException(REGISTRY_URL.toString()
					+ (call == null ? "" : call.getOperationName()), e);
		} catch (Exception e) {
			throw new MobyException(e.toString(), e);
		}
	}

	/*
	 * copied over from MobyTree.java
	 */
	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode fillSubTree(
			DefaultMutableTreeNode parentNode, ArrayList<String> children,
			HashMap<String, Household> hashmap, String base) {
		Collections.sort(children);
		Iterator<String> it = children.iterator();
		while (it.hasNext()) {
			String nextKid = it.next();
			BiomobyObjectProcessorFactory f = new BiomobyObjectProcessorFactory(
					base, "", nextKid);
			DefaultMutableTreeNode p1 = addObject(parentNode,
					new DefaultMutableTreeNode(f));
			Household param = hashmap.get(nextKid);

			if (param != null) {
				// mt.addObject(p1, nextKid);
				fillSubTree(p1, param.getChildren(), hashmap, base);
			}

		}
		return parentNode;
	}

	private List<String> getDataTypeLSIDsByAPI() throws MobyException {
		// parse returned XML
		List<String> results = new ArrayList<String>();
		Document document = loadDocument(new ByteArrayInputStream(doCall(
				"retrieveObjectNames", new Object[] {}).getBytes()));
		NodeList list = document.getElementsByTagName("Object");
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);
			results.add((elem.getAttribute("lsid")).trim().toLowerCase());
		}
		return results;
	}

	/**
	 * 
	 * @return a DefaultMutableTreeNode that represents the BioMOBY root
	 *         datatype 'Object' with all of its children
	 */
	@SuppressWarnings("unchecked")
	public DefaultMutableTreeNode getDataTypes() throws MobyException {

		updateDatatypeCache();
		try {
			// create the datatype tree
			DefaultMutableTreeNode objectRootNode = new DefaultMutableTreeNode(
					"MOBY Objects");
			BiomobyObjectProcessorFactory f = new BiomobyObjectProcessorFactory(
					REGISTRY_URL, "", "Object");
			DefaultMutableTreeNode root = new DefaultMutableTreeNode(f);
			objectRootNode.add(root);
			HashMap<String, Household> hashmap = createHomes("file:///"
					+ cache.getDatatypeRDFLocation());
			fillSubTree(root, hashmap.get("Object").getChildren(), hashmap,
					REGISTRY_URL);
			return objectRootNode;

		} catch (Exception e) {
			throw new MobyException("Error creating Datatype tree: "
					+ e.getMessage());
		}
	}

	private void getRDFLocations() {
		try {

			MobyResourceRef mrr[] = central.getResourceRefs();
			REMOTE_DATATYPE_RDF_URL = null;
			for (int x = 0; x < mrr.length; x++) {
				MobyResourceRef ref = mrr[x];
				if (ref.getResourceName().equals("Object")) {
					REMOTE_DATATYPE_RDF_URL = ref.getResourceLocation()
							.toExternalForm();
					continue;
				}
				if (ref.getResourceName().equals("ServiceInstance")) {
					REMOTE_SERVICE_RDF_URL = ref.getResourceLocation()
							.toExternalForm();
					continue;
				}
			}
		} catch (MobyException e) {
			return;
		}

		log.info("Service RDF @ "
				+ (REMOTE_SERVICE_RDF_URL == null ? "(not used)"
						: REMOTE_SERVICE_RDF_URL)
				+ ", "
				+ System.getProperty("line.separator")
				+ "\tObjects @ "
				+ (REMOTE_DATATYPE_RDF_URL == null ? "(not used)"
						: REMOTE_DATATYPE_RDF_URL));
		return;
	}

	/**
	 * @return the rEMOTE_DATATYPE_RDF_URL
	 */
	public String getREMOTE_DATATYPE_RDF_URL() {
		return REMOTE_DATATYPE_RDF_URL;
	}

	/**
	 * @return the rEMOTE_SERVICE_RDF_URL
	 */
	public String getREMOTE_SERVICE_RDF_URL() {
		return REMOTE_SERVICE_RDF_URL;
	}

	private List getServiceLSIDsByAPI() throws MobyException {
		// parse returned XML
		List<String> results = new ArrayList<String>();
		Document document = loadDocument(new ByteArrayInputStream(doCall(
				"retrieveServiceNames", new Object[] {}).getBytes()));
		NodeList list = document.getElementsByTagName("serviceName");
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);
			results.add((elem.getAttribute("lsid")).trim().toLowerCase());
		}
		return results;
	}

	/**
	 * 
	 * @return a Map whose keys are service provider authorities mapped to an
	 *         arraylist of treenodes
	 * @throws MobyException
	 *             if something goes wrong
	 */
	public synchronized ArrayList<DefaultMutableTreeNode> getServices() throws MobyException {
		try {
			updateServiceInstanceCache();
		} catch (Exception e) {
			
		}
		if (getREMOTE_SERVICE_RDF_URL() == null) {
			return getServicesClassic();
		} else {
			try {
				return getServicesRDF();
			} catch (MobyException e) {
				return getServicesClassic();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private ArrayList<DefaultMutableTreeNode> getServicesClassic()
			throws MobyException {
		ArrayList<DefaultMutableTreeNode> authorityList = new ArrayList<DefaultMutableTreeNode>();
		try {
			cache.removeFromCache(CacheImpl.CACHE_PART_SERVICES);
			Map<String, String[]> names = central.getServiceNamesByAuthority();

			Hashtable<String, Vector<String>> byAuthority = new Hashtable<String, Vector<String>>();
			for (Iterator it = names.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String authorityName = (String) entry.getKey();
				String[] serviceName = (String[]) entry.getValue();
				Vector<String> services;
				if (byAuthority.containsKey(authorityName))
					services = byAuthority.get(authorityName);
				else
					services = new Vector<String>();
				for (int i = 0; i < serviceName.length; i++) {
					services.addElement(serviceName[i]);
				}
				byAuthority.put(authorityName, services);
			}

			ArrayList<String> list = new ArrayList<String>(names.keySet());
			Collections.sort(list);
			for (Iterator<String> iterator = list.iterator(); iterator
					.hasNext();) {
				String authorityName = iterator.next();
				String[] authorities = names.get(authorityName);
				DefaultMutableTreeNode authority = new DefaultMutableTreeNode(
						authorityName);
				authorityList.add(authority);
				try {
					java.util.Arrays.sort(authorities,
							String.CASE_INSENSITIVE_ORDER);
				} catch (Exception e) {
				}
				for (int i = 0; i < authorities.length; i++) {
					String serviceName = (String) authorities[i];
					BiomobyProcessorFactory f = new BiomobyProcessorFactory(
							REGISTRY_URL, authorityName, serviceName);
					// f.setAlive(true); // not needed because default is true
					authority.add(new DefaultMutableTreeNode(f));
				}
			}
			// ///////////////////////
		} catch (Exception e) {
			log.error("Couldn't create the BioMOBY scavenger ...", e);
			throw new MobyException(
					"There was a problem creating the scavenger: "
							+ e.getMessage());
		}
		return authorityList;
	}

	private ArrayList<DefaultMutableTreeNode> getServicesRDF()
			throws MobyException {
		ArrayList<DefaultMutableTreeNode> authorityList = new ArrayList<DefaultMutableTreeNode>();
		MobyService[] services = null;
		ServiceInstanceParser parser;
		parser = new ServiceInstanceParser("file:///"
				+ cache.getServiceInstanceRDFLocation());
		try {
			services = parser.getMobyServicesFromRDF();
		} catch (NoSuchFieldError ex) {
			log.warn("Could not get moby services from RDF", ex);
			services = new MobyService[0];
		}
		StringBuffer sb = new StringBuffer();
		if (services.length == 0) {
			// try classic method
			return getServicesClassic();
		}
		
		String newline = System.getProperty("line.separator");
		
		//TODO this really should be in update ...
		// save lsids obtained via api -- prevents bug where invalid services cause cache to be updated all the time
		List<String> service_lsids = getServiceLSIDsByAPI();
		for (String s : service_lsids) {
			sb.append(s + newline);
		}

		
		// sorted by authority(case insensitive) with services sorted
		// case
		// sensitive
		SortedMap<String, SortedSet<MobyService>> map = new TreeMap<String, SortedSet<MobyService>>();
		for (int i = 0; i < services.length; i++) {
			MobyService service = services[i];
			//sb.append(service.getLSID() + newline);
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

		// store the cached rdf
		synchronized (cache.servicesCache) {
			//CacheImpl.storeListFile(cache.servicesCache, sb.toString());
			CacheImpl.storeListFile(cache.servicesCache, sb.toString());
		}
		sb = null;

		for (String authority_name : map.keySet()) {
			DefaultMutableTreeNode authority = new DefaultMutableTreeNode(
					authority_name);
			authorityList.add(authority);

			for (MobyService service : map.get(authority_name)) {
				String serviceName = service.getName();
				BiomobyProcessorFactory f = new BiomobyProcessorFactory(
						REGISTRY_URL, REGISTRY_URI, authority_name, serviceName);
				if (service.getStatus() != MobyService.UNCHECKED) {
					f.setAlive((service.getStatus() & MobyService.ALIVE) == 2);
				}
				authority.add(new DefaultMutableTreeNode(f));
			}
		}
		return authorityList;
	}

	private String resultToString(Object result) throws MobyException {
		if (result == null)
			throw new MobyException("Returned result is null.");
		if (result instanceof String)
			return (String) result;
		if (result instanceof String[]) {
			String[] tmp = (String[]) result;
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < tmp.length; i++)
				buf.append(tmp[i]);
			return new String(buf);
		}
		if (result instanceof byte[])
			return new String((byte[]) result);

		throw new MobyException("Unknown type of result: "
				+ result.getClass().getName());
	}

	/**
	 * @param remote_datatype_rdf_url
	 *            the rEMOTE_DATATYPE_RDF_URL to set
	 */
	public void setREMOTE_DATATYPE_RDF_URL(String remote_datatype_rdf_url) {
		REMOTE_DATATYPE_RDF_URL = remote_datatype_rdf_url;
	}

	/**
	 * @param remote_service_rdf_url
	 *            the rEMOTE_SERVICE_RDF_URL to set
	 */
	public void setREMOTE_SERVICE_RDF_URL(String remote_service_rdf_url) {
		REMOTE_SERVICE_RDF_URL = remote_service_rdf_url;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Details for registry: " + REGISTRY_URL);
		sb.append("\n\tCache location: " + cache.getCacheDir());
		sb.append("\n\tService cache located at: " + cache.getServiceInstanceRDFLocation());
		sb.append("\n\tService RDF located at: " + getREMOTE_SERVICE_RDF_URL());
		sb.append("\n\tDatatype cache located at: " + cache.getDatatypeRDFLocation());
		sb.append("\n\tDatatype RDF located at: " + getREMOTE_DATATYPE_RDF_URL());
		long age = cache.getCacheAge();
		sb.append("\n\tCache Creation Date: " + (age == -1 ? "n/a" : new Date(age).toString()));
		return sb.toString();
	}

	@SuppressWarnings("unused")
	private void updateDatatypeCache() {
		if (REMOTE_DATATYPE_RDF_URL == null
				|| REMOTE_DATATYPE_RDF_URL.trim().equals("")) {
			// we will have to use api -> caching not available
			return;
		}
		boolean modified = false;
		String datatype_lsids = null;
		List<String> lsid_list = null;
		synchronized (cache.dataTypesCache) {
			try {
				datatype_lsids = CacheImpl.getListFile(cache.dataTypesCache);
			} catch (MobyException e) {
				// purposely empty
			}
			if (datatype_lsids == null || datatype_lsids.trim().equals("")) {
				// time for update
				// download the RDF
				try {
					cache.storeURL(cache.dataTypesCache,
							CacheImpl.DATATYPE_FILENAME,
							REMOTE_DATATYPE_RDF_URL);
					lsid_list = getDataTypeLSIDsByAPI();
					StringBuffer sb = new StringBuffer();
					String newline = System.getProperty("line.separator");
					for (String str : lsid_list) {
						sb.append(str + newline);
					}
					// store the cached rdf
					CacheImpl
							.storeListFile(cache.dataTypesCache, sb.toString());
					sb = null;
				} catch (MobyException e) {
					log.warn("There was a problem writing to the cache:", e);
					REMOTE_DATATYPE_RDF_URL = null;
					// caching is disabled ...

					return;
				}
				// file has been downloaded ... we are done!
				return;

			} else {
				// compare to one retrieved via API calls
				try {
					lsid_list = getDataTypeLSIDsByAPI();
					String[] lsids = datatype_lsids.split(System
							.getProperty("line.separator"));
					modified = lsid_list.size() != lsids.length;
					if (!modified) {
						for (int i = 0; i < lsids.length; i++) {
							if (!lsid_list.contains(lsids[i].toLowerCase()
									.trim())) {
								modified = true;
								log.info("Modification found ... ");
								break;
							}
						}
					}
					if (modified) {
						// download new RDF
						try {
							cache.storeURL(cache.dataTypesCache,
									CacheImpl.DATATYPE_FILENAME,
									REMOTE_DATATYPE_RDF_URL);
						} catch (MobyException e) {
							log
									.warn(
											"There was a problem writing to the cache:",
											e);
							REMOTE_DATATYPE_RDF_URL = null;
							// caching is disabled ...
							return;
						}
					}
				} catch (MobyException e) {
					// TODO
				}
			}
		}
		// update list file if necessary
		if (modified && lsid_list != null) {
			try {
				StringBuffer sb = new StringBuffer();
				String newline = System.getProperty("line.separator");
				for (String str : lsid_list) {
					sb.append(str + newline);
				}
				// store the cached rdf
				CacheImpl
						.storeListFile(cache.dataTypesCache, sb.toString());
				sb = null;
			} catch (MobyException e) {
				log.warn("There was a problem writing datatype LIST file to the cache:", e);
				return;
			}
		}
	}
	
	/**
	 * 
	 * @return the CacheImpl object for this scavenger
	 */
	public CacheImpl getCacheImpl() {
	    return cache;
	}
	
	/*
	 * 
	 */
	private void updateServiceInstanceCache() {
		if (REMOTE_SERVICE_RDF_URL == null
				|| REMOTE_SERVICE_RDF_URL.trim().equals("")) {
			// we will have to use api -> caching not available
			return;
		}
		String service_lsids = null;
		synchronized (cache.servicesCache) {
			try {
				service_lsids = CacheImpl.getListFile(cache.servicesCache);
			} catch (MobyException e) {
				// purposely empty
			}
			if (service_lsids == null || service_lsids.trim().equals("")) {
				// time for update
				// download the RDF
				try {
					cache.storeURL(cache.servicesCache,
							CacheImpl.SERVICE_INSTANCE_FILENAME,
							REMOTE_SERVICE_RDF_URL);
				} catch (MobyException e) {
					log.warn("There was a problem writing to the cache:", e);
					REMOTE_SERVICE_RDF_URL = null;
					// caching is disabled ...

					return;
				}
				// file has been downloaded ... we are done!
				return;

			} else {
				// compare to one retrieved via API calls
				try {
					List lsid_list = getServiceLSIDsByAPI();
					String[] lsids = service_lsids.split(System
							.getProperty("line.separator"));
					boolean modified = lsid_list.size() != lsids.length;
					if (!modified) {
						for (int i = 0; i < lsids.length; i++) {
							if (!lsid_list.contains(lsids[i].toLowerCase()
									.trim())) {
								modified = true;
								log.info("Modification found ... ");
								break;
							}
						}
					}
					if (modified) {
						// download new RDF
						try {
							cache.storeURL(cache.servicesCache,
									CacheImpl.SERVICE_INSTANCE_FILENAME,
									REMOTE_SERVICE_RDF_URL);
						} catch (MobyException e) {
							log
									.warn(
											"There was a problem writing to the cache:",
											e);
							REMOTE_SERVICE_RDF_URL = null;
							// caching is disabled ...
							return;
						}
					}
				} catch (MobyException e) {
					log.error("Unexpected error while updating BioMOBY cache:",e);
				}
			}
		}
	}
}
