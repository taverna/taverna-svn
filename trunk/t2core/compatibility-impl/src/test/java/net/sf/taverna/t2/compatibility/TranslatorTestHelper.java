package net.sf.taverna.t2.compatibility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.t2.compatibility.WorkflowModelTranslator;

import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.junit.BeforeClass;

/**
 * A helper class to support tests for the {@link WorkflowModelTranslator}
 * @author Stuart Owen
 *
 */
public class TranslatorTestHelper {
	
	@BeforeClass
	public static void setUpRavenRepository() throws IOException {
		System.setProperty("raven.eclipse","1");
		File tmpDir = File.createTempFile("taverna", "raven");
		tmpDir.delete();
		tmpDir.mkdir();
		Repository tempRepository = LocalRepository.getRepository(tmpDir);
		TavernaSPIRegistry.setRepository(tempRepository);
	}

	protected ScuflModel loadScufl(String resourceName)
			throws UnknownProcessorException, UnknownPortException,
			ProcessorCreationException, DataConstraintCreationException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException,
			IOException {
		ScuflModel model = new ScuflModel();
		InputStream inStream = TranslatorTestHelper.class.getResourceAsStream("/"+resourceName);
		XScuflParser.populate(inStream,model,null);
		return model;
	}
}
