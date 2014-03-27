package net.sf.taverna.t2.component.registry;

import static org.apache.log4j.Logger.getLogger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.local.LocalComponentRegistryLocator;
import net.sf.taverna.t2.component.registry.myexperiment.OldComponentRegistryLocator;
import net.sf.taverna.t2.component.registry.standard.NewComponentRegistryLocator;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * @author dkf
 */
public class ComponentUtil {
	private ComponentUtil() {
		cache = new HashMap<String, Registry>();
	}

	private static Logger logger = getLogger(ComponentUtil.class);
	private static ComponentUtil impl = new ComponentUtil();
	private final Map<String, Registry> cache;

	private Registry getRegistry(URL registryBase) throws RegistryException {
		Registry registry = cache.get(registryBase.toString());
		if (registry != null)
			return registry;

		if (registryBase.getProtocol().startsWith("http")) {
			if (NewComponentRegistryLocator.verifyBase(registryBase))
				registry = NewComponentRegistryLocator
						.getComponentRegistry(registryBase);
			else
				throw new RegistryException("Unable to establish credentials for " + registryBase.toString());
//				registry = OldComponentRegistryLocator
//						.getComponentRegistry(registryBase);
		} else {
			registry = LocalComponentRegistryLocator
					.getComponentRegistry(registryBase);
		}
		cache.put(registryBase.toString(), registry);
		return registry;
	}

	private Family getFamily(URL registryBase, String familyName)
			throws RegistryException {
		return getRegistry(registryBase).getComponentFamily(familyName);
	}

	private Component getComponent(URL registryBase, String familyName,
			String componentName) throws RegistryException {
		return getRegistry(registryBase).getComponentFamily(familyName)
				.getComponent(componentName);
	}

	private Version getVersion(URL registryBase, String familyName,
			String componentName, Integer componentVersion)
			throws RegistryException {
		return getRegistry(registryBase).getComponentFamily(familyName)
				.getComponent(componentName)
				.getComponentVersion(componentVersion);
	}

	public static Registry calculateRegistry(URL registryBase)
			throws RegistryException {
		logger.info("Into calculateRegistry for " + registryBase);
		try {
			return impl.getRegistry(registryBase);
		} finally {
			logger.info("Finished calculateRegistry");
		}
	}

	public static Family calculateFamily(URL registryBase, String familyName)
			throws RegistryException {
		logger.info("Into calculateFamily");
		try {
			return impl.getFamily(registryBase, familyName);
		} finally {
			logger.info("Finished calculateFamily");
		}
	}

	public static Component calculateComponent(URL registryBase,
			String familyName, String componentName) throws RegistryException {
		logger.info("Into calculateComponent from parts");
		try {
			return impl.getComponent(registryBase, familyName, componentName);
		} finally {
			logger.info("Finished calculateComponent from parts");
		}
	}

	public static Version calculateComponentVersion(URL registryBase,
			String familyName, String componentName, Integer componentVersion)
			throws RegistryException {
		logger.info("Into calculateComponentVersion from parts");
		try {
			return impl.getVersion(registryBase, familyName, componentName,
					componentVersion);
		} finally {
			logger.info("Finished calculateComponentVersion from parts");
		}
	}

	public static Version calculateComponentVersion(Version.ID ident)
			throws RegistryException {
		logger.info("Into calculateComponentVersion from id");
		try {
			return impl.getVersion(ident.getRegistryBase(),
					ident.getFamilyName(), ident.getComponentName(),
					ident.getComponentVersion());
		} finally {
			logger.info("Finished calculateComponentVersion from id");
		}
	}

	public static Component calculateComponent(Version.ID ident)
			throws RegistryException {
		logger.info("Into calculateComponent from id");
		try {
			return impl.getComponent(ident.getRegistryBase(),
					ident.getFamilyName(), ident.getComponentName());
		} finally {
			logger.info("Finished calculateComponent from id");
		}
	}

	public static Profile makeProfile(URL url) throws RegistryException {
		logger.info("Into makeProfile");
		try {
			Profile p = new ComponentProfile(url);
			p.getProfileDocument(); // force immediate loading
			return p;
		} finally {
			logger.info("Finished makeProfile");
		}
	}
}
