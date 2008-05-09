//package org.embl.ebi.escience.scuflworkers.gt4;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflworkers.gt4.*;
import org.embl.ebi.escience.scuflworkers.java.*;
public class Test {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[] indexURL = new String[9];
		indexURL[0] = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";
		indexURL[1] = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService??SearchString==Scott";		
		indexURL[2] = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService??PointOfContact==Scott Oster";
		indexURL[3] = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService??Name==CaDSRService";
		indexURL[4] = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService??OperationName==findAllProjects";
		indexURL[5] = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService??OperationInput==Project";
		indexURL[6] = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService??OperationOutput==Project";
		indexURL[7] = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService??OperationClass==Project";
		indexURL[8] = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService??ResearchCenter==OSU";
		
		try {
			for(int i=5;i<indexURL.length-1;i++){
				GT4Scavenger s = new GT4Scavenger(indexURL[i]);
			}
			//System.out.println(s.indexURL.toString());
		} catch (ScavengerCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
