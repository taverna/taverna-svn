package org.apache.bsf;

import java.util.Iterator;
import java.util.Vector;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.io.InputStream;

/**
 * This class provides internal access to BSFManager internals
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.3 $
 */
public class ExtendedBSFManager extends BSFManager {
	static Properties props = new Properties();

	static String[] langArray = null;
	static Map engineMap = new HashMap();
	static Map abbrevMap = new HashMap();
	static {

		try {
			InputStream is = ExtendedBSFManager.class.getResourceAsStream("BSFManager.properties");
			props.load(is);
			//String langs = props.getProperty("languages");
			//langArray = langs.split(",");
			
			Set keys = props.keySet();
			Iterator it = keys.iterator();
			String currKey = null;
			String engineName = null;
			String language = null;
			String abbrev = null;
			String[] entry = null;
			while(it.hasNext()){
				currKey = (String)it.next();
				if(currKey.startsWith("bsf.engine")){
					engineName = currKey.substring(currKey.indexOf("bsf.engine."));
					entry = props.getProperty(currKey).split(",");
					language =entry[0];
					abbrev = entry[1];
					engineMap.put(language,engineName);
					registerScriptingEngine(engineName, language,entry);
					
					
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Constructor
	 */
	public ExtendedBSFManager() {
	}

	/**
	 * This method returns a list of available Scripting engines.
	 * 
	 * @return
	 */
	public Vector getProcessorNameList() {
		
		Vector procList = new Vector();
		procList.addAll(engineMap.keySet());

		return procList;
	}

}
