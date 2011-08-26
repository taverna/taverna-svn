package uk.org.mygrid.provenance.dataservice;

import java.util.Properties;

import junit.framework.TestCase;

import org.embl.ebi.escience.scufl.ScuflModel;

import uk.org.mygrid.provenance.util.LogBookConfigurationNotFoundException;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

public class TestStoreWorkflow extends TestCase {

    Properties configuration;

    public void setUp() throws Exception {
        // FIXME: Make a way to test on ALL databases
        configuration = getHypersonicConfiguration();
    }

    public static Properties getMysqlConfiguration() throws LogBookConfigurationNotFoundException {
        Properties metadataStoreConfiguration = ProvenanceConfigurator
                .getMetadataStoreConfiguration();
        metadataStoreConfiguration.setProperty(
                ProvenanceConfigurator.DATASERVICE_TYPE_KEY,
                ProvenanceConfigurator.MYSQL);
        return metadataStoreConfiguration;
    }

    public static Properties getDerbyConfiguration() throws LogBookConfigurationNotFoundException {
        Properties metadataStoreConfiguration = ProvenanceConfigurator
                .getMetadataStoreConfiguration();
        metadataStoreConfiguration.setProperty(
                ProvenanceConfigurator.DATASERVICE_TYPE_KEY,
                ProvenanceConfigurator.DERBY);
        return metadataStoreConfiguration;
    }

    public static Properties getHypersonicConfiguration() throws LogBookConfigurationNotFoundException {
        Properties metadataStoreConfiguration = ProvenanceConfigurator
                .getMetadataStoreConfiguration();
        metadataStoreConfiguration.setProperty(
                ProvenanceConfigurator.DATASERVICE_TYPE_KEY,
                ProvenanceConfigurator.HYPERSONIC);
        return metadataStoreConfiguration;
    }

    public void testStoreWorkflow() throws Exception {
        ScuflModel storedModel = new ScuflModel();
        // The unique LSID for this particular workflow that was run
        String storedLSID = "urn:example.com:storedLSID" + System.nanoTime();

        // The one inside the workflow
        String wfLSID = "urn:example.com:testLSID" + +System.nanoTime();
        storedModel.getDescription().setLSID(wfLSID);
        assertEquals(wfLSID, storedModel.getDescription().getLSID());

        DataService dataService = DataServiceFactory.getInstance(configuration);
        dataService.storeWorkflow(storedLSID, storedModel);

        ScuflModel retrievedModel = new ScuflModel();
        dataService.populateWorkflowModel(storedLSID, retrievedModel);
        assertNotSame(storedModel, retrievedModel);
        assertEquals(storedModel.getDescription().getLSID(), wfLSID);

        try {
            dataService.populateWorkflowModel(wfLSID, new ScuflModel());
        } catch (DataServiceException e) {
            // As expected
            return;
        }
        fail("Should not be able to fetch by LSID " + wfLSID);
    }

}
