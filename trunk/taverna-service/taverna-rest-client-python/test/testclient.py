#!/usr/bin/env python
"""
Tests for baclava.py

Author: Stian Soiland, David Withers
Copyright: 2008 University of Manchester, UK
URL: http://taverna.sourceforge.net/
Contact: taverna-hackers@lists.sourceforge.net
Licence: LGPL 3 (See LICENCE or http://www.gnu.org/licenses/lgpl.html)
"""

import unittest
import time
import os

from elementtree import ElementTree

from tavernaclient.client import TavernaService, Status

#ÊServer and username of server to use for tests
TEST_SERVER="http://rpc269.cs.man.ac.uk:8180/remotetaverna/v1/"
TEST_USER="snake"
# Note: Set password in data/password.txt

import testclient
ROOT_DIR = os.path.abspath(os.path.join(testclient.__file__, ".."))
DATA_DIR = os.path.join(ROOT_DIR, "data")

# Password for connecting to server
TEST_PW=open(os.path.join(DATA_DIR, "password.txt")).read().strip()

# Test workflows
ANIMAL_WF=open(os.path.join(DATA_DIR, "animal.xml")).read()
COLOUR_ANIMAL_WF=open(os.path.join(DATA_DIR, "colouranimal.xml")).read()


class TestClient(unittest.TestCase):
    
    def setUp(self):
        self.service = TavernaService(TEST_SERVER, TEST_USER, TEST_PW)
    
    def testConnect(self):
        capabilities = self.service._getCapabilities()
        users = capabilities.find("{http://taverna.sf.net/service}users")
        usersURL = users.attrib["{http://www.w3.org/1999/xlink}href"]
        self.assertEquals(TEST_SERVER  + "users", usersURL)
    
    def testGetUserURL(self):
        userURL = self.service._getUserURL()
        self.assertEquals(TEST_SERVER + "users/" + TEST_USER, userURL)
    
    def testGetUserCollectionURL(self):
        workflowURL = self.service._getUserCollectionURL("workflows")
        self.assertEquals(TEST_SERVER + "users/" + TEST_USER + "/workflows", workflowURL)
        jobURL = self.service._getUserCollectionURL("jobs")
        self.assertEquals(TEST_SERVER + "users/" + TEST_USER + "/jobs", jobURL)
        dataURL = self.service._getUserCollectionURL("datas")
        self.assertEquals(TEST_SERVER + "users/" + TEST_USER + "/data", dataURL)
    
    def testUploadWorkflow(self):
        workflowURL = self.service.uploadWorkflow(ANIMAL_WF)
        prefix = TEST_SERVER + "workflows/"
        self.assertTrue(workflowURL.startswith(prefix))
        
    def testCreateJob(self):
        workflowURL = self.service.uploadWorkflow(ANIMAL_WF)
        jobDocXML = self.service._createJobDoc(workflowURL)
        jobDoc = ElementTree.fromstring(jobDocXML)
        wfElem = jobDoc.find("{http://taverna.sf.net/service}workflow")
        self.assertEquals(workflowURL, wfElem.attrib["{http://www.w3.org/1999/xlink}href"])
    
    def testSubmitJob(self):
        workflowURL = self.service.uploadWorkflow(ANIMAL_WF)
        jobURL = self.service.submitJob(workflowURL)
        prefix = TEST_SERVER + "jobs/"
        self.assertTrue(jobURL.startswith(prefix))
        
    
    def testSubmitJob(self):
        workflowURL = self.service.uploadWorkflow(ANIMAL_WF)
        jobURL = self.service.submitJob(workflowURL)
        prefix = TEST_SERVER + "jobs/"
        self.assertTrue(jobURL.startswith(prefix))
    

    def testCreateData(self):
        inputs = {}
        inputs["colour"] = "red"
        inputs["animal"] = "snake"
        dataDocument = self.service._createDataDoc(inputs)
        parsed = self.service._parseDataDoc(dataDocument)
        self.assertEqual(inputs, parsed)
    
    def testUploadData(self):
        inputs = {}
        inputs["colour"] = "red"
        inputs["animal"] = "snake"
        dataURL = self.service.uploadData(inputs)
        prefix = TEST_SERVER + "data/"
        self.assertTrue(dataURL.startswith(prefix))
        
    def testSubmitJobWithData(self):
        inputs = {}
        inputs["colour"] = "red"
        inputs["animal"] = "snake"
        workflowURL = self.service.uploadWorkflow(COLOUR_ANIMAL_WF)
        dataURL = self.service.uploadData(inputs)
        jobURL = self.service.submitJob(workflowURL, dataURL)
    
    def testGetJobStatus(self):
        workflowURL = self.service.uploadWorkflow(ANIMAL_WF)
        jobURL = self.service.submitJob(workflowURL)
        status = self.service.getJobStatus(jobURL)
        # Assuming our server is not too quick!
        self.assertEquals(Status.QUEUED, status)
        
    def testIsFinished(self):
        workflowURL = self.service.uploadWorkflow(ANIMAL_WF)
        jobURL = self.service.submitJob(workflowURL)
        
        # Assuming our server is not VERY quick
        self.assertFalse(self.service.isFinished(jobURL))
                         
    def testWaitForJob(self):
        workflowURL = self.service.uploadWorkflow(ANIMAL_WF)
        jobURL = self.service.submitJob(workflowURL)
        now = time.time()
        timeout = 10
        status = self.service.waitForJob(jobURL, timeout)
        after = time.time()
        # Should be at least some milliseconds longer than the timeout
        self.assertTrue(after-now > timeout)
        self.assertFalse(self.service.isFinished(jobURL))
        
    def testExecute(self):
        # Note: This test might take a minute or so to complete
        results = self.service.executeSync(ANIMAL_WF)
        self.assertEquals(1, len(results))
        self.assertEquals("frog", results["animal"])
        
    def testExecuteWithData(self):
        # Note: This test might take a minute or so to complete
        inputs = {}
        inputs["colour"] = "red"
        inputs["animal"] = "snake"
        workflowURL = self.service.uploadWorkflow(COLOUR_ANIMAL_WF)
        results = self.service.executeSync(workflowURL=workflowURL, inputs=inputs)
        #print results
        self.assertEquals(1, len(results))
        self.assertEquals("redsnake", results["coulouredAnimal"])
        
    def testExecuteWithMultipleData(self):
        # Note: This test might take a minute or so to complete
        inputs = {}
        inputs["colour"] = ["red", "green"]
        inputs["animal"] = ["rabbit", "mouse", "cow"]
        workflowURL = self.service.uploadWorkflow(COLOUR_ANIMAL_WF)
        results = self.service.executeSync(workflowURL=workflowURL, inputs=inputs)
        #print results
        self.assertEquals(1, len(results))
        animals = results["coulouredAnimal"]
        self.assertEquals(2, len(animals))
        allAnimals = set(animals[0])
        allAnimals.update(animals[1])

        self.assertTrue("redmouse" in allAnimals)
        self.assertTrue("greenrabbit" in allAnimals)
        self.assertTrue("redsnake" not in allAnimals) # hehe
        
if __name__ == '__main__':
    unittest.main()

