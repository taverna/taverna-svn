package uk.org.mygrid.datalineage;

import java.io.File;

import javax.swing.JFrame;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.utils.MyGridConfiguration;

import org.embl.ebi.escience.utils.TavernaSPIRegistry;

/**
 * Requires the LSID of a workflow run in your logbook store.
 * 
 * @author dturi
 *
 */
public class DataLineageVisualiserTester {
	
	private static final String WORKFLOW_RUN_ID = "urn:lsid:net.sf.taverna:wfInstance:"
		+
		// "cd8ec7d9-ee6e-4de6-8724-b8899cf11f12"
		 "517e062b-a0ad-4ac1-8237-6108fe49e6fe"; // Dilbert
		// "15d5bfdf-b469-46db-9838-bc38b934c127"; // missing nodes
		//"f38aa277-c898-45a5-bbfb-eb0af5bca265"; // inputs
//"ce62e476-1017-4074-9938-0eaa8d029b77"; // paul's
//		"cd8ec7d9-ee6e-4de6-8724-b8899cf11f12"; // nested
// "93ffdef2-b095-44f4-87fc-fd102b19c1db";
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String workflowRunId;
		if (args.length > 0)
			workflowRunId = args[0];
		else 
			workflowRunId = WORKFLOW_RUN_ID;
		MyGridConfiguration.getInstance();
		String ravenProfile = ClassLoader.getSystemResource(
				"default-profile.xml").toString();
		System.setProperty("raven.profile", ravenProfile);
		System.setProperty("raven.eclipse", "true");
		File tmpDir = File.createTempFile("taverna", "raven");
		Repository tempRepository = LocalRepository.getRepository(tmpDir);
		TavernaSPIRegistry.setRepository(tempRepository);

		JFrame frame = new JFrame(DataLineageVisualiser.TITLE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new DataLineageVisualiser(workflowRunId));
		frame.setSize(DataLineageVisualiser.FRAME_DIMENSION);
		frame.setVisible(true);
	}

}
