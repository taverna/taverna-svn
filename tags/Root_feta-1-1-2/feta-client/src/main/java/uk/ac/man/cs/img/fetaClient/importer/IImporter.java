/*
 * IImporter.java
 *
 * Created on February 25, 2005, 4:00 PM
 */

package uk.ac.man.cs.img.fetaClient.importer;

import java.util.Map;

/**
 * 
 * @author alperp
 */
public interface IImporter {

	public Map convert() throws FetaImportException;

}
