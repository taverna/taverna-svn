/**
 * 
 */
package net.sf.taverna.t2.component.localworld;

import static com.hp.hpl.jena.rdf.model.ModelFactory.createOntologyModel;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.createTurtle;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.populateModelFromString;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.log4j.Logger.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author alanrw
 * 
 */
public class LocalWorld {
	private static final String FILENAME = "localWorld.ttl";
	private static final Logger logger = getLogger(LocalWorld.class);
	protected static final String ENCODING = "TURTLE";
	private static LocalWorld instance = null;

	private OntModel model;

	public synchronized static LocalWorld getInstance() {
		if (instance == null)
			instance = new LocalWorld();
		return instance;
	}

	private LocalWorld() {
		File modelFile = new File(calculateComponentsDirectory(), FILENAME);
		model = createOntologyModel();
		try {
			if (modelFile.exists())
				model.read(new StringReader(readFileToString(modelFile)), null,
						ENCODING);
		} catch (IOException e) {
			logger.error("failed to construct local annotation world", e);
		}
	}

	public File calculateComponentsDirectory() {
		return new File(ApplicationRuntime.getInstance()
				.getApplicationHomeDir(), "components");
	}

	public Individual createIndividual(String urlString, OntClass rangeClass) {
		try {
			return model.createIndividual(urlString, rangeClass);
		} finally {
			saveModel();
		}
	}

	private void saveModel() {
		File modelFile = new File(calculateComponentsDirectory(), FILENAME);
		try {
			writeStringToFile(modelFile, createTurtle(model));
		} catch (IOException e) {
			logger.error("failed to save local annotation world", e);
		}
	}

	public List<Individual> getIndividualsOfClass(Resource clazz) {
		return model.listIndividuals(clazz).toList();
	}

	public void addModelFromString(String addedModel) {
		try {
			populateModelFromString(model, addedModel);
		} finally {
			saveModel();
		}
	}
}
