#!/usr/bin/env python

import unittest
from tavernaclient.client import TavernaService, STATUS
from elementtree import ElementTree


TEST_SERVER="http://rpc269.cs.man.ac.uk:8180/remotetaverna/v1/"
TEST_USER="snake"
TEST_PW=open("password.txt").read().strip()

# Test workflows
ANIMAL_WF=open("../../data/animal.xml").read()
COLOUR_ANIMAL_WF=open("../../data/colouranimal.xml").read()


class TestClient(unittest.TestCase):
    
    def setUp(self):
        self.service = TavernaService(TEST_SERVER, TEST_USER, TEST_PW)
    
    def testConnect(self):
        capabilities = self.service.getCapabilities()
        users = capabilities.find("{http://taverna.sf.net/service}users")
        usersURL = users.attrib["{http://www.w3.org/1999/xlink}href"]
        self.assertEquals(TEST_SERVER  + "users", usersURL)
    
    def testGetUserURL(self):
        userURL = self.service.getUserURL()
        self.assertEquals(TEST_SERVER + "users/" + TEST_USER, userURL)
    
    def testGetUserCollectionURL(self):
        workflowURL = self.service.getUserCollectionURL("workflows")
        self.assertEquals(TEST_SERVER + "users/" + TEST_USER + "/workflows", workflowURL)
        jobURL = self.service.getUserCollectionURL("jobs")
        self.assertEquals(TEST_SERVER + "users/" + TEST_USER + "/jobs", jobURL)
        dataURL = self.service.getUserCollectionURL("datas")
        self.assertEquals(TEST_SERVER + "users/" + TEST_USER + "/data", dataURL)
    
    def testUploadWorkflow(self):
        workflowURL = self.service.uploadWorkflow(ANIMAL_WF)
        prefix = TEST_SERVER + "workflows/"
        self.assertTrue(workflowURL.startswith(prefix))
        
    def testCreateJob(self):
        workflowURL = self.service.uploadWorkflow(ANIMAL_WF)
        jobDocXML = self.service.createJobDocument(workflowURL)
        jobDoc = ElementTree.fromstring(jobDocXML)
        wfElem = jobDoc.find("{http://taverna.sf.net/service}workflow")
        self.assertEquals(workflowURL, wfElem.attrib["{http://www.w3.org/1999/xlink}href"])
    
    def testSubmitJob(self):
        jobURL = self._makeJobURL()
        prefix = TEST_SERVER + "jobs/"
        self.assertTrue(jobURL.startswith(prefix))
    

    def testCreateData(self):
        inputs = {}
        inputs["colour"] = "red"
        inputs["animal"] = "snake"
        dataDocument = self.service.createDataDocument(inputs)
        parsed = self.service.parseDataDocument(dataDocument)
        self.assertEqual(inputs, parsed)
    
    def testUploadData(self):
        inputs = {}
        inputs["colour"] = "red"
        inputs["animal"] = "snake"
        dataDocument = self.service.createDataDocument(inputs)
        dataURL = self.service.uploadData(dataDocument)
        prefix = TEST_SERVER + "data/"
        self.assertTrue(dataURL.startswith(prefix))
    
    def testGetJobStatus(self):
        jobURL = self._makeJobURL()
        status = self.service.getJobStatus(jobURL)
        # Assuming our server is not too quick!
        self.assertEquals(STATUS.QUEUED, status)
        
    def _makeJobURL(self):  
        workflowURL = self.service.uploadWorkflow(ANIMAL_WF)
        jobDoc = self.service.createJobDocument(workflowURL)
        return self.service.submitJob(jobDoc)  
        
    def testIsFinished(self):
        jobURL = self._makeJobURL()
        
        # Assuming our server is not VERY quick
        self.assertFalse(self.service.isFinished(jobURL))
                         
    def testWaitForJob(self):
        jobURL = self._makeJobURL()
        print "Waiting for", jobURL    
        status = self.service.waitForJob(jobURL, 5*60)
        print status
        
if __name__ == '__main__':
    unittest.main()

