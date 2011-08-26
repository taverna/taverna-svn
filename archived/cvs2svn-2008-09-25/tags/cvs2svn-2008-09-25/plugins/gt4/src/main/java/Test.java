//package org.embl.ebi.escience.scuflworkers.gt4;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflworkers.gt4.*;
import org.embl.ebi.escience.scuflworkers.java.*;
public class Test {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		String indexURL = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";
		ServiceQuery [] sq = new ServiceQuery[2];
		sq[0] = new ServiceQuery("Research Center","Ohio State University");
		sq[1] = new ServiceQuery("Service Name","DICOMDataService");
		try {
			GT4ScavengerAgent.load(indexURL, sq);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

}
