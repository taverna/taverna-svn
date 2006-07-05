package org.apache.bsf;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

/**
 * This class provides internal access to BSFManager internals
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.6 $
 */
public class ExtendedBSFManager extends BSFManager {
	static Properties props = new Properties();

	static String[] langArray = null;
	static Map engineMap = new HashMap();
	static Map abbrevMap = new HashMap();
	static Map engineImplMap = new HashMap();
	public ExtendedBSFManager() {
		super();
		try {
			InputStream is = ExtendedBSFManager.class
					.getResourceAsStream("BSFManager.properties");
			props.load(is);
			// String langs = props.getProperty("languages");
			// langArray = langs.split(",");

			Set keys = props.keySet();
			Iterator it = keys.iterator();
			String currKey = null;
			String engineName = null;
			String language = null;
			String abbrev = null;
			String[] entry = null;

			while (it.hasNext()) {
				currKey = (String) it.next();
				if (currKey.startsWith("bsf.engine")) {
					engineName = currKey.substring("bsf.engine.".length());
					entry = props.getProperty(currKey).split(",");
					language = entry[0];
					abbrev = entry[1];
					engineMap.put(language, engineName);

					registerScriptingEngine(language,engineName, entry);

				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

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
	
    /**
     * Execute the given script of the given language.
     *
     * @param lang     language identifier
     * @param source   (context info) the source of this expression
     (e.g., filename)
     * @param lineNo   (context info) the line number in source for expr
     * @param columnNo (context info) the column number in source for expr
     * @param script   the script to execute
     *
     * @exception BSFException if anything goes wrong while running the script
     */
    public void exec(String lang,
                     String source,
                     int lineNo,
                     int columnNo,
                     Object script)
        throws BSFException {
        final BSFEngine e = loadScriptingEngine(lang);
        final String sourcef = source;
        final int lineNof = lineNo, columnNof = columnNo;
        final Object scriptf = script;

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        e.exec(sourcef, lineNof, columnNof, scriptf);
                        return null;
                    }
                });
        }
        catch (PrivilegedActionException prive) {
        	prive.printStackTrace();
            throw (BSFException) prive.getException();
        }catch(Exception ex){
        	ex.printStackTrace();
        }
    }

}
