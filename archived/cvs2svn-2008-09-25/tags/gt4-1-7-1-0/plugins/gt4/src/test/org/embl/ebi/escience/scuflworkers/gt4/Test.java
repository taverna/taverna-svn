package org.embl.ebi.escience.scuflworkers.gt4;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String indexURL = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";
		try {
			GT4Scavenger s = new GT4Scavenger(indexURL);
			System.out.println(s.indexURL.toString());
		} catch (ScavengerCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
