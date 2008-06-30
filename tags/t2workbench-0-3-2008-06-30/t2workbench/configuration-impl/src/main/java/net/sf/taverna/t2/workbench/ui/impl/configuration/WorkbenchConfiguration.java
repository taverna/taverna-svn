package net.sf.taverna.t2.workbench.ui.impl.configuration;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;

import org.apache.log4j.Logger;

/**
 * An implementation of Configurable for general Workbench configuration properties
 * 
 * @author Stuart Owen
 *
 */
public class WorkbenchConfiguration implements Configurable {
	
	public static String uuid = "ec8b99f0-3313-11dd-bd11-0800200c9a66";
	private static WorkbenchConfiguration instance = new WorkbenchConfiguration();

	private WorkbenchConfiguration() {
		initialiseDefaults();
		ConfigurationManager manager = ConfigurationManager.getInstance();
		//FIXME: hardcoded to use the temp dir
		try {			
			if (!manager.isBaseLocationSet()) manager.setBaseConfigLocation(new File(System.getProperty("java.io.tmpdir")));
			manager.populate(this);
		} catch (Exception e) {
			logger.error("An error occurred populating the Workbench configuration, or storing the defaults",e);
		}
	}

	public static WorkbenchConfiguration getInstance() {
		return instance;
	}
	
	private static Logger logger = Logger
			.getLogger(WorkbenchConfiguration.class);
	
	Map<String,Object> defaultWorkbenchProperties = new HashMap<String, Object>();
	Map<String,Object> workbenchProperties = new HashMap<String, Object>();

	private void initialiseDefaults() {
		//FIXME: move keys to a constants file
		String dotLocation = System.getProperty("taverna.dotlocation")!=null ? System.getProperty("taverna.dotlocation") : "/Applications/Taverna-1.7.1.app/Contents/MacOS/dot"; 
		defaultWorkbenchProperties.put("taverna.dotlocation", dotLocation);
		defaultWorkbenchProperties.put("taverna.defaultwsdl", Collections.singletonList("http://www.mygrid.org.uk/taverna-tests/testwsdls/KEGG.wsdl"));
	}

	public String getCategory() {
		return "general";
	}

	public Map<String, Object> getDefaultPropertyMap() {
		return defaultWorkbenchProperties;
	}

	public String getName() {
		return "Workbench";
	}

	public Map<String, Object> getPropertyMap() {
		return workbenchProperties;
	}

	public String getUUID() {
		return uuid;
	}

	public void restoreDefaults() {
		workbenchProperties.clear();
		workbenchProperties.putAll(defaultWorkbenchProperties);
	}

}
