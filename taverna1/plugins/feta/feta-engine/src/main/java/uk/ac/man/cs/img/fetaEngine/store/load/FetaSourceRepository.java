/*
 * FetaSourceRepository.java
 *
 * Created on March 7, 2005, 2:52 PM
 */

package uk.ac.man.cs.img.fetaEngine.store.load;

import java.io.File;
import java.net.URL;

/**
 * 
 * @author alperp
 */
public class FetaSourceRepository {

	private RepositoryType type;

	private URL repositoryLocation;

	/** Creates a new instance of FetaSourceRepository */
	public FetaSourceRepository(String typeStr, String location)
			throws FetaLoadException {

		type = RepositoryType.getTypeForString(typeStr);
		if (type == null) {
			throw new FetaLoadException(
					"Can not determine the type of feta load enpoint.");
		}
		try {
			if (type == RepositoryType.FILE) {
				File tmp = new File(location);
				repositoryLocation = tmp.toURL();
			} else {
				repositoryLocation = new URL(location);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FetaLoadException(e.getMessage());
		}

	}

	public RepositoryType getRepositoryType() {
		return type;
	}

	public URL getRepositoryLocation() {
		return repositoryLocation;

	}

}
