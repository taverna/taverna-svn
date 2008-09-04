/*******************************************************************************
 * This file is a component of the Taverna project, and is licensed  under the
 *  GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 ******************************************************************************/
package net.sf.taverna.t2.activities.biomoby.query;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;
import org.biomoby.client.CentralImpl;
import org.biomoby.client.taverna.plugin.BiomobyProcessorFactory;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyResourceRef;
import org.biomoby.shared.MobyService;
import org.biomoby.shared.extended.ServiceInstanceParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BiomobyQueryHelper {

	private static Logger log = Logger.getLogger(BiomobyQueryHelper.class);

	private String REGISTRY_URI;
	private String REGISTRY_URL;
	private CacheImpl cache;

	private CentralImpl central;

	private String REMOTE_DATATYPE_RDF_URL = null;

	private String REMOTE_SERVICE_RDF_URL = null;

	public BiomobyQueryHelper(String url, String uri) throws MobyException {
		try {
			if (uri != null)
				this.REGISTRY_URI = uri;
			if (url != null)
				this.REGISTRY_URL = url;
			String tavernaHome=null;
			if (ApplicationRuntime.getInstance().getApplicationHomeDir()!=null) {
				tavernaHome=ApplicationRuntime.getInstance().getApplicationHomeDir().getAbsolutePath();
			}
			String cacheLoc = tavernaHome;
			if (cacheLoc == null || cacheLoc.trim().length() == 0)
				cacheLoc = "";
			if (!cacheLoc.endsWith(System.getProperty("file.separator")))
				cacheLoc += File.separator;
			cache = new CacheImpl(REGISTRY_URL, REGISTRY_URI, cacheLoc
					+ "moby-cache");
		} catch (MobyException e) {
			// this would flag that caching is disabled ;-) TODO use api instead
			log
					.warn(
							"There was a problem in initializing the caching agent, therefor caching is disabled.",
							e);
		}
		// an exception here states that we are SOL!
		central = new CentralImpl(REGISTRY_URL, REGISTRY_URI);
		getRDFLocations();
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
	 * 
	 * @return a Map whose keys are service provider authorities mapped to an
	 *         arraylist of treenodes
	 * @throws MobyException
	 *             if something goes wrong
	 */
	public synchronized ArrayList<BiomobyActivityItem> getServices()
			throws MobyException {
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

	private ArrayList<BiomobyActivityItem> getServicesRDF()
			throws MobyException {
		ArrayList<BiomobyActivityItem> authorityList = new ArrayList<BiomobyActivityItem>();
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

		// TODO this really should be in update ...
		// save lsids obtained via api -- prevents bug where invalid services
		// cause cache to be updated all the time
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
			// sb.append(service.getLSID() + newline);
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
			// CacheImpl.storeListFile(cache.servicesCache, sb.toString());
			CacheImpl.storeListFile(cache.servicesCache, sb.toString());
		}
		sb = null;

		for (String authority_name : map.keySet()) {
			for (MobyService service : map.get(authority_name)) {
				String serviceName = service.getName();
//				if (service.getStatus() != MobyService.UNCHECKED) {
//					f.setAlive((service.getStatus() & MobyService.ALIVE) == 2);
//				}
				authorityList.add(makeActivityItem(REGISTRY_URL, REGISTRY_URI, authority_name, serviceName));
			}
		}
		return authorityList;
	}

	private BiomobyActivityItem makeActivityItem(String url, String uri, String authorityName,String serviceName) {
		BiomobyActivityItem item = new BiomobyActivityItem();
		item.setAuthorityName(authorityName);
		item.setServiceName(serviceName);
		item.setRegistryUrl(url);
		item.setRegistryUri(uri);
		return item;
	}
	
	private ArrayList<BiomobyActivityItem> getServicesClassic()
			throws MobyException {
		ArrayList<BiomobyActivityItem> authorityList = new ArrayList<BiomobyActivityItem>();
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
					authorityList.add(makeActivityItem(REGISTRY_URL,null, authorityName, serviceName));
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
					log.error("Unexpected error while updating BioMOBY cache:",
							e);
				}
			}
		}
	}

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

	public static ThreadLocal<DocumentBuilderFactory> DOCUMENT_BUILDER_FACTORIES = new ThreadLocal<DocumentBuilderFactory>() {
		protected synchronized DocumentBuilderFactory initialValue() {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			return dbf;
		}
	};

	private List<String> getServiceLSIDsByAPI() throws MobyException {
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
}
