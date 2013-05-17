/**
 * 
 */
package net.sf.taverna.t2.component.localworld;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author alanrw
 *
 */
public class LocalWorld {
	
	private static Logger logger = Logger.getLogger(LocalWorld.class);
	
	protected static final String ENCODING = "TURTLE";
	
	private OntModel model;
	
	private static LocalWorld instance = null;
	
	public synchronized static LocalWorld getInstance() {
		if (instance == null) {
			instance = new LocalWorld();
		}
		return instance;
	}
	
	private LocalWorld() {
		super();
		File modelFile = new File(calculateComponentsDirectory(), "localWorld.ttl");
		model = ModelFactory.createOntologyModel();
		if (modelFile.exists()) {
			try {
				String content = FileUtils.readFileToString (modelFile);
				StringReader stringReader = new StringReader(content);
				model.read(stringReader, null, ENCODING);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	public File calculateComponentsDirectory() {
		return (new File(ApplicationRuntime.getInstance().getApplicationHomeDir(), "components"));
	}

	public Individual createIndividual(String urlString, OntClass rangeClass) {
		Individual result = model.createIndividual(urlString, rangeClass);
		saveModel();
		return result;
	}

	private void saveModel() {
		File modelFile = new File(calculateComponentsDirectory(), "localWorld.ttl");
		try {
			FileUtils.writeStringToFile(modelFile, SemanticAnnotationUtils.createTurtle(model));
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public List<Individual> getIndividualsOfClass(Resource clazz) {
		return model.listIndividuals(clazz).toList();
	}

}
