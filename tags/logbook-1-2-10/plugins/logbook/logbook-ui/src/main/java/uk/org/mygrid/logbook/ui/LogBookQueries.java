package uk.org.mygrid.logbook.ui;

import java.util.Vector;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;

public class LogBookQueries {

//    private static Logger logger = Logger
//            .getLogger(EnactorBrowserQueries.class);

    /**
     * Query to obtain all the workflow LSIDS for the current user as stated by
     * mygrid.usercontext.experimenter
     * 
     * @param experimenter
     * @return a Vector of the workflow LSIDs as Strings
     * @throws MetadataServiceException 
     */
    public static Vector<String> getUserWorkFlows(String experimenter,
            MetadataService metadataService) throws MetadataServiceException {

        Vector<String> usersWorkflows = metadataService.getUserWorkFlows(experimenter);
        return usersWorkflows;

    }

    // FIXME modify so it works with multiple experimenters

    // public static Set getUserWorkFlows(String[] experimenter,
    // MetadataService rdfRepository) {
    //
    // Vector usersWorkflows = new Vector();
    // Set models = new HashSet();
    //
    // if (rdfRepository instanceof JenaMetadataService ) {
    // final String query = "SELECT ?workflowRun "
    // + "WHERE ?workflowRun ( ?workflowRun "
    // + JenaProvenanceOntology
    // .bracketify(ProvenanceVocab.LAUNCHED_BY.getURI())
    // // + JenaProvenanceOntology.bracketify(experimenter)
    // + ") USING ns FOR <http://www.mygrid.org.uk/provenance#>";
    // // String rdf = rdfRepository
    // //
    // .retrieveGraph("urn:lsid:www.mygrid.org.uk:experimentinstance:KKIWOFV9NC1");
    //
    // System.out.println("Query = " + query);
    //
    // Iterator itr = TriQLQuery.exec(((JenaMetadataService ) rdfRepository)
    // .getGraphSet(), query);
    //
    // while (itr.hasNext()) {
    // Map nextMap = (Map) itr.next();
    // Node workflowRun = (Node) nextMap.get("workflowRun");
    //
    // String workflowLSID = workflowRun.toString();
    // // System.out.print("Workflow LSID: " + workflowLSID);
    // // store the user workflow LSIDs to the global Vector
    // // usersWorkflows
    // usersWorkflows.add(workflowLSID);
    // Model instance = rdfRepository.retrieveGraphModel(workflowLSID);
    // models.add(instance);
    //
    // }
    // } else {
    // System.err.println("Sesame not fully implemented yet");
    // }
    //
    // return models;
    //
    // }

    // public static Set getUserLabels(String experimenter,
    // JenaMetadataService rdfRepository) {
    //
    // final String query = "SELECT ?label ?workflowRun "
    // + "WHERE ns:"
    // + experimenter
    // + " ( ?label "
    // + JenaProvenanceOntology
    // .bracketify(EnactorInvocationBrowserModel.LABELS)
    // + " ?workflowRun "
    // + ") USING ns FOR <http://www.mygrid.org.uk/labels#>";
    // // String rdf = rdfRepository
    // //
    // .retrieveGraph("urn:lsid:www.mygrid.org.uk:experimentinstance:KKIWOFV9NC1");
    //
    // System.out.println("Query = " + query);
    //
    // Iterator itr = TriQLQuery.exec(rdfRepository.getGraphSet(), query);
    // Set models = new HashSet();
    //
    // return null;
    // }

}
