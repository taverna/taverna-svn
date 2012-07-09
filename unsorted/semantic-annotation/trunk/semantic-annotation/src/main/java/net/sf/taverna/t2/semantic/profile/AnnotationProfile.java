/**
 * 
 */
package net.sf.taverna.t2.semantic.profile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.semantic.configuration.DefaultAnnotationProfileConfiguration;
import net.sf.taverna.t2.semantic.profile.annotationbean.AnnotationProfileAssertion;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyDocumentAlreadyExistsException;

import uk.ac.manchester.cs.owl.semspreadsheets.model.OntologyManager;
import uk.ac.manchester.cs.owl.semspreadsheets.model.WorkbookManager;

/**
 * @author alanrw
 * 
 * Should really have annotation profile manager to stop lots of duplicates
 *
 */
public class AnnotationProfile {
	
	private static Logger logger = Logger
	.getLogger(AnnotationProfile.class);
	
	private WorkbookManager wmManager;


	private final URL url;
	
	private static Map<URL, AnnotationProfile> profileMap = Collections.synchronizedMap(new HashMap<URL, AnnotationProfile> ());
	
	private AnnotationProfile(URL url) {
		// Will read the profile from the URL
		
		this.url = url;
		// Assume that it loads the relevant ontologies into an ontology manager
		
		wmManager = new WorkbookManager();
		
		// Cheat for now
		try {
			try {
				wmManager.getOntologyManager().loadOntology(IRI.create("http://www.mygrid.org.uk/mygrid-moby-service/"));
			} catch (OWLOntologyDocumentAlreadyExistsException e1) {
				//nowt
			}
			wmManager.getOntologyManager().getLoadedOntologies();
		} catch (OWLOntologyCreationException e) {
			logger.error(e);
		}
	}
	
	public URL getUrl() {
		return url;
	}

	private static AnnotationProfile getProfileFromURL(URL url) {
		AnnotationProfile result = profileMap.get(url);
		if (result == null) {
			result = new AnnotationProfile(url);
			profileMap.put(url, result);
		}
		return result;
		
	}
	public static AnnotationProfile getAnnotationProfile(Dataflow d) {
		AnnotationTools at = new AnnotationTools();
		Object annotation = at.getAnnotation(d, AnnotationProfileAssertion.class);
		if (annotation == null) {
			URL url = DefaultAnnotationProfileConfiguration.getINSTANCE().getDefaultAnnotationProfileURL();
			if (url != null) {
				return getProfileFromURL(url);
			}
		} else {
			AnnotationProfileAssertion annotationProfileAssertion = (AnnotationProfileAssertion) annotation;
			return getProfileFromURL(annotationProfileAssertion.getUrl());
		}
		return null;
	}

	public static void setAnnotationProfile(Dataflow d, String urlString) {
		AnnotationTools at = new AnnotationTools();
		try {
			URL url = new URL(urlString);
			AnnotationProfileAssertion assertion = new AnnotationProfileAssertion();
			assertion.setUrl(url);
			Edit edit = at.addAnnotation(d, assertion);
			EditManager em = EditManager.getInstance();
			em.doDataflowEdit(d, edit);
		} catch (MalformedURLException e) {
			logger.error(e);
		} catch (EditException e) {
			logger.error(e);
		}
	}

	public OntologyManager getOntologyManager() {
		return wmManager.getOntologyManager();
	}
	
	public WorkbookManager getWorkbookManager() {
		return wmManager;
	}

}
