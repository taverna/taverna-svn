package net.sf.taverna.t2.provenance;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


public class FileProvenanceConnector implements ProvenanceConnector {

	private static Logger logger = Logger.getLogger(FileProvenanceConnector.class);
	
	private File file;
	
	public FileProvenanceConnector(String filePath) {
		file = new File(filePath + "provenance");
	}
	
	public void saveProvenance(String provenance) {
		System.out.println(provenance);
		try {
			FileUtils.writeStringToFile(file, provenance, "utf-8");
		} catch (IOException e) {
			logger.warn("provenance writing problem ", e);
		}
		
		//do something!! like writing to a database or....?
	}
	
	public String getProvenance() {
		try {
			String readFileToString = FileUtils.readFileToString(file, "utf-8");
			return readFileToString;
		} catch (IOException e) {
			logger.warn("provenance read bust ", e);
		}
		return null;
	}

}
