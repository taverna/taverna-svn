/**
 * 
 */
package uk.org.taverna.data.bundle.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import uk.org.taverna.platform.data.api.Data;
import uk.org.taverna.platform.data.api.DataService;

/**
 * @author alanrw
 *
 */
public interface DataBundle extends DataService {
	
	void saveData(Set<Data> setOfData, boolean snapshot) throws IOException;
	
	String saveData(Data data, boolean snapshot) throws IOException;
	
	void save(File destination) throws IOException;
	
	void save(OutputStream stream) throws IOException;
	
	void open(File source) throws IOException;
	
	void open(InputStream s) throws IOException;

}
